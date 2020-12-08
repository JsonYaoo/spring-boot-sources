/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.context.event;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Event published when a {@link SpringApplication} is starting up and the
 * {@link Environment} is first available for inspection and modification.
 *
 * @author Dave Syer
 * @since 1.0.0
 */
// 20201208 在{@link SpringApplication}启动且{@link Environment}可用时, 用于检查和修改发布的事件。
@SuppressWarnings("serial")
public class ApplicationEnvironmentPreparedEvent extends SpringApplicationEvent {

	// 20201208 引导上下文
	private final ConfigurableBootstrapContext bootstrapContext;

	// 20201208 刚创建的配置环境
	private final ConfigurableEnvironment environment;

	/**
	 * Create a new {@link ApplicationEnvironmentPreparedEvent} instance.
	 * @param application the current application
	 * @param args the arguments the application is running with
	 * @param environment the environment that was just created
	 * @deprecated since 2.4.0 in favor of
	 * {@link #ApplicationEnvironmentPreparedEvent(ConfigurableBootstrapContext, SpringApplication, String[], ConfigurableEnvironment)}
	 */
	@Deprecated
	public ApplicationEnvironmentPreparedEvent(SpringApplication application, String[] args,
			ConfigurableEnvironment environment) {
		this(null, application, args, environment);
	}

	/**
	 * Create a new {@link ApplicationEnvironmentPreparedEvent} instance.
	 * @param bootstrapContext the bootstrap context	// 20201208 引导上下文
	 * @param application the current application	// 20201208 当前的应用
	 * @param args the arguments the application is running with	// 20201208 应用程序运行时使用的参数
	 * @param environment the environment that was just created	// 20201208 刚刚创建的环境
	 */
	// 20201208 创建一个新的{@link ApplicationEnvironmentPreparedEvent}实例。
	public ApplicationEnvironmentPreparedEvent(ConfigurableBootstrapContext bootstrapContext,
			SpringApplication application, String[] args, ConfigurableEnvironment environment) {
		// 20201208 构造SpringApplicationEvent
		super(application, args);

		// 20201208 注册引导上下文
		this.bootstrapContext = bootstrapContext;

		// 20201208 注册刚创建的配置环境
		this.environment = environment;
	}

	/**
	 * Return the bootstap context.
	 * @return the bootstrap context
	 * @since 2.4.0
	 */
	public ConfigurableBootstrapContext getBootstrapContext() {
		return this.bootstrapContext;
	}

	/**
	 * Return the environment.
	 * @return the environment
	 */
	public ConfigurableEnvironment getEnvironment() {
		return this.environment;
	}

}
