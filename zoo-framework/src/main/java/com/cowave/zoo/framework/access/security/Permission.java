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

import java.util.List;

import com.cowave.zoo.framework.access.Access;
import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.configuration.ApplicationProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;

import lombok.RequiredArgsConstructor;

import static com.cowave.zoo.http.client.constants.HttpHeader.X_User_Payload;

/**
 *
 * @author shanhuiming
 *
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@Component("permits")
public class Permission {
    public static final String TENANT_SYSTEM = "system";
    public static final String ROLE_ADMIN = "sysAdmin";
    public static final String PERMIT_ADMIN = "*";
    private final AccessProperties accessProperties;
    private final ApplicationProperties applicationProperties;

    /**
     * 是否当前集群
     */
    public boolean isCurrentCluster() {
        Integer clusterId = Access.clusterId();
        return clusterId != null && Objects.equal(clusterId, applicationProperties.getClusterId());
    }

    /**
     * 是否管理员
     */
    public boolean isAdmin() {
        List<String> roles = Access.userRoles();
        return !roles.isEmpty() && roles.contains(ROLE_ADMIN);
    }

    /**
     * 是否系统管理员
     */
    public boolean isSystemAdmin() {
        String tenantId = Access.tenantId();
        List<String> roles = Access.userRoles();
        return StringUtils.isNotBlank(tenantId) && !roles.isEmpty()
                && TENANT_SYSTEM.equals(tenantId) && roles.contains(ROLE_ADMIN);
    }

    /**
     * 是否系统用户
     */
    public boolean isSystemUser() {
        String tenantId = Access.tenantId();
        return StringUtils.isNotBlank(tenantId) && TENANT_SYSTEM.equals(tenantId);
    }

    /**
     * 是否拥有指定角色
     */
    public boolean hasRole(String role) {
        if(isIgnore() || isAdmin()) {
            return true;
        }

        List<String> roles = Access.userRoles();
        if(CollectionUtils.isEmpty(roles)) {
            return false;
        }
        return roles.contains(role);
    }

    /**
     * 是否拥有指定权限
     */
    public boolean hasPermit(String permission) {
        Access access = Access.get();
        if (access != null) {
            access.setPermit(permission);
        }

        if(isIgnore() || StringUtils.isBlank(permission) || isAdmin()) {
            return true;
        }

        List<String> permits = Access.userPermissions();
        if(CollectionUtils.isEmpty(permits)) {
            return false;
        }

        for(String permit : permits){
            if(StringUtils.isNotBlank(permit) && matchPermit(permit, permission)){
                return true;
            }
        }
        return false;
    }

    public boolean isIgnore() {
        return !accessProperties.authEnable() && StringUtils.isBlank(Access.getRequestHeader(X_User_Payload));
    }

    private boolean matchPermit(String srcPermit, String destPermit) {
        String[] src = srcPermit.split(":");
        String[] dest = destPermit.split(":");
        // 左匹配
        for (int index = 0; index < src.length; index++) {
            if (PERMIT_ADMIN.equals(src[index])) {
                return true;
            }
            if (index >= dest.length) {
                return false;
            }
            if (!src[index].equals(dest[index])) {
                return false;
            }
        }
        return true;
    }
}
