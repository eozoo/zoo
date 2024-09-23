/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.convert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 对集合中的数据的类型
 *
 * @author jiangbo
 * @date 2023/5/10
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionConverts {

    /**
     * from过滤
     */
    public static <F> List<F> filter(Collection<F> from, Predicate<F> filter) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        return from.stream().filter(filter).toList();
    }

    /**
     * 将from和toClass中字段相同的拷贝到to
     */
    public static <F, T> List<T> copyTo(Collection<F> from, Class<T> toClass) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        return from.stream().map(s -> Converts.copyTo(s, toClass)).toList();
    }

    /**
     * 将from和toClass中字段相同的拷贝到to
     */
    public static <F, T> List<T> copyTo(Collection<F> from, Class<T> toClass, Predicate<F> predicate) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        return from.stream().filter(predicate).map(s -> Converts.copyTo(s, toClass)).toList();
    }

    /**
     * 将fromList转成toList
     */
    public static <F, T> List<T> copyTo(Collection<F> from, Function<F, T> mapper) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        return from.stream().map(mapper).toList();
    }

    /**
     * 将fromList转成toList
     */
    public static <F, T> List<T> copyTo(Collection<F> from, Predicate<F> filter, Function<F, T> mapper) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        return from.stream().filter(filter).map(mapper).toList();
    }

    /**
     * 将fromList转成toList
     */
    public static <F, T> List<T> copyFlatTo(Collection<F> from, Function<F, Stream<T>> mapper) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        return from.stream().flatMap(mapper).toList();
    }

    /**
     * 将fromList转成toList
     */
    public static <F, T> List<T> copyFlatTo(Collection<F> from, Predicate<F> filter, Function<F, Stream<T>> mapper) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyList();
        }
        return from.stream().filter(filter).flatMap(mapper).toList();
    }

    /**
     * 将fromList转成toList
     */
    public static <F, T> Stream<T> copyToStream(Collection<F> from, Function<F, T> mapper) {
        if (CollectionUtils.isEmpty(from)) {
            return Stream.empty();
        }
        return from.stream().map(mapper);
    }

    /**
     * 将fromList转成toList
     *
     * @param toClass    目标类型
     * @param biConsumer 转换函数
     */
    public static <F, T> List<T> copyTo(Collection<F> fromList, Class<T> toClass, BiConsumer<F, T> biConsumer) {
        if (fromList == null) {
            return Collections.emptyList();
        }
        return fromList.stream().map(from -> {
            T to = null;
            try {
                to = toClass.getDeclaredConstructor().newInstance();
                biConsumer.accept(from, to);
            } catch (Exception e) {
                throw new UnsupportedOperationException(e);
            }
            return to;
        }).toList();
    }

    /**
     * 转map,相同的key,以后面为主
     *
     * @param keyMapper key的转换函数
     */
    public static <K, V> Map<K, V> toMap(Collection<V> list, Function<V, K> keyMapper) {
        return toMap(list, keyMapper, Function.identity());
    }

    /**
     * 转map,相同的key,以后面为主
     *
     * @param keyMapper key的转换函数
     */
    public static <K, V> Map<K, V> toLinkedMap(Collection<V> list, Function<V, K> keyMapper) {
        return toLinkedMap(list, keyMapper, Function.identity());
    }

    /**
     * 转map,相同的key,以后面为主
     *
     * @param keyMapper   key的转换函数
     * @param valueMapper value的转换函数
     */
    public static <K, T, V> Map<K, V> toMap(Collection<T> list, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper, (k1, k2) -> k2));
    }

    /**
     * 转map,相同的key,以后面为主
     *
     * @param keyMapper   key的转换函数
     * @param valueMapper value的转换函数
     */
    public static <K, T, V> Map<K, V> toLinkedMap(Collection<T> list, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper, (k1, k2) -> k2, LinkedHashMap::new));
    }

    /**
     * 转map,相同的key,以后面为主
     *
     * @param filter      过滤条件
     * @param keyMapper   key的转换函数
     * @param valueMapper value的转换函数
     */
    public static <K, T, V> Map<K, V> toMap(Collection<T> list, Predicate<T> filter, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().filter(filter).collect(Collectors.toMap(keyMapper, valueMapper, (k1, k2) -> k2));
    }

    /**
     * 转map,相同的key,以后面为主
     *
     * @param filter      过滤条件
     * @param keyMapper   key的转换函数
     * @param valueMapper value的转换函数
     */
    public static <K, T, V> Map<K, V> toLinkedMap(Collection<T> list, Predicate<T> filter, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().filter(filter).collect(Collectors.toMap(keyMapper, valueMapper, (k1, k2) -> k2, LinkedHashMap::new));
    }

    /**
     * 按条件分组
     *
     * @param keyMapper 分组条件
     */
    public static <K, T> Map<K, List<T>> groupBy(Collection<T> list, Function<T, K> keyMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * 按条件分组
     *
     * @param keyMapper 分组条件
     * @param vClass    转换的目标类型
     */
    public static <K, T, V> Map<K, List<V>> groupBy(Collection<T> list, Function<T, K> keyMapper, Class<V> vClass) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(keyMapper, Collectors.mapping(from -> Converts.copyTo(from, vClass), Collectors.toList())));
    }

    /**
     * 按条件分组
     *
     * @param keyMapper   分组条件
     * @param valueMapper 转换函数
     */
    public static <K, T, V> Map<K, List<V>> groupBy(Collection<T> list, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(keyMapper, Collectors.mapping(valueMapper, Collectors.toList())));
    }

    /**
     * 按条件分组
     *
     * @param keyMapper 分组条件
     */
    public static <K, T> Map<K, List<T>> groupByWithFilter(Collection<T> list, Predicate<T> filter, Function<T, K> keyMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().filter(filter).collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * 按条件分组
     *
     * @param keyMapper 分组条件
     */
    public static <K, T, V> Map<K, List<V>> groupByWithFilter(Collection<T> list, Predicate<T> filter, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().filter(filter).collect(Collectors.groupingBy(keyMapper, Collectors.mapping(valueMapper, Collectors.toList())));
    }

    /**
     * 执行zip操作
     *
     * @param keys   key
     * @param values value
     * @return 合并后的map
     */
    public static Map<String, String> toMap(List<String> keys, List<String> values) {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("%s, %s 参数值不匹配".formatted(keys, values));
        }
        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            results.put(keys.get(i), values.get(i));
        }
        return results;
    }

    /**
     * 执行zip操作
     *
     * @param keys   key
     * @param values value
     * @param clazz  目标类型
     * @return 合并后的map
     */
    public static <K, T> Map<K, T> toMap(List<K> keys, List<String> values, Class<T> clazz) {
        return toMap(keys, values, value -> Converts.copyTo(value, clazz));
    }

    /**
     * 执行zip操作
     *
     * @param keys     key
     * @param values   value
     * @param function 转换函数
     * @return 合并后的map
     */
    public static <K, T> Map<K, T> toMap(List<K> keys, List<String> values, Function<String, T> function) {
        return toMap(keys, values, function, false);
    }

    /**
     * 执行zip操作
     *
     * @param keys              key
     * @param values            value
     * @param function          转换函数
     * @param ignoreValueIsNull 是否忽略空值
     * @return 合并后的map
     */
    public static <K, T> Map<K, T> toMap(List<K> keys, List<String> values, Function<String, T> function, boolean ignoreValueIsNull) {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("%s, %s 参数值不匹配".formatted(keys, values));
        }
        Map<K, T> results = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (StringUtils.isNotBlank(value)) {
                T t = function.apply(value);
                results.put(keys.get(i), t);
            } else if (!ignoreValueIsNull) {
                results.put(keys.get(i), null);
            }
        }
        return results;
    }

    @SafeVarargs
    public static <T> List<T> mergeList(Collection<T>... list) {
        return Arrays.stream(list).filter(Objects::nonNull).flatMap(Collection::stream).toList();
    }

    @SafeVarargs
    public static <T> List<T> mergeList(Collection<T> list, T... t) {
        return Stream.concat(list.stream(), Stream.of(t)).toList();
    }

    @SafeVarargs
    public static <T> List<T> mergeDistinctList(Collection<T>... list) {
        return Arrays.stream(list).filter(Objects::nonNull).flatMap(Collection::stream).distinct().toList();
    }

    /**
     * 合并字节数组
     *
     * @param first  字节数组
     * @param second 字节数组
     * @return 合并后字节数组
     */
    public static byte[] mergeBytes(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
