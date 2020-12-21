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

package org.springframework.web.multipart.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.logging.LogFactory;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

/**
 * 20201221
 * A. {@link MultipartResolver}接口的标准实现，基于Servlet 3.0 {@link Part} API。 可以作为“multipartResolver” bean添加到Spring DispatcherServlet上下文中，
 *    而无需在bean级别进行任何额外配置（请参见下文）。
 * B. 注意：为了使用基于Servlet 3.0的多部分解析，您需要在{@code web.xml}中使用“multipart-config”部分或在Programmatic中使用
 *    {@link javax.servlet.MultipartConfigElement}标记受影响的servlet。 Servlet注册，或者（对于自定义Servlet类）可能在Servlet类上带有
 *    {@link javax.servlet.annotation.MultipartConfig}注释。 需要在该servlet注册级别应用配置设置，例如最大大小或存储位置；
 *    Servlet 3.0不允许在MultipartResolver级别上设置它们:
 * 			public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
 *	 			// ...
 *	 			@Override
 *	 			protected void customizeRegistration(ServletRegistration.Dynamic registration) {
 *     				// Optionally also set maxFileSize, maxRequestSize, fileSizeThreshold
 *     				registration.setMultipartConfig(new MultipartConfigElement("/tmp"));
 *   			}
 * 			}
 */
/**
 * A.
 * Standard implementation of the {@link MultipartResolver} interface,
 * based on the Servlet 3.0 {@link Part} API.
 * To be added as "multipartResolver" bean to a Spring DispatcherServlet context,
 * without any extra configuration at the bean level (see below).
 *
 * B.
 * <p><b>Note:</b> In order to use Servlet 3.0 based multipart parsing,
 * you need to mark the affected servlet with a "multipart-config" section in
 * {@code web.xml}, or with a {@link javax.servlet.MultipartConfigElement}
 * in programmatic servlet registration, or (in case of a custom servlet class)
 * possibly with a {@link javax.servlet.annotation.MultipartConfig} annotation
 * on your servlet class. Configuration settings such as maximum sizes or
 * storage locations need to be applied at that servlet registration level;
 * Servlet 3.0 does not allow for them to be set at the MultipartResolver level.
 *
 * <pre class="code">
 * public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
 *	 // ...
 *	 &#064;Override
 *	 protected void customizeRegistration(ServletRegistration.Dynamic registration) {
 *     // Optionally also set maxFileSize, maxRequestSize, fileSizeThreshold
 *     registration.setMultipartConfig(new MultipartConfigElement("/tmp"));
 *   }
 * }
 * </pre>
 *
 * @author Juergen Hoeller
 * @since 3.1
 * @see #setResolveLazily
 * @see HttpServletRequest#getParts()
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 */
// 20201221 基于Servlet 3.0 {@link Part} API: 可以作为“multipartResolver” bean添加到Spring DispatcherServlet上下文中，而无需在bean级别进行任何额外配置（请参见下文）
public class StandardServletMultipartResolver implements MultipartResolver {

	private boolean resolveLazily = false;


	/**
	 * Set whether to resolve the multipart request lazily at the time of
	 * file or parameter access.
	 * <p>Default is "false", resolving the multipart elements immediately, throwing
	 * corresponding exceptions at the time of the {@link #resolveMultipart} call.
	 * Switch this to "true" for lazy multipart parsing, throwing parse exceptions
	 * once the application attempts to obtain multipart files or parameters.
	 * @since 3.2.9
	 */
	public void setResolveLazily(boolean resolveLazily) {
		this.resolveLazily = resolveLazily;
	}


	@Override
	public boolean isMultipart(HttpServletRequest request) {
		return StringUtils.startsWithIgnoreCase(request.getContentType(), "multipart/");
	}

	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		return new StandardMultipartHttpServletRequest(request, this.resolveLazily);
	}

	@Override
	public void cleanupMultipart(MultipartHttpServletRequest request) {
		if (!(request instanceof AbstractMultipartHttpServletRequest) ||
				((AbstractMultipartHttpServletRequest) request).isResolved()) {
			// To be on the safe side: explicitly delete the parts,
			// but only actual file parts (for Resin compatibility)
			try {
				for (Part part : request.getParts()) {
					if (request.getFile(part.getName()) != null) {
						part.delete();
					}
				}
			}
			catch (Throwable ex) {
				LogFactory.getLog(getClass()).warn("Failed to perform cleanup of multipart items", ex);
			}
		}
	}

}
