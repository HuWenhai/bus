/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.logger.dialect.commons;

import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;

/**
 * Apache Commons Logging
 *
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public class ApacheCommonsLogFactory extends LogFactory {

    public ApacheCommonsLogFactory() {
        super("Apache Common Logging");
        checkLogExist(org.apache.commons.logging.LogFactory.class);
    }

    @Override
    public Log createLog(String name) {
        try {
            return new ApacheCommonsLog4J(name);
        } catch (Exception e) {
            return new ApacheCommonsLog(name);
        }
    }

    @Override
    public Log createLog(Class<?> clazz) {
        try {
            return new ApacheCommonsLog4J(clazz);
        } catch (Exception e) {
            return new ApacheCommonsLog(clazz);
        }
    }

    @Override
    protected void checkLogExist(Object logClassName) {
        super.checkLogExist(logClassName);
        //Commons Logging在调用getLog时才检查是否有日志实现，在此提前检查，如果没有实现则跳过之
        getLog(ApacheCommonsLogFactory.class);
    }
}
