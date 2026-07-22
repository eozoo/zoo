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

import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.access.annotation.AnonymousAccess;
import com.cowave.zoo.framework.access.filter.AccessIdGenerator;
import com.cowave.zoo.framework.configuration.ApplicationProperties;
import com.cowave.zoo.framework.helper.redis.RedisHelper;
import com.cowave.zoo.tools.Collections;
import com.cowave.zoo.tools.SpringContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Nullable;
import java.util.*;
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

    @ConditionalOnMissingBean(BearerTokenDelegate.class)
    @Bean
    public BearerTokenDelegate bearerTokenDelegate() {
        return new BearerTokenDelegateImpl(accessProperties, applicationProperties);
    }

    @ConditionalOnMissingBean(BearerTokenService.class)
    @Bean
    public BearerTokenService bearerTokenService(@Nullable RedisHelper redisHelper, ObjectMapper objectMapper,
                                                 AccessIdGenerator accessIdGenerator, BearerTokenDelegate bearerTokenDelegate) {
        return new BearerTokenServiceImpl(redisHelper, objectMapper, accessIdGenerator, bearerTokenDelegate);
    }

    @ConditionalOnMissingBean(TenantUserDetailsService.class)
    @Bean
    public TenantUserDetailsService tenantUserDetailsService(
            PasswordEncoder passwordEncoder, @Nullable BearerTokenService bearerTokenService) {
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
            Map<String, Set<String>> anonymousUrls = getAnonymousUrl();
            if (ArrayUtils.isNotEmpty(accessProperties.ignoreUrls()) || !anonymousUrls.isEmpty()) {
                httpSecurity.authorizeRequests()
                        .antMatchers(accessProperties.ignoreUrls()).permitAll()
                        .antMatchers(anonymousUrls.getOrDefault("ALL", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.GET, anonymousUrls.getOrDefault("GET", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.PUT, anonymousUrls.getOrDefault("PUT", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.POST, anonymousUrls.getOrDefault("POST", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.PATCH, anonymousUrls.getOrDefault("PATCH", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.DELETE, anonymousUrls.getOrDefault("DELETE", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.HEAD, anonymousUrls.getOrDefault("HEAD", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.OPTIONS, anonymousUrls.getOrDefault("OPTIONS", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.TRACE, anonymousUrls.getOrDefault("TRACE", new HashSet<>()).toArray(new String[0])).permitAll()
                        .anyRequest().authenticated();
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
            Map<String, Set<String>> anonymousUrls = getAnonymousUrl();
            if (ArrayUtils.isNotEmpty(tokenIgnoreUrls) || !anonymousUrls.isEmpty()) {
                httpSecurity.authorizeRequests()
                        .antMatchers(tokenIgnoreUrls).permitAll()
                        .antMatchers(anonymousUrls.getOrDefault("ALL", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.GET, anonymousUrls.getOrDefault("GET", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.PUT, anonymousUrls.getOrDefault("PUT", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.POST, anonymousUrls.getOrDefault("POST", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.PATCH, anonymousUrls.getOrDefault("PATCH", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.DELETE, anonymousUrls.getOrDefault("DELETE", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.HEAD, anonymousUrls.getOrDefault("HEAD", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.OPTIONS, anonymousUrls.getOrDefault("OPTIONS", new HashSet<>()).toArray(new String[0])).permitAll()
                        .antMatchers(HttpMethod.TRACE, anonymousUrls.getOrDefault("TRACE", new HashSet<>()).toArray(new String[0])).permitAll()
                        .anyRequest().authenticated();
            } else {
                httpSecurity.authorizeRequests().anyRequest().authenticated();
            }

            boolean basicWithConfigUser = accessProperties.basicWithConfigUser();

            // Bearer Token认证
            BearerTokenFilter bearerTokenFilter = new BearerTokenFilter(useRefreshToken, bearerTokenService, ignoreUrls, anonymousUrls);
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

    private Map<String, Set<String>> getAnonymousUrl() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = SpringContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();

        Map<String, Set<String>> anonymousUrls = new HashMap<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = infoEntry.getValue();
            AnonymousAccess anonymousAccess = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
            if (anonymousAccess != null) {
                RequestMappingInfo requestMappingInfo = infoEntry.getKey();
                List<RequestMethod> requestMethods = new ArrayList<>(requestMappingInfo.getMethodsCondition().getMethods());
                if (requestMethods.isEmpty()) {
                    anonymousUrls.computeIfAbsent("ALL", k -> new HashSet<>())
                            .addAll(requestMappingInfo.getPathPatternsCondition().getPatternValues());
                } else {
                    for (RequestMethod requestMethod : requestMethods) {
                        anonymousUrls.computeIfAbsent(requestMethod.name(), k -> new HashSet<>())
                                .addAll(requestMappingInfo.getPathPatternsCondition().getPatternValues());
                    }
                }
            }
        }
        return anonymousUrls;
    }
}
