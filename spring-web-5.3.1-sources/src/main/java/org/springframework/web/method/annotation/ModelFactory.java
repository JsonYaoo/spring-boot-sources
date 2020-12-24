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

package org.springframework.web.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 20201222
 * A. 在控制器方法调用之前协助{@link Model}初始化，并在调用之后对其进行更新。
 * B. 初始化时，通过调用{@code @ModelAttribute}方法将临时存储在会话中的属性填充到模型中。
 * C. 更新时，模型属性与会话同步，并且如果缺少属性，还将添加{@link BindingResult}属性。
 */
/**
 * A.
 * Assist with initialization of the {@link Model} before controller method
 * invocation and with updates to it after the invocation.
 *
 * B.
 * <p>On initialization the model is populated with attributes temporarily stored
 * in the session and through the invocation of {@code @ModelAttribute} methods.
 *
 * C.
 * <p>On update model attributes are synchronized with the session and also
 * {@link BindingResult} attributes are added if missing.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
// 20201222 在控制器方法调用之前协助{@link Model}初始化，并在调用之后对其进行更新。
public final class ModelFactory {

	private static final Log logger = LogFactory.getLog(ModelFactory.class);

	// 20201222 模型方法列表
	private final List<ModelMethod> modelMethods = new ArrayList<>();

	private final WebDataBinderFactory dataBinderFactory;

	// 20201222 管理通过{@link SessionAttributes @SessionAttributes}声明的控制器特定的会话属性。 实际的存储委托给{@link SessionAttributeStore}实例。
	private final SessionAttributesHandler sessionAttributesHandler;

	/**
	 * Create a new instance with the given {@code @ModelAttribute} methods.
	 * @param handlerMethods the {@code @ModelAttribute} methods to invoke
	 * @param binderFactory for preparation of {@link BindingResult} attributes
	 * @param attributeHandler for access to session attributes
	 */
	// 20201222 使用给定的{@code @ModelAttribute}方法创建一个新实例。
	public ModelFactory(@Nullable List<InvocableHandlerMethod> handlerMethods,
			WebDataBinderFactory binderFactory, SessionAttributesHandler attributeHandler) {

		// 20201222 eg: []
		if (handlerMethods != null) {
			for (InvocableHandlerMethod handlerMethod : handlerMethods) {
				this.modelMethods.add(new ModelMethod(handlerMethod));
			}
		}

		// 20201222 eg: ServletRequestDataBinderFactory
		this.dataBinderFactory = binderFactory;

		// 20201222 eg: SessionAttributesHandler
		this.sessionAttributesHandler = attributeHandler;
	}

	/**
	 * 20201222
	 * A. 按以下顺序填充模型：
	 * 		a. 检索列为{@code @SessionAttributes}的“已知”会话属性。
	 * 		b. 调用{@code @ModelAttribute}方法
	 * 		c. 找到也列为{@code @SessionAttributes}的{@code @ModelAttribute}方法参数，并确保它们存在于模型中，并在必要时引发异常。
	 */
	/**
	 * A.
	 * Populate the model in the following order:
	 * <ol>
	 * a.
	 * <li>Retrieve "known" session attributes listed as {@code @SessionAttributes}.
	 *
	 * b.
	 * <li>Invoke {@code @ModelAttribute} methods
	 *
	 * c.
	 * <li>Find {@code @ModelAttribute} method arguments also listed as
	 * {@code @SessionAttributes} and ensure they're present in the model raising
	 * an exception if necessary.
	 * </ol>
	 * @param request the current request
	 * @param container a container with the model to be initialized
	 * @param handlerMethod the method for which the model is initialized
	 * @throws Exception may arise from {@code @ModelAttribute} methods
	 */
	// 20201222 初始化模型
	public void initModel(
			// 20201222 eg: ServletWebRequest@xxxx, ModelAndViewContainer@xxxx, ServletInvocableHandlerMethod@xxxx
			NativeWebRequest request, ModelAndViewContainer container, HandlerMethod handlerMethod) throws Exception {

		// 20201222 eg: []
		Map<String, ?> sessionAttributes = this.sessionAttributesHandler.retrieveAttributes(request);

		// 20201222 eg: do nothing
		container.mergeAttributes(sessionAttributes);

		// 20201223 eg: ServletWebRequest@xxxx: "ServletWebRequest: uri=/testController/testRestController;client=0:0:0:0:0:0:0:1", ModelAndViewContainer@xxxx: ModelAndViewContainer: View is [null]; default model {}
		// 20201223 eg: do nothing
		invokeModelAttributeMethods(request, container);

		// 20201223 eg: do nothing
		for (String name : findSessionAttributeArguments(handlerMethod)) {
			if (!container.containsAttribute(name)) {
				Object value = this.sessionAttributesHandler.retrieveAttribute(request, name);
				if (value == null) {
					throw new HttpSessionRequiredException("Expected session attribute '" + name + "'", name);
				}
				container.addAttribute(name, value);
			}
		}
	}

	/**
	 * Invoke model attribute methods to populate the model.
	 * Attributes are added only if not already present in the model.
	 */
	// 20201223 调用模型属性方法以填充模型。 仅当模型中不存在属性时，才添加属性。
	private void invokeModelAttributeMethods(NativeWebRequest request, ModelAndViewContainer container)
			throws Exception {
		// 20201222 eg: []
		while (!this.modelMethods.isEmpty()) {
			InvocableHandlerMethod modelMethod = getNextModelMethod(container).getHandlerMethod();
			ModelAttribute ann = modelMethod.getMethodAnnotation(ModelAttribute.class);
			Assert.state(ann != null, "No ModelAttribute annotation");
			if (container.containsAttribute(ann.name())) {
				if (!ann.binding()) {
					container.setBindingDisabled(ann.name());
				}
				continue;
			}

			Object returnValue = modelMethod.invokeForRequest(request, container);
			if (modelMethod.isVoid()) {
				if (StringUtils.hasText(ann.value())) {
					if (logger.isDebugEnabled()) {
						logger.debug("Name in @ModelAttribute is ignored because method returns void: " +
								modelMethod.getShortLogMessage());
					}
				}
				continue;
			}

			String returnValueName = getNameForReturnValue(returnValue, modelMethod.getReturnType());
			if (!ann.binding()) {
				container.setBindingDisabled(returnValueName);
			}
			if (!container.containsAttribute(returnValueName)) {
				container.addAttribute(returnValueName, returnValue);
			}
		}
	}

	private ModelMethod getNextModelMethod(ModelAndViewContainer container) {
		for (ModelMethod modelMethod : this.modelMethods) {
			if (modelMethod.checkDependencies(container)) {
				this.modelMethods.remove(modelMethod);
				return modelMethod;
			}
		}
		ModelMethod modelMethod = this.modelMethods.get(0);
		this.modelMethods.remove(modelMethod);
		return modelMethod;
	}

	/**
	 * Find {@code @ModelAttribute} arguments also listed as {@code @SessionAttributes}.
	 */
	// 20201223 查找{@code @SessionAttributes}的{@code @ModelAttribute}参数。
	private List<String> findSessionAttributeArguments(HandlerMethod handlerMethod) {
		List<String> result = new ArrayList<>();

		// 20201223 eg: []
		for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
			if (parameter.hasParameterAnnotation(ModelAttribute.class)) {
				String name = getNameForParameter(parameter);
				Class<?> paramType = parameter.getParameterType();
				if (this.sessionAttributesHandler.isHandlerSessionAttribute(name, paramType)) {
					result.add(name);
				}
			}
		}
		return result;
	}

	/**
	 * Promote model attributes listed as {@code @SessionAttributes} to the session.
	 * Add {@link BindingResult} attributes where necessary.
	 * @param request the current request
	 * @param container contains the model to update
	 * @throws Exception if creating BindingResult attributes fails
	 */
	// 20201223 将列为{@code @SessionAttributes}的模型属性提升到会话。 如有必要，添加{@link BindingResult}属性。
	public void updateModel(NativeWebRequest request, ModelAndViewContainer container) throws Exception {
		// 20201223 返回实例化时创建的“默认”模型 => eg: BindingAwareModelMap@xxxx: {}
		ModelMap defaultModel = container.getDefaultModel();

		// 20201213 eg: SimpleSessionStatus@xxxx: complete: false
		if (container.getSessionStatus().isComplete()){
			this.sessionAttributesHandler.cleanupAttributes(request);
		}
		else {
			// 20201223 eg: do nothing
			this.sessionAttributesHandler.storeAttributes(request, defaultModel);
		}

		// 20201223 !true => false, 表示请求已经处理完成
		if (!container.isRequestHandled() && container.getModel() == defaultModel) {
			updateBindingResult(request, defaultModel);
		}
	}

	/**
	 * Add {@link BindingResult} attributes to the model for attributes that require it.
	 */
	private void updateBindingResult(NativeWebRequest request, ModelMap model) throws Exception {
		List<String> keyNames = new ArrayList<>(model.keySet());
		for (String name : keyNames) {
			Object value = model.get(name);
			if (value != null && isBindingCandidate(name, value)) {
				String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + name;
				if (!model.containsAttribute(bindingResultKey)) {
					WebDataBinder dataBinder = this.dataBinderFactory.createBinder(request, value, name);
					model.put(bindingResultKey, dataBinder.getBindingResult());
				}
			}
		}
	}

	/**
	 * Whether the given attribute requires a {@link BindingResult} in the model.
	 */
	private boolean isBindingCandidate(String attributeName, Object value) {
		if (attributeName.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
			return false;
		}

		if (this.sessionAttributesHandler.isHandlerSessionAttribute(attributeName, value.getClass())) {
			return true;
		}

		return (!value.getClass().isArray() && !(value instanceof Collection) &&
				!(value instanceof Map) && !BeanUtils.isSimpleValueType(value.getClass()));
	}


	/**
	 * Derive the model attribute name for the given method parameter based on
	 * a {@code @ModelAttribute} parameter annotation (if present) or falling
	 * back on parameter type based conventions.
	 * @param parameter a descriptor for the method parameter
	 * @return the derived name
	 * @see Conventions#getVariableNameForParameter(MethodParameter)
	 */
	public static String getNameForParameter(MethodParameter parameter) {
		ModelAttribute ann = parameter.getParameterAnnotation(ModelAttribute.class);
		String name = (ann != null ? ann.value() : null);
		return (StringUtils.hasText(name) ? name : Conventions.getVariableNameForParameter(parameter));
	}

	/**
	 * Derive the model attribute name for the given return value. Results will be
	 * based on:
	 * <ol>
	 * <li>the method {@code ModelAttribute} annotation value
	 * <li>the declared return type if it is more specific than {@code Object}
	 * <li>the actual return value type
	 * </ol>
	 * @param returnValue the value returned from a method invocation
	 * @param returnType a descriptor for the return type of the method
	 * @return the derived name (never {@code null} or empty String)
	 */
	public static String getNameForReturnValue(@Nullable Object returnValue, MethodParameter returnType) {
		ModelAttribute ann = returnType.getMethodAnnotation(ModelAttribute.class);
		if (ann != null && StringUtils.hasText(ann.value())) {
			return ann.value();
		}
		else {
			Method method = returnType.getMethod();
			Assert.state(method != null, "No handler method");
			Class<?> containingClass = returnType.getContainingClass();
			Class<?> resolvedType = GenericTypeResolver.resolveReturnType(method, containingClass);
			return Conventions.getVariableNameForReturnType(method, resolvedType, returnValue);
		}
	}

	// 20201222 模型方法
	private static class ModelMethod {

		private final InvocableHandlerMethod handlerMethod;

		private final Set<String> dependencies = new HashSet<>();

		public ModelMethod(InvocableHandlerMethod handlerMethod) {
			this.handlerMethod = handlerMethod;
			for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
				if (parameter.hasParameterAnnotation(ModelAttribute.class)) {
					this.dependencies.add(getNameForParameter(parameter));
				}
			}
		}

		public InvocableHandlerMethod getHandlerMethod() {
			return this.handlerMethod;
		}

		public boolean checkDependencies(ModelAndViewContainer mavContainer) {
			for (String name : this.dependencies) {
				if (!mavContainer.containsAttribute(name)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			return this.handlerMethod.getMethod().toGenericString();
		}
	}

}
