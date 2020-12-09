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

/**
 * A {@link Condition} that offers more fine-grained control when used with
 * {@code @Configuration}. Allows certain conditions to adapt when they match
 * based on the configuration phase. For example, a condition that checks if a bean
 * has already been registered might choose to only be evaluated during the
 * {@link ConfigurationPhase#REGISTER_BEAN REGISTER_BEAN} {@link ConfigurationPhase}.
 *
 * @author Phillip Webb
 * @since 4.0
 * @see Configuration
 */
// 20201205 与{@code @Configuration}配合使用时，{@link Condition}可以提供更精细的控制。 当某些条件根据配置阶段匹配时允许进行调整。
// 20201205 例如，检查bean是否已经注册的条件可能选择仅在{@link ConfigurationPhase＃REGISTER_BEAN REGISTER_BEAN} {@link ConfigurationPhase}期间进行评估。
public interface ConfigurationCondition extends Condition {

	/**
	 * Return the {@link ConfigurationPhase} in which the condition should be evaluated.
	 */
	// 20201209 返回{@link ConfigurationPhase}，其中应评估条件。
	ConfigurationPhase getConfigurationPhase();

	/**
	 * The various configuration phases where the condition could be evaluated.
	 */
	// 20201208 可以评估条件的各种配置阶段。
	enum ConfigurationPhase {

		/**
		 * 20201208
		 * A. 应该在解析{@code @Configuration}类时评估{@link Condition}。
		 * B. 如果此时条件不匹配，则不会添加{@code @Configuration}类。
		 */
		/**
		 * A.
		 * The {@link Condition} should be evaluated as a {@code @Configuration}
		 * class is being parsed.
		 *
		 * B.
		 * <p>If the condition does not match at this point, the {@code @Configuration}
		 * class will not be added.
		 */
		// 20201208 配置阶段: 应该在解析{@code @Configuration}类时评估{@link Condition}
		PARSE_CONFIGURATION,

		/**
		 * 20201208
		 * A. 添加常规非（{@code @Configuration}）bean时，应评估{@link Condition}。 该条件不会阻止添加{@code @Configuration}类。
		 * B. 在评估条件时，将解析所有{@code @Configuration}类。
		 */
		/**
		 * A.
		 * The {@link Condition} should be evaluated when adding a regular
		 * (non {@code @Configuration}) bean. The condition will not prevent
		 * {@code @Configuration} classes from being added.
		 *
		 * B.
		 * <p>At the time that the condition is evaluated, all {@code @Configuration}
		 * classes will have been parsed.
		 */
		// 20201208 Bean注册阶段: 添加常规非（{@code @Configuration}）bean时，应评估{@link Condition}。 该条件不会阻止添加{@code @Configuration}类。
		REGISTER_BEAN
	}

}
