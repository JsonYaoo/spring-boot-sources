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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.core.CoroutinesUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * 20201222
 * {@link HandlerMethod}的扩展，它通过{@link HandlerMethodArgumentResolver}列表从当前HTTP请求中解析的参数值调用基础方法。
 */

/**
 * Extension of {@link HandlerMethod} that invokes the underlying method with
 * argument values resolved from the current HTTP request through a list of
 * {@link HandlerMethodArgumentResolver}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 3.1
 */
// 20201222 {@link HandlerMethod}的扩展，它通过通过{@link HandlerMethodArgumentResolver}列表从当前HTTP请求中解析的参数值调用基础方法。
public class InvocableHandlerMethod extends HandlerMethod {

	// 20201223 空参数
	private static final Object[] EMPTY_ARGS = new Object[0];

	private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	@Nullable
	private WebDataBinderFactory dataBinderFactory;

	/**
	 * Create an instance from a {@code HandlerMethod}.
	 */
	// 20201222 从{@code HandlerMethod}创建一个实例
	public InvocableHandlerMethod(HandlerMethod handlerMethod) {
		super(handlerMethod);
	}

	/**
	 * Create an instance from a bean instance and a method.
	 */
	public InvocableHandlerMethod(Object bean, Method method) {
		super(bean, method);
	}

	/**
	 * Construct a new handler method with the given bean instance, method name and parameters.
	 * @param bean the object bean
	 * @param methodName the method name
	 * @param parameterTypes the method parameter types
	 * @throws NoSuchMethodException when the method cannot be found
	 */
	public InvocableHandlerMethod(Object bean, String methodName, Class<?>... parameterTypes)
			throws NoSuchMethodException {

		super(bean, methodName, parameterTypes);
	}

