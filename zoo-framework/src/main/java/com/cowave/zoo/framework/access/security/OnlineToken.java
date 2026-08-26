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
package com.cowave.zoo.framework.access.security;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shanhuiming
 */
@NoArgsConstructor
@Data
public class OnlineToken {

    /**
     * 会话信息
     */
    private RefreshTokenInfo refreshToken;

    /**
     * 持有的Access令牌
     */
    private List<AccessTokenInfo> accessTokens = new ArrayList<>();

    /**
     * 持有的Oauth令牌
     */
    private List<RefreshTokenInfo> oauthTokens = new ArrayList<>();

    public OnlineToken(RefreshTokenInfo refreshToken) {
        this.refreshToken = refreshToken;
    }
}
