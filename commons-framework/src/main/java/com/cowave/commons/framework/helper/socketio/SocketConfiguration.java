package com.cowave.commons.framework.helper.socketio;

import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.cowave.commons.framework.access.AccessProperties;
import com.cowave.commons.framework.access.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(SocketIOServer.class)
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(SocketIoProperties.class)
public class SocketConfiguration {

    @Nullable
    private final TokenService tokenService;

    @Nullable
    private final ClientMsgHandler clientMsgHandler;

    @Nullable
    private final ConnectedHandler connectedHandler;

    private final AccessProperties accessProperties;

    @Bean
    public SocketIoHelper socketServer(SocketIoProperties properties) {
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        if(properties.getHost() != null){
            configuration.setHostname(properties.getHost());
        }
        configuration.setPort(properties.getPort());
        configuration.setContext(properties.getContextPath());
        configuration.setBossThreads(properties.getBossCount());
        configuration.setWorkerThreads(properties.getWorkCount());
        configuration.setUpgradeTimeout(properties.getUpgradeTimeout());
        configuration.setPingTimeout(properties.getPingTimeout());
        configuration.setPingInterval(properties.getPingInterval());
        configuration.setAllowCustomRequests(properties.isAllowCustomRequests());
        configuration.setMaxHttpContentLength(properties.getMaxHttpContentLength());
        configuration.setMaxFramePayloadLength(properties.getMaxFramePayloadLength());
        if(tokenService != null) {
            configuration.setAuthorizationListener(data -> {
                String authorization = data.getSingleUrlParam(accessProperties.tokenHeader());
                return tokenService.validAccessToken(authorization);
            });
        }

        SocketConfig socket = new SocketConfig();
        socket.setTcpNoDelay(true);
        socket.setSoLinger(0);
        configuration.setSocketConfig(socket);
        return new SocketIoHelper(clientMsgHandler, connectedHandler, new SocketIOServer(configuration));
    }
}
