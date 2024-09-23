/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.entry;

import com.cowave.convert.Converts;
import com.cowave.lambda.TypeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangbo
 * @date 2024/1/15
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlatTargetManager {

    private static final Object LOCK = new Object();
    private static final Map<Class<?>, Class<?>> CACHE = new HashMap<>();

    /**
     * 获取目标类型
     */
    public static <T> Class<T> getTargetClass(Class<? extends FlatTarget<T>> clazz) {
        Class<?> targetClass = CACHE.get(clazz);
        if (targetClass == null) {
            synchronized (LOCK) {
                targetClass = CACHE.get(clazz);
                if (targetClass == null) {
                    targetClass = fetchTargetClass(clazz);
                }
            }
        }
        return Converts.cast(targetClass);
    }

    private static <T> Class<T> fetchTargetClass(Class<? extends FlatTarget<T>> clazz) {
        Type type = TypeUtils.getGenericInterfaceType(clazz, FlatTarget.class);
        type = type instanceof ParameterizedType parameterizedType ? parameterizedType.getRawType() : type;
        Class<T> targetClass = Converts.cast(type);
        CACHE.put(clazz, targetClass);
        return targetClass;
    }

}
