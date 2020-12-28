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

package org.springframework.boot.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

/**
 * 20201228
 * A. 基于Servlet 3.0+的注册Bean的基类。
 */
/**
 * A.
 * Base class for Servlet 3.0+ based registration beans.
 *
 * @author Phillip Webb
 * @since 1.4.0
 * @see ServletRegistrationBean
 * @see FilterRegistrationBean
 * @see DelegatingFilterProxyRegistrationBean
 * @see ServletListenerRegistrationBean
 */
// 20201228 基于Servlet 3.0+的注册Bean的基类。
public abstract class RegistrationBean implements ServletContextInitializer, Ordered {

	private static final Log logger = LogFactory.getLog(RegistrationBean.class);

	private int order = Ordered.LOWEST_PRECEDENCE;

	private boolean enabled = true;

	// 20201213 使用初始化所需的所有Servlet，过滤器，侦听器上下文参数和属性配置给定的{@link ServletContext}。
	@Override
	public final void onStartup(ServletContext servletContext) throws ServletException {
		// 20201228 返回注册说明 => eg: "servlet dispatcherServlet"
		String description = getDescription();
		if (!isEnabled()) {
			logger.info(StringUtils.capitalize(description) + " was not registered (disabled)");
			return;
		}

		// 20201228 在servlet上下文中注册此bean。
		register(description, servletContext);
	}

	/**
	 * Return a description of the registration. For example "Servlet resourceServlet"
	 * @return a description of the registration
	 */
	// 20201228 返回注册说明。 例如“ Servlet resourceServlet”
	protected abstract String getDescription();

	/**
	 * Register this bean with the servlet context.
	 * @param description a description of the item being registered
	 * @param servletContext the servlet context
	 */
	// 20201228 在servlet上下文中注册此bean。
	protected abstract void register(String description, ServletContext servletContext);

	/**
	 * Flag to indicate that the registration is enabled.
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Return if the registration is enabled.
	 * @return if enabled (default {@code true})
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Set the order of the registration bean.
	 * @param order the order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Get the order of the registration bean.
	 * @return the order
	 */
	@Override
	public int getOrder() {
		return this.order;
	}

}
