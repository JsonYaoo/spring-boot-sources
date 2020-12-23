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

package org.springframework.web.util;

import java.net.URLDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.MappingMatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 20201221
 * A. URL路径匹配的帮助程序类。 为{@code RequestDispatcher}中的URL路径提供支持，并支持一致的URL解码。
 * B. {@link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping}和{@link org.springframework.web.servlet.support.RequestContext}
 *    用于路径匹配和/或URI确定。
 */
/**
 * A.
 * Helper class for URL path matching. Provides support for URL paths in
 * {@code RequestDispatcher} includes and support for consistent URL decoding.
 *
 * B.
 * <p>Used by {@link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping}
 * and {@link org.springframework.web.servlet.support.RequestContext} for path matching
 * and/or URI determination.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rossen Stoyanchev
 * @since 14.01.2004
 * @see #getLookupPathForRequest
 * @see javax.servlet.RequestDispatcher
 */
// 20201221 URL路径匹配的帮助程序类。 为{@code RequestDispatcher}中的URL路径提供支持，并支持一致的URL解码。
public class UrlPathHelper {

	/**
	 * Name of Servlet request attribute that holds a
	 * {@link #getLookupPathForRequest resolved} lookupPath.
	 * @since 5.3
	 */
	// 20201221 包含{@link #getLookupPathForRequest} lookupPath已解决的Servlet请求属性的名称。=> "org.springframework.web.util.UrlPathHelper.path"
	public static final String PATH_ATTRIBUTE = UrlPathHelper.class.getName() + ".path";

	private static boolean isServlet4Present =
			ClassUtils.isPresent("javax.servlet.http.HttpServletMapping",
					UrlPathHelper.class.getClassLoader());

	/**
	 * Special WebSphere request attribute, indicating the original request URI.
	 * Preferable over the standard Servlet 2.4 forward attribute on WebSphere,
	 * simply because we need the very first URI in the request forwarding chain.
	 */
	// 20201223 特殊的WebSphere请求属性，指示原始请求URI。 比起WebSphere上的标准Servlet 2.4转发属性，它更可取，因为我们需要请求转发链中的第一个URI。
	private static final String WEBSPHERE_URI_ATTRIBUTE = "com.ibm.websphere.servlet.uri_non_decoded";

	private static final Log logger = LogFactory.getLog(UrlPathHelper.class);

	@Nullable
	static volatile Boolean websphereComplianceFlag;

	// 20201221 是否始终使用完整路径, 默认为false
	private boolean alwaysUseFullPath = false;

	// 20201221 是否允许URL解码, 默认为true
	private boolean urlDecode = true;

	// 20201221 是否需要移除;号内容, 默认为true
	private boolean removeSemicolonContent = true;

	private String defaultEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

	private boolean readOnly = false;


	/**
	 * Whether URL lookups should always use the full path within the current
	 * web application context, i.e. within
	 * {@link javax.servlet.ServletContext#getContextPath()}.
	 * <p>If set to {@literal false} the path within the current servlet mapping
	 * is used instead if applicable (i.e. in the case of a prefix based Servlet
	 * mapping such as "/myServlet/*").
	 * <p>By default this is set to "false".
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		checkReadOnly();
		this.alwaysUseFullPath = alwaysUseFullPath;
	}

	/**
	 * Whether the context path and request URI should be decoded -- both of
	 * which are returned <i>undecoded</i> by the Servlet API, in contrast to
	 * the servlet path.
	 * <p>Either the request encoding or the default Servlet spec encoding
	 * (ISO-8859-1) is used when set to "true".
	 * <p>By default this is set to {@literal true}.
	 * <p><strong>Note:</strong> Be aware the servlet path will not match when
	 * compared to encoded paths. Therefore use of {@code urlDecode=false} is
	 * not compatible with a prefix-based Servlet mapping and likewise implies
	 * also setting {@code alwaysUseFullPath=true}.
	 * @see #getServletPath
	 * @see #getContextPath
	 * @see #getRequestUri
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 * @see ServletRequest#getCharacterEncoding()
	 * @see URLDecoder#decode(String, String)
	 */
	public void setUrlDecode(boolean urlDecode) {
		checkReadOnly();
		this.urlDecode = urlDecode;
	}

	/**
	 * Whether to decode the request URI when determining the lookup path.
	 * @since 4.3.13
	 */
	public boolean isUrlDecode() {
		return this.urlDecode;
	}

