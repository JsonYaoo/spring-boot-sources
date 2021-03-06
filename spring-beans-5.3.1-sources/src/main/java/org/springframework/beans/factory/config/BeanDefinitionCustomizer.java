/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory.config;

/**
 * Callback for customizing a given bean definition.
 * Designed for use with a lambda expression or method reference.
 *
 * @author Juergen Hoeller
 * @since 5.0
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder#applyCustomizers
 */
// 20201209 用于自定义给定bean定义的回调。 设计用于lambda表达式或方法引用。
@FunctionalInterface
public interface BeanDefinitionCustomizer {

	/**
	 * Customize the given bean definition.
	 */
	// 20201209 自定义给定的bean定义。
	void customize(BeanDefinition bd);

}
