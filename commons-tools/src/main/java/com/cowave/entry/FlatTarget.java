/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.entry;

import com.cowave.convert.Converts;

/**
 * @author jiangbo
 * @date 2023/12/14
 */
public interface FlatTarget<T> {

    /**
     * 将当前对象转换成目标对象
     */
    default T flat() {
        Class<T> clazz = FlatTargetManager.getTargetClass(Converts.cast(this.getClass()));
        return Converts.copyTo(this, clazz);
    }
}
