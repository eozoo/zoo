/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimeZone;

/**
 * @author jiangbo
 * @date 2023/12/20
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtils {

    /**
     * 将原对象转换成目标类型
     */
    public static <T> T convert(Object content, Class<T> type) {
        try {
            ObjectMapper mapper = OBJECT_MAPPER;
            byte[] data = mapper.writer().writeValueAsBytes(content);
            JsonParser jsonParser = mapper.getFactory().createParser(data);
            return mapper.readValue(jsonParser, mapper.constructType(type));
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    /**
     * 将原对象转换成目标类型
     */
    public static <T> T convert(Object content, Type type) {
        try {
            ObjectMapper mapper = OBJECT_MAPPER;
            byte[] data = mapper.writer().writeValueAsBytes(content);
            JsonParser jsonParser = mapper.getFactory().createParser(data);
            return mapper.readValue(jsonParser, mapper.constructType(type));
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    /**
     * 将字符串转换成目标类型
     */
    public static <T> T readValue(String data, Class<T> type) {
        try {
            ObjectMapper mapper = OBJECT_MAPPER;
            JsonParser jsonParser = mapper.getFactory().createParser(data);
            return mapper.readValue(jsonParser, type);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    /**
     * 将字符串转换成目标类型
     */
    public static <T> List<T> readValueList(String data, Class<T> type) {
        try {
            ObjectMapper mapper = OBJECT_MAPPER;
            JsonParser jsonParser = mapper.getFactory().createParser(data);
            return mapper.readValues(jsonParser, type).readAll();
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    /**
     * 将字符串转换成目标类型
     */
    public static <T> T readValue(String data, Type type) {
        try {
            ObjectMapper mapper = OBJECT_MAPPER;
            JsonParser jsonParser = mapper.getFactory().createParser(data);
            return mapper.readValue(jsonParser, mapper.constructType(type));
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    /**
     * 将字节数组转换成目标类型
     */
    public static <T> T readValue(byte[] data, Type type) {
        try {
            ObjectMapper mapper = OBJECT_MAPPER;
            JsonParser jsonParser = mapper.getFactory().createParser(data);
            return mapper.readValue(jsonParser, mapper.constructType(type));
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    /**
     * 转换成字符串
     */
    public static String writeValue(Object contents) {
        if (contents == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writer().writeValueAsString(contents);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setTimeZone(TimeZone.getDefault())
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
