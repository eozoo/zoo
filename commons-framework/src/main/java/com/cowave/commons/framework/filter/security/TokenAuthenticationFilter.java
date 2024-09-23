package com.cowave.commons.framework.filter.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author shanhuiming
 *
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

	private final AntPathRequestMatcher refreshMatcher = new AntPathRequestMatcher("/api/v1/auth/refresh", null);

	private final TokenService tokenService;

	private List<RequestMatcher> requestMatchers;

	public TokenAuthenticationFilter(TokenService tokenService, String[] permitAll) {
		this.tokenService = tokenService;
		this.requestMatchers = RequestMatchers.antMatchers(permitAll);
	}

	@Override
	protected void doFilterInternal(
			@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {
		AccessToken accessToken = tokenService.parseToken(request);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(accessToken, null, accessToken.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		for(RequestMatcher matcher : requestMatchers) {
			if(matcher.matches(request)) {
				chain.doFilter(request, response);
				return;
			}
		}

		if(accessToken.getValidCode() == 0 || (accessToken.getValidCode() == 498 && refreshMatcher.matches(request))) {
			chain.doFilter(request, response);
			return;
		}

		response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(ResponseCode.OK.getCode());
        response.getWriter().write(JSON.toJSONString(Response.error(accessToken.getValidCode(), accessToken.getValidDesc())));
	}
}
