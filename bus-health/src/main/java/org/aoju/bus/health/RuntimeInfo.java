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

import org.aoju.bus.core.utils.FileUtils;

/**
 * 运行时信息,包括内存总大小、已用大小、可用大小等
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class RuntimeInfo {

    private Runtime currentRuntime = Runtime.getRuntime();

    /**
     * 获得运行时对象
     *
     * @return {@link Runtime}
     */
    public final Runtime getRuntime() {
        return currentRuntime;
    }

    /**
     * 获得JVM最大可用内存
     *
     * @return 最大可用内存
     */
    public final long getMaxMemory() {
        return currentRuntime.maxMemory();
    }

    /**
     * 获得JVM已分配内存
     *
     * @return 已分配内存
     */
    public final long getTotalMemory() {
        return currentRuntime.totalMemory();
    }

    /**
     * 获得JVM已分配内存中的剩余空间
     *
     * @return 已分配内存中的剩余空间
     */
    public final long getFreeMemory() {
        return currentRuntime.freeMemory();
    }

    /**
     * 获得JVM最大可用内存
     *
     * @return 最大可用内存
     */
    public final long getUsableMemory() {
        return currentRuntime.maxMemory() - currentRuntime.totalMemory() + currentRuntime.freeMemory();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        HealthUtils.append(builder, "Max Memory:    ", FileUtils.readableFileSize(getMaxMemory()));
        HealthUtils.append(builder, "Total Memory:     ", FileUtils.readableFileSize(getTotalMemory()));
        HealthUtils.append(builder, "Free Memory:     ", FileUtils.readableFileSize(getFreeMemory()));
        HealthUtils.append(builder, "Usable Memory:     ", FileUtils.readableFileSize(getUsableMemory()));
        return builder.toString();
    }

}
