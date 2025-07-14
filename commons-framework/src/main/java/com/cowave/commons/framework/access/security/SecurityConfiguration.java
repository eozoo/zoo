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
import com.cowave.commons.framework.access.filter.AccessIdGenerator;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.redis.RedisHelper;
import com.cowave.commons.tools.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author shanhuiming
 */
@ConditionalOnClass({SecurityFilterChain.class})
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AccessProperties accessProperties;

    private final ApplicationProperties applicationProperties;

    @ConditionalOnMissingBean(PasswordEncoder.class)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @ConditionalOnMissingBean(BearerTokenService.class)
    @Bean
    public BearerTokenService bearerTokenService(
            AccessIdGenerator accessIdGenerator, ObjectMapper objectMapper,
            @Nullable RedisHelper redisHelper, @Nullable BearerTokenInterceptor bearerTokenInterceptor){
        return new BearerTokenServiceImpl(applicationProperties, accessProperties,
                accessIdGenerator, objectMapper, redisHelper, bearerTokenInterceptor);
    }

    @ConditionalOnMissingBean(TenantUserDetailsService.class)
    @Bean
    public TenantUserDetailsService tenantUserDetailsService(PasswordEncoder passwordEncoder, @Nullable BearerTokenService bearerTokenService) {
        List<AccessUser> userList = accessProperties.accessUsers();
        return new AccessUserDetailsServiceImpl(accessProperties.authMode(), applicationProperties,
                passwordEncoder, bearerTokenService, Collections.copyToMap(userList, AccessUser::getUsername));
    }

    @ConditionalOnMissingBean(AuthenticationManager.class)
    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder, TenantUserDetailsService userDetailsService) {
		return new ProviderManager(new TenantAuthenticationProvider(userDetailsService, passwordEncoder));
	}

    /**
     * basic认证
     */
    @ConditionalOnMissingBean(value = {SecurityFilterChain.class, WebSecurityConfigurerAdapter.class})
    @ConditionalOnProperty(name = "spring.access.auth.mode", havingValue = "basic", matchIfMissing = true)
    @Bean
    public SecurityFilterChain basicSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        if (ArrayUtils.isNotEmpty(accessProperties.authUrls())) {
            httpSecurity.requestMatchers(requestMatchers ->
                    requestMatchers.antMatchers(accessProperties.authUrls())
            );
        }
        // 允许跨域
        httpSecurity.csrf().disable();
        // 无状态会话
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 取消X-Frame-Options，允许嵌入到<iframe>
        httpSecurity.headers().frameOptions().disable();
        // username:password Base64编码
        httpSecurity.httpBasic();
        if (accessProperties.authEnable()) {
            if (ArrayUtils.isNotEmpty(accessProperties.ignoreUrls())) {
                httpSecurity.authorizeRequests().antMatchers(accessProperties.ignoreUrls()).permitAll().anyRequest().authenticated();
            } else {
                httpSecurity.authorizeRequests().anyRequest().authenticated();
            }
        } else {
            httpSecurity.authorizeRequests().anyRequest().permitAll();
        }
        return httpSecurity.build();
    }

    /**
     * Access认证
     */
    @ConditionalOnProperty(name = "spring.access.auth.mode", havingValue = "access")
    @ConditionalOnMissingBean(value = {SecurityFilterChain.class, WebSecurityConfigurerAdapter.class})
    @Bean
    public SecurityFilterChain accessBearerSecurityFilterChain(
            HttpSecurity httpSecurity, @Nullable BearerTokenService bearerTokenService,
            TenantUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        return newBearerSecurityFilterChain(httpSecurity, bearerTokenService, userDetailsService, passwordEncoder, false);
    }

    /**
     * Access-Refresh认证
     */
    @ConditionalOnProperty(name = "spring.access.auth.mode", havingValue = "access-refresh")
    @ConditionalOnMissingBean(value = {SecurityFilterChain.class, WebSecurityConfigurerAdapter.class})
    @Bean
    public SecurityFilterChain refreshBearerSecurityFilterChain(
            HttpSecurity httpSecurity, @Nullable BearerTokenService bearerTokenService,
            TenantUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        return newBearerSecurityFilterChain(httpSecurity, bearerTokenService, userDetailsService, passwordEncoder, true);
    }

    private SecurityFilterChain newBearerSecurityFilterChain(HttpSecurity httpSecurity, BearerTokenService bearerTokenService,
            TenantUserDetailsService userDetailsService, PasswordEncoder passwordEncoder, boolean useRefreshToken) throws Exception {
        if (ArrayUtils.isNotEmpty(accessProperties.authUrls())) {
            httpSecurity.requestMatchers(requestMatchers ->
                    requestMatchers.antMatchers(accessProperties.authUrls())
            );
        }

        // 允许跨域
        httpSecurity.csrf().disable();
        // 无状态会话
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 取消X-Frame-Options，允许嵌入到<iframe>
        httpSecurity.headers().frameOptions().disable();
        if (accessProperties.authEnable()) {
            String[] basicUrls = accessProperties.basicUrls();
            String[] ignoreUrls = accessProperties.ignoreUrls();
            String[] tokenIgnoreUrls = Stream.of(basicUrls, ignoreUrls)
                    .filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).toArray(String[]::new);
            if (ArrayUtils.isNotEmpty(tokenIgnoreUrls)) {
                httpSecurity.authorizeRequests().antMatchers(tokenIgnoreUrls).permitAll().anyRequest().authenticated();
            } else {
                httpSecurity.authorizeRequests().anyRequest().authenticated();
            }

            boolean basicWithConfigUser = accessProperties.basicWithConfigUser();

            // Bearer Token认证
            BearerTokenFilter bearerTokenFilter = new BearerTokenFilter(useRefreshToken, bearerTokenService, ignoreUrls);
            httpSecurity.addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class);

            // Basic认证
            if (ArrayUtils.isNotEmpty(basicUrls)) {
                TenantUserDetailsService defaultUserDetailsService = tenantUserDetailsService(passwordEncoder, bearerTokenService);
                BasicAuthFilter basicAuthFilter = new BasicAuthFilter(
                        userDetailsService, defaultUserDetailsService, basicWithConfigUser, passwordEncoder, basicUrls);
                httpSecurity.addFilterBefore(basicAuthFilter, BearerTokenFilter.class);
            }
        } else {
            httpSecurity.authorizeRequests().anyRequest().permitAll();
        }
        return httpSecurity.build();
    }
}
