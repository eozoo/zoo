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
 * Value值转换器
 *
 * @author shanhuiming
 */
public interface DictValueParser {

    /**
     * 转换值
     * @param value 字面值
     * @param param 转换参数
     */
    Object parse(String value, String param);
}
