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
import org.springframework.context.ApplicationEvent;

/**
 * Base class for {@link ApplicationEvent} related to a {@link SpringApplication}.
 *
 * @author Phillip Webb
 * @since 1.0.0
 */
// 20201207 Springboot应用程序事件: 与{@link SpringApplication}相关的{@link ApplicationEvent}的基类。
@SuppressWarnings("serial")
public abstract class SpringApplicationEvent extends ApplicationEvent {

	private final String[] args;

	// 20201207 构造SpringApplicationEvent:Springboot应用程序事件
	public SpringApplicationEvent(SpringApplication application, String[] args) {
		// 20201027 构造ApplicationEvent
		super(application);

		// 20201207 设置参数
		this.args = args;
	}

	public SpringApplication getSpringApplication() {
		return (SpringApplication) getSource();
	}

	public final String[] getArgs() {
		return this.args;
	}

}
