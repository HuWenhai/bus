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
package org.aoju.bus.cache.support.cache;

import java.util.Collection;
import java.util.Map;

/**
 * 缓存接口
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public interface Cache {

    /**
     * 从缓存中获得对象
     *
     * @param key 键
     * @return 键对应的对象
     * @see #read(String)
     */
    Object read(String key);

    /**
     * 从缓存中获得一组对象信息
     *
     * @param keys 多个键
     * @return 值对象
     */
    Map<String, Object> read(Collection<String> keys);

    /**
     * 将对象加入到缓存,使用指定失效时长
     *
     * @param key    键
     * @param value  缓存的对象
     * @param expire 失效时长,单位毫秒
     * @see Cache#write(String, Object, long)
     */
    void write(String key, Object value, long expire);

    /**
     * 将对象加入到缓存,使用指定失效时长
     *
     * @param map    缓存的对象
     * @param expire 失效时长,单位毫秒
     * @see Cache#write(Map, long)
     */
    void write(Map<String, Object> map, long expire);

    /**
     * 从缓存中移除对象
     *
     * @param keys 键
     */
    void remove(String... keys);

    /**
     * 清空缓存信息
     */
    void clear();
}
