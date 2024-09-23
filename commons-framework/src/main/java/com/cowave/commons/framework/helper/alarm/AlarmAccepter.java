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
