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

package org.springframework.web.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

/**
 * 20201221
 * 处理程序执行链，包括处理程序对象和任何处理程序拦截器。 由HandlerMapping的{@link HandlerMapping＃getHandler}方法返回。
 */
/**
 * Handler execution chain, consisting of handler object and any handler interceptors.
 * Returned by HandlerMapping's {@link HandlerMapping#getHandler} method.
 *
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerInterceptor
 */
// 20201221 处理程序执行链，包括处理程序对象和任何处理程序拦截器
public class HandlerExecutionChain {

	private static final Log logger = LogFactory.getLog(HandlerExecutionChain.class);

	private final Object handler;

	// 20201222 工作流接口，允许自定义处理程序执行链: 添加常见的预处理行为，而无需修改每个处理程序实现, 在适当的HandlerAdapter触发处理程序本身的执行之前，将调用HandlerInterceptor
	private final List<HandlerInterceptor> interceptorList = new ArrayList<>();

	// 20201222 拦截器索引
	private int interceptorIndex = -1;

	/**
	 * Create a new HandlerExecutionChain.
	 * @param handler the handler object to execute
	 */
	public HandlerExecutionChain(Object handler) {
		this(handler, (HandlerInterceptor[]) null);
	}

	/**
	 * Create a new HandlerExecutionChain.
	 * @param handler the handler object to execute
	 * @param interceptors the array of interceptors to apply
	 * (in the given order) before the handler itself executes
	 */
	public HandlerExecutionChain(Object handler, @Nullable HandlerInterceptor... interceptors) {
		this(handler, (interceptors != null ? Arrays.asList(interceptors) : Collections.emptyList()));
	}

	/**
	 * Create a new HandlerExecutionChain.
	 * @param handler the handler object to execute
	 * @param interceptorList the list of interceptors to apply
	 * (in the given order) before the handler itself executes
	 * @since 5.3
	 */
	public HandlerExecutionChain(Object handler, List<HandlerInterceptor> interceptorList) {
		if (handler instanceof HandlerExecutionChain) {
			HandlerExecutionChain originalChain = (HandlerExecutionChain) handler;
			this.handler = originalChain.getHandler();
			this.interceptorList.addAll(originalChain.interceptorList);
		}
		else {
			this.handler = handler;
		}
		this.interceptorList.addAll(interceptorList);
	}


	/**
	 * Return the handler object to execute.
	 */
	public Object getHandler() {
		return this.handler;
	}

	/**
	 * Add the given interceptor to the end of this chain.
	 */
	// 20201222 将给定的拦截器添加到此链的末尾。
	public void addInterceptor(HandlerInterceptor interceptor) {
		this.interceptorList.add(interceptor);
	}

	/**
	 * Add the given interceptor at the specified index of this chain.
	 * @since 5.2
	 */
	public void addInterceptor(int index, HandlerInterceptor interceptor) {
		this.interceptorList.add(index, interceptor);
	}

	/**
	 * Add the given interceptors to the end of this chain.
	 */
	public void addInterceptors(HandlerInterceptor... interceptors) {
		CollectionUtils.mergeArrayIntoCollection(interceptors, this.interceptorList);
	}

	/**
	 * Return the array of interceptors to apply (in the given order).
	 * @return the array of HandlerInterceptors instances (may be {@code null})
	 */
	@Nullable
	public HandlerInterceptor[] getInterceptors() {
		return (!this.interceptorList.isEmpty() ? this.interceptorList.toArray(new HandlerInterceptor[0]) : null);
	}

	/**
	 * Return the list of interceptors to apply (in the given order).
	 * @return the list of HandlerInterceptors instances (potentially empty)
	 * @since 5.3
	 */
	public List<HandlerInterceptor> getInterceptorList() {
		return (!this.interceptorList.isEmpty() ? Collections.unmodifiableList(this.interceptorList) :
				Collections.emptyList());
	}


