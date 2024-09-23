/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.convert;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.cowave.jackson.JacksonUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author jiangbo
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Converts {

    /**
     * 将from和toClass中字段相同的拷贝到to
     */
    public static <F, T> T copyTo(F from, Class<T> toClass) {
        return BeanUtil.copyProperties(from, toClass);
    }

    /**
     * 将from和to中字段相同的拷贝到to
     */
    public static <F, T> void copyTo(F from, T to) {
        BeanUtil.copyProperties(from, to);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mergeMap(Map<K, V>... maps) {
        Map<K, V> newMap = new HashMap<>();
        for (Map<K, V> map : maps) {
            if (MapUtils.isNotEmpty(map)) {
                newMap.putAll(map);
            }
        }
        return newMap;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mergeLinkedMap(Map<K, V>... maps) {
        Map<K, V> newMap = new LinkedHashMap<>();
        for (Map<K, V> map : maps) {
            if (MapUtils.isNotEmpty(map)) {
                newMap.putAll(map);
            }
        }
        return newMap;
    }

    /**
     * 获取参数不为空值
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * 类型强转
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * 转换为字符串
     */
    public static String toStr(Object value, String defaultValue) {
        return Objects.toString(value, defaultValue);
    }

    /**
     * 转换为字符串，可能返回null
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }

    /**
     * 转换为字符串
     */
    public static String toJsonStr(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        try {
            return value instanceof String str ? str : JacksonUtils.writeValue(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为字符串，可能返回null
     */
    public static String toJsonStr(Object value) {
        return toJsonStr(value, null);
    }

    /**
     * 转换为字符
     */
    public static Character toChar(Object value, Character defaultValue) {
        if (null == value) {
            return defaultValue;
        }

        if (value instanceof Character c) {
            return c;
        }

        final String valueStr = toStr(value, null);
        return StringUtils.isEmpty(valueStr) ? defaultValue : valueStr.charAt(0);
    }

    /**
     * 转换为字符
     */
    public static Character toChar(Object value) {
        return toChar(value, null);
    }

    /**
     * 转换为byte
     */
    public static Byte toByte(Object value, Byte defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Byte b) {
            return b;
        }
        if (value instanceof Number nb) {
            return nb.byteValue();
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Byte.parseByte(valueStr);
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为byte，出错返回null
     */
    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    /**
     * 转换为Short
     */
    public static Short toShort(Object value, Short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Short s) {
            return s;
        }
        if (value instanceof Number nb) {
            return nb.shortValue();
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Short.parseShort(valueStr.trim());
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为Short，出错返回null
     */
    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    /**
     * 转换为Number
     */
    public static Number toNumber(Object value, Number defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number nb) {
            return nb;
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return NumberFormat.getInstance().parse(valueStr);
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为Number，出错返回null
     */
    public static Number toNumber(Object value) {
        return toNumber(value, null);
    }

    /**
     * 转换为int
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number nb) {
            return nb.intValue();
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(valueStr.trim());
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为int，出错返回null
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }

    /**
     * 转换为Integer数组，默认分隔符","
     */
    public static Integer[] toIntArray(String str) {
        return toIntArray(",", str);
    }

    /**
     * 转换为Long数组，默认分隔符","
     */
    public static Long[] toLongArray(String str) {
        return toLongArray(",", str);
    }

    /**
     * 转换为String数组，默认分隔符","
     */
    public static String[] toStrArray(String str) {
        return toStrArray(",", str);
    }

    /**
     * 转换为Integer数组
     */
    public static Integer[] toIntArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Integer[]{};
        }

        String[] arr = str.split(split);
        final Integer[] ints = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final Integer v = toInt(arr[i], 0);
            ints[i] = v;
        }
        return ints;
    }

    /**
     * 转换为Long数组
     */
    public static Long[] toLongArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Long[]{};
        }

        String[] arr = str.split(split);
        final Long[] longs = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final Long v = toLong(arr[i], null);
            longs[i] = v;
        }
        return longs;
    }

    /**
     * 转换为String数组
     */
    public static String[] toStrArray(String split, String str) {
        return str.split(split);
    }

    /**
     * 转换为long
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long l) {
            return l;
        }
        if (value instanceof Number nb) {
            return nb.longValue();
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigDecimal(valueStr.trim()).longValue();
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为long，出错返回null
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * 转换为double
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Double d) {
            return d;
        }
        if (value instanceof Number nb) {
            return nb.doubleValue();
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigDecimal(valueStr.trim()).doubleValue();
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为double，出错返回null
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * 转换为Float
     */
    public static Float toFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Float f) {
            return f;
        }
        if (value instanceof Number nb) {
            return nb.floatValue();
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(valueStr.trim());
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为Float，出错返回null
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * 转换为boolean，支持：true、false、yes、ok、no，1,0
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bl) {
            return bl;
        }

        String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        valueStr = valueStr.trim().toLowerCase();
        return switch (valueStr) {
            case "true", "yes", "ok", "1" -> true;
            case "false", "no", "0" -> false;
            default -> defaultValue;
        };
    }

    /**
     * 转换为boolean，出错返回null
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }

    /**
     * 转换为Enum
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value, E defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (clazz.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            E e = (E) value;
            return e;
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(clazz, valueStr);
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为Enum，出错返回null
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value) {
        return toEnum(clazz, value, null);
    }

    /**
     * 转换为BigInteger
     */
    public static BigInteger toBigInteger(Object value, BigInteger defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigInteger bi) {
            return bi;
        }
        if (value instanceof Long l) {
            return BigInteger.valueOf(l);
        }

        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigInteger(valueStr);
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为BigInteger，出错返回null
     */
    public static BigInteger toBigInteger(Object value) {
        return toBigInteger(value, null);
    }

    /**
     * 转换为BigDecimal
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number nb) {
            return new BigDecimal(String.valueOf(nb));
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigDecimal(valueStr);
        } catch (Exception e) {
            log.debug("", e);
            return defaultValue;
        }
    }

    /**
     * 转换为BigDecimal，出错返回null
     */
    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, null);
    }

    /**
     * 将对象转为字符串
     * <p>Byte数组和ByteBuffer会被转换为对应字符串的数组； 对象数组会调用Arrays.toString方法
     */
    public static String utf8Str(Object obj) {
        return str(obj, "UTF-8");
    }

    /**
     * 将对象转为字符串
     * <p>Byte数组和ByteBuffer会被转换为对应字符串的数组； 对象数组会调用Arrays.toString方法
     */
    public static String str(Object obj, String charsetName) {
        return str(obj, Charset.forName(charsetName));
    }

    /**
     * 将对象转为字符串
     * <p>Byte数组和ByteBuffer会被转换为对应字符串的数组； 对象数组会调用Arrays.toString方法
     */
    public static String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof String str) {
            return str;
        } else if (obj instanceof byte[] bytes) {
            return str(bytes, charset);
        } else if (obj instanceof Byte[] bs) {
            byte[] bytes = ArrayUtils.toPrimitive(bs);
            return str(bytes, charset);
        } else if (obj instanceof ByteBuffer bb) {
            return str(bb, charset);
        }
        return obj.toString();
    }

    /**
     * 将byte数组转为字符串
     */
    public static String str(byte[] bytes, String charset) {
        return str(bytes, StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * 将byte数组转为字符串
     */
    public static String str(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * 将byteBuffer数组转为字符串
     */
    public static String str(ByteBuffer data, String charset) {
        if (data == null) {
            return null;
        }

        return str(data, Charset.forName(charset));
    }

    /**
     * 将byteBuffer数组转为字符串
     */
    public static String str(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**
     * 半角转全角
     */
    public static String toSbc(String input) {
        return toSbc(input, null);
    }

    /**
     * 半角转全角
     */
    public static String toSbc(String input, Set<Character> notConvertSet) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                continue; // 跳过不替换的字符
            }

            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    /**
     * 全角转半角
     */
    public static String toDbc(String input) {
        return toDbc(input, null);
    }

    /**
     * 全角转半角
     */
    public static String toDbc(String text, Set<Character> notConvertSet) {
        char[] c = text.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                continue; // 跳过不替换的字符
            }

            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 数字金额大写转换
     */
    public static String digitUppercase(double n) {
        String[] fraction = {"角", "分"};
        String[] digit = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[][] unit = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};

        String head = n < 0 ? "负" : "";
        n = Math.abs(n);

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < fraction.length; i++) {
            s.append((digit[(int) (Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", ""));
        }
        if (s.length() < 1) {
            s = new StringBuilder("整");
        }

        int integerPart = (int) Math.floor(n);
        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            StringBuilder p = new StringBuilder();
            for (int j = 0; j < unit[1].length && n > 0; j++) {
                p.insert(0, digit[integerPart % 10] + unit[1][j]);
                integerPart = integerPart / 10;
            }
            s.insert(0, p.toString().replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i]);
        }
        return head + s.toString().replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }

    /**
     * 将list转换成目标类型
     */
    public static <T> T toTargetType(List<Map<String, Object>> result, Class<T> targetClass) {
        Object obj;
        if (List.class.isAssignableFrom(targetClass)) {
            obj = result;
        } else if (Map.class.isAssignableFrom(targetClass)) {
            obj = CollectionUtils.isEmpty(result) ? Collections.emptyMap() : result.get(0);
        } else {
            obj = toTargetTypeByFirstValue(result, targetClass);
        }
        return Converts.cast(obj);
    }

    /**
     * 从list中获取第一个值
     */
    private static <T> Object toTargetTypeByFirstValue(List<Map<String, Object>> result, Class<T> targetClass) {
        Map<String, Object> map = CollectionUtils.isEmpty(result) ? Collections.emptyMap() : result.get(0);
        Object value = null;
        if (MapUtils.isNotEmpty(map)) {
            List<Object> list = new ArrayList<>(map.values());
            value = list.get(0);
            if (value instanceof List<?> valueList) {
                value = CollectionUtils.isEmpty(valueList) ? null : valueList.get(0);
            }
        }
        return toTargetType(value, targetClass);
    }

    /**
     * 转换成目标类型
     */
    public static <T> T toTargetType(Object value, Class<T> clazz) {
        Object obj = value;
        if (Number.class.isAssignableFrom(clazz)) {
            value = value == null ? 0 : value;
            try {
                obj = JSON.parseObject(JSON.toJSONString(value), clazz);
            } catch (Exception e) {
                log.debug("", e);
                obj = 0;
            }
        } else if (value != null) {
            obj = BeanUtil.copyProperties(value, clazz);
        }
        return Converts.cast(obj);
    }

}
