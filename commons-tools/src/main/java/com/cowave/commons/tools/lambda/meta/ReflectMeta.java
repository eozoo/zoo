/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.lambda.meta;

import com.cowave.commons.tools.ReflectUtils;
import com.cowave.commons.tools.lambda.LambdaMeta;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * @author jiangbo
 */
@Slf4j
public class ReflectMeta implements LambdaMeta {

    private static final String SEMICOLON = ";";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final Field FIELD_CAPTURING_CLASS;

    static {
        Field fieldCapturingClass;
        try {
            Class<SerializedMeta> aClass = SerializedMeta.class;
            fieldCapturingClass = ReflectUtils.setAccessible(aClass.getDeclaredField("capturingClass"));
        } catch (Exception e) {
            // 解决高版本 jdk 的问题 gitee: https://gitee.com/baomidou/mybatis-plus/issues/I4A7I5
            log.warn(e.getMessage());
            fieldCapturingClass = null;
        }
        FIELD_CAPTURING_CLASS = fieldCapturingClass;
    }

    private final SerializedMeta lambda;

    public ReflectMeta(SerializedMeta lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(SEMICOLON)).replace(SLASH, DOT);
        return ReflectUtils.loadClass(instantiatedType, getCapturingClassClassLoader());
    }

    private ClassLoader getCapturingClassClassLoader() {
        // 如果反射失败，使用默认的 classloader
        if (FIELD_CAPTURING_CLASS == null) {
            return null;
        }

        try {
            return ((Class<?>) FIELD_CAPTURING_CLASS.get(lambda)).getClassLoader();
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
