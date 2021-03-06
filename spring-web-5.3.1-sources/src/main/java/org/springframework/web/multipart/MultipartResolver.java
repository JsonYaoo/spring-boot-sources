/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.web.multipart;

import javax.servlet.http.HttpServletRequest;

/**
 * 20201221
 * A. 符合<a href="https://www.ietf.org/rfc/rfc1867.txt"> RFC 1867 </a>的多文件上传解决方案的策略接口。 通常，实现既可以在应用程序上下文中使用，也可以独立使用。
 * B. 从Spring 3.1开始，Spring中包含两个具体的实现：
 * 		a. {@link org.springframework.web.multipart.commons.CommonsMultipartResolver} for Apache Commons FileUpload
 * 		b. {@link org.springframework.web.multipart.support.StandardServletMultipartResolver}用于Servlet 3.0+ Part API
 * C. Spring {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlets}没有使用默认的解析程序实现，因为应用程序可能会选择自行解析其多部分请求。
 *    要定义实现，请在{@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet's}应用程序上下文中创建一个ID为“multipartResolver”的bean。
 *    这样的解析器将应用于该{@link org.springframework.web.servlet.DispatcherServlet}处理的所有请求。
 * D. 如果{@link org.springframework.web.servlet.DispatcherServlet}检测到多部分请求，它将通过配置的{@link MultipartResolver}对其进行解析，并传递一个包装好的
 *    {@link HttpServletRequest}。 然后，控制器可以将给定的请求投射到{@link MultipartHttpServletRequest}接口，该接口允许访问任何
 *    {@link MultipartFile MultipartFiles}。 请注意，仅在实际的多部分请求的情况下才支持此转换:
 * 			public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
 *   			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 *   			MultipartFile multipartFile = multipartRequest.getFile("image");
 *   			...
 * 			}
 * E. 代替直接访问，命令或表单控制器可以将{@link org.springframework.web.multipart.support.ByteArrayMultipartFileEditor}或
 *    {@link org.springframework.web.multipart.support.StringMultipartFileEditor}注册为数据绑定器，以自动进行应用多部分内容来形成bean属性。
 * F. 作为将{@link MultipartResolver}与{@link org.springframework.web.servlet.DispatcherServlet}结合使用的替代方法，可以在web.xml中注册一个
 *    {@code {@link org.springframework.web.multipart.support.MultipartFilter}}。 它将委派给根应用程序上下文中的相应{@link MultipartResolver} bean。
 *    这主要用于不使用Spring自己的Web MVC框架的应用程序。
 * G. 注意：几乎不需要从应用程序代码访问{@link MultipartResolver}本身。 它将简单地在后台进行工作，从而使
 *    {@link MultipartHttpServletRequest MultipartHttpServletRequests}对控制器可用。
 */
/**
 * A.
 * A strategy interface for multipart file upload resolution in accordance
 * with <a href="https://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * Implementations are typically usable both within an application context
 * and standalone.
 *
 * B.
 * <p>There are two concrete implementations included in Spring, as of Spring 3.1:
 * <ul>
 * a.
 * <li>{@link org.springframework.web.multipart.commons.CommonsMultipartResolver}
 * for Apache Commons FileUpload
 *
 * b.
 * <li>{@link org.springframework.web.multipart.support.StandardServletMultipartResolver}
 * for the Servlet 3.0+ Part API
 * </ul>
 *
 * C.
 * <p>There is no default resolver implementation used for Spring
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlets},
 * as an application might choose to parse its multipart requests itself. To define
 * an implementation, create a bean with the id "multipartResolver" in a
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet's}
 * application context. Such a resolver gets applied to all requests handled
 * by that {@link org.springframework.web.servlet.DispatcherServlet}.
 *
 * D.
 * <p>If a {@link org.springframework.web.servlet.DispatcherServlet} detects a
 * multipart request, it will resolve it via the configured {@link MultipartResolver}
 * and pass on a wrapped {@link HttpServletRequest}. Controllers
 * can then cast their given request to the {@link MultipartHttpServletRequest}
 * interface, which allows for access to any {@link MultipartFile MultipartFiles}.
 * Note that this cast is only supported in case of an actual multipart request.
 *
 * <pre class="code">
 * public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
 *   MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 *   MultipartFile multipartFile = multipartRequest.getFile("image");
 *   ...
 * }</pre>
 *
 * E.
 * Instead of direct access, command or form controllers can register a
 * {@link org.springframework.web.multipart.support.ByteArrayMultipartFileEditor}
 * or {@link org.springframework.web.multipart.support.StringMultipartFileEditor}
 * with their data binder, to automatically apply multipart content to form
 * bean properties.
 *
 * F.
 * <p>As an alternative to using a {@link MultipartResolver} with a
 * {@link org.springframework.web.servlet.DispatcherServlet},
 * a {@link org.springframework.web.multipart.support.MultipartFilter} can be
 * registered in {@code web.xml}. It will delegate to a corresponding
 * {@link MultipartResolver} bean in the root application context. This is mainly
 * intended for applications that do not use Spring's own web MVC framework.
 *
 * G.
 * <p>Note: There is hardly ever a need to access the {@link MultipartResolver}
 * itself from application code. It will simply do its work behind the scenes,
 * making {@link MultipartHttpServletRequest MultipartHttpServletRequests}
 * available to controllers.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartHttpServletRequest
 * @see MultipartFile
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see org.springframework.web.multipart.support.ByteArrayMultipartFileEditor
 * @see org.springframework.web.multipart.support.StringMultipartFileEditor
 * @see org.springframework.web.servlet.DispatcherServlet
 */
// 20201221 多文件上传解决方案的策略接口: 通常，实现既可以在应用程序上下文中使用，也可以独立使用
public interface MultipartResolver {

	/**
	 * Determine if the given request contains multipart content.
	 * <p>Will typically check for content type "multipart/form-data", but the actually
	 * accepted requests might depend on the capabilities of the resolver implementation.
	 * @param request the servlet request to be evaluated
	 * @return whether the request contains multipart content
	 */
	boolean isMultipart(HttpServletRequest request);

	/**
	 * 20201221
	 * 将给定的HTTP请求解析为多部分文件和参数，并将请求包装在{@link MultipartHttpServletRequest}对象中，该对象提供对文件描述符的访问，并使包含的参数可通过标准
	 * ServletRequest方法访问。
	 */
	/**
	 * Parse the given HTTP request into multipart files and parameters,
	 * and wrap the request inside a
	 * {@link MultipartHttpServletRequest}
	 * object that provides access to file descriptors and makes contained
	 * parameters accessible via the standard ServletRequest methods.
	 * @param request the servlet request to wrap (must be of a multipart content type)
	 * @return the wrapped servlet request
	 * @throws MultipartException if the servlet request is not multipart, or if
	 * implementation-specific problems are encountered (such as exceeding file size limits)
	 * @see MultipartHttpServletRequest#getFile
	 * @see MultipartHttpServletRequest#getFileNames
	 * @see MultipartHttpServletRequest#getFileMap
	 * @see HttpServletRequest#getParameter
	 * @see HttpServletRequest#getParameterNames
	 * @see HttpServletRequest#getParameterMap
	 */
	// 20201221 将给定的HTTP请求解析为多部分文件和参数，并将请求包装在MultipartHttpServletRequest对象中: 提供对文件描述符的访问, 可通过标准ServletRequest方法访问
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * Cleanup any resources used for the multipart handling,
	 * like a storage for the uploaded files.
	 * @param request the request to cleanup resources for
	 */
	void cleanupMultipart(MultipartHttpServletRequest request);

}
