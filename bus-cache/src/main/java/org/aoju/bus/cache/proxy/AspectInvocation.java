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
package org.aoju.bus.cache.proxy;

import org.aoju.bus.proxy.Invocation;

/**
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class AspectInvocation implements ProxyChain {

    private Object target;

    private Invocation invocation;

    public AspectInvocation(Object target, Invocation invocation) {
        this.target = target;
        this.invocation = invocation;
    }

    @Override
    public Object[] getArgs() {
        return invocation.getArguments();
    }

    @Override
    public Object proceed() throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return invocation.getMethod().invoke(target, args);
    }

}
