/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import com.cowave.commons.tools.executors.TtlThreadPoolExecutor;
import org.dromara.dynamictp.core.support.DynamicTp;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@EnableAsync
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
@Configuration
@EnableConfigurationProperties(TaskExecutionProperties.class)
public class ApplicationAsyncConfiguration implements AsyncConfigurer {

    private final TaskExecutionProperties taskExecutionProperties;

    @DynamicTp
    @Primary
    @Bean(name = { "applicationExecutor" })
    @Override
    public ThreadPoolExecutor getAsyncExecutor() {
        TaskExecutionProperties.Pool pool = taskExecutionProperties.getPool();
        return new TtlThreadPoolExecutor(pool.getCoreSize(), pool.getMaxSize(), pool.getKeepAlive().toSeconds(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(pool.getQueueCapacity()));
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
