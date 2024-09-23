/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.annotation;

import com.cowave.commons.tools.Converts;
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
 */
@Slf4j
public class Annotations {

    /**
     * 获取package下所有注解
     */
    public static <T extends Annotation> List<AnnotationMeta<T>> getAnnotations(List<String> packages, Class<T> clazz) {
        return getAnnotations(packages, null, clazz);
    }

    /**
     * 获取package下所有注解
     */
    public static <T extends Annotation> List<AnnotationMeta<T>> getAnnotations(List<String> packages, List<String> excludes, Class<T> clazz) {
        packages = distinctPackage(packages);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(packages.toArray(new String[0])));
        return getAnnotations(reflections, excludes, clazz).toList();
    }

    /**
     * 获取package下所有子类
     */
    public static <T> List<Class<? extends T>> getSubClasses(List<String> packages, List<String> excludes, Class<T> clazz) {
        packages = distinctPackage(packages);
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(packages.toArray(new String[0])));
        return getSubClasses(reflections, excludes, clazz).toList();
    }

    private static <T extends Annotation> Stream<AnnotationMeta<T>> getAnnotations(Reflections reflections, List<String> excludes, Class<T> clazz) {
        return reflections.getTypesAnnotatedWith(clazz).stream().map(c -> {
                    if (excludes != null && excludes.stream().anyMatch(ex -> c.getName().startsWith(ex))) {
                        log.debug("getAnnotations exclude class: {}", clazz);
                        return null;
                    }
                    T t = c.getAnnotation(clazz);
                    return t == null ? null : new AnnotationMeta<>(c, t);
                }).filter(Objects::nonNull);
    }

    private static <T> Stream<Class<? extends T>> getSubClasses(Reflections reflections, List<String> excludes, Class<T> clazz) {
        Stream<? extends Class<? extends T>> stream =
                reflections.getSubTypesOf(clazz).stream().map(c -> {
                    if (excludes != null && excludes.stream().anyMatch(ex -> c.getName().startsWith(ex))) {
                        log.debug("getSubClasses exclude class: {}", c);
                        return null;
                    }
                    return c;
                }).filter(Objects::nonNull);
        return Converts.cast(stream);
    }

    private static List<String> distinctPackage(List<String> packages) {
        List<String> list = new ArrayList<>();
        packages.stream().sorted(Comparator.comparingInt(String::length))
                .forEach(p -> {
                    if (list.stream().noneMatch(p::startsWith)) {
                        list.add(p);
                    }
                });
        return list;
    }
}
