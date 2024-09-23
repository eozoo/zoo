/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.lambda.lambda.meta;

import com.cowave.lambda.ReflectionUtils;
import com.cowave.lambda.lambda.LambdaMeta;

/**
 * @author jiangbo
 */
public class ShadowLambdaMeta implements LambdaMeta {

    private static final String SEMICOLON = ";";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private final SerializedLambda lambda;

    public ShadowLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(SEMICOLON)).replace(SLASH, DOT);
        return ReflectionUtils.toClassConfident(instantiatedType, lambda.getCapturingClass().getClassLoader());
    }

}
