/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.access;

import com.cowave.commons.framework.helper.MessageHelper;
import com.cowave.commons.framework.helper.alarm.AccessAlarmFactory;
import com.cowave.commons.framework.helper.alarm.Alarm;
import com.cowave.commons.framework.helper.alarm.AlarmHandler;
import com.cowave.commons.tools.AssertsException;
import com.cowave.commons.tools.HttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.feign.codec.HttpResponse;
import org.springframework.feign.invoke.RemoteAssertsException;
import org.springframework.feign.invoke.RemoteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * @author shanhuiming
 */
@ConditionalOnProperty(prefix = "spring.application.advice.http", value = "enabled")
@RequiredArgsConstructor
@RestControllerAdvice
public class HttpAccessAdvice {

    // ErrorLog和Response.cause都不记内容
    private static final int ERR_LEVEL_0 = 0;

    // ErrorLog不记，Response.cause记录e.msg
    private static final int ERR_LEVEL_1 = 1;

    // ErrorLog和Response.cause都记e.msg
    private static final int ERR_LEVEL_2 = 2;

    // ErrorLog和Response.cause都记e.stack
    private static final int ERR_LEVEL_3 = 3;

    private final AccessLogger accessLogger;

    private final MessageHelper messageHelper;

    private final ThreadPoolExecutor applicationExecutor;

    @Nullable
    private final AlarmHandler alarmHandler;

    @Nullable
    private final AccessAlarmFactory<? extends Alarm> accessAlarmFactory;

    @ExceptionHandler(HttpException.class)
    public HttpResponse<Map<String, String>> handleHttpException(HttpException e) {
        AccessLogger.error("", e);

        Map<String, String> body = Map.of("code", e.getCode(), "msg", e.getMessage());
        HttpResponse<Map<String, String>> httpResponse = new HttpResponse<>(e.getStatus(), null, body);

        httpResponse.setMessage(String.format("{code=%s, msg=%s}", e.getCode(), e.getMessage()));
        accessLogger.logResponse(httpResponse);

        processAccessAlarm(e.getStatus(), e.getCode(), e.getMessage(), httpResponse, e);
        return httpResponse;
    }

    @ExceptionHandler(AssertsException.class)
    public HttpResponse<Map<String, String>> handleAssertsException(AssertsException e) {
        AccessLogger.error("", e);

        String message = messageHelper.translateAssertsMessage(e);
        Map<String, String> body = Map.of("code", INTERNAL_SERVER_ERROR.name(), "msg", message);
        HttpResponse<Map<String, String>> httpResponse = new HttpResponse<>(INTERNAL_SERVER_ERROR.value(), null, body);

        httpResponse.setMessage(String.format("{code=%s, msg=%s}", INTERNAL_SERVER_ERROR.name(), message));
        accessLogger.logResponse(httpResponse);

        processAccessAlarm(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.name(), message, httpResponse, e);
        return httpResponse;
    }

    @ExceptionHandler(RemoteAssertsException.class)
    public HttpResponse<Map<String, String>> handleRemoteAssertsException(RemoteAssertsException e) {
        return error(e, INTERNAL_SERVER_ERROR, null, e.getMessage(), ERR_LEVEL_0);
    }

    @ExceptionHandler(RemoteException.class)
    public HttpResponse<Map<String, String>> handleRemoteException(RemoteException e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.remote.failed", "远程调用失败", ERR_LEVEL_1);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public HttpResponse<Map<String, String>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return error(e, BAD_REQUEST, "frame.advice.httpRequestMethodNotSupportedException", "不支持的请求方法", ERR_LEVEL_2);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public HttpResponse<Map<String, String>> handleHttpMessageConversionException(HttpMessageConversionException e) {
        return error(e, BAD_REQUEST, "frame.advice.httpMessageConversionException", "请求参数转换失败", ERR_LEVEL_2);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpResponse<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return error(e, BAD_REQUEST, msg, msg, ERR_LEVEL_2);
    }

    @ExceptionHandler(BindException.class)
    public HttpResponse<Map<String, String>> handleBindException(BindException e) {
        String msg = messageHelper.msg(e.getAllErrors().get(0).getDefaultMessage());
        return error(e, BAD_REQUEST, msg, msg, ERR_LEVEL_2);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public HttpResponse<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getMessage().split(": ")[1];
        return error(e, BAD_REQUEST, msg, msg, ERR_LEVEL_2);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public HttpResponse<Map<String, String>> handleAccessDeniedException(AccessDeniedException e) {
        return error(e, FORBIDDEN, "frame.auth.denied", "没有访问权限", ERR_LEVEL_2);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public HttpResponse<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return error(e, BAD_REQUEST, "frame.advice.illegalArgumentException", "非法参数", ERR_LEVEL_3);
    }

    @ExceptionHandler(SQLException.class)
    public HttpResponse<Map<String, String>> handleSqlException(SQLException e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.sqlException", "数据操作失败", ERR_LEVEL_3);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public HttpResponse<Map<String, String>> handleDuplicateKeyException(DuplicateKeyException e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.duplicateKeyException", "数据主键冲突", ERR_LEVEL_3);
    }

    @ExceptionHandler(DataAccessException.class)
    public HttpResponse<Map<String, String>> handleDataAccessException(DataAccessException e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.dataAccessException", "数据访问失败", ERR_LEVEL_3);
    }

    @ExceptionHandler(Exception.class)
    public HttpResponse<Map<String, String>> handleException(Exception e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.exception", "系统错误", ERR_LEVEL_3);
    }

    private HttpResponse<Map<String, String>> error(Exception e, HttpStatus status, String msgKey, String msg, int errLevel) {
        // 异常日志
        if(errLevel >= ERR_LEVEL_3){
            AccessLogger.error("", e);
        }else if(errLevel == ERR_LEVEL_2){
            AccessLogger.error(e.getMessage());
        }

        String message = messageHelper.translateErrorMessage(msgKey, msg);
        Map<String, String> body = Map.of("code", status.name(), "msg", message);
        HttpResponse<Map<String, String>> httpResponse = new HttpResponse<>(status.value(), null, body);

        httpResponse.setMessage(String.format("{code=%s, msg=%s}", status.name(), message));
        accessLogger.logResponse(httpResponse);

        processAccessAlarm(status.value(), status.name(), message, httpResponse, e);
        return httpResponse;
    }

    private void processAccessAlarm(int httpStatus, String code, String message, Object response, Exception e) {
        if (alarmHandler != null && accessAlarmFactory != null) {
            Alarm alarm = accessAlarmFactory.createAlarm(httpStatus, code, message, response, e);
            if(alarm.isAsync()){
                applicationExecutor.execute(() -> alarmHandler.handle(alarm));
            }else{
                alarmHandler.handle(alarm);
            }
        }
    }
}
