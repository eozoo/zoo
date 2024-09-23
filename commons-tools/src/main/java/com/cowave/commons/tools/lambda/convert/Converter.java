/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.lambda.convert;

import com.cowave.commons.tools.Converts;

/**
 * @author jiangbo
 */
public interface Converter<T> {

    default T convert() {
        Class<T> clazz = ConverterCache.getCacheClass(Converts.cast(this.getClass()));
        return Converts.copyProperties(this, clazz);
    }
}
