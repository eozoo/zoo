/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.redisson.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author aKuang
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLock {

    /**
     * 锁名称
     */
    String name();

    /**
     * 锁标识信息
     */
    String key();

    /**
     * 等待时间
     */
    long await() default 3;

    /**
     * 存续时间
     */
    long lease() default -1;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
