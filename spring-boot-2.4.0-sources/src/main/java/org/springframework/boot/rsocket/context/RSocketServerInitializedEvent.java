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

package org.springframework.boot.rsocket.context;

import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.context.ApplicationEvent;

/**
 * Event to be published after the application context is refreshed and the
 * {@link RSocketServer} is ready. Useful for obtaining the local port of a running
 * server.
 *
 * @author Brian Clozel
 * @since 2.2.0
 */
// 20201207 刷新应用程序上下文并准备好{@link RSocketServer}之后将发布的事件。 对于获取正在运行的服务器的本地端口很有用。
public class RSocketServerInitializedEvent extends ApplicationEvent {

	public RSocketServerInitializedEvent(RSocketServer server) {
		super(server);
	}

	/**
	 * Access the {@link RSocketServer}.
	 * @return the embedded RSocket server
	 */
	// 20201207 访问{@link RSocketServer}。
	public RSocketServer getServer() {
		return getSource();
	}

	/**
	 * Access the source of the event (an {@link RSocketServer}).
	 * @return the embedded web server
	 */
	// 20201207 访问事件的来源（{@link RSocketServer}）。
	@Override
	public RSocketServer getSource() {
		return (RSocketServer) super.getSource();
	}

}
