/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.annotations;

import com.cowave.convert.Converts;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author jiangbo
 * @date 2023/12/21
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationUtils {

    /**
     * 找到包下所有注解信息
     */
    public static <T extends Annotation> List<AnnotationInfo<T>> getAnnotationClass(List<String> basePackages, Class<T> tClass) {
        return getAnnotationClass(basePackages, null, tClass);
    }

    /**
     * 找到包下所有注解信息
     */
    public static <T extends Annotation> List<AnnotationInfo<T>> getAnnotationClass(List<String> basePackages, List<String> excludes, Class<T> tClass) {
        basePackages = distinctPkg(basePackages);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(basePackages.toArray(new String[0])));
        return flatAnnotationClass(reflections, excludes, tClass).toList();
    }

    private static <T extends Annotation> Stream<AnnotationInfo<T>> flatAnnotationClass(Reflections reflections, List<String> excludePkg, Class<T> tClass) {
        return reflections.getTypesAnnotatedWith(tClass)
                .stream()
                .map(clazz -> {
                    if (excludePkg != null && excludePkg.stream().anyMatch(ex -> clazz.getName().startsWith(ex))) {
                        log.debug("exclude class {}", clazz);
                        return null;
                    }
                    T t = clazz.getAnnotation(tClass);
                    return t == null ? null : new AnnotationInfo<>(clazz, t);
                }).filter(Objects::nonNull);
    }

    /**
     * 找到包下所有子类
     */
    public static <T> List<Class<? extends T>> getSubTypesOf(List<String> basePackages, List<String> excludes, Class<T> tClass) {
        basePackages = distinctPkg(basePackages);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(basePackages.toArray(new String[0])));
        return getSubTypesOf(reflections, excludes, tClass).toList();
    }

    private static <T> Stream<Class<? extends T>> getSubTypesOf(Reflections reflections, List<String> excludePkg, Class<T> tClass) {
        Stream<? extends Class<? extends T>> stream = reflections.getSubTypesOf(tClass)
                .stream()
                .map(clazz -> {
                    if (excludePkg != null && excludePkg.stream().anyMatch(ex -> clazz.getName().startsWith(ex))) {
                        log.debug("exclude class {}", clazz);
                        return null;
                    }
                    return clazz;
                }).filter(Objects::nonNull);
        return Converts.cast(stream);
    }

    private static List<String> distinctPkg(List<String> basePackages) {
        List<String> packages = new ArrayList<>();
        basePackages.stream()
                .sorted(Comparator.comparingInt(String::length))
                .forEach(pkg -> {
                    if (packages.stream().noneMatch(pkg::startsWith)) {
                        packages.add(pkg);
                    }
                });
        return packages;
    }

}
