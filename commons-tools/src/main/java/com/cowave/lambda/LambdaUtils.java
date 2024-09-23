/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.lambda;


import com.cowave.lambda.function.EntryGetFunction;
import com.cowave.lambda.lambda.LambdaMeta;
import com.cowave.lambda.lambda.meta.IdeaProxyLambdaMeta;
import com.cowave.lambda.lambda.meta.ReflectLambdaMeta;
import com.cowave.lambda.lambda.meta.SerializedLambda;
import com.cowave.lambda.lambda.meta.ShadowLambdaMeta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Lambda 解析工具类
 *
 * @author jiangbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaUtils {

    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     */
    public static <T> LambdaMeta extract(EntryGetFunction<T, ?> func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy proxy) {
            return new IdeaProxyLambdaMeta(proxy);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return new ReflectLambdaMeta((SerializedLambda) ReflectionUtils.setAccessible(method).invoke(func));
        } catch (Exception e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(SerializedLambda.extract(func));
        }
    }

}
