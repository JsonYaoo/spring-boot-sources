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

package org.springframework.boot.context.event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Event published when a {@link SpringApplication} is starting up and the
 * {@link ApplicationContext} is prepared and ApplicationContextInitializers have been
 * called but before any bean definitions are loaded.
 *
 * @author Artsiom Yudovin
 * @since 2.1.0
 */
// 20201208 在启动{@link SpringApplication}并准备好{@link ApplicationContext}并已调用ApplicationContextInitializers之前（但未加载任何bean定义之前）发布的事件。
@SuppressWarnings("serial")
public class ApplicationContextInitializedEvent extends SpringApplicationEvent {

	// 20201208 准备好了的配置上下文
	private final ConfigurableApplicationContext context;

	/**
	 * Create a new {@link ApplicationContextInitializedEvent} instance.
	 * @param application the current application	// 20201208 当前的应用
	 * @param args the arguments the application is running with // 20201208 应用程序运行时使用的参数
	 * @param context the context that has been initialized	// 20201208 已初始化的上下文
	 */
	// 20201208 创建一个新的{@link ApplicationContextInitializedEvent}实例。
	public ApplicationContextInitializedEvent(SpringApplication application, String[] args,
			ConfigurableApplicationContext context) {
		// 20201208 构造SpringApplicationEvent:Springboot应用程序事件
		super(application, args);

		// 20201208 注册准备好了的配置上下文
		this.context = context;
	}

	/**
	 * Return the application context.
	 * @return the context
	 */
	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}

}
