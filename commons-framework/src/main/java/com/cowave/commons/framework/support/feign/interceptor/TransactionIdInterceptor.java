/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.support.feign.interceptor;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.configuration.ApplicationConfiguration;
import com.cowave.commons.framework.configuration.ClusterInfo;
import com.cowave.commons.framework.filter.security.AccessToken;
import com.cowave.commons.framework.filter.security.TokenService;
import com.cowave.commons.tools.ids.IdGenerator;
import com.cowave.commons.tools.SpringContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class TransactionIdInterceptor implements RequestInterceptor {

    private static final IdGenerator GENERATOR = new IdGenerator();

    private final ApplicationConfiguration applicationConfiguration;

    private final TokenService tokenService;

    private final ClusterInfo clusterInfo;

    private final String port;

    public TransactionIdInterceptor(ApplicationConfiguration applicationConfiguration, TokenService tokenService, ClusterInfo clusterInfo) {
        this.applicationConfiguration = applicationConfiguration;
        this.tokenService = tokenService;
        this.clusterInfo = clusterInfo;
        this.port = SpringContext.getProperty("server.port", "8080");
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String accessId = Access.accessId();
        String authorization = Access.tokenAccessValue();
        if(StringUtils.isBlank(accessId)) {
            accessId = newAccessId();
            log.info(">< new access-id: {}", accessId);
        }
        if(StringUtils.isBlank(authorization) && tokenService != null) {
            authorization = newAuthorization();
        }
        requestTemplate.header("Access-Id", accessId);
        requestTemplate.header("Authorization", authorization);

        // 设置Seata事务id
        String xid = RootContext.getXID();
        if(StringUtils.isNotBlank(xid)){
            requestTemplate.header(RootContext.KEY_XID, xid);
        }
    }

    private String newAccessId() {
        String clusterId = clusterInfo.getId() == null ? "" : String.valueOf(clusterInfo.getId());
        String prefix = "#" + clusterId + port;
        return GENERATOR.generateIdWithDate(prefix, "", "yyyyMMddHHmmss", 1000);
    }

    private String newAuthorization() {
        AccessToken appToken = AccessToken.newToken();
        appToken.setType(AccessToken.TYPE_APP);
        appToken.setUserId(-1L);
        appToken.setDeptId(-1L);
        appToken.setUsername(applicationConfiguration.getName());
        appToken.setClusterId(clusterInfo.getId());
        appToken.setClusterName(clusterInfo.getName());
        appToken.setClusterLevel(clusterInfo.getLevel());
        return tokenService.createAccessToken(appToken, 300);
    }
}
