/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.limiter;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author aKuang
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MethodLimiter {

    /**
     * 默认（类名 + 方法名）
     */
    String name() default "";

    /**
     * 每秒令牌数
     */
    double permitsPerSecond();

    /**
     * 等待时间
     * -1：一直阻塞
     */
    long waitTime() default 0;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
