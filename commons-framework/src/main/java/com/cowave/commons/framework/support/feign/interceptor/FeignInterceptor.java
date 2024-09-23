package com.cowave.commons.framework.support.feign.interceptor;

import com.alibaba.excel.util.StringUtils;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.filter.security.TokenService;
import com.cowave.commons.framework.configuration.ApplicationConfiguration;
import com.cowave.commons.framework.configuration.ClusterInfo;
import com.cowave.commons.framework.filter.security.AccessToken;
import com.cowave.commons.framework.util.IdGenerator;
import com.cowave.commons.framework.util.SpringContext;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 *
 * @author shanhuiming
 *
 */
public class FeignInterceptor implements RequestInterceptor {

    private static final IdGenerator GENERATOR = new IdGenerator();

    private final ApplicationConfiguration applicationConfiguration;

    private final TokenService tokenService;

    private final ClusterInfo clusterInfo;

    private final String port;

    public FeignInterceptor(ApplicationConfiguration applicationConfiguration, TokenService tokenService, ClusterInfo clusterInfo) {
        this.applicationConfiguration = applicationConfiguration;
        this.tokenService = tokenService;
        this.clusterInfo = clusterInfo;
        this.port = SpringContext.getProperty("server.port", "8080");
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String requestId = Access.id();
        String authorization = Access.token();
        if(StringUtils.isBlank(requestId)) {
            requestId = requestId();
        }
        if(StringUtils.isBlank(authorization) && tokenService != null) {
            authorization = newToken();
        }
        requestTemplate.header("requestId", requestId);
        requestTemplate.header("Authorization", authorization);
    }

    private String requestId() {
        String clusterId = clusterInfo.getId() == null ? "" : String.valueOf(clusterInfo.getId());
        String prefix = "#" + clusterId + port;
        return GENERATOR.generateIdWithDate(prefix, "", "yyyyMMddHHmmss", 1000);
    }

    private String newToken() {
        AccessToken appToken = AccessToken.newToken();
        appToken.setType(AccessToken.TYPE_APP);
        appToken.setUserId(-1L);
        appToken.setDeptId(-1L);
        appToken.setUsername(applicationConfiguration.getName());
        appToken.setClusterId(clusterInfo.getId());
        appToken.setClusterName(clusterInfo.getName());
        appToken.setClusterLevel(clusterInfo.getLevel());
        return tokenService.newToken(appToken);
    }
}
