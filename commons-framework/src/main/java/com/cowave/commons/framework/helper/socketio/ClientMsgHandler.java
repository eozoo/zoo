package com.cowave.commons.framework.helper.socketio;

/**
 *
 * @author shanhuiming
 *
 */
public interface ClientMsgHandler {

    String getEvent();

    void onMsg(Long userId, String data);
}
