/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.web.method.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 20201222
 * 通过委派给已注册的{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}列表来处理方法返回值。 先前解析的返回类型将被缓存，以加快查找速度。
 */
/**
 * Handles method return values by delegating to a list of registered
 * {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}.
 * Previously resolved return types are cached for faster lookups.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
// 20201222 通过委派给已注册的{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}列表来处理方法返回值。 先前解析的返回类型将被缓存，以加快查找速度。
public class HandlerMethodReturnValueHandlerComposite implements HandlerMethodReturnValueHandler {

	// 20201223 策略接口处理从处理程序方法调用返回的值。
	private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();

	/**
	 * Return a read-only list with the registered handlers, or an empty list.
	 */
	public List<HandlerMethodReturnValueHandler> getHandlers() {
		return Collections.unmodifiableList(this.returnValueHandlers);
	}

	/**
	 * Whether the given {@linkplain MethodParameter method return type} is supported by any registered
	 * {@link HandlerMethodReturnValueHandler}.
	 */
	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return getReturnValueHandler(returnType) != null;
	}

	@Nullable
	private HandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
		for (HandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
			if (handler.supportsReturnType(returnType)) {
				return handler;
			}
		}
		return null;
	}

	/**
	 * 20201223
	 * 遍历已注册的{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}并调用支持它的那个。 如果找不到合适的
	 * {@link HandlerMethodReturnValueHandler}，则@throws IllegalStateException。
	 */
	/**
	 * Iterate over registered {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers} and invoke the one that supports it.
	 * @throws IllegalStateException if no suitable {@link HandlerMethodReturnValueHandler} is found.
	 */
	// 20201223 找到合适的{@link HandlerMethodReturnValueHandler} => eg: 页面输出"Test RestController~~~"
	@Override
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

		// 20201223 eg: RequestResponseBodyMethodProcessor@xxxx
		HandlerMethodReturnValueHandler handler = selectHandler(returnValue, returnType);
		if (handler == null) {
			throw new IllegalArgumentException("Unknown return value type: " + returnType.getParameterType().getName());
		}

		// 20201223 eg: "Test RestController~~~", "method 'testRestController' parameter -1", "ModelAndViewContainer: View is [null]; default model {}", "ServletWebRequest: uri=/testController/testRestController;client=0:0:0:0:0:0:0:1"
		// 20201223 eg: 页面输出"Test RestController~~~"
		handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
	}

	// 20201223 找到合适的HandlerMethodReturnValueHandler
	@Nullable
	private HandlerMethodReturnValueHandler selectHandler(@Nullable Object value, MethodParameter returnType) {
		// 20201223 是否为异步的返回值 => eg: false
		boolean isAsyncValue = isAsyncReturnValue(value, returnType);

		// 20201223 ArrayList@xxxx: size = 15
		for (HandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
			if (isAsyncValue && !(handler instanceof AsyncHandlerMethodReturnValueHandler)) {
				continue;
			}

			// 20201223 eg: RequestResponseBodyMethodProcessor@xxxx, HandlerMethod$ReturnValueMethodParameter: "method 'testRestController' parameter -1"
			// 20201223 eg: true(@RestController)|| false => true
			if (handler.supportsReturnType(returnType)) {
				// 20201223 eg: RequestResponseBodyMethodProcessor@xxxx
				return handler;
			}
		}
		return null;
	}

	// 20201223 是否为异步的返回值
	private boolean isAsyncReturnValue(@Nullable Object value, MethodParameter returnType) {
		for (HandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
			if (handler instanceof AsyncHandlerMethodReturnValueHandler &&
					((AsyncHandlerMethodReturnValueHandler) handler).isAsyncReturnValue(value, returnType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add the given {@link HandlerMethodReturnValueHandler}.
	 */
	public HandlerMethodReturnValueHandlerComposite addHandler(HandlerMethodReturnValueHandler handler) {
		this.returnValueHandlers.add(handler);
		return this;
	}

	/**
	 * Add the given {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}.
	 */
	public HandlerMethodReturnValueHandlerComposite addHandlers(
			@Nullable List<? extends HandlerMethodReturnValueHandler> handlers) {

		if (handlers != null) {
			this.returnValueHandlers.addAll(handlers);
		}
		return this;
	}

}
