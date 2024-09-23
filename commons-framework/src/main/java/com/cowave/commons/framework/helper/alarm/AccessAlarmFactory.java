package com.cowave.commons.framework.helper.alarm;

import org.springframework.feign.codec.Response;

/**
 *
 * @author shanhuiming
 *
 */
public interface AccessAlarmFactory<T extends Alarm> {

    T newAccessAlarm(Response<Void> errorResp);
}
