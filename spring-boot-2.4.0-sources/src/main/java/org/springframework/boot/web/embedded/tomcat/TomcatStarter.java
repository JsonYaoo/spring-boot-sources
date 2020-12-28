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

package org.springframework.boot.web.embedded.tomcat;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.web.servlet.ServletContextInitializer;

/**
 * 20201228
 * A. {@link ServletContainerInitializer}用于触发{@link ServletContextInitializer ServletContextInitializers}并跟踪启动错误。
 */
/**
 * A.
 * {@link ServletContainerInitializer} used to trigger {@link ServletContextInitializer
 * ServletContextInitializers} and track startup errors.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
// 20201228 {@link ServletContainerInitializer}用于触发{@link ServletContextInitializer ServletContextInitializers}并跟踪启动错误
class TomcatStarter implements ServletContainerInitializer {

	private static final Log logger = LogFactory.getLog(TomcatStarter.class);

	private final ServletContextInitializer[] initializers;

	private volatile Exception startUpException;

	TomcatStarter(ServletContextInitializer[] initializers) {
		this.initializers = initializers;
	}

	// 20201228 【重点】在Web应用程序启动期间接收与通过{@link javax.servlet.annotation.HandlesTypes}注释定义的条件相匹配的Web应用程序中的类的通知。
	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
		try {
			// 20201228 eg: ServletContextInitialzer[3]@xxxx: AbstractServletWebServerFactory$lambda@xxxx、AbstractServletWebServerFactory$SessionConfigurationInitializer@xxxx、ServletWebServerApplicationContext$lambda@xxxx
			for (ServletContextInitializer initializer : this.initializers) {
				// 20201213 【重点】使用初始化所需的所有Servlet，过滤器，侦听器上下文参数和属性配置给定的{@link ServletContext}。
				initializer.onStartup(servletContext);
			}
		}
		catch (Exception ex) {
			this.startUpException = ex;
			// Prevent Tomcat from logging and re-throwing when we know we can
			// deal with it in the main thread, but log for information here.
			if (logger.isErrorEnabled()) {
				logger.error("Error starting Tomcat context. Exception: " + ex.getClass().getName() + ". Message: "
						+ ex.getMessage());
			}
		}
	}

	Exception getStartUpException() {
		return this.startUpException;
	}

}
