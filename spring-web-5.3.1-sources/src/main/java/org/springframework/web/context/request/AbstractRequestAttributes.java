/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.web.context.request;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * 20201221
 * RequestAttributes实现的抽象支持类，为特定于请求的销毁回调和更新访问的会话属性提供请求完成机制。
 */
/**
 * Abstract support class for RequestAttributes implementations,
 * offering a request completion mechanism for request-specific destruction
 * callbacks and for updating accessed session attributes.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #requestCompleted()
 */
// 20201221 RequestAttributes实现的抽象支持类，为特定于请求的销毁回调和更新访问的会话属性提供请求完成机制
public abstract class AbstractRequestAttributes implements RequestAttributes {

	// 20201221 从属性名称String映射到销毁回调Runnable。
	/** Map from attribute name String to destruction callback Runnable. */
	protected final Map<String, Runnable> requestDestructionCallbacks = new LinkedHashMap<>(8);

	private volatile boolean requestActive = true;

	/**
	 * 20201221
	 * A. 发出请求已完成的信号。
	 * B. 执行所有请求销毁回调，并更新在请求处理期间已访问的会话属性。
	 */
	/**
	 * A.
	 * Signal that the request has been completed.
	 *
	 * B.
	 * <p>Executes all request destruction callbacks and updates the
	 * session attributes that have been accessed during request processing.
	 */
	// 20201221 发出请求已完成的信号: 执行所有请求销毁回调，并更新在请求处理期间已访问的会话属性
	public void requestCompleted() {
		// 20201221 请求完成后，执行所有已注册执行的回调。
		executeRequestDestructionCallbacks();

		// 20201221 更新在请求处理期间已访问的所有会话属性，以向基础会话管理器公开它们的潜在更新状态。
		updateAccessedSessionAttributes();
		this.requestActive = false;
	}

	/**
	 * Determine whether the original request is still active.
	 * @see #requestCompleted()
	 */
	protected final boolean isRequestActive() {
		return this.requestActive;
	}

	/**
	 * Register the given callback as to be executed after request completion.
	 * @param name the name of the attribute to register the callback for
	 * @param callback the callback to be executed for destruction
	 */
	protected final void registerRequestDestructionCallback(String name, Runnable callback) {
		Assert.notNull(name, "Name must not be null");
		Assert.notNull(callback, "Callback must not be null");
		synchronized (this.requestDestructionCallbacks) {
			this.requestDestructionCallbacks.put(name, callback);
		}
	}

	/**
	 * Remove the request destruction callback for the specified attribute, if any.
	 * @param name the name of the attribute to remove the callback for
	 */
	protected final void removeRequestDestructionCallback(String name) {
		Assert.notNull(name, "Name must not be null");
		synchronized (this.requestDestructionCallbacks) {
			this.requestDestructionCallbacks.remove(name);
		}
	}

	/**
	 * Execute all callbacks that have been registered for execution
	 * after request completion.
	 */
	// 20201221 请求完成后，执行所有已注册执行的回调。
	private void executeRequestDestructionCallbacks() {
		synchronized (this.requestDestructionCallbacks) {
			for (Runnable runnable : this.requestDestructionCallbacks.values()) {
				runnable.run();
			}
			this.requestDestructionCallbacks.clear();
		}
	}

	/**
	 * Update all session attributes that have been accessed during request processing,
	 * to expose their potentially updated state to the underlying session manager.
	 */
	// 20201221 更新在请求处理期间已访问的所有会话属性，以向基础会话管理器公开它们的潜在更新状态。
	protected abstract void updateAccessedSessionAttributes();

}
