/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.lambda.function;

import cn.hutool.core.util.ReflectUtil;
import com.cowave.lambda.LambdaUtils;
import com.cowave.lambda.lambda.LambdaMeta;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author jiangbo
 * @date 2023/5/17
 */
@FunctionalInterface
public interface EntryGetFunction<T, R> extends Function<T, R>, Serializable {

    @Override
    default <V> EntryGetFunction<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    /**
     * T 的类型
     */
    @SuppressWarnings("unchecked")
    default Class<T> getEntryClass() {
        LambdaMeta meta = LambdaUtils.extract(this);
        return (Class<T>) meta.getInstantiatedClass();
    }

    /**
     * 字段信息
     */
    @SuppressWarnings("unchecked")
    default Tuple2<String, Class<?>> getField() {
        LambdaMeta meta = LambdaUtils.extract(this);
        Class<T> clazz = (Class<T>) meta.getInstantiatedClass();
        String fieldName = meta.getFieldName();
        Field field = ReflectUtil.getField(clazz, fieldName);
        return Tuples.of(fieldName, field.getType());
    }

    /**
     * 字段名
     */
    default String getFieldName() {
        LambdaMeta meta = LambdaUtils.extract(this);
        return meta.getFieldName();
    }

}

