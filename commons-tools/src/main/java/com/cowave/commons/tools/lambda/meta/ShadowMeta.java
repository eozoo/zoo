/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.lambda.meta;

import com.cowave.commons.tools.ReflectUtils;
import com.cowave.commons.tools.lambda.LambdaMeta;

/**
 * @author jiangbo
 */
public class ShadowMeta implements LambdaMeta {

    private static final String SEMICOLON = ";";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private final SerializedMeta lambda;

    public ShadowMeta(SerializedMeta lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(SEMICOLON)).replace(SLASH, DOT);
        return ReflectUtils.loadClass(instantiatedType, lambda.getCapturingClass().getClassLoader());
    }

}
