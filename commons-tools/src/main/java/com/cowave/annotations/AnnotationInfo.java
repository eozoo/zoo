/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.annotations;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author jiangbo
 * @date 2023/12/21
 */
@Data
@AllArgsConstructor
public class AnnotationInfo<T> {
    private Class<?> clazz;
    private T t;
}
