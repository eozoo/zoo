/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.lambda;

import com.cowave.commons.tools.ReflectUtils;
import com.cowave.commons.tools.lambda.meta.IdeaProxyMeta;
import com.cowave.commons.tools.lambda.meta.ReflectMeta;
import com.cowave.commons.tools.lambda.meta.SerializedMeta;
import com.cowave.commons.tools.lambda.meta.ShadowMeta;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Lambda 解析工具
 *
 * @author jiangbo
 */
public final class Lambdas {

    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     */
    public static <T> LambdaMeta extract(GetterFunction<T, ?> func) {
        // 1. Idea调试模式下, lambda表达式是一个代理
        if (func instanceof Proxy proxy) {
            return new IdeaProxyMeta(proxy);
        }

        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return new ReflectMeta((SerializedMeta) ReflectUtils.setAccessible(method).invoke(func));
        } catch (Exception e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowMeta(SerializedMeta.extract(func));
        }
    }
}
