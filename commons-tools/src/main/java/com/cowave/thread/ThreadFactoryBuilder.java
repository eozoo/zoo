/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.thread;

import lombok.extern.slf4j.Slf4j;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jiangbo
 */
@Slf4j
public class ThreadFactoryBuilder {

    private String nameFormat = null;

    private boolean daemon = true;

    private Integer priority = null;

    private UncaughtExceptionHandler uncaughtExceptionHandler = null;

    private ThreadFactory backingThreadFactory = null;

    private ThreadFactoryBuilder() {
    }

    public static ThreadFactoryBuilder apply() {
        return new ThreadFactoryBuilder();
    }

    public ThreadFactoryBuilder nameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
        return this;
    }

    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactoryBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public ThreadFactoryBuilder uncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    public ThreadFactoryBuilder threadFactory(ThreadFactory backingThreadFactory) {
        this.backingThreadFactory = backingThreadFactory;
        return this;
    }

    public ThreadFactory build() {
        return build(this);
    }

    private static ThreadFactory build(ThreadFactoryBuilder builder) {
        String nameFormat = builder.nameFormat;
        boolean daemon = builder.daemon;
        Integer priority = builder.priority;
        UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
        ThreadFactory backingThreadFactory = (builder.backingThreadFactory != null) ? builder.backingThreadFactory
                : Executors.defaultThreadFactory();
        AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;

        return ((Runnable runnable) -> {
            Thread thread = backingThreadFactory.newThread(runnable);
            if (nameFormat != null) {
                thread.setName(String.format(nameFormat, count.getAndIncrement()));
            }
            thread.setDaemon(daemon);
            if (priority != null) {
                thread.setPriority(priority);
            }
            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            } else {
                thread.setUncaughtExceptionHandler((Thread t, Throwable e) -> log.error("Thread[" + t.getName() + "] exited anomaly", e));
            }
            return thread;
        });
    }
}
