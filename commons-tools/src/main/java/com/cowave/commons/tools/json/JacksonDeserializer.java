/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.json;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.cowave.commons.tools.Converts;
import com.cowave.commons.tools.json.JacksonUtils;

import java.lang.reflect.Type;

/**
 * fastjson使用jackson反序列化
 *
 * @author jiangbo
 */
public class JacksonDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Object value = parser.parse();
        if (type instanceof Class<?> clazz && clazz.isEnum()) {
            String obj = Converts.toStr(value);
            try {
                if (obj == null) {
                    return null;
                }
                return JacksonUtils.readValue(obj, type);
            } catch (Exception e) {
                try {
                    return (T) Enum.valueOf(Converts.cast(clazz), obj);
                } catch (Exception ex) {
                    return null;
                }
            }
        }

        try {
            return JacksonUtils.convert(value, type);
        } catch (Exception e) {
            return (T) value;
        }
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
