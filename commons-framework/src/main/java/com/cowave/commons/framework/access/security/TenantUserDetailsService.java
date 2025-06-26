package com.cowave.commons.framework.access.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author shanhuiming
 */
public interface TenantUserDetailsService {

    UserDetails loadTenantUserByUsername(String tenantId, String username) throws UsernameNotFoundException;
}
