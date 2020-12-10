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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ErrorHandler;

/**
 * {@link SpringApplicationRunListener} to publish {@link SpringApplicationEvent}s.
 * <p>
 * Uses an internal {@link ApplicationEventMulticaster} for the events that are fired
 * before the context is actually refreshed.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @author Artsiom Yudovin
 * @author Brian Clozel
 * @since 1.0.0
 */
public class EventPublishingRunListener implements SpringApplicationRunListener, Ordered {

	private final SpringApplication application;

	private final String[] args;

	// 20201207 简单的应用程序事件多播器实现
	private final SimpleApplicationEventMulticaster initialMulticaster;

	public EventPublishingRunListener(SpringApplication application, String[] args) {
		this.application = application;
		this.args = args;
		this.initialMulticaster = new SimpleApplicationEventMulticaster();
		for (ApplicationListener<?> listener : application.getListeners()) {
			this.initialMulticaster.addApplicationListener(listener);
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	// 20201207 执行监听操作, 多播事件 DefaultBootstrapContext
	@Override
	public void starting(ConfigurableBootstrapContext bootstrapContext) {
		// 20201208 将封装好的应用程序事件ApplicationStartingEvent多播到适当的侦听器
		this.initialMulticaster.multicastEvent(
				// 20201207 构造ApplicationStartingEvent
				new ApplicationStartingEvent(bootstrapContext, this.application, this.args)
		);
	}

	// 20201208 在环境准备好之后，但在{@link ApplicationContext}创建之前调用 -> 将ApplicationEnvironmentPreparedEvent事件多播到适当的侦听器
	@Override
	public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
		// 20201208 将给定的应用程序事件多播到适当的侦听器
		this.initialMulticaster.multicastEvent(
				// 20201208 在{@link SpringApplication}启动且{@link Environment}可用时, 构建用于检查和修改发布的事件。
				new ApplicationEnvironmentPreparedEvent(bootstrapContext, this.application, this.args, environment));
	}

	// 20201208 一旦创建并准备好{@link ApplicationContext}，但在加载源之前调用。
	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		// 20201208 将ApplicationContextInitializedEvent事件多播到适当的侦听器
		this.initialMulticaster.multicastEvent(
				// 20201208 创建一个新的{@link ApplicationContextInitializedEvent}实例 -> 说明配置上下文已经准备好
				new ApplicationContextInitializedEvent(this.application, this.args, context));
	}

	// 20201209 一旦应用程序上下文已加载但在刷新之前调用 -> 将ApplicationPreparedEvent事件多播到适当的侦听器 -> 应用上下文已准备齐全但未刷新时发布的事件
	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		// 20201209 遍历在上下文注册的监听器列表
		for (ApplicationListener<?> listener : this.application.getListeners()) {
			// 20201209 监听器设置上下文
			if (listener instanceof ApplicationContextAware) {
				((ApplicationContextAware) listener).setApplicationContext(context);
			}
			// 20201209 重新添加该监听器
			context.addApplicationListener(listener);
		}
		// 20201208 将ApplicationPreparedEvent事件多播到适当的侦听器 -> 应用上下文已准备齐全但未刷新时发布的事件
		this.initialMulticaster.multicastEvent(
				// 20201209 构造应用上下文已准备齐全但未刷新时发布的事件
				new ApplicationPreparedEvent(this.application, this.args, context)
		);
	}

	// 20201210 上下文已刷新，应用程序已启动，但尚未调用{@link CommandLineRunner CommandLineRunners}和{@link ApplicationRunner ApplicationRunners}。
	@Override
	public void started(ConfigurableApplicationContext context) {
		// 20201210 通知所有与此应用程序注册的匹配侦听器事件
		context.publishEvent(
				// 20201210 刷新应用程序上下文后，但在调用任何ApplicationRunner应用程序和CommandLineRunner命令行运行程序之前，发布事件
				new ApplicationStartedEvent(this.application, this.args, context)
		);

		// 20201210 可用于将{@link AvailabilityChangeEvent}发布到给定应用程序上下文的便捷方法。
		AvailabilityChangeEvent.publish(
				context,
				// 20201210 该应用程序正在运行，并且其内部状态正确。
				LivenessState.CORRECT);
	}

	@Override
	public void running(ConfigurableApplicationContext context) {
		context.publishEvent(new ApplicationReadyEvent(this.application, this.args, context));
		AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		ApplicationFailedEvent event = new ApplicationFailedEvent(this.application, this.args, context, exception);
		if (context != null && context.isActive()) {
			// Listeners have been registered to the application context so we should
			// use it at this point if we can
			context.publishEvent(event);
		}
		else {
			// An inactive context may not have a multicaster so we use our multicaster to
			// call all of the context's listeners instead
			if (context instanceof AbstractApplicationContext) {
				for (ApplicationListener<?> listener : ((AbstractApplicationContext) context)
						.getApplicationListeners()) {
					this.initialMulticaster.addApplicationListener(listener);
				}
			}
			this.initialMulticaster.setErrorHandler(new LoggingErrorHandler());
			this.initialMulticaster.multicastEvent(event);
		}
	}

	private static class LoggingErrorHandler implements ErrorHandler {

		private static final Log logger = LogFactory.getLog(EventPublishingRunListener.class);

		@Override
		public void handleError(Throwable throwable) {
			logger.warn("Error calling ApplicationEventListener", throwable);
		}

	}

}
