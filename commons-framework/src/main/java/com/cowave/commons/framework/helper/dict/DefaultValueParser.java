/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.dict;

/**
 * 简单值转换器
 *
 * @author shanhuiming
 */
public class DefaultValueParser implements DictValueParser {

    @Override
    public Object parse(String value, String param) {
        if(value == null){
            return null;
        }
        return switch (param) {
            case "short" -> Short.parseShort(value);
            case "int" -> Integer.parseInt(value);
            case "long" -> Long.parseLong(value);
            case "float" -> Float.parseFloat(value);
            case "double" -> Double.parseDouble(value);
            case "boolean" -> Boolean.parseBoolean(value);
            default -> value;
        };
    }
}
