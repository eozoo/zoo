package com.cowave.commons.framework.filter.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
*
* @author shanhuiming
* 
*/
public final class RequestMatchers {

	/**
	 * Create a {@link List} of {@link AntPathRequestMatcher} instances.
	 * @param httpMethod the {@link HttpMethod} to use or {@code null} for any
	 * {@link HttpMethod}.
	 * @param antPatterns the ant patterns to create {@link AntPathRequestMatcher}
	 * from
	 * @return a {@link List} of {@link AntPathRequestMatcher} instances
	 */
	public static List<RequestMatcher> antMatchers(HttpMethod httpMethod, String... antPatterns) {
		String method = (httpMethod != null) ? httpMethod.toString() : null;
		List<RequestMatcher> matchers = new ArrayList<>();
		for (String pattern : antPatterns) {
			matchers.add(new AntPathRequestMatcher(pattern, method));
		}
		return matchers;
	}

	/**
	 * Create a {@link List} of {@link AntPathRequestMatcher} instances that do not
	 * specify an {@link HttpMethod}.
	 * @param antPatterns the ant patterns to create {@link AntPathRequestMatcher}
	 * from
	 * @return a {@link List} of {@link AntPathRequestMatcher} instances
	 */
	public static List<RequestMatcher> antMatchers(String... antPatterns) {
		return antMatchers(null, antPatterns);
	}

	/**
	 * Create a {@link List} of {@link RegexRequestMatcher} instances.
	 * @param httpMethod the {@link HttpMethod} to use or {@code null} for any
	 * {@link HttpMethod}.
	 * @param regexPatterns the regular expressions to create
	 * {@link RegexRequestMatcher} from
	 * @return a {@link List} of {@link RegexRequestMatcher} instances
	 */
	public static List<RequestMatcher> regexMatchers(HttpMethod httpMethod, String... regexPatterns) {
		String method = (httpMethod != null) ? httpMethod.toString() : null;
		List<RequestMatcher> matchers = new ArrayList<>();
		for (String pattern : regexPatterns) {
			matchers.add(new RegexRequestMatcher(pattern, method));
		}
		return matchers;
	}

	/**
	 * Create a {@link List} of {@link RegexRequestMatcher} instances that do not
	 * specify an {@link HttpMethod}.
	 * @param regexPatterns the regular expressions to create
	 * {@link RegexRequestMatcher} from
	 * @return a {@link List} of {@link RegexRequestMatcher} instances
	 */
	public static List<RequestMatcher> regexMatchers(String... regexPatterns) {
		return regexMatchers(null, regexPatterns);
	}
}