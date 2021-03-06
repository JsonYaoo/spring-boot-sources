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

import org.springframework.beans.BeansException;

/**
 * 20201207
 * A. Aware beans自己的bean工厂
 * B. 例如，bean可以通过工厂（Dependency Lookup）来查找协作bean。 注意，大多数bean将选择通过相应的bean属性或构造函数参数（依赖注入）来接收对协作bean的引用。
 * C. 有关所有bean生命周期方法的列表，请参见{@link BeanFactory BeanFactory javadocs}。
 */
/**
 * A.
 * Interface to be implemented by beans that wish to be aware of their
 * owning {@link BeanFactory}.
 *
 * B.
 * <p>For example, beans can look up collaborating beans via the factory
 * (Dependency Lookup). Note that most beans will choose to receive references
 * to collaborating beans via corresponding bean properties or constructor
 * arguments (Dependency Injection).
 *
 * C.
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 11.03.2003
 * @see BeanNameAware
 * @see BeanClassLoaderAware
 * @see InitializingBean
 * @see org.springframework.context.ApplicationContextAware
 */
// 20201207 BeanFactory自觉接口, Aware beans自己的bean工厂
public interface BeanFactoryAware extends Aware {

	/**
	 * 20201211
	 * A. 将拥有的工厂提供给Bean实例的回调。
	 * B. 在填充正常的bean属性之后但在初始化回调（例如{@link InitializingBean＃afterPropertiesSet（）}或自定义的init方法）之前调用。
	 */
	/**
	 * A.
	 * Callback that supplies the owning factory to a bean instance.
	 *
	 * B.
	 * <p>Invoked after the population of normal bean properties
	 * but before an initialization callback such as
	 * {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
	 *
	 * @param beanFactory owning BeanFactory (never {@code null}).
	 * @throws BeansException in case of initialization errors
	 * @see BeanInitializationException
	 */
	// 20201211 将拥有的工厂提供给Bean实例的回调
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
