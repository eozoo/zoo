/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.thread;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @author jiangbo
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TtlExecutorsUtil {

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return TtlExecutors.getTtlExecutorService(ExecutorsUtil.newCachedThreadPool(threadFactory));
    }

    public static ExecutorService newFixedThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return TtlExecutors.getTtlExecutorService(ExecutorsUtil.newFixedThreadPool(corePoolSize, threadFactory));
    }

    public static void createAndStartThread(Runnable runnable, String name, boolean daemon) {
        ExecutorsUtil.createAndStartThread(TtlRunnable.get(runnable), name, daemon);
    }
}
