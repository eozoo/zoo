/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.enums;

import cn.hutool.core.util.ClassUtil;
import com.alibaba.fastjson.JSON;
import com.cowave.convert.Converts;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * 标识字段类型
 *
 * @author jiangbo
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FieldType {
    /**
     * 字符
     */
    STRING(0),
    /**
     * 数字
     */
    NUMBER(1),
    /**
     * 数字-整形
     */
    NUMBER_LONG(102),
    /**
     * 数字-浮点数
     */
    NUMBER_DOUBLE(103),
    /**
     * 枚举
     */
    ENUM(2),
    /**
     * 布尔值
     */
    BOOL(3),
    /**
     * ip
     */
    IPV4(4),
    /**
     * 数组（包括集合）
     */
    ARRAY(5),
    /**
     * 键值对
     */
    MAP(6),
    /**
     * 时间戳
     */
    TIMESTAMP(7),
    /**
     * 日期
     */
    DATE(8),
    /**
     * 未知
     */
    NONE(-1),
    ;

    @JsonValue
    private final int key;

    public static FieldType of(Object obj) {
        if (obj == null) {
            return FieldType.NONE;
        }
        return of(obj.getClass());
    }

    public static FieldType of(Class<?> type) {
        if (type == null) {
            return FieldType.NONE;
        }
        if (String.class.isAssignableFrom(type)) {
            return FieldType.STRING;
        } else if (Map.class.isAssignableFrom(type)) {
            return FieldType.STRING;
        } else if (Collection.class.isAssignableFrom(type)) {
            return FieldType.STRING;
        } else if (Boolean.class.isAssignableFrom(type)) {
            return FieldType.BOOL;
        } else if (Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type) || Short.class.isAssignableFrom(type)) {
            return FieldType.NUMBER_LONG;
        } else if (Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type)) {
            return FieldType.NUMBER_LONG;
        } else if (Number.class.isAssignableFrom(type)) {
            return FieldType.NUMBER;
        } else if (ClassUtil.isBasicType(type)) {
            return FieldType.NUMBER;
        } else if (ClassUtil.isEnum(type)) {
            return FieldType.ENUM;
        } else if (Date.class.isAssignableFrom(type)) {
            return FieldType.DATE;
        }
        return FieldType.NONE;
    }

    public static FieldType of(String type) {
        if (StringUtils.isBlank(type)) {
            return FieldType.NONE;
        }
        try {
            return FieldType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("不支持的类型【%s】".formatted(type));
        }
    }

    private static final Map<Integer, FieldType> FIELD_TYPE_MAP = Arrays.stream(FieldType.values()).collect(Collectors.toMap(FieldType::getKey, Function.identity()));

    public static FieldType of(Integer type) {
        return MapUtils.getObject(FIELD_TYPE_MAP, type, NONE);
    }

    /**
     * 根据字段类型转换字段,如果不能转换，则返回原始值
     *
     * @param fieldType 字段类型
     * @param obj       原始值
     * @return 转换后的值
     */
    public static Object tryConvert(FieldType fieldType, Object obj) {
        if (fieldType == null) {
            return obj;
        }
        return switch (fieldType) {
            case STRING -> tryConvert(Converts::toJsonStr, obj);
            case NUMBER -> tryConvert(s -> {
                Class<?> clazz = s.getClass();
                return (Number.class.isAssignableFrom(clazz) || ClassUtil.isEnum(clazz)) ? s : Converts.toBigDecimal(s);
            }, obj);
            case NUMBER_LONG, TIMESTAMP -> tryConvert(Converts::toLong, obj);
            case NUMBER_DOUBLE -> tryConvert(Converts::toDouble, obj);
            case BOOL -> tryConvert(Converts::toBool, obj);
            case IPV4, DATE -> tryConvert(Converts::toStr, obj);
            case ARRAY, MAP -> tryConvert(s -> JSON.parse(Converts.toJsonStr(s)), obj);
            default -> obj;
        };
    }

    private static Object tryConvert(UnaryOperator<Object> mapping, Object src) {
        Object dst = mapping.apply(src);
        return dst == null ? src : dst;
    }

    /**
     * 是否数字
     *
     * @param fieldType 类型
     * @return 结果
     */
    public static boolean isNumber(FieldType fieldType) {
        return NUMBER.equals(fieldType) || NUMBER_LONG.equals(fieldType) || NUMBER_DOUBLE.equals(fieldType);
    }

}
