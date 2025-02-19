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
package org.aoju.bus.core.thread;

import org.aoju.bus.core.builder.Builder;

import java.util.concurrent.*;

/**
 * {@link ThreadPoolExecutor} 建造者
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class ExecutorBuilder implements Builder<ThreadPoolExecutor> {

    private int corePoolSize;
    private int maxPoolSize = Integer.MAX_VALUE;
    private long keepAliveTime = TimeUnit.SECONDS.toNanos(60);
    private BlockingQueue<Runnable> workQueue;
    private ThreadFactory threadFactory;
    private RejectedExecutionHandler handler;

    /**
     * 创建ExecutorBuilder,开始构建
     *
     * @return {@link ExecutorBuilder}
     */
    public static ExecutorBuilder create() {
        return new ExecutorBuilder();
    }

    /**
     * 构建ThreadPoolExecutor
     *
     * @param builder {@link ExecutorBuilder}
     * @return {@link ThreadPoolExecutor}
     */
    private static ThreadPoolExecutor build(ExecutorBuilder builder) {
        final int corePoolSize = builder.corePoolSize;
        final int maxPoolSize = builder.maxPoolSize;
        final long keepAliveTime = builder.keepAliveTime;
        final BlockingQueue<Runnable> workQueue;
        if (null != builder.workQueue) {
            workQueue = builder.workQueue;
        } else {
            //corePoolSize为0则要使用SynchronousQueue避免无限阻塞
            workQueue = (corePoolSize <= 0) ? new SynchronousQueue<Runnable>() : new LinkedBlockingQueue<Runnable>();
        }
        final ThreadFactory threadFactory = (null != builder.threadFactory) ? builder.threadFactory : Executors.defaultThreadFactory();
        final RejectedExecutionHandler handler = builder.handler;

        if (null == handler) {
            return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.NANOSECONDS, workQueue, threadFactory);
        } else {
            return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.NANOSECONDS, workQueue, threadFactory, handler);
        }
    }

    /**
     * 设置初始池大小,默认0
     *
     * @param corePoolSize 初始池大小
     * @return this
     */
    public ExecutorBuilder setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    /**
     * 设置最大池大小（允许同时执行的最大线程数）
     *
     * @param maxPoolSize 最大池大小（允许同时执行的最大线程数）
     * @return this
     */
    public ExecutorBuilder setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    /**
     * 设置线程存活时间,既当池中线程多于初始大小时,多出的线程保留的时长
     *
     * @param keepAliveTime 线程存活时间
     * @param unit          单位
     * @return this
     */
    public ExecutorBuilder setKeepAliveTime(long keepAliveTime, TimeUnit unit) {
        return setKeepAliveTime(unit.toNanos(keepAliveTime));
    }

    /**
     * 设置线程存活时间,既当池中线程多于初始大小时,多出的线程保留的时长,单位纳秒
     *
     * @param keepAliveTime 线程存活时间,单位纳秒
     * @return this
     */
    public ExecutorBuilder setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    /**
     * 设置队列,用于存在未执行的线程
     * 可选队列有：
     * <pre>
     * 1. SynchronousQueue    它将任务直接提交给线程而不保持它们 当运行线程小于maxPoolSize时会创建新线程
     * 2. LinkedBlockingQueue 无界队列,当运行线程大于corePoolSize时始终放入此队列,此时maximumPoolSize无效
     * 3. ArrayBlockingQueue  有界队列,相对无界队列有利于控制队列大小,队列满时,运行线程小于maxPoolSize时会创建新线程,否则触发异常策略
     * </pre>
     *
     * @param workQueue 队列
     * @return this
     */
    public ExecutorBuilder setWorkQueue(BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    /**
     * 使用{@link SynchronousQueue} 做为等待队列
     *
     * @return this
     */
    public ExecutorBuilder useSynchronousQueue() {
        return setWorkQueue(new SynchronousQueue<Runnable>());
    }

    /**
     * 设置线程工厂,用于自定义线程创建
     *
     * @param threadFactory 线程工厂
     * @return this
     * @see ThreadBuilder
     */
    public ExecutorBuilder setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * 设置当线程阻塞（block）时的异常处理器,所谓线程阻塞既线程池和等待队列已满,无法处理线程时采取的策略
     * <p>
     * 此处可以使用JDK预定义的几种策略,见{@link RejectPolicy}枚举
     *
     * @param handler {@link RejectedExecutionHandler}
     * @return this
     * @see RejectPolicy
     */
    public ExecutorBuilder setHandler(RejectedExecutionHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * 构建ThreadPoolExecutor
     */
    @Override
    public ThreadPoolExecutor build() {
        return build(this);
    }

}
