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

package org.springframework.ui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.Conventions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201222
 * A. {@link java.util.Map}的实现，用于构建用于UI工具的模型数据。 支持链式调用和模型属性名称的生成。
 * B. 此类充当Servlet MVC的通用模型持有人，但并不与之相关。 请查看{@link Model}接口以获取接口变体。
 */
/**
 * A.
 * Implementation of {@link java.util.Map} for use when building model data for use
 * with UI tools. Supports chained calls and generation of model attribute names.
 *
 * B.
 * <p>This class serves as generic model holder for Servlet MVC but is not tied to it.
 * Check out the {@link Model} interface for an interface variant.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see Conventions#getVariableName
 * @see org.springframework.web.servlet.ModelAndView
 */
// 20201222 {@link java.util.Map}的实现: 用于构建用于UI工具的模型数据、支持链式调用和模型属性名称的生成。此类充当Servlet MVC的通用模型持有人，但并不与之相关。
@SuppressWarnings("serial")
public class ModelMap extends LinkedHashMap<String, Object> {

	/**
	 * Construct a new, empty {@code ModelMap}.
	 */
	public ModelMap() {
	}

	/**
	 * Construct a new {@code ModelMap} containing the supplied attribute
	 * under the supplied name.
	 * @see #addAttribute(String, Object)
	 */
	public ModelMap(String attributeName, @Nullable Object attributeValue) {
		addAttribute(attributeName, attributeValue);
	}

	/**
	 * Construct a new {@code ModelMap} containing the supplied attribute.
	 * Uses attribute name generation to generate the key for the supplied model
	 * object.
	 * @see #addAttribute(Object)
	 */
	public ModelMap(Object attributeValue) {
		addAttribute(attributeValue);
	}


	/**
	 * Add the supplied attribute under the supplied name.
	 * @param attributeName the name of the model attribute (never {@code null})
	 * @param attributeValue the model attribute value (can be {@code null})
	 */
	public ModelMap addAttribute(String attributeName, @Nullable Object attributeValue) {
		Assert.notNull(attributeName, "Model attribute name must not be null");
		put(attributeName, attributeValue);
		return this;
	}

	/**
	 * 20201222
	 * A. 使用{@link org.springframework.core.Conventions＃getVariableName生成的名称}将提供的属性添加到此{@code Map}。
	 * B. 注意：使用此方法时，不会将空的{@link Collection Collections}添加到模型中，因为我们无法正确确定真实的约定名称。 查看代码应检查{@code null}，而不是JSTL标签已完成的空集合。
	 */
	/**
	 * A.
	 * Add the supplied attribute to this {@code Map} using a
	 * {@link org.springframework.core.Conventions#getVariableName generated name}.
	 *
	 * B.
	 * <p><i>Note: Empty {@link Collection Collections} are not added to
	 * the model when using this method because we cannot correctly determine
	 * the true convention name. View code should check for {@code null} rather
	 * than for empty collections as is already done by JSTL tags.</i>
	 * @param attributeValue the model attribute value (never {@code null})
	 */
	// 20201222 使用{@link org.springframework.core.Conventions＃getVariableName生成的名称}将提供的属性添加到此{@code Map}。
	public ModelMap addAttribute(Object attributeValue) {
		Assert.notNull(attributeValue, "Model object must not be null");
		if (attributeValue instanceof Collection && ((Collection<?>) attributeValue).isEmpty()) {
			return this;
		}
		return addAttribute(Conventions.getVariableName(attributeValue), attributeValue);
	}

	/**
	 * Copy all attributes in the supplied {@code Collection} into this
	 * {@code Map}, using attribute name generation for each element.
	 * @see #addAttribute(Object)
	 */
	public ModelMap addAllAttributes(@Nullable Collection<?> attributeValues) {
		if (attributeValues != null) {
			for (Object attributeValue : attributeValues) {
				addAttribute(attributeValue);
			}
		}
		return this;
	}

	/**
	 * Copy all attributes in the supplied {@code Map} into this {@code Map}.
	 * @see #addAttribute(String, Object)
	 */
	public ModelMap addAllAttributes(@Nullable Map<String, ?> attributes) {
		if (attributes != null) {
			putAll(attributes);
		}
		return this;
	}

	/**
	 * Copy all attributes in the supplied {@code Map} into this {@code Map},
	 * with existing objects of the same name taking precedence (i.e. not getting
	 * replaced).
	 */
	// 20201222 将提供的{@code映射}中的所有属性复制到该{@code映射}中，同名的现有对象优先（即不被替换）。
	public ModelMap mergeAttributes(@Nullable Map<String, ?> attributes) {
		// 20201222 eg: []
		if (attributes != null) {
			attributes.forEach((key, value) -> {
				if (!containsKey(key)) {
					put(key, value);
				}
			});
		}
		return this;
	}

	/**
	 * Does this model contain an attribute of the given name?
	 * @param attributeName the name of the model attribute (never {@code null})
	 * @return whether this model contains a corresponding attribute
	 */
	public boolean containsAttribute(String attributeName) {
		return containsKey(attributeName);
	}

	/**
	 * Return the attribute value for the given name, if any.
	 * @param attributeName the name of the model attribute (never {@code null})
	 * @return the corresponding attribute value, or {@code null} if none
	 * @since 5.2
	 */
	@Nullable
	public Object getAttribute(String attributeName) {
		return get(attributeName);
	}

}
