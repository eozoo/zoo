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

    /**
     * 提示
     */
    int LEVEL_TIP = 1;

    /**
     * 普通
     */
    int LEVEL_COMMON = 2;

    /**
     * 重要
     */
    int LEVEL_IMPORTANT = 3;

    /**
     * 严重
     */
    int LEVEL_SERIOUS = 4;

    /**
     * 严重
     */
    int LEVEL_DISASTER = 5;
}
