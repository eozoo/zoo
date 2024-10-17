/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.configuration;

import com.cowave.commons.tools.executors.TtlThreadPoolExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
@EnableConfigurationProperties(TaskExecutionProperties.class)
public class AsyncConfiguration implements AsyncConfigurer {

    private final TaskExecutionProperties taskExecutionProperties;

    @Primary
    @Bean(name = {"applicationTaskExecutor", "taskExecutor"})
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
