/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.lambda.convert;

import com.cowave.commons.tools.Converts;
import com.cowave.commons.tools.ReflectUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConverterCache {

    private static final Object LOCK = new Object();

    private static final Map<Class<?>, Class<?>> CACHE = new HashMap<>();

    public static <T> Class<T> getCacheClass(Class<? extends Converter<T>> clazz) {
        Class<?> targetClass = CACHE.get(clazz);
        if (targetClass == null) {
            synchronized (LOCK) {
                targetClass = CACHE.get(clazz);
                if (targetClass == null) {
                    targetClass = getTargetClass(clazz);
                }
            }
        }
        return Converts.cast(targetClass);
    }

    private static <T> Class<T> getTargetClass(Class<? extends Converter<T>> clazz) {
        Type type = ReflectUtils.getGenericInterfaceType(clazz, Converter.class);
        type = type instanceof ParameterizedType parameterizedType ? parameterizedType.getRawType() : type;
        Class<T> targetClass = Converts.cast(type);
        CACHE.put(clazz, targetClass);
        return targetClass;
    }
}
