package com.cowave.commons.framework.access.security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author shanhuiming
 */
@Getter
public class TenantUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    /**
     * 租户id
     */
    private final String tenantId;

    public TenantUsernamePasswordAuthenticationToken(String tenantId, Object principal, Object credentials) {
        super(principal, credentials);
        this.tenantId = tenantId;
    }
}
