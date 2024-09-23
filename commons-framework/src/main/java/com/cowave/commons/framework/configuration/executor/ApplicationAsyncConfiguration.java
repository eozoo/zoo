package com.cowave.commons.framework.configuration.executor;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Shutdown;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.alibaba.ttl.threadpool.TtlExecutors;

import lombok.RequiredArgsConstructor;

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

    @Bean(name = { "applicationExecutor" })
    @Primary
    @Override
    public Executor getAsyncExecutor() {
        TaskExecutionProperties.Pool pool = taskExecutionProperties.getPool();
        Shutdown shutdown = taskExecutionProperties.getShutdown();
        ApplicationTaskExecutor taskExecutor = new ApplicationTaskExecutor(new ApplicationThreadPool(pool));
        taskExecutor.setAllowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(shutdown.isAwaitTermination());
        if(shutdown.isAwaitTermination()) {
            taskExecutor.setAwaitTerminationMillis(shutdown.getAwaitTerminationPeriod().toMillis());
        }
        return TtlExecutors.getTtlExecutor(taskExecutor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
