/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.security;

import java.util.List;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.configuration.ClusterInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Service;

import com.google.common.base.Objects;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author shanhuiming
 *
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@Service("permit")
public class Permission {

    public static final String ROLE_ADMIN = "sysAdmin";

    public static final String PERMIT_ADMIN = "*:*:*";

    private final ClusterInfo clusterInfo;

    public boolean isAdmin() {
        List<String> roles = Access.userRoles();
        if(CollectionUtils.isEmpty(roles)) {
            return false;
        }
        return roles.contains(ROLE_ADMIN);
    }

    public boolean hasPermit(String permission) {
        if(isAdmin()) {
            return true;
        }

        List<String> perms = Access.userPermissions();
        if(CollectionUtils.isEmpty(perms)) {
            return false;
        }
        return perms.contains(permission) || perms.contains(PERMIT_ADMIN);
    }

    public boolean hasRole(String role) {
        if(isAdmin()) {
            return true;
        }
        List<String> roles = Access.userRoles();
        if(CollectionUtils.isEmpty(roles)) {
            return false;
        }
        return roles.contains(role);
    }

    public boolean isCurrentCluster() {
        Integer clusterId = Access.clusterId();
    	return clusterId != null && Objects.equal(clusterId, clusterInfo.getId());
    }
}
