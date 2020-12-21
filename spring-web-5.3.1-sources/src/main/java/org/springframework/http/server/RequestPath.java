/*
 * Copyright 2002-2020 the original author or authors.
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
package org.springframework.http.server;

import java.net.URI;

import org.springframework.lang.Nullable;

/**
 * 20201221
 * {@link PathContainer}的专业化，将路径细分为{@link #contextPath（）}和其余的{@link #pathWithinApplication（）}。后者通常用于应用程序内的请求映射，而前者则是
 * 在准备指向应用程序的外部链接时很有用。
 */
/**
 * Specialization of {@link PathContainer} that sub-divides the path into a
 * {@link #contextPath()} and the remaining {@link #pathWithinApplication()}.
 * The lattery is typically used for request mapping within the application
 * while the former is useful when preparing external links that point back to
 * the application.
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 */
// 20201221 {@link PathContainer}的专业化: 将路径细分为{@link #contextPath（）}和其余的{@link #pathWithinApplication（）}
public interface RequestPath extends PathContainer {

	/**
	 * Returns the portion of the URL path that represents the application.
	 * The context path is always at the beginning of the path and starts but
	 * does not end with "/". It is shared for URLs of the same application.
	 * <p>The context path may come from the underlying runtime API such as
	 * when deploying as a WAR to a Servlet container or it may be assigned in
	 * a WebFlux application through the use of
	 * {@link org.springframework.http.server.reactive.ContextPathCompositeHandler
	 * ContextPathCompositeHandler}.
	 */
	PathContainer contextPath();

	/**
	 * The portion of the request path after the context path which is typically
	 * used for request mapping within the application .
	 */
	PathContainer pathWithinApplication();

	/**
	 * Return a new {@code RequestPath} instance with a modified context path.
	 * The new context path must match 0 or more path segments at the start.
	 * @param contextPath the new context path
	 * @return a new {@code RequestPath} instance
	 */
	RequestPath modifyContextPath(String contextPath);


	/**
	 * Parse the URI for a request into a {@code RequestPath}.
	 * @param uri the URI of the request
	 * @param contextPath the contextPath portion of the URI path
	 */
	static RequestPath parse(URI uri, @Nullable String contextPath) {
		return parse(uri.getRawPath(), contextPath);
	}

	/**
	 * Variant of {@link #parse(URI, String)} with the encoded
	 * {@link URI#getRawPath() raw path}.
	 * @param rawPath the path
	 * @param contextPath the contextPath portion of the URI path
	 * @since 5.3
	 */
	// 20201221 {@link #parse（URI，String）}与已编码的{@link URI＃getRawPath（）原始路径}的变体。
	static RequestPath parse(String rawPath, @Nullable String contextPath) {
		return new DefaultRequestPath(rawPath, contextPath);
	}

}
