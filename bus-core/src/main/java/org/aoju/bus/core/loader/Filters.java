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
package org.aoju.bus.core.loader;

import java.net.URL;
import java.util.Collection;

/**
 * 过滤器工具类
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public abstract class Filters {

    /**
     * 永远返回true的过滤器
     */
    public static final Filter ALWAYS = new Filter() {
        public boolean filtrate(String name, URL url) {
            return true;
        }
    };

    /**
     * 永远返回false的过滤器
     */
    public static final Filter NEVER = new Filter() {
        public boolean filtrate(String name, URL url) {
            return false;
        }
    };

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static Filter all(Filter... filters) {
        return new AllFilter(filters);
    }

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static Filter all(Collection<? extends Filter> filters) {
        return new AllFilter(filters);
    }

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static Filter and(Filter... filters) {
        return all(filters);
    }

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static Filter and(Collection<? extends Filter> filters) {
        return all(filters);
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static Filter any(Filter... filters) {
        return new AnyFilter(filters);
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static Filter any(Collection<? extends Filter> filters) {
        return new AnyFilter(filters);
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static Filter or(Filter... filters) {
        return any(filters);
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param filters 子过滤器
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static Filter or(Collection<? extends Filter> filters) {
        return any(filters);
    }

}
