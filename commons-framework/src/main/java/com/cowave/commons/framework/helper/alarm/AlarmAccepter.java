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
public interface AlarmAccepter<T extends Alarm> {

    /**
     * 生成告警
     */
    void accept(T alarm);
}
