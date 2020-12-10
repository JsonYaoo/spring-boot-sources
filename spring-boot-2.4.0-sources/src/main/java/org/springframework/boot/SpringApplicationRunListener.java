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

package org.springframework.boot;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Listener for the {@link SpringApplication} {@code run} method.
 * {@link SpringApplicationRunListener}s are loaded via the {@link SpringFactoriesLoader}
 * and should declare a public constructor that accepts a {@link SpringApplication}
 * instance and a {@code String[]} of arguments. A new
 * {@link SpringApplicationRunListener} instance will be created for each run.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @since 1.0.0
 */
// 20201201 {@link springapplication}{@code run}方法的侦听器。{@link SpringApplicationRunListener}是通过{@link SpringFactoriesLoader}加载的，
// 20201201 并且应该声明一个公共构造函数，该构造函数接受一个{@link SpringApplication}实例和一个{@code String[]}参数。
// 20201201 {springlistener}将为每个应用程序创建一个新的runlink实例。
public interface SpringApplicationRunListener {

	/**
	 * Called immediately when the run method has first started. Can be used for very
	 * early initialization.
	 * @param bootstrapContext the bootstrap context
	 */
	// 20201201 当run方法第一次启动时立即调用。可用于非常早期的初始化。
	default void starting(ConfigurableBootstrapContext bootstrapContext) {
		starting();
	}

	/**
	 * Called immediately when the run method has first started. Can be used for very
	 * early initialization.
	 * @deprecated since 2.4.0 in favor of {@link #starting(ConfigurableBootstrapContext)} // 20201201 从2.4.0开始支持{@link #starting（ConfigurableBootstrapContext）}
	 */
	@Deprecated // 20201201 已弃用
	// 20201201 当run方法第一次启动时立即调用。可用于非常早期的初始化。
	default void starting() {
	}

	/**
	 * Called once the environment has been prepared, but before the
	 * {@link ApplicationContext} has been created.
	 * @param bootstrapContext the bootstrap context
	 * @param environment the environment
	 */
	// 20201202 在环境准备好之后，但在{@link ApplicationContext}创建之前调用。
	default void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
                                     ConfigurableEnvironment environment) {
		environmentPrepared(environment);
	}

	/**
	 * Called once the environment has been prepared, but before the
	 * {@link ApplicationContext} has been created.
	 * @param environment the environment
	 * @deprecated since 2.4.0 in favor of
	 * {@link #environmentPrepared(ConfigurableBootstrapContext, ConfigurableEnvironment)}
	 */
	// 20201202 在环境准备好之后，但在{@link ApplicationContext}创建之前调用。
	@Deprecated // 20201202 已弃用
	default void environmentPrepared(ConfigurableEnvironment environment) {
	}

	/**
	 * Called once the {@link ApplicationContext} has been created and prepared, but
	 * before sources have been loaded.
	 * @param context the application context
	 */
	// 20201208 一旦创建并准备好{@link ApplicationContext}，但在加载源之前调用。
	default void contextPrepared(ConfigurableApplicationContext context) {
	}

	/**
	 * Called once the application context has been loaded but before it has been
	 * refreshed.
	 * @param context the application context
	 */
	// 20201209 一旦应用程序上下文已加载但在刷新之前调用 -> 将ApplicationPreparedEvent事件多播到适当的侦听器 -> 应用上下文已准备齐全但未刷新时发布的事件
	default void contextLoaded(ConfigurableApplicationContext context) {
	}

	/**
	 * The context has been refreshed and the application has started but
	 * {@link CommandLineRunner CommandLineRunners} and {@link ApplicationRunner
	 * ApplicationRunners} have not been called.
	 * @param context the application context.
	 * @since 2.0.0
	 */
	// 20201210 上下文已刷新，应用程序已启动，但尚未调用{@link CommandLineRunner CommandLineRunners}和{@link ApplicationRunner ApplicationRunners}。
	default void started(ConfigurableApplicationContext context) {
	}

	/**
	 * Called immediately before the run method finishes, when the application context has
	 * been refreshed and all {@link CommandLineRunner CommandLineRunners} and
	 * {@link ApplicationRunner ApplicationRunners} have been called.
	 * @param context the application context.
	 * @since 2.0.0
	 */
	// 20201210 在刷新应用程序上下文并已调用所有{@link CommandLineRunner CommandLineRunners}和{@link ApplicationRunner ApplicationRunners}之前，在run方法完成之前立即调用。
	default void running(ConfigurableApplicationContext context) {
	}

	/**
	 * Called when a failure occurs when running the application.
	 * @param context the application context or {@code null} if a failure occurred before
	 * the context was created
	 * @param exception the failure
	 * @since 2.0.0
	 */
	// 20201210 运行应用程序时发生故障时调用。
	default void failed(ConfigurableApplicationContext context, Throwable exception) {
	}

}
