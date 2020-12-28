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

package org.springframework.boot.web.servlet.server;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;

/**
 * 20201228
 * 可用于创建{@link WebServer}的工厂接口
 */
/**
 * Factory interface that can be used to create a {@link WebServer}.
 *
 * @author Phillip Webb
 * @see WebServer
 * @since 2.0.0
 */
// 20201228 可用于创建{@link WebServer}的工厂接口
@FunctionalInterface
public interface ServletWebServerFactory {

	/**
	 * // 20201213 获取一个新的完全配置但暂停的{@link WebServer}实例。 在调用{@link WebServer＃start（）}之前（在完全刷新{@code ApplicationContext}之后，
	 *    客户端才应该能够连接到返回的服务器）。
	 * Gets a new fully configured but paused {@link WebServer} instance. Clients should
	 * not be able to connect to the returned server until {@link WebServer#start()} is
	 * called (which happens when the {@code ApplicationContext} has been fully
	 * refreshed).
	 * @param initializers {@link ServletContextInitializer}s that should be applied as
	 * the server starts
	 * @return a fully configured and started {@link WebServer}
	 * @see WebServer#stop()
	 */
	// 20201213 获取一个新的完全配置但暂停的{@link WebServer}实例
	WebServer getWebServer(ServletContextInitializer... initializers);

}
