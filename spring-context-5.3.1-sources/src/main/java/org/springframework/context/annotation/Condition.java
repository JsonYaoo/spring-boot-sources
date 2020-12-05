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

package org.springframework.context.annotation;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 20201205
 * A. 单个{@code condition}，必须与{@linkplain #matches匹配}才能注册组件。
 * B. 条件将在即将进行Bean定义注册之前检查，并且可以根据当时可以确定的任何标准进行否决注册。
 * C. 条件必须遵循与{@link BeanFactoryPostProcessor}相同的限制，并注意不要与bean实例进行交互。 要更精细地控制与{@code @Configuration} bean交互的条件，
 *    请考虑实现{@link ConfigurationCondition}接口。
 */
/**
 * A.
 * A single {@code condition} that must be {@linkplain #matches matched} in order
 * for a component to be registered.
 *
 * B.
 * <p>Conditions are checked immediately before the bean-definition is due to be
 * registered and are free to veto registration based on any criteria that can
 * be determined at that point.
 *
 * C.
 * <p>Conditions must follow the same restrictions as {@link BeanFactoryPostProcessor}
 * and take care to never interact with bean instances. For more fine-grained control
 * of conditions that interact with {@code @Configuration} beans consider implementing
 * the {@link ConfigurationCondition} interface.
 *
 * @author Phillip Webb
 * @since 4.0
 * @see ConfigurationCondition
 * @see Conditional
 * @see ConditionContext
 */
// 20201205 条件将在即将进行Bean定义注册之前检查，并且可以根据当时可以确定的任何标准进行否决注册
@FunctionalInterface
public interface Condition {

	/**
	 * // 202021205 确定条件是否匹配。
	 * Determine if the condition matches.
	 *
	 * // 20201205 条件上下文
	 * @param context the condition context
	 *
	 * // 20201205 {@link org.springframework.core.type.AnnotationMetadata}类或{@link org.springframework.core.type.MethodMetadata}方法的元数据
	 * @param metadata the metadata of the {@link org.springframework.core.type.AnnotationMetadata class}
	 * or {@link org.springframework.core.type.MethodMetadata method} being checked
	 *
	 * // 20201205 {@code true}（如果条件匹配且可以注册该组件），或{@code false}否决带注解的组件的注册
	 * @return {@code true} if the condition matches and the component can be registered,
	 * or {@code false} to veto the annotated component's registration
	 */
	// 202021205 确定条件是否匹配。
	boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata);

}
