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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author jiangbo
 * @date 2023/12/14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeUtils {

    /**
     * 获取当前类接口上的范型
     *
     * @param clazz             当前类
     * @param interfaceClazz    拥有相同范型的接口
     */
    public static <T> Type getGenericInterfaceType(Class<? extends T> clazz, Class<T> interfaceClazz) {
        return Arrays.stream(clazz.getGenericInterfaces())
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .filter(type -> Objects.equals(type.getRawType(), interfaceClazz))
                .findFirst()
                .map(type -> {
                    Type[] types = type.getActualTypeArguments();
                    if(types.length != 1){
                        throw new UnsupportedOperationException("范型未定义");
                    }
                    return types[0];
                }).orElseThrow(() -> new UnsupportedOperationException("范型未定义"));

    }
}
