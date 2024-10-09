/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.limiter;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author aKuang
 */
public class MethodLimiterHelper {

    private static final Map<String, RateLimiter> RATE_LIMITER = new ConcurrentHashMap<>();

    static RateLimiter getLimiter(String key, double permitsPerSecond) {
        RateLimiter limiter = RATE_LIMITER.get(key);
        if (limiter != null) {
            return limiter;
        }

        limiter = RateLimiter.create(permitsPerSecond);
        return RATE_LIMITER.putIfAbsent(key, limiter);
    }

    public static void acquire(String name, double permitsPerSecond) {
        MethodLimiterHelper.getLimiter(name, permitsPerSecond).acquire();
    }

    public static boolean tryAcquire(String name, double permitsPerSecond) {
        return MethodLimiterHelper.getLimiter(name, permitsPerSecond).tryAcquire();
    }

    public static boolean tryAcquire(String name, double permitsPerSecond, long waitTime, TimeUnit timeUnit) {
        return MethodLimiterHelper.getLimiter(name, permitsPerSecond).tryAcquire(waitTime, timeUnit);
    }
}
