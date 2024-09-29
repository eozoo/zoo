/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author shanhuiming
 *
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Operation {

    /**
     * 操作类型
     */
    String type();

    /**
     * 操作动作
     */
    String action();

    /**
     * 操作描述
     */
    String desc() default "";

    /**
     * 操作内容
     */
    Content content() default Content.EMPTY;

    /**
     * 详情处理
     */
    Class<? extends OperationParser> contentHandler() default EmptyOperationParser.class;

    enum Content {

        /**
         * 填空
         */
        EMPTY,

        /**
         * 请求
         */
        REQ,

        /**
         * 响应
         */
        RESP,

        /**
         * 请求&响应
         */
        ALL
    }
}
