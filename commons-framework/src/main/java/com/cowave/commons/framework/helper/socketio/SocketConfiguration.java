/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.socketio;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.cowave.commons.framework.filter.security.TokenService;

import lombok.Data;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(SocketIOServer.class)
@ConfigurationProperties(prefix = "spring.socket-io")
@Data
@RequiredArgsConstructor
@Configuration
public class SocketConfiguration {

    @Nullable
    private final TokenService tokenService;

    @Nullable
    private final ClientMsgHandler clientMsgHandler;

    @Nullable
    private final ConnectedHandler connectedHandler;

    private String host;

    /**
     * socket端口，默认复用Http端口
     */
    private Integer port;

    /**
     * socket路径
     */
    private String contextPath = "/socket.io";

    /**
     * 每帧处理数据的最大长度，防止他人利用大数据来攻击服务器
     */
    private int maxFramePayloadLength = 1048576;

    /**
     * http交互最大内容长度
     */
    private int maxHttpContentLength = 1048576;

    /**
     * HTTP握手升级为ws协议超时时间，默认10秒
     */
    private int upgradeTimeout = 1000000;

    /**
     * 客户端向服务器发送心跳周期
     */
    private int pingInterval = 25000;

    /**
     * 间隔内没有接收到心跳就会发送超时事件
     */
    private int pingTimeout = 6000000;

    private int bossCount = 1;

    private int workCount = 100;

    private boolean allowCustomRequests = true;

    @Bean
    public SocketServer socketServer() {
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        if(host != null){
            configuration.setHostname(host);
        }
        configuration.setPort(port);
        configuration.setContext(contextPath);
        configuration.setBossThreads(bossCount);
        configuration.setWorkerThreads(workCount);
        configuration.setUpgradeTimeout(upgradeTimeout);
        configuration.setPingTimeout(pingTimeout);
        configuration.setPingInterval(pingInterval);
        configuration.setAllowCustomRequests(allowCustomRequests);
        configuration.setMaxHttpContentLength(maxHttpContentLength);
        configuration.setMaxFramePayloadLength(maxFramePayloadLength);
        if(tokenService != null) {
            configuration.setAuthorizationListener(data -> {
                String authorization = data.getSingleUrlParam("Authorization");
                return tokenService.validAccessToken(authorization);
            });
        }

        SocketConfig socket = new SocketConfig();
        socket.setTcpNoDelay(true);
        socket.setSoLinger(0);
        configuration.setSocketConfig(socket);
        return new SocketServer(clientMsgHandler, connectedHandler, new SocketIOServer(configuration));
    }
}
