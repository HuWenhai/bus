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
package org.aoju.bus.tracer;

import org.aoju.bus.tracer.config.TraceFilterConfiguration;

import java.util.Map;

/**
 * 后端应该是线程安全的(读写被委托给线程本地状态).
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public interface Backend {

    TraceFilterConfiguration getConfiguration(String profileName);

    TraceFilterConfiguration getConfiguration();

    boolean containsKey(String key);

    String get(String key);

    int size();

    void clear();

    boolean isEmpty();

    void put(String key, String value);

    void putAll(Map<? extends String, ? extends String> m);

    Map<String, String> copyToMap();

    void remove(String key);

    String getInvocationId();

    String getSessionId();

}
