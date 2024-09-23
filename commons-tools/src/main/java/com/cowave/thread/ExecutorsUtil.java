/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.thread;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author jiangbo
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutorsUtil {

    public static ScheduledExecutorService scheduledExecutorService(int corePoolSize, ThreadFactory threadFactory) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize, threadFactory);

        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));

        return service;
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdownNow));
        return executor;
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(threadFactory);
        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
        return service;
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        ExecutorService service = Executors.newCachedThreadPool(threadFactory);
        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
        return service;
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        ExecutorService service = Executors.newFixedThreadPool(corePoolSize, threadFactory);
        // add shutdown hook when JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
        return service;
    }

    public static void createAndStartThread(Runnable runnable, String name, boolean daemon) {
        Executors.newSingleThreadExecutor().execute(runnable);
    }
}
