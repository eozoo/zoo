/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

/**
 *
 * @author shanhuiming
 *
 */
@Component
public class Messages {

    private static final ThreadLocal<Locale> LOCAL = new TransmittableThreadLocal<>();

    private static MessageSource messageSource;

    @Resource
    public void setMessageSource(MessageSource messageSource) {
        Messages.messageSource = messageSource;
    }

    public static void setLanguage(String language){
        if(StringUtils.isNotBlank(language)){
            if(language.toLowerCase().contains("en")) {
                LOCAL.set(new Locale("en", "US"));
            }else if(language.toLowerCase().contains("zh")) {
                LOCAL.set(new Locale("zh", "CN"));
            }
        }
    }

    public static Locale getLanguage() {
        Locale local = LOCAL.get();
        if(local != null){
            return local;
        }
        return Locale.getDefault();
    }

    public static void clearLanguage() {
        LOCAL.remove();
    }

    public static String msg(String key) {
        return messageSource.getMessage(key, null, "not support Key: " + key, getLanguage());
    }

    public static String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, "not Support Key: " + key, getLanguage());
    }

    public static String msgWithDefault(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, getLanguage());
    }

    public static String msgWithDefault(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, getLanguage());
    }

    public static String translateIfNeed(String message, Object... args){
        if(StringUtils.isBlank(message)){
            return "";
        }
        if (message.startsWith("{") && message.endsWith("}")) {
            message = message.substring(1, message.length() - 1);
            return messageSource.getMessage(message, args, "not Support Key: " + message, getLanguage());
        }
        return message;
    }
}
