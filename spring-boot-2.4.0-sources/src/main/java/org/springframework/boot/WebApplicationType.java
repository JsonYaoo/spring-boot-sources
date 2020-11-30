/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot;

import org.springframework.util.ClassUtils;

/**
 * An enumeration of possible types of web application.
 *
 * @author Andy Wilkinson
 * @author Brian Clozel
 * @since 2.0.0
 */
// 202011130 web应用程序可能类型的枚举。
public enum WebApplicationType {

	/**
	 * The application should not run as a web application and should not start an
	 * embedded web server.
	 */
	// 20201130 应用程序不应作为web应用程序运行，也不应启动嵌入式web服务器。
	NONE,

	/**
	 * The application should run as a servlet-based web application and should start an
	 * embedded servlet web server.
	 */
	// 20201130 应用程序应作为基于servlet的web应用程序运行，并应启动嵌入式servlet web服务器。
	SERVLET,

	/**
	 * The application should run as a reactive web application and should start an
	 * embedded reactive web server.
	 */
	// 20201130 应用程序应该作为一个反应式web应用程序运行，并且应该启动一个嵌入式的反应式web服务器。
	REACTIVE;

	private static final String[] SERVLET_INDICATOR_CLASSES = { "javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private static final String WEBMVC_INDICATOR_CLASS = "org.springframework.web.servlet.DispatcherServlet";

	private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";

	private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

	private static final String SERVLET_APPLICATION_CONTEXT_CLASS = "org.springframework.web.context.WebApplicationContext";

	private static final String REACTIVE_APPLICATION_CONTEXT_CLASS = "org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext";

	// 20201130 从Classpath推断出服务器类型
	static WebApplicationType deduceFromClasspath() {
		// 20201130 如果是org.springframework.web.reactive.DispatcherHandler, 且不是org.springframework.web.servlet.DispatcherServlet, 也不是org.glassfish.jersey.servlet.ServletContainer
		// 20201130 则说明为REACTIVE类型
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
			return WebApplicationType.REACTIVE;
		}

		// 20201130 否则如果不是javax.servlet.Servlet, 也不是org.springframework.web.context.ConfigurableWebApplicationContext, 则为NONE类型
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE;
			}
		}

		// 20201130 如果为rg.springframework.web.servlet.DispatcherServlet | org.glassfish.jersey.servlet.ServletContainer | javax.servlet.Servlet | org.springframework.web.context.ConfigurableWebApplicationContext
		// 20201130 则为SERVLET类型
		return WebApplicationType.SERVLET;
	}

	static WebApplicationType deduceFromApplicationContext(Class<?> applicationContextClass) {
		if (isAssignable(SERVLET_APPLICATION_CONTEXT_CLASS, applicationContextClass)) {
			return WebApplicationType.SERVLET;
		}
		if (isAssignable(REACTIVE_APPLICATION_CONTEXT_CLASS, applicationContextClass)) {
			return WebApplicationType.REACTIVE;
		}
		return WebApplicationType.NONE;
	}

	private static boolean isAssignable(String target, Class<?> type) {
		try {
			return ClassUtils.resolveClassName(target, null).isAssignableFrom(type);
		}
		catch (Throwable ex) {
			return false;
		}
	}

}
