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
     * 处理表达式，可用参数：
     * <p> 1.opHandler: 处理类
     * <p> 2.方法入参
     * <p> 3.opInfo: 操作信息（类型 OperationInfo）
     * <p> 4.resp: 返回值
     * <p> 5.exception: 异常对象
     * <p>
     * <p> 示例：opHandler.handle(opInfo, resp, exception, ...)
     */
    String opExpr();

    /**
     * 日志处理类（spring bean）
     */
    Class<?> handlerClass();

    /**
     * 是否异步处理
     */
    boolean isAsync() default false;
}
