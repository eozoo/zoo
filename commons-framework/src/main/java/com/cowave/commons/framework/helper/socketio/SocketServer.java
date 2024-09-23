package com.cowave.commons.framework.helper.socketio;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.util.CollectionUtils;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class SocketServer {

    private static Map<Long, SocketIOClient> socketclientMap = new ConcurrentHashMap<>();

    private final ClientMsgHandler clientMsgHandler;

    private final ConnectedHandler connectedHandler;

    private final SocketIOServer socketIOServer;

    @PostConstruct
    private void init() {
        if(connectedHandler != null) {
            socketIOServer.addConnectListener(client -> {
                Long userId = getUserId(client);
                if (userId != null) {
                    socketclientMap.put(userId, client);
                    connectedHandler.onConnected(userId, SocketServer.this);
                }
            });
        }

        socketIOServer.addDisconnectListener(client -> {
            Long userId = getUserId(client);
            if (userId != null) {
                socketclientMap.remove(userId);
                client.disconnect();
            }
        });

        if(clientMsgHandler != null) {
            String event = clientMsgHandler.getEvent();
            socketIOServer.addEventListener(event, String.class, (client, data, ackSender) -> {
                Long userId = getUserId(client);
                clientMsgHandler.onMsg(userId, data);
            });
        }

        socketIOServer.start();
    }

    @PreDestroy
    private void destroy() {
        if (socketIOServer != null) {
            socketIOServer.stop();
        }
    }

    private Long getUserId(SocketIOClient client) {
        Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
        List<String> userIdList = params.get("userId");
        if (!CollectionUtils.isEmpty(userIdList)) {
            return Long.valueOf(userIdList.get(0));
        }
        return null;
    }

    /**
     * 私聊
     * @param userId 用户id
     * @param event  事件
     * @param data   消息
     */
    public <T> void sendSingle(String event, T data, Long userId) {
        SocketIOClient client = socketclientMap.get(userId);
        if (client != null) {
            client.sendEvent(event, data);
        }
    }

    /**
     * 群发
     * @param userIds 用户id
     * @param event   事件
     * @param data    消息
     */
    public <T> void sendGroup(String event, T data, List<Long> userIds) {
        for(Long userId : userIds) {
            SocketIOClient client = socketclientMap.get(userId);
            if (client != null) {
                client.sendEvent(event, data);
            }
        }
    }

    /**
     * 广播
     * @param event 事件
     * @param data  消息
     */
    public <T> void sendAll(String event, T data) {
        for(SocketIOClient client : socketclientMap.values()) {
            client.sendEvent(event, data);
        }
    }
}
