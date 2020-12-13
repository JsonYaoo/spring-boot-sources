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
import org.springframework.lang.Nullable;

/**
 * 20201210
 * A. 工厂挂钩，允许自定义修改新bean实例＆mdash; 例如，检查标记界面或使用代理包装bean。
 * B. 通常，通过标记接口等填充bean的后处理器将实现{@link #postProcessBeforeInitialization}，而使用代理包装bean的后处理器通常将实现{@link #postProcessAfterInitialization}。
 * C. 注册: {@code ApplicationContext}可以在其bean定义中自动检测{@code BeanPostProcessor} bean，并将这些后处理器应用于随后创建的任何bean。 普通的{@code BeanFactory}允许
 *    以编程方式注册后处理器，并将其应用于通过bean工厂创建的所有bean。
 * D. 顺序: 在{@code ApplicationContext}中自动检测到的{@code BeanPostProcessor} bean将根据{@link org.springframework.core.PriorityOrdered}和
 *    {@link org.springframework.core.Ordered}语义进行排序。 相反，以{@code BeanFactory}编程方式注册的{@code BeanPostProcessor} bean将按照注册顺序应用；
 *    以编程方式注册的后处理器将忽略通过实现{@code PriorityOrdered}或{@code Ordered}接口表示的任何排序语义。 此外，{@code BeanPostProcessor} bean不考虑
 *    {@link org.springframework.core.annotation.Order @Order}注解。
 */
/**
 * A.
 * Factory hook that allows for custom modification of new bean instances &mdash;
 * for example, checking for marker interfaces or wrapping beans with proxies.
 *
 * B.
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.
 *
 * C.
 * <h3>Registration</h3>
 * <p>An {@code ApplicationContext} can autodetect {@code BeanPostProcessor} beans
 * in its bean definitions and apply those post-processors to any beans subsequently
 * created. A plain {@code BeanFactory} allows for programmatic registration of
 * post-processors, applying them to all beans created through the bean factory.
 *
 * D.
 * <h3>Ordering</h3>
 * <p>{@code BeanPostProcessor} beans that are autodetected in an
 * {@code ApplicationContext} will be ordered according to
 * {@link org.springframework.core.PriorityOrdered} and
 * {@link org.springframework.core.Ordered} semantics. In contrast,
 * {@code BeanPostProcessor} beans that are registered programmatically with a
 * {@code BeanFactory} will be applied in the order of registration; any ordering
 * semantics expressed through implementing the
 * {@code PriorityOrdered} or {@code Ordered} interface will be ignored for
 * programmatically registered post-processors. Furthermore, the
 * {@link org.springframework.core.annotation.Order @Order} annotation is not
 * taken into account for {@code BeanPostProcessor} beans.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
// 20201210 BeanPostProcessor：允许自定义修改新bean实例, 在Bean生产之前或者之后
public interface BeanPostProcessor {

	/**
	 * 20201213
	 * A. 在任何bean初始化回调（例如InitializingBean的{@code afterPropertiesSet}或自定义的初始化方法）之前，将此{@code BeanPostProcessor}应用于给定的新bean实例。
	 *    该bean将已经用属性值填充。 返回的Bean实例可能是原始实例的包装。
	 * B. 默认实现按原样返回给定的{@code bean}。
	 */
	/**
	 * A.
	 * Apply this {@code BeanPostProcessor} to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 *
	 * B.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 *
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	// 20201213 BeanPostProcessor: bean实例化后初始化前增强方法
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * 20201213
	 * A. 在任何bean初始化回调（例如InitializingBean的{@code afterPropertiesSet}或自定义的初始化方法）之后，将此{@code BeanPostProcessor}应用于给定的新bean实例。
	 *    该bean将已经用属性值填充。 返回的Bean实例可能是原始实例的包装。
	 * B. 对于FactoryBean，将为FactoryBean实例和由FactoryBean创建的对象（从Spring 2.0开始）调用此回调。 后处理器可以通过相应的{@code bean instanceof FactoryBean}
	 *    检查来决定是应用到FactoryBean还是创建的对象，还是两者都应用。
	 * C. 与所有其他{@code BeanPostProcessor}回调相反，此回调还将在{@link InstantiationAwareBeanPostProcessor＃postProcessBeforeInstantiation}方法触发短路后被调用。
	 * D. 默认实现按原样返回给定的{@code bean}。
	 */
	/**
	 * A.
	 * Apply this {@code BeanPostProcessor} to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 *
	 * B.
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 *
	 * C.
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other {@code BeanPostProcessor} callbacks.
	 *
	 * D.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 *
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	// 20201213 BeanPostProcessor: bean实例化后初始化后增强方法
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
