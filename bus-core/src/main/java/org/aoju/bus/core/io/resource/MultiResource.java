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
package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CollUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * 多资源组合资源
 * 此资源为一个利用游标自循环资源,只有调用{@link #next()} 方法才会获取下一个资源,使用完毕后调用{@link #reset()}方法重置游标
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class MultiResource implements Resource, Iterable<Resource>, Iterator<Resource> {

    private List<Resource> resources;
    private int cursor;

    /**
     * 构造
     *
     * @param resources 资源数组
     */
    public MultiResource(Resource... resources) {
        this(CollUtils.newArrayList(resources));
    }

    /**
     * 构造
     *
     * @param resources 资源列表
     */
    public MultiResource(Collection<Resource> resources) {
        if (resources instanceof List) {
            this.resources = (List<Resource>) resources;
        } else {
            this.resources = CollUtils.newArrayList(resources);
        }
    }

    @Override
    public String getName() {
        return resources.get(cursor).getName();
    }

    @Override
    public URL getUrl() {
        return resources.get(cursor).getUrl();
    }

    @Override
    public InputStream getStream() {
        return resources.get(cursor).getStream();
    }

    @Override
    public BufferedReader getReader(Charset charset) {
        return resources.get(cursor).getReader(charset);
    }

    @Override
    public String readStr(Charset charset) throws InstrumentException {
        return resources.get(cursor).readStr(charset);
    }

    @Override
    public String readUtf8Str() throws InstrumentException {
        return resources.get(cursor).readUtf8Str();
    }

    @Override
    public byte[] readBytes() throws InstrumentException {
        return resources.get(cursor).readBytes();
    }

    @Override
    public Iterator<Resource> iterator() {
        return resources.iterator();
    }

    @Override
    public boolean hasNext() {
        return cursor < resources.size();
    }

    @Override
    public Resource next() {
        if (cursor >= resources.size()) {
            throw new ConcurrentModificationException();
        }
        this.cursor++;
        return this;
    }

    @Override
    public void remove() {
        this.resources.remove(this.cursor);
    }

    /**
     * 重置游标
     */
    public void reset() {
        this.cursor = 0;
    }

    /**
     * 增加资源
     *
     * @param resource 资源
     * @return this
     */
    public MultiResource add(Resource resource) {
        this.resources.add(resource);
        return this;
    }

}
