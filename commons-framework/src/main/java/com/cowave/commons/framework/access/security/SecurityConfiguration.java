/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.security;

import com.cowave.commons.framework.access.AccessProperties;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.tools.Collections;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author shanhuiming
 */
@ConditionalOnClass(SecurityFilterChain.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AccessProperties accessProperties;

    private final ApplicationProperties applicationProperties;

    @Nullable
    private final BearerTokenService bearerTokenService;

    @ConditionalOnMissingBean(PasswordEncoder.class)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @ConditionalOnMissingBean(UserDetailsService.class)
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return defaultUserDetailsService(passwordEncoder);
    }

    private UserDetailsService defaultUserDetailsService(PasswordEncoder passwordEncoder) {
        List<AccessUser> userList = accessProperties.accessUsers();
        return new AccessUserDetailsServiceImpl(accessProperties.authMode(), passwordEncoder,
                bearerTokenService, applicationProperties, Collections.copyToMap(userList, AccessUser::getUsername));
    }

    @ConditionalOnMissingBean(AuthenticationManager.class)
    @Bean
    public AuthenticationManager authenticationManagerBean(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		authenticationProvider.setUserDetailsService(userDetailsService);
		return new ProviderManager(authenticationProvider);
	}

    @Order
    @ConditionalOnMissingBean(value = {SecurityFilterChain.class, WebSecurityConfigurerAdapter.class})
    @Bean
    public SecurityFilterChain throughSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // 允许跨域
        httpSecurity.csrf().disable();
        // 无状态会话
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 取消X-Frame-Options，允许嵌入到<iframe>
        httpSecurity.headers().frameOptions().disable();
        // 放行所有url
        httpSecurity.authorizeRequests().anyRequest().permitAll();
        return httpSecurity.build();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnHasUrls("basic")
    @Bean
    public SecurityFilterChain basicSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.requestMatchers(requestMatchers ->
                requestMatchers.antMatchers(accessProperties.basicUrls())
        );
        // 允许跨域
        httpSecurity.csrf().disable();
        // 无状态会话
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 取消X-Frame-Options，允许嵌入到<iframe>
        httpSecurity.headers().frameOptions().disable();
        // username:password Base64编码
        httpSecurity.httpBasic();
        // url匹配
        if (ArrayUtils.isNotEmpty(accessProperties.basicIgnores())) {
            httpSecurity.authorizeRequests()
                    .antMatchers(accessProperties.basicIgnores()).permitAll().anyRequest().authenticated();
        } else {
            httpSecurity.authorizeRequests().anyRequest().authenticated();
        }
        return httpSecurity.build();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnHasUrls("access-token")
    @Bean
    public SecurityFilterChain accessBearerSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.requestMatchers(requestMatchers ->
                requestMatchers.antMatchers(accessProperties.accessTokenUrls())
        );
        // 允许跨域
        httpSecurity.csrf().disable();
        // 无状态会话
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 取消X-Frame-Options，允许嵌入到<iframe>
        httpSecurity.headers().frameOptions().disable();
        // url匹配
        if (ArrayUtils.isNotEmpty(accessProperties.accessTokenIgnores())) {
            httpSecurity.authorizeRequests()
                    .antMatchers(accessProperties.accessTokenIgnores()).permitAll().anyRequest().authenticated();
        } else {
            httpSecurity.authorizeRequests().anyRequest().authenticated();
        }

        if (bearerTokenService == null) {
            throw new IllegalStateException("SecurityFilterChain is not initialized，Please ensure the Jwt dependency has imported.");
        }

        // Bearer处理
        BearerTokenFilter bearerTokenFilter = new BearerTokenFilter(
                false, bearerTokenService, accessProperties.accessTokenIgnores());
        httpSecurity.addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnHasUrls("refresh-token")
    @Bean
    public SecurityFilterChain refreshBearerSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.requestMatchers(requestMatchers ->
                requestMatchers.antMatchers(accessProperties.refreshTokenUrls())
        );
        // 允许跨域
        httpSecurity.csrf().disable();
        // 无状态会话
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 取消X-Frame-Options，允许嵌入到<iframe>
        httpSecurity.headers().frameOptions().disable();
        // url匹配
        if (ArrayUtils.isNotEmpty(accessProperties.refreshTokenIgnores())) {
            httpSecurity.authorizeRequests()
                    .antMatchers(accessProperties.refreshTokenIgnores()).permitAll().anyRequest().authenticated();
        } else {
            httpSecurity.authorizeRequests().anyRequest().authenticated();
        }

        if (bearerTokenService == null) {
            throw new IllegalStateException("SecurityFilterChain is not initialized，Please ensure the Jwt dependency has imported.");
        }

        // Bearer处理
        BearerTokenFilter bearerTokenFilter = new BearerTokenFilter(
                true, bearerTokenService, accessProperties.refreshTokenIgnores());
        httpSecurity.addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
