/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.web.servlet.mvc.method.annotation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.View;
import org.springframework.web.util.NestedServletException;

/**
 * 20201222
 * A. 扩展了{@link InvocableHandlerMethod}，使其能够通过已注册的{@link HandlerMethodReturnValueHandler}处理返回值，并且还支持基于方法级别的{@code @ResponseStatus}注释设置响应状态。
 * B. {@code null}返回值（包括void）可以与{@code @ResponseStatus}批注（未修改的检查条件）结合起来解释为请求处理的结束（请参阅{@link ServletWebRequest＃checkNotModified（long） }）或
 *    提供访问响应流的方法参数。
 */
/**
 * A.
 * Extends {@link InvocableHandlerMethod} with the ability to handle return
 * values through a registered {@link HandlerMethodReturnValueHandler} and
 * also supports setting the response status based on a method-level
 * {@code @ResponseStatus} annotation.
 *
 * B.
 * <p>A {@code null} return value (including void) may be interpreted as the
 * end of request processing in combination with a {@code @ResponseStatus}
 * annotation, a not-modified check condition
 * (see {@link ServletWebRequest#checkNotModified(long)}), or
 * a method argument that provides access to the response stream.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
// 20201222 扩展了{@link InvocableHandlerMethod}，使其能够通过已注册的{@link HandlerMethodReturnValueHandler}处理返回值，并且还支持基于方法级别的{@code @ResponseStatus}注释设置响应状态。
public class ServletInvocableHandlerMethod extends InvocableHandlerMethod {

	private static final Method CALLABLE_METHOD = ClassUtils.getMethod(Callable.class, "call");

	// 20201222 通过委派给已注册的{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}列表来处理方法返回值。 先前解析的返回类型将被缓存，以加快查找速度。
	@Nullable
	private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

	/**
	 * Creates an instance from the given handler and method.
	 */
	public ServletInvocableHandlerMethod(Object handler, Method method) {
		super(handler, method);
	}

	/**
	 * Create an instance from a {@code HandlerMethod}.
	 */
	// 20201222 从{@code HandlerMethod}创建一个实例。
	public ServletInvocableHandlerMethod(HandlerMethod handlerMethod) {
		super(handlerMethod);
	}

	/**
	 * 20201222
	 * 注册{@link HandlerMethodReturnValueHandler}实例以处理返回值。
	 */
	/**
	 * Register {@link HandlerMethodReturnValueHandler} instances to use to
	 * handle return values.
	 */
	// 20201222 注册{@link HandlerMethodReturnValueHandler}实例以处理返回值。
	public void setHandlerMethodReturnValueHandlers(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
		this.returnValueHandlers = returnValueHandlers;
	}

	/**
	 * 20201223
	 * 调用该方法并通过已配置的{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}中的一个来处理返回值。
	 */
	/**
	 * Invoke the method and handle the return value through one of the
	 * configured {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}.
	 * @param webRequest the current request
	 * @param mavContainer the ModelAndViewContainer for this request
	 * @param providedArgs "given" arguments matched by type (not resolved)
	 */
	// 20201223 调用该方法并通过已配置的{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}中的一个来处理返回值 => eg: 页面输出"Test RestController~~~"
	public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		// 20201223 method.invoke(getBean(), args); 执行Method方法 => eg: TestController@xxxx, Object[0]xxxx
		Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);

		// 20201223 eg: do nothing
		setResponseStatus(webRequest);

		// 20201223 Method执行结果 => eg: "Test RestController~~~"
		if (returnValue == null) {
			if (isRequestNotModified(webRequest) || getResponseStatus() != null || mavContainer.isRequestHandled()) {
				disableContentCachingIfNecessary(webRequest);
				mavContainer.setRequestHandled(true);
				return;
			}
		}
		else if (StringUtils.hasText(getResponseStatusReason())) {
			mavContainer.setRequestHandled(true);
			return;
		}

		// 20201223 该请求是否已在处理程序中完全处理 => eg: false
		mavContainer.setRequestHandled(false);
		Assert.state(this.returnValueHandlers != null, "No return value handlers");
		try {
			// 20201223 eg: HandlerMethodReturnValueHandlerComposite@xxxx: [xxx]size=15
			// 20201223 通过向模型添加属性并设置视图或将{@link ModelAndViewContainer＃setRequestHandled}标志设置为{@code true}来处理给定的返回值，以指示已直接处理响应  => eg: 页面输出"Test RestController~~~"
			this.returnValueHandlers.handleReturnValue(
					// 20201223 eg: "Test RestController~~~"
					returnValue,

					// 20201223 eg: HandlerMethod$ReturnValueMethodParameter@xxxx: method 'testRestController' parameter -1
					getReturnValueType(returnValue),

					// 20201223 eg: ModelAndViewContainer@xxxx: "ModelAndViewContainer: View is [null]; default model {}"
					mavContainer,

					// 20201223 eg: ServletWebReqest@xxxx: "ServletWebRequest: uri=/testController/testRestController;client=0:0:0:0:0:0:0:1"
					webRequest);
		}
		catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace(formatErrorForReturnValue(returnValue), ex);
			}
			throw ex;
		}
	}

	/**
	 * Set the response status according to the {@link ResponseStatus} annotation.
	 */
	// 20201223 根据{@link ResponseStatus}注释设置响应状态。
	private void setResponseStatus(ServletWebRequest webRequest) throws IOException {
		// 20201223 eg: null
		HttpStatus status = getResponseStatus();
		if (status == null) {
			return;
		}

		HttpServletResponse response = webRequest.getResponse();
		if (response != null) {
			String reason = getResponseStatusReason();
			if (StringUtils.hasText(reason)) {
				response.sendError(status.value(), reason);
			}
			else {
				response.setStatus(status.value());
			}
		}

		// To be picked up by RedirectView
		webRequest.getRequest().setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, status);
	}

	/**
	 * Does the given request qualify as "not modified"?
	 * @see ServletWebRequest#checkNotModified(long)
	 * @see ServletWebRequest#checkNotModified(String)
	 */
	private boolean isRequestNotModified(ServletWebRequest webRequest) {
		return webRequest.isNotModified();
	}

	private void disableContentCachingIfNecessary(ServletWebRequest webRequest) {
		if (isRequestNotModified(webRequest)) {
			HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
			Assert.notNull(response, "Expected HttpServletResponse");
			if (StringUtils.hasText(response.getHeader(HttpHeaders.ETAG))) {
				HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
				Assert.notNull(request, "Expected HttpServletRequest");
			}
		}
	}

	private String formatErrorForReturnValue(@Nullable Object returnValue) {
		return "Error handling return value=[" + returnValue + "]" +
				(returnValue != null ? ", type=" + returnValue.getClass().getName() : "") +
				" in " + toString();
	}

	/**
	 * Create a nested ServletInvocableHandlerMethod subclass that returns the
	 * the given value (or raises an Exception if the value is one) rather than
	 * actually invoking the controller method. This is useful when processing
	 * async return values (e.g. Callable, DeferredResult, ListenableFuture).
	 */
	ServletInvocableHandlerMethod wrapConcurrentResult(Object result) {
		return new ConcurrentResultHandlerMethod(result, new ConcurrentResultMethodParameter(result));
	}


	/**
	 * A nested subclass of {@code ServletInvocableHandlerMethod} that uses a
	 * simple {@link Callable} instead of the original controller as the handler in
	 * order to return the fixed (concurrent) result value given to it. Effectively
	 * "resumes" processing with the asynchronously produced return value.
	 */
	private class ConcurrentResultHandlerMethod extends ServletInvocableHandlerMethod {

		private final MethodParameter returnType;

		public ConcurrentResultHandlerMethod(final Object result, ConcurrentResultMethodParameter returnType) {
			super((Callable<Object>) () -> {
				if (result instanceof Exception) {
					throw (Exception) result;
				}
				else if (result instanceof Throwable) {
					throw new NestedServletException("Async processing failed", (Throwable) result);
				}
				return result;
			}, CALLABLE_METHOD);

			if (ServletInvocableHandlerMethod.this.returnValueHandlers != null) {
				setHandlerMethodReturnValueHandlers(ServletInvocableHandlerMethod.this.returnValueHandlers);
			}
			this.returnType = returnType;
		}

		/**
		 * Bridge to actual controller type-level annotations.
		 */
		@Override
		public Class<?> getBeanType() {
			return ServletInvocableHandlerMethod.this.getBeanType();
		}

		/**
		 * Bridge to actual return value or generic type within the declared
		 * async return type, e.g. Foo instead of {@code DeferredResult<Foo>}.
		 */
		@Override
		public MethodParameter getReturnValueType(@Nullable Object returnValue) {
			return this.returnType;
		}

		/**
		 * Bridge to controller method-level annotations.
		 */
		@Override
		public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
			return ServletInvocableHandlerMethod.this.getMethodAnnotation(annotationType);
		}

		/**
		 * Bridge to controller method-level annotations.
		 */
		@Override
		public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
			return ServletInvocableHandlerMethod.this.hasMethodAnnotation(annotationType);
		}
	}


	/**
	 * MethodParameter subclass based on the actual return value type or if
	 * that's null falling back on the generic type within the declared async
	 * return type, e.g. Foo instead of {@code DeferredResult<Foo>}.
	 */
	private class ConcurrentResultMethodParameter extends HandlerMethodParameter {

		@Nullable
		private final Object returnValue;

		private final ResolvableType returnType;

		public ConcurrentResultMethodParameter(Object returnValue) {
			super(-1);
			this.returnValue = returnValue;
			this.returnType = (returnValue instanceof ReactiveTypeHandler.CollectedValuesList ?
					((ReactiveTypeHandler.CollectedValuesList) returnValue).getReturnType() :
					KotlinDetector.isSuspendingFunction(super.getMethod()) ?
					ResolvableType.forMethodParameter(getReturnType()) :
					ResolvableType.forType(super.getGenericParameterType()).getGeneric());
		}

		public ConcurrentResultMethodParameter(ConcurrentResultMethodParameter original) {
			super(original);
			this.returnValue = original.returnValue;
			this.returnType = original.returnType;
		}

		@Override
		public Class<?> getParameterType() {
			if (this.returnValue != null) {
				return this.returnValue.getClass();
			}
			if (!ResolvableType.NONE.equals(this.returnType)) {
				return this.returnType.toClass();
			}
			return super.getParameterType();
		}

		@Override
		public Type getGenericParameterType() {
			return this.returnType.getType();
		}

		@Override
		public <T extends Annotation> boolean hasMethodAnnotation(Class<T> annotationType) {
			// Ensure @ResponseBody-style handling for values collected from a reactive type
			// even if actual return type is ResponseEntity<Flux<T>>
			return (super.hasMethodAnnotation(annotationType) ||
					(annotationType == ResponseBody.class &&
							this.returnValue instanceof ReactiveTypeHandler.CollectedValuesList));
		}

		@Override
		public ConcurrentResultMethodParameter clone() {
			return new ConcurrentResultMethodParameter(this);
		}
	}

}
