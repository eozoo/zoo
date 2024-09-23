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
import com.cowave.commons.tools.AssertsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

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
        return messageSource.getMessage(code, args, Access.accessLanguage());
    }

    public String translateErrorMessage(String msgKey, String msg){
        if (msgKey != null && messagesEnable) {
            try {
                return msg(msgKey);
            }catch (Exception e){
                return msgKey;
            }
        } else {
            return msg;
        }
    }

    public String translateAssertsMessage(AssertsException e){
        if (messagesEnable || e.getLanguage()) {
            try {
                if (e.getArgs() != null) {
                    return msg(e.getMessage(), e.getArgs());
                } else {
                    return msg(e.getMessage());
                }
            } catch (Exception ex) {
                return e.getMessage();
            }
        } else {
            return e.getMessage();
        }
    }
}
