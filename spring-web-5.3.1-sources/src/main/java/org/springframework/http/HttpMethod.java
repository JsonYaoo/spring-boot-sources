/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http;

import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * 20201221
 * Java 5枚举的HTTP请求方法。 旨在与{@link org.springframework.http.client.ClientHttpRequest}和{@link org.springframework.web.client.RestTemplate}一起使用。
 */
/**
 * Java 5 enumeration of HTTP request methods. Intended for use
 * with {@link org.springframework.http.client.ClientHttpRequest}
 * and {@link org.springframework.web.client.RestTemplate}.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
// 20201221 HTTP请求方法枚举
public enum HttpMethod {

	GET,
	HEAD,
	POST,
	PUT,

	// 20201221 PATCH method 是在Servlet 3.0和Tomcat 7中都提到的，也就是尚未实现它: 只对已有资源进行更新操作
	PATCH,
	DELETE,
	OPTIONS,
	TRACE;

	// 20201221 HttpMethod映射器
	private static final Map<String, HttpMethod> mappings = new HashMap<>(16);

	static {
		// 20201221 初始化HttpMethod映射器: 方法名称-方法名称
		for (HttpMethod httpMethod : values()) {
			mappings.put(httpMethod.name(), httpMethod);
		}
	}

	/**
	 * Resolve the given method value to an {@code HttpMethod}.
	 * @param method the method value as a String
	 * @return the corresponding {@code HttpMethod}, or {@code null} if not found
	 * @since 4.2.4
	 */
	// 20201221 将给定的方法值解析为{@code HttpMethod}。
	@Nullable
	public static HttpMethod resolve(@Nullable String method) {
		// 20201221 从HttpMethod映射器获取HttpMethod实例
		return (method != null ? mappings.get(method) : null);
	}

	/**
	 * Determine whether this {@code HttpMethod} matches the given
	 * method value.
	 * @param method the method value as a String
	 * @return {@code true} if it matches, {@code false} otherwise
	 * @since 4.2.4
	 */
	// 20201222 确定此{@code HttpMethod}是否与给定的方法值匹配。
	public boolean matches(String method) {
		return (this == resolve(method));
	}

}
