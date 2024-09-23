/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.alarm;

/**
 *
 * @author shanhuiming
 *
 */
public interface Alarm {
    // 提示 1
    // 普通 2
    // 重要 3
    // 严重 4
    // 灾难 5

    /**
     * 是否异步处理
     */
    default boolean isAsync(){
        return true;
    }
}
