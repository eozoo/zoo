package com.cowave.commons.framework.access;

import com.cowave.commons.framework.helper.MessageHelper;
import com.cowave.commons.framework.helper.alarm.AccessAlarmFactory;
import com.cowave.commons.framework.helper.alarm.Alarm;
import com.cowave.commons.framework.helper.alarm.AlarmAccepter;
import com.cowave.commons.framework.util.AssertsException;
import com.cowave.commons.framework.util.DateUtils;
import com.cowave.exception.ViewErrorMessageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.feign.invoke.RemoteAssertsException;
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
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.feign.codec.ResponseCode.*;


/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
@RestControllerAdvice
public class AccessAdvice {

    private final AccessLogger accessLogger;

    private final MessageHelper messageHelper;

    @Nullable
    private final AlarmAccepter<Alarm> alarmAccepter;

    @Nullable
    private final AccessAlarmFactory<? extends Alarm> accessAlarmFactory;

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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return error(e, BAD_REQUEST, "frame.advice.httpRequestMethodNotSupportedException", "不支持的请求方法", false);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public Response<Void> handleHttpMessageConversionException(HttpMessageConversionException e) {
        return error(e, BAD_REQUEST, "frame.advice.httpMessageConversionException", "请求参数转换失败", false);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return error(e, BAD_REQUEST, msg, msg, false);
    }

    @ExceptionHandler(BindException.class)
    public Response<Void> handleBindException(BindException e) {
        String msg = messageHelper.msg(e.getAllErrors().get(0).getDefaultMessage());
        return error(e, BAD_REQUEST, msg, msg, false);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getMessage().split(": ")[1];
        return error(e, BAD_REQUEST, msg, msg, false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return error(e, BAD_REQUEST, "frame.advice.illegalArgumentException", "非法参数", false);
    }

    @ExceptionHandler(ViewErrorMessageException.class)
    public Response<Void> handleViewErrorMessageException(ViewErrorMessageException e) {
        return error(e, SYS_ERROR, null, e.getMessage(), true);
    }

    @ExceptionHandler(RemoteAssertsException.class)
    public Response<Void> handleRemoteAssertsException(RemoteAssertsException e) {
        return error(e, SYS_ERROR, null, e.getMessage(), true);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Response<Void> handleAccessDeniedException(AccessDeniedException e) {
        return error(e, FORBIDDEN, "frame.auth.denied", "没有访问权限", true);
    }

    @ExceptionHandler(SQLException.class)
    public Response<Void> handleSqlException(SQLException e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.sqlException", "数据操作失败", true);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Response<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.duplicateKeyException", "数据主键冲突", true);
    }

    @ExceptionHandler(DataAccessException.class)
    public Response<Void> handleDataAccessException(DataAccessException e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.dataAccessException", "数据访问失败", true);
    }

    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception e) {
        return error(e, INTERNAL_SERVER_ERROR, "frame.advice.exception", "系统错误", true);
    }

    private Response<Void> error(Exception e, ResponseCode code, String msgKey, String msg, boolean checkAlarm) {
        AccessLogger.error("", e);
        Response<Void> resp = messageHelper.translateErrorResponse(code, msgKey, msg);
        accessLogger.logResponse(resp);
        try {
            LinkedList<String> cause = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toCollection(LinkedList::new));
            cause.addFirst(e.getMessage());
            resp.setCause(cause);
            if (checkAlarm) {
                newAccessAlarm(resp);
            }
        } catch (Exception ex) {
            AccessLogger.error("", ex);
        }
        return resp;
    }

    @ExceptionHandler(AssertsException.class)
    public Response<Void> handleAssertsException(AssertsException e) {
        AccessLogger.error("", e);
        Response<Void> resp = messageHelper.translateAssertsResponse(e);
        accessLogger.logResponse(resp);
        try {
            LinkedList<String> cause = Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toCollection(LinkedList::new));
            cause.addFirst(e.getMessage());
            resp.setCause(cause);
            newAccessAlarm(resp);
        } catch (Exception ex) {
            AccessLogger.error("", ex);
        }
        return resp;
    }

    private void newAccessAlarm(Response<Void> errorResp) {
        if (alarmAccepter != null && accessAlarmFactory != null) {
            alarmAccepter.accept(accessAlarmFactory.newAccessAlarm(errorResp));
        }
    }
}
