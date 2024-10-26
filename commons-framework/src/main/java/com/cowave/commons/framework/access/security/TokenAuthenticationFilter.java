/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSON;
import org.springframework.feign.codec.HttpCode;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.springframework.feign.codec.ResponseCode.SUCCESS;

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
		ResponseCode validCode = accessToken.getValidCode();
		if(validCode != null) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			if(tokenService.isAlwaysSuccess()){
				response.setStatus(SUCCESS.getStatus());
			}else{
				response.setStatus(validCode.getStatus());
			}
			response.getWriter().write(JSON.toJSONString(Response.code(new HttpCode() {
						@Override
						public String getCode() {
							return validCode.getCode();
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