	/**
	 * 20201222
	 * 设置{@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}用于解析方法参数值。
	 */
	/**
	 * Set {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}
	 * to use for resolving method argument values.
	 */
	// 20201222 设置{@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}用于解析方法参数值。
	public void setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
		this.resolvers = argumentResolvers;
	}

	/**
	 * 20201222
	 * A. 设置ParameterNameDiscoverer以在需要时解析参数名称（例如默认请求属性名称）。
	 * B. 默认值为{@link DefaultParameterNameDiscoverer}。
	 */
	/**
	 * A.
	 * Set the ParameterNameDiscoverer for resolving parameter names when needed
	 * (e.g. default request attribute name).
	 *
	 * B.
	 * <p>Default is a {@link DefaultParameterNameDiscoverer}.
	 */
	// 20201224 设置ParameterNameDiscoverer以在需要时解析参数名称（例如默认请求属性名称）。
	public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * 20201222
	 * 设置{@link WebDataBinderFactory}传递给参数解析器，使他们可以创建{@link WebDataBinder}进行数据绑定和类型转换。
	 */
	/**
	 * Set the {@link WebDataBinderFactory} to be passed to argument resolvers allowing them
	 * to create a {@link WebDataBinder} for data binding and type conversion purposes.
	 */
	// 20201222 设置{@link WebDataBinderFactory}传递给参数解析器，使他们可以创建{@link WebDataBinder}进行数据绑定和类型转换。
	public void setDataBinderFactory(WebDataBinderFactory dataBinderFactory) {
		this.dataBinderFactory = dataBinderFactory;
	}

	/**
	 * 20201223
	 * A. 在给定请求的上下文中解析其参数值后，调用该方法。
	 * B. 参数值通常是通过{@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}解析的。{@code includedArgs}参数可以提供直接使用的参数值，
	 *    即无需参数解析。 提供的参数值的示例包括{@link WebDataBinder}，{@link SessionStatus}或引发的异常实例。 在参数解析器之前检查提供的参数值。
	 * C. 委托给{@link #getMethodArgumentValues}并使用已解析的参数调用{@link #doInvoke}。
	 */
	/**
	 * A.
	 * Invoke the method after resolving its argument values in the context of the given request.
	 *
	 * B.
	 * <p>Argument values are commonly resolved through
	 * {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}.
	 * The {@code providedArgs} parameter however may supply argument values to be used directly,
	 * i.e. without argument resolution. Examples of provided argument values include a
	 * {@link WebDataBinder}, a {@link SessionStatus}, or a thrown exception instance.
	 * Provided argument values are checked before argument resolvers.
	 *
	 * C.
	 * <p>Delegates to {@link #getMethodArgumentValues} and calls {@link #doInvoke} with the
	 * resolved arguments.
	 *
	 * @param request the current request
	 * @param mavContainer the ModelAndViewContainer for this request
	 * @param providedArgs "given" arguments matched by type, not resolved
	 * @return the raw value returned by the invoked method
	 * @throws Exception raised if no suitable argument resolver can be found,
	 * or if the method raised an exception
	 * @see #getMethodArgumentValues
	 * @see #doInvoke
	 */
	// 20201223 在给定请求的上下文中解析其参数值后，调用该方法
	@Nullable
	public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		// 20201223 eg: 空参数 Object[0]xxxx
		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
		if (logger.isTraceEnabled()) {
			logger.trace("Arguments: " + Arrays.toString(args));
		}
		return doInvoke(args);
	}

	/**
	 * 20201223
	 * A. 获取当前请求的方法参数值，检查提供的参数值并返回配置的参数解析器。
	 * B. 结果数组将传递到{@link #doInvoke}中。
	 */
	/**
	 * A.
	 * Get the method argument values for the current request, checking the provided
	 * argument values and falling back to the configured argument resolvers.
	 *
	 * B.
	 * <p>The resulting array will be passed into {@link #doInvoke}.
	 * @since 5.1.2
	 */
	// 20201223 获取当前请求的方法参数值，检查提供的参数值并返回配置的参数解析器
	protected Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		// 20201223 eg: MethodParameter[0]@xxxx
		MethodParameter[] parameters = getMethodParameters();
		if (ObjectUtils.isEmpty(parameters)) {
			// 20201223 返回空参数
			return EMPTY_ARGS;
		}

		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			MethodParameter parameter = parameters[i];
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			args[i] = findProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}
			if (!this.resolvers.supportsParameter(parameter)) {
				throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
			}
			try {
				args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
			}
			catch (Exception ex) {
				// Leave stack trace for later, exception may actually be resolved and handled...
				if (logger.isDebugEnabled()) {
					String exMsg = ex.getMessage();
					if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
						logger.debug(formatArgumentError(parameter, exMsg));
					}
				}
				throw ex;
			}
		}
		return args;
	}

	/**
	 * Invoke the handler method with the given argument values.
	 */
	// 20201223 使用给定的参数值调用处理程序方法。
	@Nullable
	protected Object doInvoke(Object... args) throws Exception {
		// 20201223 eg: Method@xxxx: "public java.lang.String com.jsonyao.cs.Controller.TestController.testRestController()"
		Method method = getBridgedMethod();

		// 20201223 使给定的方法可访问，并在必要时显式设置它的可访问性: 不是public 或者不是public也不可访问的情况下
		ReflectionUtils.makeAccessible(method);
		try {
			// 20201223 eg: false
			if (KotlinDetector.isSuspendingFunction(method)) {
				return CoroutinesUtils.invokeSuspendingFunction(method, getBean(), args);
			}

			// 20201223 执行Method方法 => eg: TestController@xxxx, Object[0]xxxx
			return method.invoke(getBean(), args);
		}
		catch (IllegalArgumentException ex) {
			assertTargetBean(method, getBean(), args);
			String text = (ex.getMessage() != null ? ex.getMessage() : "Illegal argument");
			throw new IllegalStateException(formatInvokeError(text, args), ex);
		}
		catch (InvocationTargetException ex) {
			// Unwrap for HandlerExceptionResolvers ...
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof RuntimeException) {
				throw (RuntimeException) targetException;
			}
			else if (targetException instanceof Error) {
				throw (Error) targetException;
			}
			else if (targetException instanceof Exception) {
				throw (Exception) targetException;
			}
			else {
				throw new IllegalStateException(formatInvokeError("Invocation failure", args), targetException);
			}
		}
	}

}
