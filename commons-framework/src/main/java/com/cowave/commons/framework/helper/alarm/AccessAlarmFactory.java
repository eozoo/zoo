/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.alarm;

import javax.validation.constraints.NotNull;

/**
 *
 * @author shanhuiming
 *
 */
public interface AccessAlarmFactory<T extends Alarm> {

    @NotNull
    T createAlarm(int httpStatus, String code, String message, Object response, Exception e);
}
