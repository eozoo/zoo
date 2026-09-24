/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.helper.socketio;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.AuthTokenResult;
import com.corundumstudio.socketio.listener.DataListener;
import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.access.security.AccessUserDetails;
import com.cowave.zoo.framework.access.security.BearerTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
public class SocketIoHelper {
    private static final String IDENTITY_KEY = SocketIdentity.class.getName();
    private final SocketIOServer socketIoServer;
    private final BearerTokenService bearerTokenService;
    private final AccessProperties accessProperties;
    private final Map<String, Map<String, SocketIOClient>> namespaceClients = new ConcurrentHashMap<>();
    private final Map<String, SocketIOClient> rootClients = new ConcurrentHashMap<>();
    private final Set<String> authNamespaces = ConcurrentHashMap.newKeySet();

    @PostConstruct
    private void init() {
        socketIoServer.addConnectListener(client -> registerClient(client, rootClients));
        socketIoServer.addDisconnectListener(client -> removeClient(client, rootClients));
        socketIoServer.start();
    }

    private void registerClient(SocketIOClient client, Map<String, SocketIOClient> clients) {
        SocketIdentity identity = authenticate(client);
        if (identity == null) {
            client.disconnect();
            return;
        }

        client.set(IDENTITY_KEY, identity);
        clients.put(client.getSessionId().toString(), client);
    }

    private void removeClient(SocketIOClient client, Map<String, SocketIOClient> clients) {
        clients.remove(client.getSessionId().toString(), client);
        namespaceClients.values().forEach(map -> map.remove(client.getSessionId().toString(), client));
    }

    private SocketIdentity authenticate(SocketIOClient client) {
        AccessUserDetails authorizedDetails = (AccessUserDetails) client.getHandshakeData().getAuthToken();
        if (authorizedDetails != null) {
            return new SocketIdentity(authorizedDetails);
        }

        String token = client.getHandshakeData().getHttpHeaders().get(accessProperties.tokenKey());
        AccessUserDetails details = bearerTokenService.validateSocketAccessToken(token);
        return details == null ? null : new SocketIdentity(details);
    }

    private AuthTokenResult authenticateAuthData(Object authData, SocketIOClient client) {
        if (!(authData instanceof Map<?, ?> auth)) {
            return new AuthTokenResult(false, "Socket authentication data is invalid");
        }

        Object tokenValue = auth.get("token");
        if (!(tokenValue instanceof String token) || token.isBlank()) {
            return new AuthTokenResult(false, "Socket access token is missing");
        }

        AccessUserDetails details = bearerTokenService.validateSocketAccessToken(token);
        if (details == null) {
            return new AuthTokenResult(false, "Socket access token is invalid");
        }

        client.getHandshakeData().setAuthToken(details);
        return AuthTokenResult.AuthTokenResultSuccess;
    }

    private com.corundumstudio.socketio.SocketIONamespace namespace(String namespace) {
        com.corundumstudio.socketio.SocketIONamespace socketNamespace = socketIoServer.addNamespace(namespace);
        if (authNamespaces.add(namespace)) {
            socketNamespace.addAuthTokenListener(this::authenticateAuthData);
        }
        return socketNamespace;
    }

    @PreDestroy
    private void destroy() {
        if (socketIoServer != null) {
            socketIoServer.stop();
        }
        rootClients.clear();
        namespaceClients.clear();
    }

    /**
     * 获取连接身份
     */
    public SocketIdentity identity(SocketIOClient client) {
        return client.get(IDENTITY_KEY);
    }

    /**
     * 注册全局事件监听器
     */
    public <T> void registerDataListener(String event, Class<T> type, DataListener<T> listener) {
        socketIoServer.addEventListener(event, type, listener);
    }

    /**
     * 注册 namespace 事件监听器
     */
    public <T> void registerDataListener(String namespace, String event, Class<T> type, DataListener<T> listener) {
        namespace(namespace).addEventListener(event, type, listener);
    }

    /**
     * 注册 namespace 连接监听器
     */
    public void registerConnectListener(String namespace) {
        namespace(namespace).addConnectListener(client ->
                registerClient(client, namespaceClients.computeIfAbsent(namespace, key -> new ConcurrentHashMap<>())));
    }

    /**
     * 注册 namespace 断开监听器
     */
    public void registerDisconnectListener(String namespace) {
        namespace(namespace).addDisconnectListener(client -> {
            Map<String, SocketIOClient> clients = namespaceClients.get(namespace);
            if (clients != null) {
                clients.remove(client.getSessionId().toString(), client);
            }
        });
    }

