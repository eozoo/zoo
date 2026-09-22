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
package com.cowave.zoo.framework.helper.socketio.listener;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.HandshakeData;
import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.access.security.AccessUserDetails;
import com.cowave.zoo.framework.access.security.BearerTokenService;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
public class SocketIoAuthorizationListener implements AuthorizationListener {

    private final AccessProperties accessProperties;

    @Nullable
    private final BearerTokenService bearerTokenService;

    @Override
    public AuthorizationResult getAuthorizationResult(HandshakeData handshakeData) {
        if(bearerTokenService == null){
            return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
        }

        String accessToken = handshakeData.getHttpHeaders().get(accessProperties.tokenKey());
        if (accessToken == null) {
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }

        AccessUserDetails userDetails = bearerTokenService.validateSocketAccessToken(accessToken);
        if (userDetails == null) {
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }

        handshakeData.setAuthToken(userDetails);
        return new AuthorizationResult(true);
    }
}
