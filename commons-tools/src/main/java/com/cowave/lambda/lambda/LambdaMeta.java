/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.lambda.lambda;

import java.util.Locale;

/**
 * @author jiangbo
 */
public interface LambdaMeta {

    /**
     * 获取lambda表达式实现方法的名称
     */
    String getImplMethodName();

    /**
     * 获取lambda表达式对应的字段名
     */
    default String getFieldName() {
        String name = getImplMethodName();
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new IllegalArgumentException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }
        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }

    /**
     * 实例化该方法的类
     */
    Class<?> getInstantiatedClass();

}
