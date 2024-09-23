package com.cowave.commons.framework.configuration.executor;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 *
 * @author shanhuiming
 *
 */
public class ApplicationTaskExecutor extends ThreadPoolTaskExecutor {

    private final ApplicationThreadPool threadPool;

    public ApplicationTaskExecutor(ApplicationThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
        return threadPool;
    }
}
