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

package org.springframework.web.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 20201221
 * A. 封装有关由{@linkplain #getMethod（）方法}和{@linkplain #getBean（）bean}组成的处理程序方法的信息。 提供对方法参数，方法返回值，方法注释等的便捷访问。
 * B. 可以使用bean实例或bean名称（例如lazy-init bean，prototype bean）创建该类。 使用{@link #createWithResolvedBean（）}获得一个{@code HandlerMethod}实例，
 *    该实例具有通过关联的{@link BeanFactory}解析的bean实例。
 */
/**
 * A.
 * Encapsulates information about a handler method consisting of a
 * {@linkplain #getMethod() method} and a {@linkplain #getBean() bean}.
 * Provides convenient access to method parameters, the method return value,
 * method annotations, etc.
 *
 * B.
 * <p>The class may be created with a bean instance or with a bean name
 * (e.g. lazy-init bean, prototype bean). Use {@link #createWithResolvedBean()}
 * to obtain a {@code HandlerMethod} instance with a bean instance resolved
 * through the associated {@link BeanFactory}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 */
// 20201221 封装有关由{@linkplain #getMethod（）方法}和{@linkplain #getBean（）bean}组成的处理程序方法的信息: 提供对方法参数，方法返回值，方法注释等的便捷访问。
public class HandlerMethod {

	/** Logger that is available to subclasses. */
	protected static final Log logger = LogFactory.getLog(HandlerMethod.class);

	private final Object bean;

	@Nullable
	private final BeanFactory beanFactory;

	private final Class<?> beanType;

	private final Method method;

	private final Method bridgedMethod;

	private final MethodParameter[] parameters;

	@Nullable
	private HttpStatus responseStatus;

	@Nullable
	private String responseStatusReason;

	// 20201222 解析后的HandlerMethod
	@Nullable
	private HandlerMethod resolvedFromHandlerMethod;

	@Nullable
	private volatile List<Annotation[][]> interfaceParameterAnnotations;

	private final String description;

	/**
	 * Create an instance from a bean instance and a method.
	 */
	public HandlerMethod(Object bean, Method method) {
		Assert.notNull(bean, "Bean is required");
		Assert.notNull(method, "Method is required");
		this.bean = bean;
		this.beanFactory = null;
		this.beanType = ClassUtils.getUserClass(bean);
		this.method = method;
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
		this.parameters = initMethodParameters();
		evaluateResponseStatus();
		this.description = initDescription(this.beanType, this.method);
	}

	/**
	 * Create an instance from a bean instance, method name, and parameter types.
	 * @throws NoSuchMethodException when the method cannot be found
	 */
	public HandlerMethod(Object bean, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		Assert.notNull(bean, "Bean is required");
		Assert.notNull(methodName, "Method name is required");
		this.bean = bean;
		this.beanFactory = null;
		this.beanType = ClassUtils.getUserClass(bean);
		this.method = bean.getClass().getMethod(methodName, parameterTypes);
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(this.method);
		this.parameters = initMethodParameters();
		evaluateResponseStatus();
		this.description = initDescription(this.beanType, this.method);
	}

	/**
	 * Create an instance from a bean name, a method, and a {@code BeanFactory}.
	 * The method {@link #createWithResolvedBean()} may be used later to
	 * re-create the {@code HandlerMethod} with an initialized bean.
	 */
	public HandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
		Assert.hasText(beanName, "Bean name is required");
		Assert.notNull(beanFactory, "BeanFactory is required");
		Assert.notNull(method, "Method is required");
		this.bean = beanName;
		this.beanFactory = beanFactory;
		Class<?> beanType = beanFactory.getType(beanName);
		if (beanType == null) {
			throw new IllegalStateException("Cannot resolve bean type for bean with name '" + beanName + "'");
		}
		this.beanType = ClassUtils.getUserClass(beanType);
		this.method = method;
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
		this.parameters = initMethodParameters();
		evaluateResponseStatus();
		this.description = initDescription(this.beanType, this.method);
	}

	/**
	 * Copy constructor for use in subclasses.
	 */
	// 20201222 复制用于子类的构造函数。
	protected HandlerMethod(HandlerMethod handlerMethod) {
		// 20201222 eg: ServletInvocableHandlerMethod@xxxx: "com.jsonyao.cs.Controller.TestController#testRequestMapping()"
		Assert.notNull(handlerMethod, "HandlerMethod is required");

		// 20201222 eg: TestController@xxxx
		this.bean = handlerMethod.bean;

		// 20201222 eg: DefaultListableBeanFactory@xxxx
		this.beanFactory = handlerMethod.beanFactory;

		// 20201222 eg: Class@xxxx: "class com.jsonyao.cs.Controller.TestController"
		this.beanType = handlerMethod.beanType;

		// 20201222 eg: Method@xxxx: "public void com.jsonyao.cs.Controller.TestController.testRequestMapping()"
		this.method = handlerMethod.method;

		// 20201222 eg: Method@xxxx: "public void com.jsonyao.cs.Controller.TestController.testRequestMapping()"
		this.bridgedMethod = handlerMethod.bridgedMethod;

		// 20201222 eg: MethodParameter[0]@xxxx
		this.parameters = handlerMethod.parameters;

		// 20201222 eg: null
		this.responseStatus = handlerMethod.responseStatus;

		// 20201222 eg: null
		this.responseStatusReason = handlerMethod.responseStatusReason;

		// 20201222 "com.jsonyao.cs.Controller.TestController#testRequestMapping()"
		this.description = handlerMethod.description;

		// 20201222 HandlerMethod@xxxx: "com.jsonyao.cs.Controller.TestController#testRequestMapping()"
		this.resolvedFromHandlerMethod = handlerMethod.resolvedFromHandlerMethod;
	}

	/**
	 * Re-create HandlerMethod with the resolved handler.
	 */
	// 20201221 用已解析的处理程序重新创建HandlerMethod。
	private HandlerMethod(HandlerMethod handlerMethod, Object handler) {
		Assert.notNull(handlerMethod, "HandlerMethod is required");
		Assert.notNull(handler, "Handler object is required");

		// 20201221 找到的处理器实例 TestController
		this.bean = handler;

		// 20201221 BeanFactory eg: DefaultListableBeanFactory
		this.beanFactory = handlerMethod.beanFactory;

		// 20201221 控制器Class eg: class com.jsonyao.cs.Controller.TestController
		this.beanType = handlerMethod.beanType;

		// 20201221 Method对象 eg: public void com.jsonyao.cs.Controller.TestController.testRequestMapping()
		this.method = handlerMethod.method;

		// 20201221 桥接Method对象 eg: public void com.jsonyao.cs.Controller.TestController.testRequestMapping()
		this.bridgedMethod = handlerMethod.bridgedMethod;

		// 20201221 方法参数 eg: {}
		this.parameters = handlerMethod.parameters;

		// 20201221 eg: null
		this.responseStatus = handlerMethod.responseStatus;

		// 20201221 eg: null
		this.responseStatusReason = handlerMethod.responseStatusReason;

		// 20201221 HandlerMethod实例 => eg: "com.jsonyao.cs.Controller.TestController#testRequestMapping()"
		this.resolvedFromHandlerMethod = handlerMethod;

		// 20201221 "com.jsonyao.cs.Controller.TestController#testRequestMapping()"
		this.description = handlerMethod.description;
	}

	private MethodParameter[] initMethodParameters() {
		int count = this.bridgedMethod.getParameterCount();
		MethodParameter[] result = new MethodParameter[count];
		for (int i = 0; i < count; i++) {
			result[i] = new HandlerMethodParameter(i);
		}
		return result;
	}

	private void evaluateResponseStatus() {
		ResponseStatus annotation = getMethodAnnotation(ResponseStatus.class);
		if (annotation == null) {
			annotation = AnnotatedElementUtils.findMergedAnnotation(getBeanType(), ResponseStatus.class);
		}
		if (annotation != null) {
			this.responseStatus = annotation.code();
			this.responseStatusReason = annotation.reason();
		}
	}

	private static String initDescription(Class<?> beanType, Method method) {
		StringJoiner joiner = new StringJoiner(", ", "(", ")");
		for (Class<?> paramType : method.getParameterTypes()) {
			joiner.add(paramType.getSimpleName());
		}
		return beanType.getName() + "#" + method.getName() + joiner.toString();
	}


	/**
	 * Return the bean for this handler method.
	 */
	public Object getBean() {
		return this.bean;
	}

	/**
	 * Return the method for this handler method.
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * 20201222
	 * A. 此方法返回此处理程序方法的处理程序类型。
	 * B. 请注意，如果bean类型是CGLIB生成的类，则返回原始的用户定义类。
	 */
	/**
	 * A.
	 * This method returns the type of the handler for this handler method.
	 *
	 * B.
	 * <p>Note that if the bean type is a CGLIB-generated class, the original
	 * user-defined class is returned.
	 */
	// 20201222 此方法返回此处理程序方法的处理程序类型
	public Class<?> getBeanType() {
		return this.beanType;
	}

	/**
	 * If the bean method is a bridge method, this method returns the bridged
	 * (user-defined) method. Otherwise it returns the same method as {@link #getMethod()}.
	 */
	// 20201223 如果bean方法是桥接方法，则此方法返回桥接（用户定义）方法。 否则，它将返回与{@link #getMethod（）}相同的方法。
	protected Method getBridgedMethod() {
		return this.bridgedMethod;
	}

	/**
	 * Return the method parameters for this handler method.
	 */
	// 20201223 返回此处理程序方法的方法参数。
	public MethodParameter[] getMethodParameters() {
		return this.parameters;
	}

	/**
	 * Return the specified response status, if any.
	 * @since 4.3.8
	 * @see ResponseStatus#code()
	 */
	// 20201223 返回指定的响应状态（如果有）。
	@Nullable
	protected HttpStatus getResponseStatus() {
		return this.responseStatus;
	}

	/**
	 * Return the associated response status reason, if any.
	 * @since 4.3.8
	 * @see ResponseStatus#reason()
	 */
	@Nullable
	protected String getResponseStatusReason() {
		return this.responseStatusReason;
	}

	/**
	 * Return the HandlerMethod return type.
	 */
	public MethodParameter getReturnType() {
		return new HandlerMethodParameter(-1);
	}

	/**
	 * Return the actual return value type.
	 */
	// 20201223 返回实际的返回值类型。
	public MethodParameter getReturnValueType(@Nullable Object returnValue) {
		return new ReturnValueMethodParameter(returnValue);
	}

	/**
	 * Return {@code true} if the method return type is void, {@code false} otherwise.
	 */
	public boolean isVoid() {
		return Void.TYPE.equals(getReturnType().getParameterType());
	}

	/**
	 * Return a single annotation on the underlying method traversing its super methods
	 * if no annotation can be found on the given method itself.
	 * <p>Also supports <em>merged</em> composed annotations with attribute
	 * overrides as of Spring Framework 4.2.2.
	 * @param annotationType the type of annotation to introspect the method for
	 * @return the annotation, or {@code null} if none found
	 * @see AnnotatedElementUtils#findMergedAnnotation
	 */
	@Nullable
	public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
		return AnnotatedElementUtils.findMergedAnnotation(this.method, annotationType);
	}

	/**
	 * Return whether the parameter is declared with the given annotation type.
	 * @param annotationType the annotation type to look for
	 * @since 4.3
	 * @see AnnotatedElementUtils#hasAnnotation
	 */
	public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
		return AnnotatedElementUtils.hasAnnotation(this.method, annotationType);
	}

	/**
	 * Return the HandlerMethod from which this HandlerMethod instance was
	 * resolved via {@link #createWithResolvedBean()}.
	 */
	// 20201222 通过{@link #createWithResolvedBean（）}返回从其解析了该HandlerMethod实例的HandlerMethod。
	@Nullable
	public HandlerMethod getResolvedFromHandlerMethod() {
		// 20201222 解析后的HandlerMethod eg: “com.jsonyao.cs.Controller.TestController#testRestController()”
		return this.resolvedFromHandlerMethod;
	}

	/**
	 * 20201221
	 * 如果提供的实例包含bean名称而不是对象实例，则在创建并返回{@link HandlerMethod}之前，将解析bean名称。
	 */
	/**
	 * If the provided instance contains a bean name rather than an object instance,
	 * the bean name is resolved before a {@link HandlerMethod} is created and returned.
	 */
	// 20201221 如果提供的实例包含bean名称而不是对象实例，则在创建并返回{@link HandlerMethod}之前，将解析bean名称 eg: TestController
	public HandlerMethod createWithResolvedBean() {
		// 20201221 HandlerMethod所在的Bean实例 => eg: testController
		Object handler = this.bean;
		if (this.bean instanceof String) {
			Assert.state(this.beanFactory != null, "Cannot resolve bean name without BeanFactory");
			String beanName = (String) this.bean;

			// 20201221 根据testController BeanName获取BeanFatory获取实际的控制器TestController
			handler = this.beanFactory.getBean(beanName);
		}

		// 20201221 用已解析的处理程序重新创建HandlerMethod。
		return new HandlerMethod(this, handler);
	}

	/**
	 * Return a short representation of this handler method for log message purposes.
	 * @since 4.3
	 */
	public String getShortLogMessage() {
		return getBeanType().getName() + "#" + this.method.getName() +
				"[" + this.method.getParameterCount() + " args]";
	}


	private List<Annotation[][]> getInterfaceParameterAnnotations() {
		List<Annotation[][]> parameterAnnotations = this.interfaceParameterAnnotations;
		if (parameterAnnotations == null) {
			parameterAnnotations = new ArrayList<>();
			for (Class<?> ifc : ClassUtils.getAllInterfacesForClassAsSet(this.method.getDeclaringClass())) {
				for (Method candidate : ifc.getMethods()) {
					if (isOverrideFor(candidate)) {
						parameterAnnotations.add(candidate.getParameterAnnotations());
					}
				}
			}
			this.interfaceParameterAnnotations = parameterAnnotations;
		}
		return parameterAnnotations;
	}

	private boolean isOverrideFor(Method candidate) {
		if (!candidate.getName().equals(this.method.getName()) ||
				candidate.getParameterCount() != this.method.getParameterCount()) {
			return false;
		}
		Class<?>[] paramTypes = this.method.getParameterTypes();
		if (Arrays.equals(candidate.getParameterTypes(), paramTypes)) {
			return true;
		}
		for (int i = 0; i < paramTypes.length; i++) {
			if (paramTypes[i] !=
					ResolvableType.forMethodParameter(candidate, i, this.method.getDeclaringClass()).resolve()) {
				return false;
			}
		}
		return true;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof HandlerMethod)) {
			return false;
		}
		HandlerMethod otherMethod = (HandlerMethod) other;
		return (this.bean.equals(otherMethod.bean) && this.method.equals(otherMethod.method));
	}

	@Override
	public int hashCode() {
		return (this.bean.hashCode() * 31 + this.method.hashCode());
	}

	@Override
	public String toString() {
		return this.description;
	}


	// Support methods for use in "InvocableHandlerMethod" sub-class variants..

	@Nullable
	protected static Object findProvidedArgument(MethodParameter parameter, @Nullable Object... providedArgs) {
		if (!ObjectUtils.isEmpty(providedArgs)) {
			for (Object providedArg : providedArgs) {
				if (parameter.getParameterType().isInstance(providedArg)) {
					return providedArg;
				}
			}
		}
		return null;
	}

	protected static String formatArgumentError(MethodParameter param, String message) {
		return "Could not resolve parameter [" + param.getParameterIndex() + "] in " +
				param.getExecutable().toGenericString() + (StringUtils.hasText(message) ? ": " + message : "");
	}

	/**
	 * Assert that the target bean class is an instance of the class where the given
	 * method is declared. In some cases the actual controller instance at request-
	 * processing time may be a JDK dynamic proxy (lazy initialization, prototype
	 * beans, and others). {@code @Controller}'s that require proxying should prefer
	 * class-based proxy mechanisms.
	 */
	protected void assertTargetBean(Method method, Object targetBean, Object[] args) {
		Class<?> methodDeclaringClass = method.getDeclaringClass();
		Class<?> targetBeanClass = targetBean.getClass();
		if (!methodDeclaringClass.isAssignableFrom(targetBeanClass)) {
			String text = "The mapped handler method class '" + methodDeclaringClass.getName() +
					"' is not an instance of the actual controller bean class '" +
					targetBeanClass.getName() + "'. If the controller requires proxying " +
					"(e.g. due to @Transactional), please use class-based proxying.";
			throw new IllegalStateException(formatInvokeError(text, args));
		}
	}

	protected String formatInvokeError(String text, Object[] args) {
		String formattedArgs = IntStream.range(0, args.length)
				.mapToObj(i -> (args[i] != null ?
						"[" + i + "] [type=" + args[i].getClass().getName() + "] [value=" + args[i] + "]" :
						"[" + i + "] [null]"))
				.collect(Collectors.joining(",\n", " ", " "));
		return text + "\n" +
				"Controller [" + getBeanType().getName() + "]\n" +
				"Method [" + getBridgedMethod().toGenericString() + "] " +
				"with argument values:\n" + formattedArgs;
	}

	/**
	 * A MethodParameter with HandlerMethod-specific behavior.
	 */
	// 20201223 具有HandlerMethod特定行为的MethodParameter。
	protected class HandlerMethodParameter extends SynthesizingMethodParameter {

		@Nullable
		private volatile Annotation[] combinedAnnotations;

		public HandlerMethodParameter(int index) {
			super(HandlerMethod.this.bridgedMethod, index);
		}

		protected HandlerMethodParameter(HandlerMethodParameter original) {
			super(original);
		}

		@Override
		@NonNull
		public Method getMethod() {
			return HandlerMethod.this.bridgedMethod;
		}

		@Override
		public Class<?> getContainingClass() {
			return HandlerMethod.this.getBeanType();
		}

		@Override
		public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
			return HandlerMethod.this.getMethodAnnotation(annotationType);
		}

		@Override
		public <T extends Annotation> boolean hasMethodAnnotation(Class<T> annotationType) {
			return HandlerMethod.this.hasMethodAnnotation(annotationType);
		}

		@Override
		public Annotation[] getParameterAnnotations() {
			Annotation[] anns = this.combinedAnnotations;
			if (anns == null) {
				anns = super.getParameterAnnotations();
				int index = getParameterIndex();
				if (index >= 0) {
					for (Annotation[][] ifcAnns : getInterfaceParameterAnnotations()) {
						if (index < ifcAnns.length) {
							Annotation[] paramAnns = ifcAnns[index];
							if (paramAnns.length > 0) {
								List<Annotation> merged = new ArrayList<>(anns.length + paramAnns.length);
								merged.addAll(Arrays.asList(anns));
								for (Annotation paramAnn : paramAnns) {
									boolean existingType = false;
									for (Annotation ann : anns) {
										if (ann.annotationType() == paramAnn.annotationType()) {
											existingType = true;
											break;
										}
									}
									if (!existingType) {
										merged.add(adaptAnnotation(paramAnn));
									}
								}
								anns = merged.toArray(new Annotation[0]);
							}
						}
					}
				}
				this.combinedAnnotations = anns;
			}
			return anns;
		}

		@Override
		public HandlerMethodParameter clone() {
			return new HandlerMethodParameter(this);
		}
	}


	/**
	 * A MethodParameter for a HandlerMethod return type based on an actual return value.
	 */
	private class ReturnValueMethodParameter extends HandlerMethodParameter {

		@Nullable
		private final Object returnValue;

		public ReturnValueMethodParameter(@Nullable Object returnValue) {
			super(-1);

			// 2021223 eg: "Test RestController~~~"
			this.returnValue = returnValue;
		}

		protected ReturnValueMethodParameter(ReturnValueMethodParameter original) {
			super(original);
			this.returnValue = original.returnValue;
		}

		@Override
		public Class<?> getParameterType() {
			return (this.returnValue != null ? this.returnValue.getClass() : super.getParameterType());
		}

		@Override
		public ReturnValueMethodParameter clone() {
			return new ReturnValueMethodParameter(this);
		}
	}

}
