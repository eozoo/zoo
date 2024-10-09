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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aKuang
 *
 */
public class RateLimiterLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterLogger.class);

    public static void info(double secondRate, String format, Object... arguments) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement caller = stackTrace[1];
            String scene = caller.getClassName() + "_" +  caller.getMethodName() + "_" + caller.getLineNumber();
            if (MethodLimiterHelper.tryAcquire(scene, secondRate)) {
                LOGGER.info(format, arguments);
            }
        }
    }

    public static void error(double secondRate, String msg, Throwable t) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement caller = stackTrace[1];
            String scene = caller.getClassName() + "_" +  caller.getMethodName() + "_" + caller.getLineNumber();
            if (MethodLimiterHelper.tryAcquire(scene, secondRate)) {
                LOGGER.error(msg, t);
            }
        }
    }
}
