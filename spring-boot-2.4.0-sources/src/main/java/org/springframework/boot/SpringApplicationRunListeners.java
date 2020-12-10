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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.ReflectionUtils;

/**
 * A collection of {@link SpringApplicationRunListener}.
 *
 * @author Phillip Webb
 */
// 20201201 {@link springapplicationrunlistener}的集合。
class SpringApplicationRunListeners {

	private final Log log;

	// 20201201 Spring上下文启动监听器实例列表
	private final List<SpringApplicationRunListener> listeners;

	// 20201201 上下文数据收集器
	private final ApplicationStartup applicationStartup;

	// 20201201 构造方法
	SpringApplicationRunListeners(Log log, Collection<? extends SpringApplicationRunListener> listeners,
			ApplicationStartup applicationStartup) {
		this.log = log;
		this.listeners = new ArrayList<>(listeners);
		this.applicationStartup = applicationStartup;
	}

	// 20201201 启动上下文启动监听器 DefaultBootstrapContext
	void starting(ConfigurableBootstrapContext bootstrapContext, Class<?> mainApplicationClass) {
		doWithListeners(
				// 20201201 启动步骤名称
				"spring.boot.application.starting",

				// 20201201 Consumer<SpringApplicationRunListener> listenerAction 执行监听操作, 多播事件 -> 子类EventPublishingRunListener实现
				(listener) -> listener.starting(bootstrapContext),

				// 20201201 Consumer<StartupStep> stepAction 每步执行的操作 -> 启动每个主类
				(step) -> {
					if (mainApplicationClass != null) {
						step.tag("mainApplicationClass", mainApplicationClass.getName());
					}
				});
	}

	// 20201202 监听环境准备完毕事件 -> 将ApplicationEnvironmentPreparedEvent事件多播到适当的侦听器, 执行监听ApplicationEnvironmentPreparedEvent事件
	void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
		// 20201208 执行监听ApplicationEnvironmentPreparedEvent事件
		doWithListeners("spring.boot.application.environment-prepared",
				// 20201208 在环境准备好之后，但在{@link ApplicationContext}创建之前调用 -> 将ApplicationEnvironmentPreparedEvent事件多播到适当的侦听器
				(listener) -> listener.environmentPrepared(bootstrapContext, environment));
	}

	// 20201208 监听配置上下文准备完毕事件 -> 将ApplicationContextInitializedEvent事件多播到适当的侦听器, 执行监听ApplicationContextInitializedEvent事件
	void contextPrepared(ConfigurableApplicationContext context) {
		// 20201208 执行监听ApplicationContextInitializedEvent事件
		doWithListeners("spring.boot.application.context-prepared",
				// 20201208 一旦创建并准备好{@link ApplicationContext}，但在加载源之前调用 -> 将ApplicationContextInitializedEvent事件多播到适当的侦听器
				(listener) -> listener.contextPrepared(context));
	}

	// 20201208 监听应用上下文已准备齐全但未刷新时发布的事件 -> 将ApplicationPreparedEvent事件多播到适当的侦听器, 执行监听ApplicationPreparedEvent事件
	void contextLoaded(ConfigurableApplicationContext context) {
		// 20201202 执行监听ApplicationPreparedEvent事件
		doWithListeners("spring.boot.application.context-loaded",
				// 20201209 一旦应用程序上下文已加载但在刷新之前调用 -> 将ApplicationPreparedEvent事件多播到适当的侦听器 -> 应用上下文已准备齐全但未刷新时发布的事件
				(listener) -> listener.contextLoaded(context));
	}

	// 20201210 监听上下文已刷新, 应用程序已启动事件, 但为调用CommandLineRunners和ApplicationRunners事件 -> 将ApplicationStartedEvent事件多播到适当的侦听器, 执行监听ApplicationStartedEvent事件
	void started(ConfigurableApplicationContext context) {
		// 20201202 执行监听ApplicationStartedEvent事件
		doWithListeners("spring.boot.application.started",
				// 20201210 上下文已刷新，应用程序已启动，但尚未调用{@link CommandLineRunner CommandLineRunners}和{@link ApplicationRunner ApplicationRunners}。
				(listener) -> listener.started(context));
	}

	void running(ConfigurableApplicationContext context) {
		doWithListeners("spring.boot.application.running", (listener) -> listener.running(context));
	}

	// 20201210 使用上下文启动监听器启动"spring.boot.application.failed"
	void failed(ConfigurableApplicationContext context, Throwable exception) {
		// 20201210 使用上下文启动监听器启动"spring.boot.application.failed"
		doWithListeners(
				// 20201210 每步名称: "spring.boot.application.failed"
				"spring.boot.application.failed",

				// 20201210 每步监听器执行: 运行应用程序时发生故障时调用。
				(listener) -> callFailedListener(listener, context, exception),

				// 20201210 每步动作内容
				(step) -> {
					step.tag("exception", exception.getClass().toString());
					step.tag("message", exception.getMessage());
				});
	}

	// 20201210 运行应用程序时发生故障时调用。
	private void callFailedListener(SpringApplicationRunListener listener, ConfigurableApplicationContext context,
			Throwable exception) {
		try {
			// 20201210 运行应用程序时发生故障时调用。
			listener.failed(context, exception);
		}
		catch (Throwable ex) {
			if (exception == null) {
				ReflectionUtils.rethrowRuntimeException(ex);
			}
			if (this.log.isDebugEnabled()) {
				this.log.error("Error handling failed", ex);
			}
			else {
				String message = ex.getMessage();
				message = (message != null) ? message : "no error message";
				this.log.warn("Error handling failed (" + message + ")");
			}
		}
	}

	// 20201202 执行监听事件
	private void doWithListeners(String stepName, Consumer<SpringApplicationRunListener> listenerAction) {
		// 20201202 没有指定主类的监听事件
		doWithListeners(stepName, listenerAction, null);
	}

	// 20201201 使用上下文启动监听器启动
	private void doWithListeners(String stepName, Consumer<SpringApplicationRunListener> listenerAction, Consumer<StartupStep> stepAction) {
		// 20201201 上下文记录者记录步骤
		StartupStep step = this.applicationStartup.start(stepName);

		// 20201201 启动监听操作
		this.listeners.forEach(listenerAction);

		// 20201201 启动每个主类
		if (stepAction != null) {
			stepAction.accept(step);
		}
		step.end();
	}

}
