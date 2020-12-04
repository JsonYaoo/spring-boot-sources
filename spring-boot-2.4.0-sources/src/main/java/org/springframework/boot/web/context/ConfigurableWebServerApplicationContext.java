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

package org.springframework.boot.web.context;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * 20201204
 * SPI接口将由大多数{@link WebServerApplicationContext Web服务器应用程序上下文}（如果不是全部）实现。 除了{WebServerApplicationContext}界面中的方法之外，
 * 还提供用于配置上下文的工具。
 */
/**
 * SPI interface to be implemented by most if not all {@link WebServerApplicationContext
 * web server application contexts}. Provides facilities to configure the context, in
 * addition to the methods in the {WebServerApplicationContext} interface.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
// 20201204 配置服务器应用程序上下文: 配置上下文、Web生命周期获取
public interface ConfigurableWebServerApplicationContext extends ConfigurableApplicationContext, WebServerApplicationContext {

	/**
	 * Set the server namespace of the context.
	 * @param serverNamespace the server namespace
	 * @see #getServerNamespace()
	 */
	void setServerNamespace(String serverNamespace);

}