    /**
     * 向所有全局连接发送事件
     */
    public <T> void send(String event, T data) {
        rootClients.values().forEach(client -> client.sendEvent(event, data));
    }

    /**
     * 向指定会话发送事件
     */
    public <T> void sendClients(Collection<String> sessionIds, String event, T data) {
        if (CollectionUtils.isEmpty(sessionIds)) {
            return;
        }
        sessionIds.stream().map(rootClients::get).filter(client -> client != null)
                .forEach(client -> client.sendEvent(event, data));
    }

    /**
     * 向 namespace 所有连接发送事件
     */
    public <T> void sendNamespace(String namespace, String event, T data) {
        Map<String, SocketIOClient> clients = namespaceClients.get(namespace);
        if (clients != null) {
            clients.values().forEach(client -> client.sendEvent(event, data));
        }
    }

    /**
     * 将连接加入Room
     */
    public void joinRoom(SocketIOClient client, String room) {
        if (client != null && StringUtils.isNotBlank(room)) {
            client.joinRoom(room);
        }
    }

    /**
     * 将连接移出Room
     */
    public void leaveRoom(SocketIOClient client, String room) {
        if (client != null && StringUtils.isNotBlank(room)) {
            client.leaveRoom(room);
        }
    }

    /**
     * 向Room发送事件
     */
    public <T> void sendInRoom(String room, String event, T data) {
        if (StringUtils.isBlank(room)) {
            return;
        }
        rootClients.values().forEach(client -> {
            if (client.getAllRooms().contains(room)) {
                client.sendEvent(event, data);
            }
        });
    }

    /**
     * 向 namespace 的 Room 发送事件
     */
    public <T> void sendInRoomOfNamespace(String namespace, String room, String event, T data) {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(room)) {
            return;
        }
        Map<String, SocketIOClient> clients = namespaceClients.get(namespace);
        if (clients != null) {
            clients.values().stream().filter(client -> client.getAllRooms().contains(room))
                    .forEach(client -> client.sendEvent(event, data));
        }
    }

    /**
     * 向 namespace 的指定用户 Room 发送事件
     */
    public <T> void sendClientsInRoomOfNamespace(String namespace, String room,
                                                 Collection<String> userCodes, String event, T data) {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(room) || CollectionUtils.isEmpty(userCodes)) {
            return;
        }
        Map<String, SocketIOClient> clients = namespaceClients.get(namespace);
        if (clients != null) {
            clients.values().stream()
                    .filter(client -> client.getAllRooms().contains(room))
                    .filter(client -> {
                        SocketIdentity identity = identity(client);
                        return identity != null && userCodes.contains(
                                String.valueOf(identity.getUserDetails().getUserCode()));
                    })
                    .forEach(client -> client.sendEvent(event, data));
        }
    }

    /**
     * 向 namespace 的指定用户发送事件
     */
    public <T> void sendClientsOfNamespace(String namespace, Collection<String> sessionIds, String event, T data) {
        if (StringUtils.isBlank(namespace) || CollectionUtils.isEmpty(sessionIds)) {
            return;
        }
        Map<String, SocketIOClient> clients = namespaceClients.get(namespace);
        if (clients != null) {
            clients.values().stream().filter(client -> {
                SocketIdentity identity = identity(client);
                return identity != null && sessionIds.contains(String.valueOf(identity.getUserDetails().getUserCode()));
            }).forEach(client -> client.sendEvent(event, data));
        }
    }

    /**
     * 断开全部连接
     */
    public void disconnectUser(String account) {
        disconnect(c -> account.equals(identity(c).getUserAccount()));
    }

    /**
     * 断开指定会话的连接
     */
    public void disconnectSession(String account, String session) {
        disconnect(c -> account.equals(identity(c).getUserAccount()) && session.equals(identity(c).getSessionId()));
    }

    /**
     * 断开指定租户的连接
     */
    public void disconnectTenant(String account, String tenant) {
        disconnect(c -> account.equals(identity(c).getUserAccount()) && tenant.equals(identity(c).getTenantCode()));
    }

    private void disconnect(Predicate<SocketIOClient> predicate) {
        rootClients.values().stream().filter(predicate).forEach(SocketIOClient::disconnect);
        namespaceClients.values().stream().flatMap(
                map -> map.values().stream()).filter(predicate).forEach(SocketIOClient::disconnect);
    }
}
