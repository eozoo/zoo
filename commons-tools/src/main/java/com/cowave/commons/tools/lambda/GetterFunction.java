/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.lambda;

import cn.hutool.core.util.ReflectUtil;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author jiangbo
 */
@FunctionalInterface
public interface GetterFunction<T, R> extends Function<T, R>, Serializable {

    /**
     * T 的类型
     */
    @SuppressWarnings("unchecked")
    default Class<T> extractClass() {
        LambdaMeta meta = Lambdas.extract(this);
        return (Class<T>) meta.getInstantiatedClass();
    }

    /**
     * 字段
     */
    @SuppressWarnings("unchecked")
    default Tuple2<String, Class<?>> getField() {
        LambdaMeta meta = Lambdas.extract(this);
        String fieldName = meta.getFieldName();

        Class<T> clazz = (Class<T>) meta.getInstantiatedClass();
        Field field = ReflectUtil.getField(clazz, fieldName);
        return Tuples.of(fieldName, field.getType());
    }

    /**
     * 字段名
     */
    default String getFieldName() {
        LambdaMeta meta = Lambdas.extract(this);
        return meta.getFieldName();
    }

    @Override
    default <V> GetterFunction<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
}

