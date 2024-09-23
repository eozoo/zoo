/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.TimeZone;

/**
 * @author jiangbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtils {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .setTimeZone(TimeZone.getDefault())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 对象类型转换
     */
    public static <T> T convert(Object content, Class<T> clazz) {
        if (content == null) {
            return null;
        }
        try {
            byte[] data = MAPPER.writer().writeValueAsBytes(content);
            JsonParser jsonParser = MAPPER.getFactory().createParser(data);
            return MAPPER.readValue(jsonParser, MAPPER.constructType(clazz));
        } catch (Throwable e) {
            throw new UnsupportedOperationException("", e);
        }
    }

    /**
     * 对象类型转换
     */
    public static <T> T convert(Object content, Type type) {
        if (content == null) {
            return null;
        }
        try {
            byte[] data = MAPPER.writer().writeValueAsBytes(content);
            JsonParser jsonParser = MAPPER.getFactory().createParser(data);
            return MAPPER.readValue(jsonParser, MAPPER.constructType(type));
        } catch (Throwable e) {
            throw new UnsupportedOperationException("", e);
        }
    }

    /**
     * json转对象
     */
    public static <T> T readValue(String data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        try {
            JsonParser jsonParser = MAPPER.getFactory().createParser(data);
            return MAPPER.readValue(jsonParser, clazz);
        } catch (Throwable e) {
            throw new UnsupportedOperationException("", e);
        }
    }

    /**
     * 将字符串转换成目标类型
     */
    public static <T> T readValue(String data, TypeReference<T> valueTypeRef) {
        if (data == null) {
            return null;
        }
        try {
            JsonParser jsonParser = MAPPER.getFactory().createParser(data);
            return MAPPER.readValue(jsonParser, valueTypeRef);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    /**
     * json转对象
     */
    public static <T> T readValue(String data, Type type) {
        if (data == null) {
            return null;
        }
        try {
            JsonParser jsonParser = MAPPER.getFactory().createParser(data);
            return MAPPER.readValue(jsonParser, MAPPER.constructType(type));
        } catch (Throwable e) {
            throw new UnsupportedOperationException("", e);
        }
    }

    /**
     * json转对象
     */
    public static <T> T readValue(byte[] data, Type type) {
        if (data == null) {
            return null;
        }
        try {
            JsonParser jsonParser = MAPPER.getFactory().createParser(data);
            return MAPPER.readValue(jsonParser, MAPPER.constructType(type));
        } catch (Throwable e) {
            throw new UnsupportedOperationException("", e);
        }
    }

    /**
     * 对象转json
     */
    public static String writeValue(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writer().writeValueAsString(obj);
        } catch (Throwable e) {
            throw new UnsupportedOperationException("", e);
        }
    }
}
