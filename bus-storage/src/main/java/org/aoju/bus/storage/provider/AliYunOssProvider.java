/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.storage.provider;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.*;
import com.google.common.collect.Maps;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.storage.Builder;
import org.aoju.bus.storage.Context;
import org.aoju.bus.storage.magic.Attachs;
import org.aoju.bus.storage.magic.Readers;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 存储服务-阿里云
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class AliYunOssProvider extends AbstractProvider {

    private OSSClient client;

    public AliYunOssProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.context.getEndpoint(), "[endpoint] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] not defined");
        Assert.notNull(this.context.isSecure(), "[secure] not defined");

        this.client = new OSSClient(this.context.getEndpoint(), new DefaultCredentialProvider(this.context.getAccessKey(), this.context.getSecretKey()), null);
        if (!this.client.doesBucketExist(this.context.getBucket())) {
            System.out.println("Creating bucket " + this.context.getBucket() + "\n");
            this.client.createBucket(this.context.getBucket());
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(this.context.getBucket());
            createBucketRequest.setCannedACL(this.context.isSecure() ? CannedAccessControlList.Private : CannedAccessControlList.PublicRead);
            this.client.createBucket(createBucketRequest);
        }
    }

    @Override
    public Readers download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Readers download(String bucket, String fileName) {
        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流
        OSSObject ossObject = this.client.getObject(bucket, fileName);
        // 读取文件内容
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
            }
            return new Readers(reader);
        } catch (Exception e) {
            Logger.error("file download failed", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    @Override
    public Readers download(String bucket, String fileName, File file) {
        this.client.getObject(new GetObjectRequest(bucket, fileName), file);
        return new Readers(Builder.SUCCESS);
    }

    @Override
    public Readers list() {
        ListObjectsRequest request = new ListObjectsRequest(this.context.getBucket());
        ObjectListing objectListing = client.listObjects(request);
        return new Readers(objectListing.getObjectSummaries().stream().map(item -> {
            Attachs storageItem = new Attachs();
            storageItem.setName(item.getKey());
            storageItem.setSize(StringUtils.toString(item.getSize()));
            Map<String, Object> extended = Maps.newHashMap();
            extended.put("tag", item.getETag());
            extended.put("storageClass", item.getStorageClass());
            extended.put("lastModified", item.getLastModified());
            storageItem.setExtended(extended);
            return storageItem;
        }).collect(Collectors.toList()));
    }

    @Override
    public Readers rename(String oldName, String newName) {
        return rename(this.context.getBucket(), oldName, newName);
    }

    @Override
    public Readers rename(String bucket, String oldName, String newName) {
        boolean keyExists = true;
        try {
            this.client.getObjectMetadata(bucket, oldName);
        } catch (Exception e) {
            keyExists = false;
        }
        if (keyExists) {
            this.client.copyObject(bucket, oldName, bucket, newName);
        }
        return new Readers(Builder.SUCCESS);
    }

    @Override
    public Readers upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), fileName, content);
    }

    @Override
    public Readers upload(String bucket, String fileName, InputStream content) {
        try {
            byte[] bytes = new byte[content.available()];
            return upload(this.context.getBucket(), fileName, bytes);
        } catch (IOException e) {
            Logger.error("file upload failed ", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers upload(String bucket, String fileName, byte[] content) {
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        try {
            PutObjectResult objectResult = this.client.putObject(bucket, fileName, bis);
            ResponseMessage response = objectResult.getResponse();
            if (!response.isSuccessful()) {
                return new Readers(null, response.getErrorResponseAsString());
            }
            return new Readers(Attachs.builder().name(fileName)
                    .size("" + response.getContentLength())
                    .path(response.getUri())
                    .build());
        } catch (Exception e) {
            this.client.putObject(bucket, fileName, bis);
            Logger.error("file upload failed ", e.getMessage());
        }
        return new Readers(Builder.FAILURE);
    }

    @Override
    public Readers remove(String fileName) {
        return remove(this.context.getBucket(), fileName);
    }

    @Override
    public Readers remove(String bucket, String fileName) {
        this.client.deleteObject(bucket, fileName);
        return new Readers(Builder.SUCCESS);
    }

    @Override
    public Readers remove(String bucket, Path path) {
        remove(bucket, path.toString());
        return new Readers(Builder.SUCCESS);
    }

}
