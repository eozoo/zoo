/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.lambda;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author jiangbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    private static ClassLoader systemClassLoader;

    static {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {
        }
    }

    /**
     * 加载对应名称的类
     */
    public static Class<?> toClassConfident(String name, ClassLoader classLoader) {
        try {
            return loadClass(name, getClassLoaders(classLoader));
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法", e);
        }
    }

    private static Class<?> loadClass(String className, ClassLoader[] classLoaders) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                try {
                    return Class.forName(className, true, classLoader);
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        throw new ClassNotFoundException("Cannot find class: " + className);
    }

    private static ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                Thread.currentThread().getContextClassLoader(),
                ReflectionUtils.class.getClassLoader(),
                systemClassLoader};
    }

    /**
     * 设置可访问对象的可访问权限为 true
     *
     * @param object 可访问的对象
     * @param <T>    类型
     * @return 返回设置后的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T object) {
        return AccessController.doPrivileged(new SetAccessibleAction<>(object));
    }

    public static class SetAccessibleAction<T extends AccessibleObject> implements PrivilegedAction<T> {
        private final T obj;

        public SetAccessibleAction(T obj) {
            this.obj = obj;
        }

        @Override
        public T run() {
            obj.setAccessible(true);
            return obj;
        }

    }
}
