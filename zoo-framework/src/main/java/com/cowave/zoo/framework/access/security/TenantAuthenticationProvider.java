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

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
public class TenantAuthenticationProvider implements AuthenticationProvider {

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private final TenantUserDetailsService tenantUserDetailsService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(TenantUsernamePasswordAuthenticationToken.class, authentication, () -> this.messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.onlySupports", "Only TenantUsernamePasswordAuthenticationToken is supported"));
        TenantUsernamePasswordAuthenticationToken tenantAuthentication = (TenantUsernamePasswordAuthenticationToken) authentication;
        String tenantId = tenantAuthentication.getTenantId();

        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        UserDetails userDetails =
                tenantUserDetailsService.loadTenantUserByUsername(tenantAuthentication.getTenantId(), username);
        if(userDetails == null){
            throw new UsernameNotFoundException("User not found");
        }

        if (authentication.getCredentials() == null) {
			throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}

        String presentedPassword = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        return new TenantUsernamePasswordAuthenticationToken(tenantId, userDetails, null);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TenantUsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
