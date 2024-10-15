/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
        return Locale.CHINA;
    }

    public static void clearLanguage() {
        LOCAL.remove();
    }

    public static String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, "not Support Key: " + key, getLanguage());
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
