/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.limiter;

import com.cowave.commons.tools.HttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.springframework.feign.codec.ResponseCode.TOO_MANY_REQUESTS;

/**
 * @author aKuang
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MethodLimiterAspect {

    @Pointcut("@annotation(com.cowave.commons.framework.support.limiter.MethodLimiter)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        MethodLimiter methodLimiter = signature.getMethod().getAnnotation(MethodLimiter.class);
        String name = methodLimiter.name();
        if (StringUtils.isBlank(name)) {
            String className = Optional.ofNullable(point.getTarget()).map(Object::getClass).map(Class::getSimpleName).orElse("");
            String methodName = Optional.ofNullable(signature.getMethod()).map(Method::getName).orElse("");
            name = className + "_" + methodName;
        }

        long waitTime = methodLimiter.waitTime();
        if (waitTime == -1) {
            MethodLimiterHelper.acquire(name, methodLimiter.permitsPerSecond());
        } else {
            if (!MethodLimiterHelper.tryAcquire(name, methodLimiter.permitsPerSecond(), waitTime, methodLimiter.timeUnit())) {
                throw new HttpException(TOO_MANY_REQUESTS, "{frame.operation.limit}");
            }
        }
        return point.proceed();
    }
}