	/**
	 * Apply preHandle methods of registered interceptors.
	 *
	 * // 20201222 执行链是否应该使用下一个拦截器或处理程序本身。 否则，DispatcherServlet假定此拦截器已经处理了响应本身。
	 * @return {@code true} if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherServlet assumes
	 * that this interceptor has already dealt with the response itself.
	 */
	// 20201222 应用注册拦截器的preHandle方法。
	boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 20201222 工作流接口，允许自定义处理程序执行链 => eg: ConversionServiceExposingInterceptor、ResourceUrlProviderExposingInterceptor
		for (int i = 0; i < this.interceptorList.size(); i++) {
			HandlerInterceptor interceptor = this.interceptorList.get(i);

			// 20201222 eg: ConversionServiceExposingInterceptor: "org.springframework.core.convert.ConversionService"-WebConversionService, 返回true
			// 20201222 eg: ResourceUrlProviderExposingInterceptor: eg: "org.springframework.web.servlet.resource.ResourceUrlProvider"-ResourceUrlProvider, 返回true
			if (!interceptor.preHandle(request, response, this.handler)) {
				triggerAfterCompletion(request, response, null);
				return false;
			}

			// 20201222 更新拦截器索引
			this.interceptorIndex = i;
		}

		// 20201222 返回true表示成功应用拦截器的preHandle方法
		return true;
	}

	/**
	 * Apply postHandle methods of registered interceptors.
	 */
	// 20201224 应用注册拦截器的postHandle方法。
	void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
			throws Exception {
		// 20201224 ArrayList@xxxx: ConversionServiceExposingInterceptor@xxxx, ResourceUrlProviderExposingInterceptor@xxxx
		// 20201224 逆序遍历
		for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
			HandlerInterceptor interceptor = this.interceptorList.get(i);

			// 202012224 拦截处理程序的执行。 在HandlerAdapter实际调用处理程序之后但在DispatcherServlet呈现视图之前调用。
			interceptor.postHandle(request, response, this.handler, mv);
		}
	}

	/**
	 * Trigger afterCompletion callbacks on the mapped HandlerInterceptors.
	 * Will just invoke afterCompletion for all interceptors whose preHandle invocation
	 * has successfully completed and returned true.
	 */
	// 20201224 在映射的HandlerInterceptor上触发afterCompletion回调。 只会对preHandle调用已成功完成并返回true的所有拦截器调用afterCompletion。
	void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
		// 20201224 拦截器索引 => eg: = 1
		for (int i = this.interceptorIndex; i >= 0; i--) {
			// 20201224 eg: ResourceUrlProviderExposingInterceptor@xxxx、ConversionServiceExposingInterceptor@xxxx
			HandlerInterceptor interceptor = this.interceptorList.get(i);
			try {
				// 20201224 完成请求处理后（即渲染视图之后）的回调。 处理程序执行的任何结果都将被调用，从而允许适当的资源清理 => eg: do nothing
				interceptor.afterCompletion(request, response, this.handler, ex);
			}
			catch (Throwable ex2) {
				logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
			}
		}
	}

	/**
	 * Apply afterConcurrentHandlerStarted callback on mapped AsyncHandlerInterceptors.
	 */
	void applyAfterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response) {
		for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
			HandlerInterceptor interceptor = this.interceptorList.get(i);
			if (interceptor instanceof AsyncHandlerInterceptor) {
				try {
					AsyncHandlerInterceptor asyncInterceptor = (AsyncHandlerInterceptor) interceptor;
					asyncInterceptor.afterConcurrentHandlingStarted(request, response, this.handler);
				}
				catch (Throwable ex) {
					if (logger.isErrorEnabled()) {
						logger.error("Interceptor [" + interceptor + "] failed in afterConcurrentHandlingStarted", ex);
					}
				}
			}
		}
	}


	/**
	 * Delegates to the handler's {@code toString()} implementation.
	 */
	@Override
	public String toString() {
		return "HandlerExecutionChain with [" + getHandler() + "] and " + this.interceptorList.size() + " interceptors";
	}

}
