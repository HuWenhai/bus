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
package org.aoju.bus.crypto.digest;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.HexUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.digest.mac.MacEngine;
import org.aoju.bus.crypto.digest.mac.MacEngineFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * HMAC摘要算法
 * HMAC,全称为“Hash Message Authentication Code”,中文名“散列消息鉴别码”
 * 主要是利用哈希算法,以一个密钥和一个消息为输入,生成一个消息摘要作为输出
 * 一般的,消息鉴别码用于验证传输于两个共 同享有一个密钥的单位之间的消息
 * HMAC 可以与任何迭代散列函数捆绑使用 MD5 和 SHA-1 就是这种散列函数 HMAC 还可以使用一个用于计算和确认消息鉴别值的密钥
 * 注意：此对象实例化后为非线程安全！
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class HMac {

    private MacEngine engine;

    /**
     * 构造,自动生成密钥
     *
     * @param algorithm 算法
     */
    public HMac(String algorithm) {
        this(algorithm, (SecretKey) null);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public HMac(String algorithm, byte[] key) {
        this(algorithm, new SecretKeySpec(key, algorithm));
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public HMac(String algorithm, SecretKey key) {
        this(MacEngineFactory.createEngine(algorithm, key));
    }

    /**
     * 构造
     *
     * @param engine MAC算法实现引擎
     */
    public HMac(MacEngine engine) {
        this.engine = engine;
    }

    /**
     * 生成文件摘要
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public byte[] digest(String data, String charset) {
        return digest(StringUtils.bytes(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public byte[] digest(String data) {
        return digest(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 生成文件摘要,并转为16进制字符串
     *
     * @param data    被摘要数据
     * @param charset 编码
     * @return 摘要
     */
    public String digestHex(String data, String charset) {
        return HexUtils.encodeHexStr(digest(data, charset));
    }

    /**
     * 生成文件摘要
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(String data) {
        return digestHex(data, Charset.DEFAULT_UTF_8);
    }

    /**
     * 生成文件摘要
     * 使用默认缓存大小
     *
     * @param file 被摘要文件
     * @return 摘要bytes
     * @throws InstrumentException Cause by IOException
     */
    public byte[] digest(File file) throws InstrumentException {
        InputStream in = null;
        try {
            in = FileUtils.getInputStream(file);
            return digest(in);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 生成文件摘要,并转为16进制字符串
     * 使用默认缓存大小
     *
     * @param file 被摘要文件
     * @return 摘要
     */
    public String digestHex(File file) {
        return HexUtils.encodeHexStr(digest(file));
    }

    /**
     * 生成摘要
     *
     * @param data 数据bytes
     * @return 摘要bytes
     */
    public byte[] digest(byte[] data) {
        return digest(new ByteArrayInputStream(data), -1);
    }

    /**
     * 生成摘要,并转为16进制字符串
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(byte[] data) {
        return HexUtils.encodeHexStr(digest(data));
    }

    /**
     * 生成摘要,使用默认缓存大小
     *
     * @param data {@link InputStream} 数据流
     * @return 摘要bytes
     */
    public byte[] digest(InputStream data) {
        return digest(data, IoUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 生成摘要,并转为16进制字符串
     * 使用默认缓存大小
     *
     * @param data 被摘要数据
     * @return 摘要
     */
    public String digestHex(InputStream data) {
        return HexUtils.encodeHexStr(digest(data));
    }

    /**
     * 生成摘要
     *
     * @param data         {@link InputStream} 数据流
     * @param bufferLength 缓存长度
     * @return 摘要bytes
     */
    public byte[] digest(InputStream data, int bufferLength) {
        return this.engine.digest(data, bufferLength);
    }

    /**
     * 生成摘要,并转为16进制字符串
     * 使用默认缓存大小
     *
     * @param data         被摘要数据
     * @param bufferLength 缓存长度
     * @return 摘要
     */
    public String digestHex(InputStream data, int bufferLength) {
        return HexUtils.encodeHexStr(digest(data, bufferLength));
    }

}
