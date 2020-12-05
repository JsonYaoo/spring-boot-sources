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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 20201205
 * A. 表示仅当所有{@linkplain #value指定条件}都匹配时，组件才有资格注册。
 * B. 条件是可以在要注册Bean定义之前以编程方式确定的任何状态（有关详细信息，请参见{@link Condition}）。
 * C. {@code @Conditional}注解可以通过以下任何一种方式使用：
 * 		a. 作为任何直接或间接用{@code @Component}注解的类的类型级别注解，包括{@link Configuration @Configuration}类
 * 		b. 作为元注解，目的是组成自定义构造型注解
 * 		c. 作为任何{@link Bean @Bean}方法上的方法级注解
 * D. 如果{@code @Configuration}类标记有{@code @Conditional}，则所有与之关联的{@code @Bean}方法，{@link Import @Import}注解和
 *    {@link ComponentScan @ComponentScan}注解, 这些类将受条件限制。
 * E. 注意：不支持{@code @Conditional}注解的继承。 不会考虑超类或重写方法中的任何条件。 为了强制执行这些语义，{@code @Conditional}本身未声明为
 *    {@link java.lang.annotation.Inherited @Inherited}； 此外，任何使用{@code @Conditional}进行元注解的自定义组合注解都不得声明为{@code @Inherited}。
 */
/**
 * A.
 * Indicates that a component is only eligible for registration when all
 * {@linkplain #value specified conditions} match.
 *
 * B.
 * <p>A <em>condition</em> is any state that can be determined programmatically
 * before the bean definition is due to be registered (see {@link Condition} for details).
 *
 * C.
 * <p>The {@code @Conditional} annotation may be used in any of the following ways:
 * <ul>
 * <li>as a type-level annotation on any class directly or indirectly annotated with
 * {@code @Component}, including {@link Configuration @Configuration} classes</li>
 * <li>as a meta-annotation, for the purpose of composing custom stereotype
 * annotations</li>
 * <li>as a method-level annotation on any {@link Bean @Bean} method</li>
 * </ul>
 *
 * D.
 * <p>If a {@code @Configuration} class is marked with {@code @Conditional},
 * all of the {@code @Bean} methods, {@link Import @Import} annotations, and
 * {@link ComponentScan @ComponentScan} annotations associated with that
 * class will be subject to the conditions.
 *
 * E.
 * <p><strong>NOTE</strong>: Inheritance of {@code @Conditional} annotations
 * is not supported; any conditions from superclasses or from overridden
 * methods will not be considered. In order to enforce these semantics,
 * {@code @Conditional} itself is not declared as
 * {@link java.lang.annotation.Inherited @Inherited}; furthermore, any
 * custom <em>composed annotation</em> that is meta-annotated with
 * {@code @Conditional} must not be declared as {@code @Inherited}.
 *
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see Condition
 */
// 20201205 表示仅当所有{@linkplain #value指定条件}都匹配时，组件才有资格注册
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {

	/**
	 * All {@link Condition} classes that must {@linkplain Condition#matches match}
	 * in order for the component to be registered.
	 */
	// 20201205 为了注册组件，必须{@linkplain Condition＃matches匹配}的所有{@link Condition}类。
	Class<? extends Condition>[] value();

}
