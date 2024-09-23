/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.lambda.lambda.meta;


import com.cowave.lambda.ReflectionUtils;
import com.cowave.lambda.lambda.LambdaMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author jiangbo
 */
public class IdeaProxyLambdaMeta implements LambdaMeta {
    private static final Field FIELD_MEMBER_NAME;
    private static final Field FIELD_MEMBER_NAME_CLAZZ;
    private static final Field FIELD_MEMBER_NAME_NAME;

    static {
        try {
            Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
            FIELD_MEMBER_NAME = ReflectionUtils.setAccessible(classDirectMethodHandle.getDeclaredField("member"));
            Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
            FIELD_MEMBER_NAME_CLAZZ = ReflectionUtils.setAccessible(classMemberName.getDeclaredField("clazz"));
            FIELD_MEMBER_NAME_NAME = ReflectionUtils.setAccessible(classMemberName.getDeclaredField("name"));
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Class<?> clazz;
    private final String name;

    public IdeaProxyLambdaMeta(Proxy func) {
        InvocationHandler handler = Proxy.getInvocationHandler(func);
        try {
            Object dmh = ReflectionUtils.setAccessible(handler.getClass().getDeclaredField("val$target")).get(handler);
            Object member = FIELD_MEMBER_NAME.get(dmh);
            clazz = (Class<?>) FIELD_MEMBER_NAME_CLAZZ.get(member);
            name = (String) FIELD_MEMBER_NAME_NAME.get(member);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getImplMethodName() {
        return name;
    }

    @Override
    public Class<?> getInstantiatedClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return clazz.getSimpleName() + "::" + name;
    }

}
