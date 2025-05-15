/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.socketio.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ExceptionListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class SocketIoExceptionListener implements ExceptionListener {

    @Override
    public void onEventException(Exception e, List<Object> list, SocketIOClient socketIoClient) {
        log.error("socketIo event error, " + socketIoClient.getHandshakeData().getUrlParams(), e);
    }

    @Override
    public void onDisconnectException(Exception e, SocketIOClient socketIoClient) {
        log.error("socketIo disconnect error, " + socketIoClient.getHandshakeData().getUrlParams(), e);
    }

    @Override
    public void onConnectException(Exception e, SocketIOClient socketIoClient) {
        log.error("socketIo connect error" + socketIoClient.getHandshakeData().getUrlParams(), e);
    }

    @Override
    public void onPingException(Exception e, SocketIOClient socketIoClient) {
        log.error("socketIo ping error" + socketIoClient.getHandshakeData().getUrlParams(), e);
    }

    @Override
    public void onPongException(Exception e, SocketIOClient socketIoClient) {
        log.error("socketIo pong error" + socketIoClient.getHandshakeData().getUrlParams(), e);
    }

    @Override
    public boolean exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
        return true;
    }
}
