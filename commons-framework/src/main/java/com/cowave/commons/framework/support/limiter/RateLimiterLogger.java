/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
