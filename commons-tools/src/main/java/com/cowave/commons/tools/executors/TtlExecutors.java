/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.executors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author jiangbo
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TtlExecutors {

    public static void exec(Runnable runnable) {
        ThreadPoolExecutor executor = new TtlThreadPoolExecutor(0, 1,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        executor.execute(runnable);
    }

    public static ThreadPoolExecutor newSingleThreadPool(ThreadFactory threadFactory) {
        ThreadPoolExecutor executor = new TtlThreadPoolExecutor(1, 1,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdownNow));
        return executor;
    }

    public static ThreadPoolExecutor newCachedThreadPool(ThreadFactory threadFactory) {
        ThreadPoolExecutor executor = new TtlThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdownNow));
        return executor;
    }

    public static ThreadPoolExecutor newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        ThreadPoolExecutor executor = new TtlThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdownNow));
        return executor;
    }

    public static ScheduledThreadPoolExecutor newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, threadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdownNow));
        return executor;
    }

    public static ScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdownNow));
        return executor;
    }
}
