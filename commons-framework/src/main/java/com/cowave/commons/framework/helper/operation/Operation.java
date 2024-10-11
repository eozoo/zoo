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
    String type() default "";

    /**
     * 操作动作
     */
    String action() default "";

    /**
     * 操作描述，支持SPEL，可用参数
     * <p> 1.方法入参
     * <p> 2.resp: 返回值
     * <p> 3.exception: 异常对象
     */
    String summary() default "";

    /**
     * 操作处理，支持SPEL，可用参数：
     * <p> 1.opHandler: 处理类
     * <p> 2.方法入参
     * <p> 3.resp: 返回值
     * <p> 4.exception: 异常对象
     * <p> 5.opInfo: 操作信息（类型 OperationInfo）
     * <p>
     * <p> 示例：opHandler.handle(opInfo, resp, exception, ...)
     */
    String expr();

    /**
     * 日志处理类（spring bean）
     */
    Class<?> handler();

    /**
     * 是否异步处理
     */
    boolean isAsync() default false;
}
