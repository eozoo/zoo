package com.cowave.commons.framework.access.security;

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
