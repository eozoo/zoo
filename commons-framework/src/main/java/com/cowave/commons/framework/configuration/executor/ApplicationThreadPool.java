package com.cowave.commons.framework.configuration.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cowave.commons.framework.access.Access;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;

import com.alibaba.excel.util.StringUtils;

/**
 *
 * @author shanhuiming
 *
 */
public class ApplicationThreadPool extends ThreadPoolExecutor {

    public ApplicationThreadPool(TaskExecutionProperties.Pool pool) {
        super(pool.getCoreSize(), pool.getMaxSize(), pool.getKeepAlive().toSeconds(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(pool.getQueueCapacity()));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        String requestId = Access.id();
        if(StringUtils.isNotBlank(requestId)) {
            t.setName("*" + requestId);
        }
        MDC.put("tid", String.valueOf(Thread.currentThread().getId()));
    }
}
