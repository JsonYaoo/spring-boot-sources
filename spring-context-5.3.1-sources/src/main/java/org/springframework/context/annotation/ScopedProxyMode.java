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
 * 20201209
 * A. 枚举各种作用域代理选项。
 * B. 有关确切的作用域代理是什么的更完整的讨论，请参阅Spring参考文档标题为“将作用域bean作为依赖项”的部分。
 */
/**
 * A.
 * Enumerates the various scoped-proxy options.
 *
 * B.
 * <p>For a more complete discussion of exactly what a scoped proxy is, see the
 * section of the Spring reference documentation entitled '<em>Scoped beans as
 * dependencies</em>'.
 *
 * @author Mark Fisher
 * @since 2.5
 * @see ScopeMetadata
 */
// 20201209 枚举各种作用域代理选项
public enum ScopedProxyMode {

	/**
	 * Default typically equals {@link #NO}, unless a different default
	 * has been configured at the component-scan instruction level.
	 */
	// 20201209 默认值通常等于{@link #NO}，除非在组件扫描指令级别配置了其他默认值。
	DEFAULT,

	/**
	 * 20201209
	 * A. 不要创建作用域代理。
	 * B. 与非单一作用域的实例一起使用时，此代理模式通常不可用，它应支持使用{@link #INTERFACES}或{@link #TARGET_CLASS}代理模式，而不是将其用作代理模式依赖性。
	 */
	/**
	 * A.
	 * Do not create a scoped proxy.
	 *
	 * B.
	 * <p>This proxy-mode is not typically useful when used with a
	 * non-singleton scoped instance, which should favor the use of the
	 * {@link #INTERFACES} or {@link #TARGET_CLASS} proxy-modes instead if it
	 * is to be used as a dependency.
	 */
	NO,

	/**
	 * Create a JDK dynamic proxy implementing <i>all</i> interfaces exposed by
	 * the class of the target object.
	 */
	// 20201209 创建一个JDK动态代理，以实现目标对象的类公开的所有接口。
	INTERFACES,

	/**
	 * Create a class-based proxy (uses CGLIB).
	 */
	// 20201209 创建一个基于类的代理（使用CGLIB）。
	TARGET_CLASS

}
