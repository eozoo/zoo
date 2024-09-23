/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.ttl.TtlRunnable;
import com.cowave.commons.framework.access.Access;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;

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
            MDC.put("rid", requestId);
        }else{
            // 避免线程复用导致root.log的内容打到access.log
            MDC.put("rid", null);
        }
    }

    @Override
    public void execute(Runnable task) {
        super.execute(TtlRunnable.get(task, false, true));
    }
}
