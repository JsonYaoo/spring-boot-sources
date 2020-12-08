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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * 20201208
 * A. 工厂修改器允许对应用程序上下文的bean定义进行自定义修改，以适应上下文基础bean工厂的bean属性值。
 * B. 对于针对系统管理员的自定义配置文件很有用，这些文件覆盖了在应用程序上下文中配置的Bean属性。 请参阅{@link PropertyResourceConfigurer}及其具体实现，
 *    以了解解决此类配置需求的即用型解决方案。
 * C. {@code BeanFactoryPostProcessor}可以与Bean定义进行交互并对其进行修改，但不能与Bean实例进行交互。 这样做可能会导致bean实例化过早，从而违反了容器并造成了意外的副作用。
 *    如果需要与bean实例交互，请考虑改为实现{@link BeanPostProcessor}。
 * D. 注册: {@code ApplicationContext}在其bean定义中自动检测{@code BeanFactoryPostProcessor} bean，并在创建任何其他bean之前应用它们。
 *    {@code BeanFactoryPostProcessor}也可以通过编程方式向{@code ConfigurableApplicationContext}注册。
 * E. 顺序: 在{@code ApplicationContext}中自动检测到的{@code BeanFactoryPostProcessor} bean将根据{@link org.springframework.core.PriorityOrdered}和
 *    {@link org.springframework.core.Ordered}语义进行排序。 相反，以{@code ConfigurableApplicationContext}编程方式注册的{@code BeanFactoryPostProcessor} bean
 *    将按注册顺序应用； 以编程方式注册的后处理器将忽略通过实现{@code PriorityOrdered}或{@code Ordered}接口表示的任何排序语义。 此外，
 *    {@code BeanFactoryPostProcessor} bean不考虑{@link org.springframework.core.annotation.Order @Order}批注。
 */
/**
 * A.
 * Factory hook that allows for custom modification of an application context's
 * bean definitions, adapting the bean property values of the context's underlying
 * bean factory.
 *
 * B.
 * <p>Useful for custom config files targeted at system administrators that
 * override bean properties configured in the application context. See
 * {@link PropertyResourceConfigurer} and its concrete implementations for
 * out-of-the-box solutions that address such configuration needs.
 *
 * C.
 * <p>A {@code BeanFactoryPostProcessor} may interact with and modify bean
 * definitions, but never bean instances. Doing so may cause premature bean
 * instantiation, violating the container and causing unintended side-effects.
 * If bean instance interaction is required, consider implementing
 * {@link BeanPostProcessor} instead.
 *
 * D.
 * <h3>Registration</h3>
 * <p>An {@code ApplicationContext} auto-detects {@code BeanFactoryPostProcessor}
 * beans in its bean definitions and applies them before any other beans get created.
 * A {@code BeanFactoryPostProcessor} may also be registered programmatically
 * with a {@code ConfigurableApplicationContext}.
 *
 * E.
 * <h3>Ordering</h3>
 * <p>{@code BeanFactoryPostProcessor} beans that are autodetected in an
 * {@code ApplicationContext} will be ordered according to
 * {@link org.springframework.core.PriorityOrdered} and
 * {@link org.springframework.core.Ordered} semantics. In contrast,
 * {@code BeanFactoryPostProcessor} beans that are registered programmatically
 * with a {@code ConfigurableApplicationContext} will be applied in the order of
 * registration; any ordering semantics expressed through implementing the
 * {@code PriorityOrdered} or {@code Ordered} interface will be ignored for
 * programmatically registered post-processors. Furthermore, the
 * {@link org.springframework.core.annotation.Order @Order} annotation is not
 * taken into account for {@code BeanFactoryPostProcessor} beans.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
// 20201208 工厂修改器允许对应用程序上下文的bean定义进行自定义修改，以适应上下文基础bean工厂的bean属性值
@FunctionalInterface
public interface BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException in case of errors
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
