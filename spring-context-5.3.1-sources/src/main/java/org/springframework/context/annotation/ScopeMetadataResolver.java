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

package org.springframework.context.annotation;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Strategy interface for resolving the scope of bean definitions.
 *
 * @author Mark Fisher
 * @since 2.5
 * @see org.springframework.context.annotation.Scope
 */
// 20201209 解决bean定义范围的策略接口。
@FunctionalInterface
public interface ScopeMetadataResolver {

	/**
	 * 20201209
	 * A. 解决适合于提供的bean {@code definition}的{@link ScopeMetadata}。
	 * B. 实现当然可以使用他们喜欢的任何策略来确定范围元数据，但是立即想到的一些实现可能是使用提供的{@code BeanDefinition}的
	 *   {@link BeanDefinition＃getBeanClassName（）the class}上存在的源级别注解，或使用提供的{@code definition}的{@link BeanDefinition＃attributeNames（）}
	 *   中存在的元数据。
	 */
	/**
	 * A.
	 * Resolve the {@link ScopeMetadata} appropriate to the supplied
	 * bean {@code definition}.
	 *
	 * B.
	 * <p>Implementations can of course use any strategy they like to
	 * determine the scope metadata, but some implementations that spring
	 * immediately to mind might be to use source level annotations
	 * present on {@link BeanDefinition#getBeanClassName() the class} of the
	 * supplied {@code definition}, or to use metadata present in the
	 * {@link BeanDefinition#attributeNames()} of the supplied {@code definition}.
	 *
	 * @param definition the target bean definition
	 * @return the relevant scope metadata; never {@code null} // 20201209 相关范围元数据； 永不{@code null}
	 */
	// 20201209 解决出适合于提供的bean定义器的bean的范围特征
	ScopeMetadata resolveScopeMetadata(BeanDefinition definition);

}
