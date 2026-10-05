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

import java.util.Date;

/**
 * @author shanhuiming
 */
@NoArgsConstructor
@Data
public class OnlineIndex {

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 登录设备标识
     */
    private String deviceId;

    /**
     * 当前租户编码
     */
    private String tenantCode;

    /**
     * 登录时间
     */
    private Date loginTime;

    public OnlineIndex(String userAccount, String deviceId, Date loginTime) {
        this.userAccount = userAccount;
        this.deviceId = deviceId;
        this.loginTime = loginTime;
    }

    public OnlineIndex(String userAccount, String deviceId, String tenantCode, Date loginTime) {
        this.userAccount = userAccount;
        this.deviceId = deviceId;
        this.tenantCode = tenantCode;
        this.loginTime = loginTime;
    }
}
