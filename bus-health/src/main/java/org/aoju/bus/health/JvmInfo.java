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
package org.aoju.bus.health;

import org.aoju.bus.core.consts.System;

/**
 * 代表Java Virtual Machine Implementation的信息
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class JvmInfo {

    /**
     * 取得当前JVM impl.的名称（取自系统属性：<code>java.vm.name</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"Java HotSpot(TM) Client VM"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getName() {
        return HealthUtils.get(System.VM_NAME, false);
    }

    /**
     * 取得当前JVM impl.的版本（取自系统属性：<code>java.vm.version</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"1.4.2-b28"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getVersion() {
        return HealthUtils.get(System.VM_VERSION, false);
    }

    /**
     * 取得当前JVM impl.的厂商（取自系统属性：<code>java.vm.vendor</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"Sun Microsystems Inc."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getVendor() {
        return HealthUtils.get(System.VM_VENDOR, false);
    }

    /**
     * 取得当前JVM impl.的信息（取自系统属性：<code>java.vm.info</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"mixed mode"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getInfo() {
        return HealthUtils.get(System.VM_INFO, false);
    }

    /**
     * 将Java Virutal Machine Implementation的信息转换成字符串
     *
     * @return JVM impl.的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();

        HealthUtils.append(builder, "JavaVM Name:    ", getName());
        HealthUtils.append(builder, "JavaVM Version: ", getVersion());
        HealthUtils.append(builder, "JavaVM Vendor:  ", getVendor());
        HealthUtils.append(builder, "JavaVM Info:    ", getInfo());

        return builder.toString();
    }

}
