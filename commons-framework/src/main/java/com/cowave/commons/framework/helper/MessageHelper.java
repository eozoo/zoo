/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.util.AssertsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import static org.springframework.feign.codec.ResponseCode.SYS_ERROR;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Component
public class MessageHelper {

    @Value("${spring.messages.enable:false}")
    private boolean messagesEnable;

    private final MessageSource messageSource;

    public String msg(String code, Object... args){
        return messageSource.getMessage(code, args, Access.language());
    }

    public Response<Void> translateErrorResponse(ResponseCode code, String msgKey, String msg){
        if (msgKey != null && messagesEnable) {
            try {
                return Response.error(code, msg(msgKey));
            }catch (Exception e){
                return Response.error(code, msgKey);
            }
        } else {
            return Response.error(code, msg);
        }
    }

    public Response<Void> translateAssertsResponse(AssertsException e){
        if (messagesEnable || e.getLanguage()) {
            try {
                if (e.getArgs() != null) {
                    return Response.error(SYS_ERROR, msg(e.getMessage(), e.getArgs()));
                } else {
                    return Response.error(SYS_ERROR, msg(e.getMessage()));
                }
            } catch (Exception ex) {
                return Response.error(SYS_ERROR, e.getMessage());
            }
        } else {
            return Response.error(SYS_ERROR, e.getMessage());
        }
    }
}
