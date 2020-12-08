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

package org.springframework.boot.rsocket.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 20201207
 * A. {@link ApplicationContextInitializer}为{@link RSocketServer}服务器实际监听的端口设置{@link Environment}属性。
 * 	  属性{@literal“ local.rsocket.server.port”}可以使用{@link Value @Value}直接注入测试中，也可以通过{@link Environment}获得。
 * B. 属性会自动传播到任何父上下文。
 */
/**
 * A.
 * {@link ApplicationContextInitializer} that sets {@link Environment} properties for the
 * ports that {@link RSocketServer} servers are actually listening on. The property
 * {@literal "local.rsocket.server.port"} can be injected directly into tests using
 * {@link Value @Value} or obtained via the {@link Environment}.
 *
 * B.
 * <p>
 * Properties are automatically propagated up to any parent context.
 *
 * @author Verónica Vásquez
 * @author Eddú Meléndez
 * @since 2.2.0
 */
// 20201207 RSocketServer服务器端口应用程序上下文初始化器
public class RSocketPortInfoApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		applicationContext.addApplicationListener(new Listener(applicationContext));
	}

	private static class Listener implements ApplicationListener<RSocketServerInitializedEvent> {

		private static final String PROPERTY_NAME = "local.rsocket.server.port";

		private final ConfigurableApplicationContext applicationContext;

		Listener(ConfigurableApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		// 20201207 处理应用程序事件 -> 设置应用程序上下文及递归设置其父上下文端口属性
		@Override
		public void onApplicationEvent(RSocketServerInitializedEvent event) {
			// 20201207 如果RSocketServer正在侦听的地址存在
			if (event.getServer().address() != null) {
				// 20201207 设置应用程序上下文及递归设置其父上下文端口属性
				setPortProperty(this.applicationContext, event.getServer().address().getPort());
			}
		}

		// 20201207 设置应用程序上下文端口属性
		private void setPortProperty(ApplicationContext context, int port) {
			// 20201207 如果上下文为配置上下文
			if (context instanceof ConfigurableApplicationContext) {
				// 20201208 设置配置上下文端口属性
				setPortProperty(((ConfigurableApplicationContext) context).getEnvironment(), port);
			}

			// 20201207 如果配置上下文的父上下文不为空, 则递归设置其端口属性
			if (context.getParent() != null) {
				setPortProperty(context.getParent(), port);
			}
		}

		// 20201208 设置配置上下文端口属性
		private void setPortProperty(ConfigurableEnvironment environment, int port) {
			// 20201208 获取环境属性源
			MutablePropertySources sources = environment.getPropertySources();

			// 20201208 获取属性源的端口属性
			PropertySource<?> source = sources.get("server.ports");

			// 20201208 如果获取不到, 则创建端口属性, 然后添加到环境属性源中
			if (source == null) {
				source = new MapPropertySource("server.ports", new HashMap<>());
				sources.addFirst(source);
			}

			// 20201208 修改local.rsocket.server.port属性值
			setPortProperty(port, source);
		}

		// 20201208 修改local.rsocket.server.port属性值
		@SuppressWarnings("unchecked")
		private void setPortProperty(int port, PropertySource<?> source) {
			((Map<String, Object>) source.getSource()).put(PROPERTY_NAME, port);
		}

	}

}
