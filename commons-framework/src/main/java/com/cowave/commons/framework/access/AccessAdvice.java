/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.access;

import com.cowave.commons.tools.Messages;
import com.cowave.commons.framework.helper.alarm.AccessAlarmFactory;
import com.cowave.commons.framework.helper.alarm.Alarm;
import com.cowave.commons.framework.helper.alarm.AlarmHandler;
import com.cowave.commons.tools.AssertsException;
import com.cowave.commons.tools.DateUtils;
import com.cowave.commons.tools.HttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.feign.codec.HttpResponse;
import org.springframework.feign.codec.Response;
import org.springframework.feign.invoke.RemoteAssertsException;
import org.springframework.feign.invoke.RemoteException;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;
import java.beans.PropertyEditorSupport;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.springframework.feign.codec.ResponseCode.*;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@RestControllerAdvice
public class AccessAdvice {

    // ErrorLog和Response.cause都不记内容
    private static final int ERR_LEVEL_0 = 0;

    // ErrorLog不记，Response.cause记录e.msg
    private static final int ERR_LEVEL_1 = 1;

    // ErrorLog和Response.cause都记e.msg
    private static final int ERR_LEVEL_2 = 2;

    // ErrorLog和Response.cause都记e.stack
    private static final int ERR_LEVEL_3 = 3;

    private final AccessLogger accessLogger;

    private final ThreadPoolExecutor applicationExecutor;

    @Nullable
    private final AccessAlarmFactory<? extends Alarm> accessAlarmFactory;

    @Nullable
    private final AlarmHandler alarmHandler;

    @Value("${spring.application.response-always-success:false}")
    private boolean responseAlwaysSuccess;

    /**
     * 参数转换
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parse(text));
            }
        });
    }

    @ExceptionHandler(HttpException.class)
    public HttpResponse<Response<Void>> handleHttpException(HttpException e) {
        return error(e, e.getStatus(), e.getCode(), ERR_LEVEL_3, e.getMessage());
    }

    @ExceptionHandler(AssertsException.class)
    public HttpResponse<Response<Void>> handleAssertsException(AssertsException e) {
        return error(e, SYS_ERROR.getStatus(), SYS_ERROR.getCode(), ERR_LEVEL_3, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpResponse<Response<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return error(e, BAD_REQUEST.getStatus(), BAD_REQUEST.getCode(), ERR_LEVEL_2, message);
    }

    @ExceptionHandler(BindException.class)
    public HttpResponse<Response<Void>> handleBindException(BindException e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return error(e, BAD_REQUEST.getStatus(), BAD_REQUEST.getCode(), ERR_LEVEL_2, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public HttpResponse<Response<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getMessage().split(": ")[1];
        return error(e, BAD_REQUEST.getStatus(), BAD_REQUEST.getCode(), ERR_LEVEL_2, message);
    }

    @ExceptionHandler(RemoteAssertsException.class)
    public HttpResponse<Response<Void>> handleRemoteAssertsException(RemoteAssertsException e) {
        return error(e, INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getCode(), ERR_LEVEL_0, e.getMessage());
    }

    @ExceptionHandler(RemoteException.class)
    public HttpResponse<Response<Void>> handleRemoteException(RemoteException e) {
        return error(e, INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getCode(), ERR_LEVEL_1, Messages.msg("frame.remote.failed"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public HttpResponse<Response<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return error(e, BAD_REQUEST.getStatus(), BAD_REQUEST.getCode(), ERR_LEVEL_2, Messages.msg("frame.advice.httpRequestMethodNotSupportedException"));
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public HttpResponse<Response<Void>> handleHttpMessageConversionException(HttpMessageConversionException e) {
        return error(e, BAD_REQUEST.getStatus(), BAD_REQUEST.getCode(), ERR_LEVEL_2, Messages.msg("frame.advice.httpMessageConversionException"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public HttpResponse<Response<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return error(e, FORBIDDEN.getStatus(), FORBIDDEN.getCode(), ERR_LEVEL_2, Messages.msg("frame.auth.denied"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public HttpResponse<Response<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return error(e, BAD_REQUEST.getStatus(), BAD_REQUEST.getCode(), ERR_LEVEL_3, Messages.msg("frame.advice.illegalArgumentException"));
    }

    @ExceptionHandler(SQLException.class)
    public HttpResponse<Response<Void>> handleSqlException(SQLException e) {
        return error(e, INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getCode(), ERR_LEVEL_3, Messages.msg("frame.advice.sqlException"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public HttpResponse<Response<Void>> handleDuplicateKeyException(DuplicateKeyException e) {
        return error(e, INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getCode(), ERR_LEVEL_3, Messages.msg("frame.advice.duplicateKeyException"));
    }

    @ExceptionHandler(DataAccessException.class)
    public HttpResponse<Response<Void>> handleDataAccessException(DataAccessException e) {
        return error(e, INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getCode(), ERR_LEVEL_3, Messages.msg("frame.advice.dataAccessException"));
    }

    @ExceptionHandler(Exception.class)
    public HttpResponse<Response<Void>> handleException(Exception e) {
        return error(e, INTERNAL_SERVER_ERROR.getStatus(), INTERNAL_SERVER_ERROR.getCode(), ERR_LEVEL_3, Messages.msg("frame.advice.exception"));
    }

    private HttpResponse<Response<Void>> error(Exception e, int httpStatus, String code, int errLevel, String message) {
        if(errLevel >= ERR_LEVEL_3){
            AccessLogger.error("", e);
        }else if(errLevel == ERR_LEVEL_2){
            AccessLogger.error(e.getMessage());
        }
        // Response
        Response<Void> response = new Response<>(code, message, null);
        if(errLevel >= ERR_LEVEL_3){
            try {
                LinkedList<String> cause = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toCollection(LinkedList::new));
                cause.addFirst(e.getMessage());
                response.setCause(cause);
            } catch (Exception ex) {
                AccessLogger.error("", ex);
            }
        }else if(errLevel >= ERR_LEVEL_1){
            response.setCause(List.of(e.getMessage()));
        }
        // HttpResponse
        if(responseAlwaysSuccess){
            httpStatus = SUCCESS.getStatus();
        }
        HttpResponse<Response<Void>> httpResponse = new HttpResponse<>(httpStatus, null, response);
        httpResponse.setMessage(String.format("{code=%s, msg=%s}", code, message));
        // log & alarm
        accessLogger.logResponse(httpResponse);
        if (alarmHandler != null && accessAlarmFactory != null) {
            Alarm alarm = accessAlarmFactory.createAlarm(httpStatus, code, message, response, e);
            if(alarm.isAsync()){
                applicationExecutor.execute(() -> alarmHandler.handle(alarm));
            }else{
                alarmHandler.handle(alarm);
            }
        }
        return httpResponse;
    }
}
