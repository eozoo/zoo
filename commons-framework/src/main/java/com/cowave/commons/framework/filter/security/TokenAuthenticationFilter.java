/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.feign.codec.HttpCode;
import org.springframework.feign.codec.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author shanhuiming
 *
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

	private final TokenService tokenService;

	public TokenAuthenticationFilter(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	protected void doFilterInternal(
			@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {
		AccessToken accessToken = tokenService.parseToken(request);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(accessToken, null, accessToken.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		if(StringUtils.isNotBlank(accessToken.getValidCode())) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpStatus.OK.value());
			response.getWriter().write(JSON.toJSONString(Response.code(new HttpCode() {
						@Override
						public String getCode() {
							return accessToken.getValidCode();
						}

						@Override
						public String getMsg() {
							return accessToken.getValidDesc();
						}
					})));
			return;
		}
		chain.doFilter(request, response);
	}
}
