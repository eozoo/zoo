package com.cowave.commons.framework.helper.socketio;

/**
 *
 * @author shanhuiming
 *
 */
public interface ConnectedHandler {

    void onConnected(Long userId, SocketServer socketServer);
}
