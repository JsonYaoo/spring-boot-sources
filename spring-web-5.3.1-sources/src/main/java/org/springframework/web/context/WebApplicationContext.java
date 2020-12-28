/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

/**
 * 20201204
 * A. 提供Web应用程序配置的接口。 在应用程序运行时，它是只读的，但是如果实现支持，则可以重新加载。
 * B. 此接口向通用ApplicationContext接口添加一个{@code getServletContext（）}方法，并定义一个众所周知的应用程序属性名称，在引导过程中根上下文必须绑定到该名称。
 * C. 像通用应用程序上下文一样，Web应用程序上下文是分层的。 每个应用程序只有一个根上下文，而应用程序中的每个servlet（包括MVC框架中的调度程序servlet）都有自己的子上下文。
 * D. 除了标准的应用程序上下文生命周期功能外，WebApplicationContext实现还需要检测{@link ServletContextAware} bean，并相应地调用{@code setServletContext}方法。
 */
/**
 * A.
 * Interface to provide configuration for a web application. This is read-only while
 * the application is running, but may be reloaded if the implementation supports this.
 *
 * B.
 * <p>This interface adds a {@code getServletContext()} method to the generic
 * ApplicationContext interface, and defines a well-known application attribute name
 * that the root context must be bound to in the bootstrap process.
 *
 * C.
 * <p>Like generic application contexts, web application contexts are hierarchical.
 * There is a single root context per application, while each servlet in the application
 * (including a dispatcher servlet in the MVC framework) has its own child context.
 *
 * D.
 * <p>In addition to standard application context lifecycle capabilities,
 * WebApplicationContext implementations need to detect {@link ServletContextAware}
 * beans and invoke the {@code setServletContext} method accordingly.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 19, 2001
 * @see ServletContextAware#setServletContext
 */
// 20201204 此接口向通用ApplicationContext接口添加一个{@code getServletContext（）}方法，并定义一个众所周知的应用程序属性名称，在引导过程中根上下文必须绑定到该名称。
public interface WebApplicationContext extends ApplicationContext {

	/**
	 * Context attribute to bind root WebApplicationContext to on successful startup.
	 * <p>Note: If the startup of the root context fails, this attribute can contain
	 * an exception or error as value. Use WebApplicationContextUtils for convenient
	 * lookup of the root WebApplicationContext.
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getWebApplicationContext
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext
	 */
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	/**
	 * Scope identifier for request scope: "request".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_REQUEST = "request";

	/**
	 * Scope identifier for session scope: "session".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_SESSION = "session";

	/**
	 * Scope identifier for the global web application scope: "application".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	// 20201228 全局Web应用程序范围的范围标识符：“应用程序”。 除了标准范围“ singleton”和“ prototype”外，还受支持。
	String SCOPE_APPLICATION = "application";

	/**
	 * Name of the ServletContext environment bean in the factory.
	 * @see ServletContext
	 */
	// 20201228 工厂中ServletContext环境Bean的名称。
	String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

	/**
	 * 20201228
	 * A. 工厂中ServletContext init-params环境Bean的名称。
	 * B. 注意：可能与ServletConfig参数合并。 ServletConfig参数覆盖同名的ServletContext参数。
	 */
	/**
	 * A.
	 * Name of the ServletContext init-params environment bean in the factory.
	 *
	 * B.
	 * <p>Note: Possibly merged with ServletConfig parameters.
	 * ServletConfig parameters override ServletContext parameters of the same name.
	 * @see ServletContext#getInitParameterNames()
	 * @see ServletContext#getInitParameter(String)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 * @see javax.servlet.ServletConfig#getInitParameter(String)
	 */
	// 20201228 工厂中ServletContext init-params环境Bean的名称
	String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

	/**
	 * Name of the ServletContext attributes environment bean in the factory.
	 * @see ServletContext#getAttributeNames()
	 * @see ServletContext#getAttribute(String)
	 */
	String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";


	/**
	 * Return the standard Servlet API ServletContext for this application.
	 */
	@Nullable
	ServletContext getServletContext();

}
