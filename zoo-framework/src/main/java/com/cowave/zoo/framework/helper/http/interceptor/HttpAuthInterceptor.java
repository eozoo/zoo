package com.cowave.zoo.framework.helper.http.interceptor;

import cn.hutool.core.util.IdUtil;
import com.cowave.zoo.framework.access.Access;
import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.access.security.AccessUserDetails;
import com.cowave.zoo.framework.access.security.BearerTokenDelegate;
import com.cowave.zoo.framework.access.security.BearerTokenDelegateImpl;
import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.request.HttpRequest;
import com.cowave.zoo.tools.SpringContext;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static com.cowave.zoo.framework.access.security.AuthMode.BASIC;
import static com.cowave.zoo.http.client.constants.HttpHeader.Authorization;

/**
 * @author shanhuiming
 */
public class HttpAuthInterceptor implements HttpClientInterceptor {

    @Override
    public void apply(HttpRequest request) {
        ConfigurableListableBeanFactory beanFactory = SpringContext.getBeanFactory();
        BearerTokenDelegate delegate = beanFactory == null ? null
                : beanFactory.getBeanProvider(BearerTokenDelegate.class).getIfAvailable();
        String tokenKey = delegate == null ? Authorization : delegate.tokenKey();
        // 调用已设置令牌
        if (request.headers().keySet().stream().anyMatch(tokenKey::equalsIgnoreCase)) {
            return;
        }

        // 获取上下文中的Access令牌
        AccessUserDetails userDetails = Access.userDetails();
        String token = userDetails == null ? null : userDetails.getAccessToken();
        if (StringUtils.isBlank(token)) {
            token = delegate != null && "cookie".equals(delegate.tokenStore())
                    ? Access.getCookie(tokenKey) : Access.getRequestHeader(tokenKey);
        }
        if (StringUtils.isNotBlank(token)) {
            // 忽略Basic账号密码
            if (token.startsWith("Basic ")) {
                return;
            }
            request.header(tokenKey, token.startsWith("Bearer ") ? token : "Bearer " + token);
            return;
        }

        // 配置支持的话，签发匿名Access
        if (delegate == null || delegate.authMode() == BASIC) {
            return;
        }
        AccessProperties properties = beanFactory.getBeanProvider(AccessProperties.class).getIfAvailable();
        if (properties == null || !properties.authEnable()) {
            return;
        }
        token = anonymousToken(delegate, properties);
        if (token != null) {
            request.header(tokenKey, "Bearer " + token);
        }
    }

    // 无角色权限，不写Redis，不生成Refresh，不改写线程上下文
    private String anonymousToken(BearerTokenDelegate delegate, AccessProperties properties) {
        SignatureAlgorithm algorithm = delegate.getAccessAlgorithm();
        // 只有公钥时，不具备签发能力
        if (delegate instanceof BearerTokenDelegateImpl && !algorithm.isHmac()
                && StringUtils.isBlank(properties.accessPrivateKey())) {
            return null;
        }
        Key signingKey = delegate.getAccessSigningKey(algorithm);
        if (signingKey == null) {
            return null;
        }

        AccessUserDetails anonymous = new AccessUserDetails();
        anonymous.setAccessId(IdUtil.fastSimpleUUID());
        anonymous.setAuthType("anonymous");
        anonymous.setUsername("anonymous");
        anonymous.setDeviceLimit(false);
        anonymous.setAccessStore(false);
        anonymous.setRoles(Collections.emptyList());
        anonymous.setPermissions(Collections.emptyList());
        anonymous.setPermitScopes(Collections.emptyMap());

        JwtBuilder builder = Jwts.builder();
        delegate.setAccessClaims(builder, anonymous);
        Date issuedAt = new Date();
        return builder.setIssuer(delegate.getAccessIssuer())
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + delegate.getAccessExpireSeconds() * 1000L))
                .signWith(algorithm, signingKey)
                .compact();
    }
}
