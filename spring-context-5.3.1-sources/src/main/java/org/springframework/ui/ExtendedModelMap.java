/*
 * Copyright 2002-2017 the original author or authors.
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
import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * 20201222
 * A. 实现{@link Model}接口的{@link ModelMap}的子类。特定于Java 5的{@code Model}接口本身。
 * B. 这是Spring MVC公开给处理程序方法的实现类，通常是通过声明{@link org.springframework.ui.Model}接口来实现的。 无需在用户代码中构建它；一个简单的
 *    {@link org.springframework.ui.ModelMap}甚至只是一个带有String键的常规{@link Map}都足以返回用户模型。
 */
/**
 * A.
 * Subclass of {@link ModelMap} that implements the {@link Model} interface.
 * Java 5 specific like the {@code Model} interface itself.
 *
 * B.
 * <p>This is an implementation class exposed to handler methods by Spring MVC, typically via
 * a declaration of the {@link org.springframework.ui.Model} interface. There is no need to
 * build it within user code; a plain {@link org.springframework.ui.ModelMap} or even a just
 * a regular {@link Map} with String keys will be good enough to return a user model.
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 */
// 20201222 Spring MVC公开给处理程序方法的实现类, 返回用户模型
@SuppressWarnings("serial")
public class ExtendedModelMap extends ModelMap implements Model {

	@Override
	public ExtendedModelMap addAttribute(String attributeName, @Nullable Object attributeValue) {
		super.addAttribute(attributeName, attributeValue);
		return this;
	}

	// 20201222 使用{@link org.springframework.core.Conventions＃getVariableName生成的名称}将提供的属性添加到此{@code Map}。
	@Override
	public ExtendedModelMap addAttribute(Object attributeValue) {
		// 20201222 eg: null
		super.addAttribute(attributeValue);

		// 20201222 eg: BindingAwareModelMap@xxxx
		return this;
	}

	@Override
	public ExtendedModelMap addAllAttributes(@Nullable Collection<?> attributeValues) {
		super.addAllAttributes(attributeValues);
		return this;
	}

	@Override
	public ExtendedModelMap addAllAttributes(@Nullable Map<String, ?> attributes) {
		super.addAllAttributes(attributes);
		return this;
	}

	@Override
	public ExtendedModelMap mergeAttributes(@Nullable Map<String, ?> attributes) {
		super.mergeAttributes(attributes);
		return this;
	}

	@Override
	public Map<String, Object> asMap() {
		return this;
	}

}
