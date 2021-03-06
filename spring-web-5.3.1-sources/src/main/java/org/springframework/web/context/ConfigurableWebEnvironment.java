/*
 * Copyright 2002-2012 the original author or authors.
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;

/**
 * Specialization of {@link ConfigurableEnvironment} allowing initialization of
 * servlet-related {@link org.springframework.core.env.PropertySource} objects at the
 * earliest moment that the {@link ServletContext} and (optionally) {@link ServletConfig}
 * become available.
 *
 * @author Chris Beams
 * @since 3.1.2
 * @see ConfigurableWebApplicationContext#getEnvironment()
 */
// 20201202 {@link ConfigurableEnvironment}的专门化允许初始化与servlet相关的{@linkorg.springframework.core.env.PropertySource}对象,
// 20201202 在{@link ServletContext}和（可选）{@link ServletConfig}可用的最早时刻。
public interface ConfigurableWebEnvironment extends ConfigurableEnvironment {

	/**
	 * // 20201213 替换任何{@linkplain org.springframework.core.env.PropertySource.StubPropertySource stub属性source实例使用给定的参数充当具有
	 *   实际servlet上下文/配置属性源的占位符。
	 * Replace any {@linkplain
	 * org.springframework.core.env.PropertySource.StubPropertySource stub property source}
	 * instances acting as placeholders with real servlet context/config property sources
	 * using the given parameters.
	 * @param servletContext the {@link ServletContext} (may not be {@code null})
	 * @param servletConfig the {@link ServletConfig} ({@code null} if not available)
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#initServletPropertySources(
	 * org.springframework.core.env.MutablePropertySources, ServletContext, ServletConfig)
	 */
	// 20201213 替换任何StubPropertySource属性, 使用给定的参数充当具有实际servlet上下文/配置属性源的占位符
	void initPropertySources(@Nullable ServletContext servletContext, @Nullable ServletConfig servletConfig);

}
