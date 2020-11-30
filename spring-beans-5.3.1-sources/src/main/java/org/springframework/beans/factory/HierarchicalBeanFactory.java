/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * 20201130
 * A. 子接口由bean工厂实现，可以是层次结构的一部分。
 * B. 可以在ConfigurableBeanFactory接口中找到用于bean工厂的{@code setParentBeanFactory}方法，该方法允许以configurable方式设置父级。
 */
/**
 * A.
 * Sub-interface implemented by bean factories that can be part
 * of a hierarchy.
 *
 * B.
 * <p>The corresponding {@code setParentBeanFactory} method for bean
 * factories that allow setting the parent in a configurable
 * fashion can be found in the ConfigurableBeanFactory interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 07.07.2003
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
 */
// 20201130 层次bean工厂 => 声明获取父bean工厂 & 判断本地bean工厂是否包含具体bean 的方法
public interface HierarchicalBeanFactory extends BeanFactory {

	/**
	 * Return the parent bean factory, or {@code null} if there is none.
	 */
	// 20201130 返回父bean工厂，如果没有父bean工厂，则返回{@code null}。
	@Nullable
	BeanFactory getParentBeanFactory();

	/**
	 * Return whether the local bean factory contains a bean of the given name,
	 * ignoring beans defined in ancestor contexts.
	 * <p>This is an alternative to {@code containsBean}, ignoring a bean
	 * of the given name from an ancestor bean factory.
	 * @param name the name of the bean to query
	 * @return whether a bean with the given name is defined in the local factory
	 * @see BeanFactory#containsBean
	 */
	// 20201130 返回本地bean工厂是否包含给定名称的bean，忽略在祖先上下文中定义的bean。这是{@code containsBean}的一个替代方法，它忽略来自祖先bean工厂的给定名称的bean。
	boolean containsLocalBean(String name);

}