	/**
	 * Set if ";" (semicolon) content should be stripped from the request URI.
	 * <p>Default is "true".
	 */
	public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
		checkReadOnly();
		this.removeSemicolonContent = removeSemicolonContent;
	}

	/**
	 * Whether configured to remove ";" (semicolon) content from the request URI.
	 */
	// 20201221 是否配置为删除“;” （分号）来自请求URI的内容。
	public boolean shouldRemoveSemicolonContent() {
		checkReadOnly();
		return this.removeSemicolonContent;
	}

	/**
	 * Set the default character encoding to use for URL decoding.
	 * Default is ISO-8859-1, according to the Servlet spec.
	 * <p>If the request specifies a character encoding itself, the request
	 * encoding will override this setting. This also allows for generically
	 * overriding the character encoding in a filter that invokes the
	 * {@code ServletRequest.setCharacterEncoding} method.
	 * @param defaultEncoding the character encoding to use
	 * @see #determineEncoding
	 * @see ServletRequest#getCharacterEncoding()
	 * @see ServletRequest#setCharacterEncoding(String)
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		checkReadOnly();
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Return the default character encoding to use for URL decoding.
	 */
	protected String getDefaultEncoding() {
		return this.defaultEncoding;
	}

	/**
	 * Switch to read-only mode where further configuration changes are not allowed.
	 */
	private void setReadOnly() {
		this.readOnly = true;
	}

	private void checkReadOnly() {
		Assert.isTrue(!this.readOnly, "This instance cannot be modified");
	}

	/**
	 * 20201221
	 * {@link #getLookupPathForRequest Resolve} lookupPath并将其缓存在带有键{@link #PATH_ATTRIBUTE}的请求属性中，以便随后通过{@link #getResolvedLookupPath（ServletRequest）}访问。
	 */
	/**
	 * {@link #getLookupPathForRequest Resolve} the lookupPath and cache it in a
	 * a request attribute with the key {@link #PATH_ATTRIBUTE} for subsequent
	 * access via {@link #getResolvedLookupPath(ServletRequest)}.
	 * @param request the current request
	 * @return the resolved path
	 * @since 5.3
	 */
	// 20201221 lookupPath并将其缓存在带有键{@link #PATH_ATTRIBUTE}的请求属性中
	public String resolveAndCacheLookupPath(HttpServletRequest request) {
		// 20201221 返回给定请求的映射查找路径 => eg: "/testController/testRequestMapping"
		String lookupPath = getLookupPathForRequest(request);

		// 20201221 包含{@link #getLookupPathForRequest} lookupPath已解决的Servlet请求属性的名称。=> "org.springframework.web.util.UrlPathHelper.path"
		request.setAttribute(PATH_ATTRIBUTE, lookupPath);

		// 20201221 => eg: "/testController/testRequestMapping"
		return lookupPath;
	}

	/**
	 * Return a previously {@link #getLookupPathForRequest resolved} lookupPath.
	 * @param request the current request
	 * @return the previously resolved lookupPath
	 * @throws IllegalArgumentException if the not found
	 * @since 5.3
	 */
	// 20201221 返回以前的{@link #getLookupPathForRequest} 已解决的lookupPath。
	public static String getResolvedLookupPath(ServletRequest request) {
		// 20201221 eg: "org.springframework.web.util.UrlPathHelper.path" -> "/testController/testRequestMapping"
		String lookupPath = (String) request.getAttribute(PATH_ATTRIBUTE);
		Assert.notNull(lookupPath, "Expected lookupPath in request attribute \"" + PATH_ATTRIBUTE + "\".");
		return lookupPath;
	}

	/**
	 * Variant of {@link #getLookupPathForRequest(HttpServletRequest)} that
	 * automates checking for a previously computed lookupPath saved as a
	 * request attribute. The attribute is only used for lookup purposes.
	 * @param request current HTTP request
	 * @param name the request attribute that holds the lookupPath
	 * @return the lookup path
	 * @since 5.2
	 * @deprecated as of 5.3 in favor of using
	 * {@link #resolveAndCacheLookupPath(HttpServletRequest)} and
	 * {@link #getResolvedLookupPath(ServletRequest)}.
	 */
	@Deprecated
	public String getLookupPathForRequest(HttpServletRequest request, @Nullable String name) {
		String result = null;
		if (name != null) {
			result = (String) request.getAttribute(name);
		}
		return (result != null ? result : getLookupPathForRequest(request));
	}

	/**
	 * 20201221
	 * A. 返回给定请求的映射查找路径，如果适用，则在当前Servlet映射中，否则返回Web应用程序中的映射查找路径。
	 * B. 如果在RequestDispatcher包含中调用，则检测包含请求URL。
	 */
	/**
	 * A.
	 * Return the mapping lookup path for the given request, within the current
	 * servlet mapping if applicable, else within the web application.
	 *
	 * B.
	 * <p>Detects include request URL if called within a RequestDispatcher include.
	 * @param request current HTTP request
	 * @return the lookup path
	 * @see #getPathWithinServletMapping
	 * @see #getPathWithinApplication
	 */
	// 20201221 返回给定请求的映射查找路径
	public String getLookupPathForRequest(HttpServletRequest request) {
		// 20201221 返回Web应用程序中给定请求的路径 => eg: "/testController/testRequestMapping"
		String pathWithinApp = getPathWithinApplication(request);

		// 20201221 始终在当前servlet上下文中使用完整路径吗？
		// Always use full path within current servlet context?
		// 20201221 是否始终使用完整路径, 默认为false => eg:true ||
		if (this.alwaysUseFullPath || skipServletPathDetermination(request)) {
			// 20201221 > eg: "/testController/testRequestMapping"
			return pathWithinApp;
		}
		// Else, use path within current servlet mapping if applicable
		String rest = getPathWithinServletMapping(request, pathWithinApp);
		if (StringUtils.hasLength(rest)) {
			return rest;
		}
		else {
			return pathWithinApp;
		}
	}

	private boolean skipServletPathDetermination(HttpServletRequest request) {
		if (isServlet4Present) {
			if (request.getHttpServletMapping().getMappingMatch() != null) {
				return !request.getHttpServletMapping().getMappingMatch().equals(MappingMatch.PATH) ||
						request.getHttpServletMapping().getPattern().equals("/*");
			}
		}
		return false;
	}

	/**
	 * Return the path within the servlet mapping for the given request,
	 * i.e. the part of the request's URL beyond the part that called the servlet,
	 * or "" if the whole URL has been used to identify the servlet.
	 * @param request current HTTP request
	 * @return the path within the servlet mapping, or ""
	 * @see #getPathWithinServletMapping(HttpServletRequest, String)
	 */
	public String getPathWithinServletMapping(HttpServletRequest request) {
		return getPathWithinServletMapping(request, getPathWithinApplication(request));
	}

	/**
	 * Return the path within the servlet mapping for the given request,
	 * i.e. the part of the request's URL beyond the part that called the servlet,
	 * or "" if the whole URL has been used to identify the servlet.
	 * <p>Detects include request URL if called within a RequestDispatcher include.
	 * <p>E.g.: servlet mapping = "/*"; request URI = "/test/a" -> "/test/a".
	 * <p>E.g.: servlet mapping = "/"; request URI = "/test/a" -> "/test/a".
	 * <p>E.g.: servlet mapping = "/test/*"; request URI = "/test/a" -> "/a".
	 * <p>E.g.: servlet mapping = "/test"; request URI = "/test" -> "".
	 * <p>E.g.: servlet mapping = "/*.test"; request URI = "/a.test" -> "".
	 * @param request current HTTP request
	 * @param pathWithinApp a precomputed path within the application
	 * @return the path within the servlet mapping, or ""
	 * @since 5.2.9
	 * @see #getLookupPathForRequest
	 */
	protected String getPathWithinServletMapping(HttpServletRequest request, String pathWithinApp) {
		String servletPath = getServletPath(request);
		String sanitizedPathWithinApp = getSanitizedPath(pathWithinApp);
		String path;

		// If the app container sanitized the servletPath, check against the sanitized version
		if (servletPath.contains(sanitizedPathWithinApp)) {
			path = getRemainingPath(sanitizedPathWithinApp, servletPath, false);
		}
		else {
			path = getRemainingPath(pathWithinApp, servletPath, false);
		}

		if (path != null) {
			// Normal case: URI contains servlet path.
			return path;
		}
		else {
			// Special case: URI is different from servlet path.
			String pathInfo = request.getPathInfo();
			if (pathInfo != null) {
				// Use path info if available. Indicates index page within a servlet mapping?
				// e.g. with index page: URI="/", servletPath="/index.html"
				return pathInfo;
			}
			if (!this.urlDecode) {
				// No path info... (not mapped by prefix, nor by extension, nor "/*")
				// For the default servlet mapping (i.e. "/"), urlDecode=false can
				// cause issues since getServletPath() returns a decoded path.
				// If decoding pathWithinApp yields a match just use pathWithinApp.
				path = getRemainingPath(decodeInternal(request, pathWithinApp), servletPath, false);
				if (path != null) {
					return pathWithinApp;
				}
			}
			// Otherwise, use the full servlet path.
			return servletPath;
		}
	}

	/**
	 * 20201221
	 * A. 返回Web应用程序中给定请求的路径。
	 * B. 如果在RequestDispatcher包含中调用，则检测包含请求URL。
	 */
	/**
	 * A.
	 * Return the path within the web application for the given request.
	 *
	 * B.
	 * <p>Detects include request URL if called within a RequestDispatcher include.
	 * @param request current HTTP request
	 * @return the path within the web application
	 * @see #getLookupPathForRequest
	 */
	// 20201221 返回Web应用程序中给定请求的路径
	public String getPathWithinApplication(HttpServletRequest request) {
		// 20201221 返回给定请求的上下文路径 => eg: 根上下文返回""
		String contextPath = getContextPath(request);

		// 20201221 返回给定请求的请求URI, 可消除错误的“;”或者“; jsessionid” => eg: "/testController/testRequestMapping"
		String requestUri = getRequestUri(request);

		// 20201221 将给定的“映射”匹配到“ requestUri”的开头，如果匹配则返回多余的部分 => eg: "/testController/testRequestMapping"
		String path = getRemainingPath(requestUri, contextPath, true);

		// 20201221 => eg: "/testController/testRequestMapping"
		if (path != null) {
			// 20201221 正常情况：URI包含上下文路径。=> eg: "/testController/testRequestMapping"
			// Normal case: URI contains context path.
			return (StringUtils.hasText(path) ? path : "/");
		}
		else {
			return requestUri;
		}
	}

	/**
	 * 20201221
	 * 将给定的“映射”匹配到“ requestUri”的开头，如果匹配则返回多余的部分。 之所以需要此方法，是因为与requestUri不同，HttpServletRequest返回的上下文路径和servlet路径已去除分号内容。
	 */
	/**
	 * Match the given "mapping" to the start of the "requestUri" and if there
	 * is a match return the extra part. This method is needed because the
	 * context path and the servlet path returned by the HttpServletRequest are
	 * stripped of semicolon content unlike the requestUri.
	 */
	// 20201221 将给定的“映射”匹配到“ requestUri”的开头，如果匹配则返回多余的部分 => eg: "/testController/testRequestMapping"、""、true
	@Nullable
	private String getRemainingPath(String requestUri, String mapping, boolean ignoreCase) {
		int index1 = 0;
		int index2 = 0;
		for (; (index1 < requestUri.length()) && (index2 < mapping.length()); index1++, index2++) {
			char c1 = requestUri.charAt(index1);
			char c2 = mapping.charAt(index2);
			if (c1 == ';') {
				index1 = requestUri.indexOf('/', index1);
				if (index1 == -1) {
					return null;
				}
				c1 = requestUri.charAt(index1);
			}
			if (c1 == c2 || (ignoreCase && (Character.toLowerCase(c1) == Character.toLowerCase(c2)))) {
				continue;
			}
			return null;
		}
		if (index2 != mapping.length()) {
			return null;
		}
		else if (index1 == requestUri.length()) {
			return "";
		}
		else if (requestUri.charAt(index1) == ';') {
			index1 = requestUri.indexOf('/', index1);
		}

		// 20201221 => eg: "/testController/testRequestMapping"
		return (index1 != -1 ? requestUri.substring(index1) : "");
	}

	/**
	 * Sanitize the given path. Uses the following rules:
	 * <ul>
	 * <li>replace all "//" by "/"</li>
	 * </ul>
	 */
	// 20201221 清理给定的路径。 使用以下规则：用“ /”替换所有“ //”
	private static String getSanitizedPath(final String path) {
		int index = path.indexOf("//");
		if (index >= 0) {
			StringBuilder sanitized = new StringBuilder(path);
			while (index != -1) {
				sanitized.deleteCharAt(index);
				index = sanitized.indexOf("//", index);
			}
			return sanitized.toString();
		}
		return path;
	}

	/**
	 * 20201221
	 * A. 返回给定请求的请求URI，如果在RequestDispatcher包含中调用了该请求，则检测包含请求URL。
	 * B. 由于{@code request.getRequestURI（）}返回的值未被servlet容器解码，因此此方法将对其进行解码。
	 * C. Web容器解析的URI应该正确，但是某些容器（如JBoss / Jetty）错误地包含“;”。 字符串，例如URI中的“; jsessionid”。 此方法可消除此类不正确的附录。
	 */
	/**
	 * A.
	 * Return the request URI for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 *
	 * B.
	 * <p>As the value returned by {@code request.getRequestURI()} is <i>not</i>
	 * decoded by the servlet container, this method will decode it.
	 *
	 * C.
	 * <p>The URI that the web container resolves <i>should</i> be correct, but some
	 * containers like JBoss/Jetty incorrectly include ";" strings like ";jsessionid"
	 * in the URI. This method cuts off such incorrect appendices.
	 * @param request current HTTP request
	 * @return the request URI
	 */
	// 20201221 返回给定请求的请求URI, 可消除错误的“;”或者“; jsessionid”
	public String getRequestUri(HttpServletRequest request) {
		// 20201221 获取包含请求URI的标准Servlet 2.3+ spec请求属性 "javax.servlet.include.request_uri" => eg: null
		String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		if (uri == null) {
			// 20201221 返回此请求的URL的一部分，从协议名称到HTTP请求第一行中的查询字符串 => eg: "/testController/testRequestMapping"
			uri = request.getRequestURI();
		}

		// 20201221 解码提供的URI字符串，并在';'之后去除任何多余的部分。 => eg: "/testController/testRequestMapping"
		return decodeAndCleanUriString(request, uri);
	}

	/**
	 * 20201221
	 * A. 返回给定请求的上下文路径，如果在RequestDispatcher包含中调用了包含请求URL，则检测到该请求。
	 * B. 由于{@code request.getContextPath（）}返回的值未被Servlet容器解码，因此此方法将对其进行解码。
	 */
	/**
	 * A.
	 * Return the context path for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 *
	 * B.
	 * <p>As the value returned by {@code request.getContextPath()} is <i>not</i>
	 * decoded by the servlet container, this method will decode it.
	 * @param request current HTTP request
	 * @return the context path
	 */
	// 20201221 返回给定请求的上下文路径 eg: 根上下文返回""
	public String getContextPath(HttpServletRequest request) {
		// 20201221 用于包含上下文路径的标准Servlet 2.3+规范请求属性 "javax.servlet.include.context_path" => eg: null
		String contextPath = (String) request.getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
		if (contextPath == null) {
			// 20201221 返回请求URI中指示请求上下文的部分, eg: 根上下文返回""
			contextPath = request.getContextPath();
		}
		if (StringUtils.matchesCharacter(contextPath, '/')) {
			// Invalid case, but happens for includes on Jetty: silently adapt it.
			contextPath = "";
		}

		// 20201221 使用URLDecoder解码给定的源字符串 -> 编码将从请求中获取，并使用默认的“ ISO-8859-1”
		return decodeRequestString(request, contextPath);
	}

	/**
	 * Return the servlet path for the given request, regarding an include request
	 * URL if called within a RequestDispatcher include.
	 * <p>As the value returned by {@code request.getServletPath()} is already
	 * decoded by the servlet container, this method will not attempt to decode it.
	 * @param request current HTTP request
	 * @return the servlet path
	 */
	public String getServletPath(HttpServletRequest request) {
		String servletPath = (String) request.getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
		if (servletPath == null) {
			servletPath = request.getServletPath();
		}
		if (servletPath.length() > 1 && servletPath.endsWith("/") && shouldRemoveTrailingServletPathSlash(request)) {
			// On WebSphere, in non-compliant mode, for a "/foo/" case that would be "/foo"
			// on all other servlet containers: removing trailing slash, proceeding with
			// that remaining slash as final lookup path...
			servletPath = servletPath.substring(0, servletPath.length() - 1);
		}
		return servletPath;
	}


	/**
	 * Return the request URI for the given request. If this is a forwarded request,
	 * correctly resolves to the request URI of the original request.
	 */
	// 20201223 返回给定请求的请求URI。 如果这是转发的请求，则正确解析为原始请求的请求URI。
	public String getOriginatingRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WEBSPHERE_URI_ATTRIBUTE);
		if (uri == null) {
			uri = (String) request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
			if (uri == null) {
				uri = request.getRequestURI();
			}
		}
		return decodeAndCleanUriString(request, uri);
	}

	/**
	 * Return the context path for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 * <p>As the value returned by {@code request.getContextPath()} is <i>not</i>
	 * decoded by the servlet container, this method will decode it.
	 * @param request current HTTP request
	 * @return the context path
	 */
	public String getOriginatingContextPath(HttpServletRequest request) {
		String contextPath = (String) request.getAttribute(WebUtils.FORWARD_CONTEXT_PATH_ATTRIBUTE);
		if (contextPath == null) {
			contextPath = request.getContextPath();
		}
		return decodeRequestString(request, contextPath);
	}

	/**
	 * Return the servlet path for the given request, detecting an include request
	 * URL if called within a RequestDispatcher include.
	 * @param request current HTTP request
	 * @return the servlet path
	 */
	public String getOriginatingServletPath(HttpServletRequest request) {
		String servletPath = (String) request.getAttribute(WebUtils.FORWARD_SERVLET_PATH_ATTRIBUTE);
		if (servletPath == null) {
			servletPath = request.getServletPath();
		}
		return servletPath;
	}

	/**
	 * Return the query string part of the given request's URL. If this is a forwarded request,
	 * correctly resolves to the query string of the original request.
	 * @param request current HTTP request
	 * @return the query string
	 */
	public String getOriginatingQueryString(HttpServletRequest request) {
		if ((request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE) != null) ||
			(request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE) != null)) {
			return (String) request.getAttribute(WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE);
		}
		else {
			return request.getQueryString();
		}
	}

	/**
	 * Decode the supplied URI string and strips any extraneous portion after a ';'.
	 */
	// 20201221 解码提供的URI字符串，并在';'之后去除任何多余的部分。
	private String decodeAndCleanUriString(HttpServletRequest request, String uri) {
		// 20201221 删除分号内容
		uri = removeSemicolonContent(uri);

		// 20201221 使用URLDecoder解码给定的源字符串 -> 编码将从请求中获取，并使用默认的“ ISO-8859-1”
		uri = decodeRequestString(request, uri);

		// 20201221 清理给定的路径。 使用以下规则：用“ /”替换所有“ //”
		uri = getSanitizedPath(uri);
		return uri;
	}

	/**
	 * 20201221
	 * A. 使用URLDecoder解码给定的源字符串。 编码将从请求中获取，并使用默认的“ ISO-8859-1”。
	 * B. 默认实现使用{@code URLDecoder.decode（input，enc）}。
	 */
	/**
	 * A.
	 * Decode the given source string with a URLDecoder. The encoding will be taken
	 * from the request, falling back to the default "ISO-8859-1".
	 *
	 * B.
	 * <p>The default implementation uses {@code URLDecoder.decode(input, enc)}.
	 * @param request current HTTP request
	 * @param source the String to decode
	 * @return the decoded String
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 * @see ServletRequest#getCharacterEncoding
	 * @see URLDecoder#decode(String, String)
	 * @see URLDecoder#decode(String)
	 */
	// 20201221 使用URLDecoder解码给定的源字符串 -> 编码将从请求中获取，并使用默认的“ ISO-8859-1”
	public String decodeRequestString(HttpServletRequest request, String source) {
		// 20201221 是否允许URL解码, 默认为true
		if (this.urlDecode) {
			// 20201221 解码给定的已编码URI组件
			return decodeInternal(request, source);
		}
		return source;
	}

	// 20201221 解码给定的已编码URI组件
	@SuppressWarnings("deprecation")
	private String decodeInternal(HttpServletRequest request, String source) {
		// 20201221 确定给定请求的编码
		String enc = determineEncoding(request);
		try {
			// 20201221 解码给定的已编码URI组件
			return UriUtils.decode(source, enc);
		}
		catch (UnsupportedCharsetException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not decode request string [" + source + "] with encoding '" + enc +
						"': falling back to platform default encoding; exception message: " + ex.getMessage());
			}
			return URLDecoder.decode(source);
		}
	}

	/**
	 * 20201221
	 * A. 确定给定请求的编码。 可以在子类中覆盖。
	 * B. 默认实现检查请求编码，并回退为此解析器指定的默认编码。
	 */
	/**
	 * A.
	 * Determine the encoding for the given request.
	 * Can be overridden in subclasses.
	 *
	 * B.
	 * <p>The default implementation checks the request encoding,
	 * falling back to the default encoding specified for this resolver.
	 * @param request current HTTP request
	 * @return the encoding for the request (never {@code null})
	 * @see ServletRequest#getCharacterEncoding()
	 * @see #setDefaultEncoding
	 */
	// 20201221 确定给定请求的编码
	protected String determineEncoding(HttpServletRequest request) {
		String enc = request.getCharacterEncoding();
		if (enc == null) {
			enc = getDefaultEncoding();
		}
		return enc;
	}

	/**
	 * 20201221
	 * 去掉 ”;” 如果{@linkplain #setRemoveSemicolonContent removeSemicolonContent}属性设置为“ true”，则来自给定请求URI的（分号）内容。 注意，“ jsessionid”始终被删除。
	 */
	/**
	 * Remove ";" (semicolon) content from the given request URI if the
	 * {@linkplain #setRemoveSemicolonContent removeSemicolonContent}
	 * property is set to "true". Note that "jsessionid" is always removed.
	 * @param requestUri the request URI string to remove ";" content from
	 * @return the updated URI string
	 */
	// 20201221 删除分号内容
	public String removeSemicolonContent(String requestUri) {
		// 20201221 是否需要移除;号内容, 默认为true
		return (this.removeSemicolonContent ?
				// 20201221 删除给定请求URI的（分号）内容
				removeSemicolonContentInternal(requestUri) :

				// 20201221 删除;jsessionid=
				removeJsessionid(requestUri));
	}

	// 20201221 删除给定请求URI的（分号）内容
	private static String removeSemicolonContentInternal(String requestUri) {
		int semicolonIndex = requestUri.indexOf(';');
		if (semicolonIndex == -1) {
			return requestUri;
		}
		StringBuilder sb = new StringBuilder(requestUri);
		while (semicolonIndex != -1) {
			int slashIndex = sb.indexOf("/", semicolonIndex + 1);
			if (slashIndex == -1) {
				return sb.substring(0, semicolonIndex);
			}
			sb.delete(semicolonIndex, slashIndex);
			semicolonIndex = sb.indexOf(";", semicolonIndex);
		}
		return sb.toString();
	}

	// 20201221 删除;jsessionid=
	private String removeJsessionid(String requestUri) {
		String key = ";jsessionid=";
		int index = requestUri.toLowerCase().indexOf(key);
		if (index == -1) {
			return requestUri;
		}
		String start = requestUri.substring(0, index);
		for (int i = key.length(); i < requestUri.length(); i++) {
			char c = requestUri.charAt(i);
			if (c == ';' || c == '/') {
				return start + requestUri.substring(i);
			}
		}
		return start;
	}

	/**
	 * 20201221
	 * 除非将{@link #setUrlDecode}设置为{@code true}，否则通过{@link #decodeRequestString}解码给定的URI路径变量，在这种情况下，假定通过调用，已经解码了从中提取变量的URL路径。
	 * {@link #getLookupPathForRequest（HttpServletRequest）}。
	 */
	/**
	 * Decode the given URI path variables via {@link #decodeRequestString} unless
	 * {@link #setUrlDecode} is set to {@code true} in which case it is assumed
	 * the URL path from which the variables were extracted is already decoded
	 * through a call to {@link #getLookupPathForRequest(HttpServletRequest)}.
	 * @param request current HTTP request
	 * @param vars the URI variables extracted from the URL path
	 * @return the same Map or a new Map instance
	 */
	// 20201221 通过{@link #decodeRequestString}解码给定的URI路径变量
	public Map<String, String> decodePathVariables(HttpServletRequest request, Map<String, String> vars) {
		// 20201221 是否允许URL解码, 默认为true
		if (this.urlDecode) {
			return vars;
		}
		else {
			Map<String, String> decodedVars = CollectionUtils.newLinkedHashMap(vars.size());
			vars.forEach((key, value) -> decodedVars.put(key, decodeInternal(request, value)));
			return decodedVars;
		}
	}

	/**
	 * Decode the given matrix variables via {@link #decodeRequestString} unless
	 * {@link #setUrlDecode} is set to {@code true} in which case it is assumed
	 * the URL path from which the variables were extracted is already decoded
	 * through a call to {@link #getLookupPathForRequest(HttpServletRequest)}.
	 * @param request current HTTP request
	 * @param vars the URI variables extracted from the URL path
	 * @return the same Map or a new Map instance
	 */
	public MultiValueMap<String, String> decodeMatrixVariables(
			HttpServletRequest request, MultiValueMap<String, String> vars) {

		if (this.urlDecode) {
			return vars;
		}
		else {
			MultiValueMap<String, String> decodedVars = new LinkedMultiValueMap<>(vars.size());
			vars.forEach((key, values) -> {
				for (String value : values) {
					decodedVars.add(key, decodeInternal(request, value));
				}
			});
			return decodedVars;
		}
	}

	private boolean shouldRemoveTrailingServletPathSlash(HttpServletRequest request) {
		if (request.getAttribute(WEBSPHERE_URI_ATTRIBUTE) == null) {
			// Regular servlet container: behaves as expected in any case,
			// so the trailing slash is the result of a "/" url-pattern mapping.
			// Don't remove that slash.
			return false;
		}
		Boolean flagToUse = websphereComplianceFlag;
		if (flagToUse == null) {
			ClassLoader classLoader = UrlPathHelper.class.getClassLoader();
			String className = "com.ibm.ws.webcontainer.WebContainer";
			String methodName = "getWebContainerProperties";
			String propName = "com.ibm.ws.webcontainer.removetrailingservletpathslash";
			boolean flag = false;
			try {
				Class<?> cl = classLoader.loadClass(className);
				Properties prop = (Properties) cl.getMethod(methodName).invoke(null);
				flag = Boolean.parseBoolean(prop.getProperty(propName));
			}
			catch (Throwable ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not introspect WebSphere web container properties: " + ex);
				}
			}
			flagToUse = flag;
			websphereComplianceFlag = flag;
		}
		// Don't bother if WebSphere is configured to be fully Servlet compliant.
		// However, if it is not compliant, do remove the improper trailing slash!
		return !flagToUse;
	}

	/**
	 * 20201221
	 * A. 具有默认值的共享只读实例。 以下内容适用：
	 * 		a. {@code alwaysUseFullPath = false}
	 * 		b. {@code urlDecode = true}
	 * 		c. {@code removeSemicolon = true}
	 * 		d. {@code defaultEncoding =} {@ link WebUtils＃DEFAULT_CHARACTER_ENCODING}
	 */
	/**
	 * A.
	 * Shared, read-only instance with defaults. The following apply:
	 * <ul>
	 * a.
	 * <li>{@code alwaysUseFullPath=false}
	 *
	 * b.
	 * <li>{@code urlDecode=true}
	 *
	 * c.
	 * <li>{@code removeSemicolon=true}
	 *
	 * d.
	 * <li>{@code defaultEncoding=}{@link WebUtils#DEFAULT_CHARACTER_ENCODING}
	 * </ul>
	 */
	// 20201221 具有默认值的共享只读实例
	public static final UrlPathHelper defaultInstance = new UrlPathHelper();
	static {
		defaultInstance.setReadOnly();
	}

	/**
	 * 20201223
	 * A. 完整的编码路径的共享只读实例。 以下内容适用：
	 * 		a. {@code alwaysUseFullPath=true}
	 * 		b. {@code urlDecode=false}
	 * 		c. {@code removeSemicolon=false}
	 * 		d. {@code defaultEncoding=}{@link WebUtils#DEFAULT_CHARACTER_ENCODING}
	 */
	/**
	 * A.
	 * Shared, read-only instance for the full, encoded path. The following apply:
	 * <ul>
	 * a.
	 * <li>{@code alwaysUseFullPath=true}
	 *
	 * b.
	 * <li>{@code urlDecode=false}
	 *
	 * c.
	 * <li>{@code removeSemicolon=false}
	 *
	 * d.
	 * <li>{@code defaultEncoding=}{@link WebUtils#DEFAULT_CHARACTER_ENCODING}
	 * </ul>
	 */
	// 20201223 完整的编码路径的共享只读实例
	public static final UrlPathHelper rawPathInstance = new UrlPathHelper() {

		@Override
		public String removeSemicolonContent(String requestUri) {
			return requestUri;
		}
	};

	static {
		rawPathInstance.setAlwaysUseFullPath(true);
		rawPathInstance.setUrlDecode(false);
		rawPathInstance.setRemoveSemicolonContent(false);
		rawPathInstance.setReadOnly();
	}

}
