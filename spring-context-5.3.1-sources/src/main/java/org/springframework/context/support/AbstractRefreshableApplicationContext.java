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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.lang.Nullable;

/**
 * 20201211
 * A. {@link org.springframework.context.ApplicationContext}实现的基类，应该支持对{@link #refresh（）}的多次调用，每次都创建一个新的内部bean工厂实例。
 *    通常（但不是必须），这样的上下文将由一组配置位置驱动，以从中加载bean定义。
 * B. 子类唯一实现的方法是{@link #loadBeanDefinitions}，每次刷新时都会调用该方法。 一个具体的实现应该将bean定义加载到给定的
 *    {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}中，通常委托给一个或多个特定的bean定义阅读器。
 * C. 注意，WebApplicationContexts有一个类似的基类。 {@link org.springframework.web.context.support.AbstractRefreshableWebApplicationContext}提供了相同的子类化策略，
 *    但还为Web环境预先实现了所有上下文功能。 还有一种预定义的方式来接收Web上下文的配置位置。
 * D. 以特定的bean定义格式读取的该基类的具体独立子类是{@link ClassPathXmlApplicationContext}和{@link FileSystemXmlApplicationContext}，它们都从通用的
 *    {@link AbstractXmlApplicationContext}基类派生； {@link org.springframework.context.annotation.AnnotationConfigApplicationContext}支持使用
 *    {@code @Configuration}注释的类作为Bean定义的源。
 */
/**
 * A.
 * Base class for {@link org.springframework.context.ApplicationContext}
 * implementations which are supposed to support multiple calls to {@link #refresh()},
 * creating a new internal bean factory instance every time.
 * Typically (but not necessarily), such a context will be driven by
 * a set of config locations to load bean definitions from.
 *
 * B.
 * <p>The only method to be implemented by subclasses is {@link #loadBeanDefinitions},
 * which gets invoked on each refresh. A concrete implementation is supposed to load
 * bean definitions into the given
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory},
 * typically delegating to one or more specific bean definition readers.
 *
 * C.
 * <p><b>Note that there is a similar base class for WebApplicationContexts.</b>
 * {@link org.springframework.web.context.support.AbstractRefreshableWebApplicationContext}
 * provides the same subclassing strategy, but additionally pre-implements
 * all context functionality for web environments. There is also a
 * pre-defined way to receive config locations for a web context.
 *
 * D.
 * <p>Concrete standalone subclasses of this base class, reading in a
 * specific bean definition format, are {@link ClassPathXmlApplicationContext}
 * and {@link FileSystemXmlApplicationContext}, which both derive from the
 * common {@link AbstractXmlApplicationContext} base class;
 * {@link org.springframework.context.annotation.AnnotationConfigApplicationContext}
 * supports {@code @Configuration}-annotated classes as a source of bean definitions.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.3
 * @see #loadBeanDefinitions
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
 * @see AbstractXmlApplicationContext
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
// 20201211 支持对{@link #refresh（）}的多次调用，每次都创建一个新的内部bean工厂实例
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	// 20201211 是否允许覆盖BeanDefinition实例
	@Nullable
	private Boolean allowBeanDefinitionOverriding;

	// 20201211 是否允许循环依赖
	@Nullable
	private Boolean allowCircularReferences;

	/** Bean factory for this context. */
	// 20201211 Bean工厂适用于这种情况。
	@Nullable
	private volatile DefaultListableBeanFactory beanFactory;

	/**
	 * Create a new AbstractRefreshableApplicationContext with no parent.
	 */
	public AbstractRefreshableApplicationContext() {
	}

	/**
	 * Create a new AbstractRefreshableApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractRefreshableApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. Default is "true".
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 */
	// 20201211 通过注册具有相同名称的其他定义（自动替换前者）来设置是否应允许它覆盖bean定义。 否则，将引发异常。 默认值为“ true”。
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * 20201211
	 * A. 设置是否在bean之间允许循环引用-并自动尝试解决它们。
	 * B. 默认值为“ true”。 遇到循环引用时，请将其关闭以引发异常，从而完全禁止它们。
	 */
	/**
	 * A.
	 * Set whether to allow circular references between beans - and automatically
	 * try to resolve them.
	 *
	 * B.
	 * <p>Default is "true". Turn this off to throw an exception when encountering
	 * a circular reference, disallowing them completely.
	 *
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 */
	// 20201211 设置是否在bean之间允许循环引用 -> 遇到循环引用时，请将其关闭以引发异常
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}


	/**
	 * This implementation performs an actual refresh of this context's underlying
	 * bean factory, shutting down the previous bean factory (if any) and
	 * initializing a fresh bean factory for the next phase of the context's lifecycle.
	 */
	// 20201211 此实现对此上下文的基础bean工厂执行实际的刷新，关闭前一个bean工厂（如果有），并为上下文生命周期的下一阶段初始化一个新的bean工厂。
	@Override
	protected final void refreshBeanFactory() throws BeansException {
		// 20201211 确定此上下文当前是否拥有Bean工厂，即是否至少刷新一次且尚未关闭。
		if (hasBeanFactory()) {
			// 20201210 销毁该工厂中的所有Beans，包括已注册为一次性的Bean。 在工厂关闭时被调用。
			destroyBeans();

			// 20201211 清空已注册的beanFactory
			closeBeanFactory();
		}

		// 20201211 重新创建新的beanFactory
		try {
			// 20201211 为此上下文创建一个内部bean工厂 -> 创建一个新的DefaultListableBeanFactory -> 注册Cglib动态代理Bean实例化策略、设置父级Bean工厂
			DefaultListableBeanFactory beanFactory = createBeanFactory();

			// 20201211 指定一个ID以进行序列化，如果需要的话，允许将该BeanFactory从该ID反序列化回BeanFactory对象。
			beanFactory.setSerializationId(getId());

			// 20201211 定制此上下文使用的内部bean工厂 -> 是否允许覆盖BeanDefinition实例、是否允许循环依赖
			customizeBeanFactory(beanFactory);


			loadBeanDefinitions(beanFactory);
			this.beanFactory = beanFactory;
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		DefaultListableBeanFactory beanFactory = this.beanFactory;
		if (beanFactory != null) {
			beanFactory.setSerializationId(null);
		}
		super.cancelRefresh(ex);
	}

	// 20201211 清空已注册的beanFactory
	@Override
	protected final void closeBeanFactory() {
		DefaultListableBeanFactory beanFactory = this.beanFactory;
		if (beanFactory != null) {
			beanFactory.setSerializationId(null);
			this.beanFactory = null;
		}
	}

	/**
	 * Determine whether this context currently holds a bean factory,
	 * i.e. has been refreshed at least once and not been closed yet.
	 */
	// 20201211 确定此上下文当前是否拥有Bean工厂，即是否至少刷新一次且尚未关闭。
	protected final boolean hasBeanFactory() {
		return (this.beanFactory != null);
	}

	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		DefaultListableBeanFactory beanFactory = this.beanFactory;
		if (beanFactory == null) {
			throw new IllegalStateException("BeanFactory not initialized or already closed - " +
					"call 'refresh' before accessing beans via the ApplicationContext");
		}
		return beanFactory;
	}

	/**
	 * Overridden to turn it into a no-op: With AbstractRefreshableApplicationContext,
	 * {@link #getBeanFactory()} serves a strong assertion for an active context anyway.
	 */
	@Override
	protected void assertBeanFactoryActive() {
	}

	/**
	 * 20201211
	 * A. 为此上下文创建一个内部bean工厂。 每次尝试{@link #refresh（）}时都要调用。
	 * B. 默认实现创建一个{@link org.springframework.beans.factory.support.DefaultListableBeanFactory}，并将此上下文的父级的
	 *   {@linkplain #getInternalParentBeanFactory（）内部bean工厂}作为父bean工厂。 可以在子类中重写，例如以自定义DefaultListableBeanFactory的设置。
	 */
	/**
	 * A.
	 * Create an internal bean factory for this context.
	 * Called for each {@link #refresh()} attempt.
	 *
	 * B.
	 * <p>The default implementation creates a
	 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
	 * with the {@linkplain #getInternalParentBeanFactory() internal bean factory} of this
	 * context's parent as parent bean factory. Can be overridden in subclasses,
	 * for example to customize DefaultListableBeanFactory's settings.
	 *
	 * @return the bean factory for this context
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowEagerClassLoading
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 */
	// 20201211 为此上下文创建一个内部bean工厂 -> 创建一个新的DefaultListableBeanFactory -> 注册Cglib动态代理Bean实例化策略、设置父级Bean工厂
	protected DefaultListableBeanFactory createBeanFactory() {
		// 20201211 使用给定的父级创建一个新的DefaultListableBeanFactory -> 注册Cglib动态代理Bean实例化策略、设置父级Bean工厂
		return new DefaultListableBeanFactory(
				// 20201211 如果实现了ConfigurableApplicationContext，则返回父上下文的内部bean工厂；否则，返回false。 否则，返回父上下文本身。
				getInternalParentBeanFactory()
		);
	}

	/**
	 * 20201211
	 * A. 定制此上下文使用的内部bean工厂。 每次尝试{@link #refresh（）}时都要调用。
	 * B. 如果指定的话，默认实现将应用此上下文的{@linkplain #setAllowBeanDefinitionOverriding“ allowBeanDefinitionOverriding”}}和
	 *    {@linkplain #setAllowCircularReferences“ allowCircularReferences”}设置。 可以在子类中重写以自定义{@link DefaultListableBeanFactory}的任何设置。
	 */
	/**
	 * A.
	 * Customize the internal bean factory used by this context.
	 * Called for each {@link #refresh()} attempt.
	 *
	 * B.
	 * <p>The default implementation applies this context's
	 * {@linkplain #setAllowBeanDefinitionOverriding "allowBeanDefinitionOverriding"}
	 * and {@linkplain #setAllowCircularReferences "allowCircularReferences"} settings,
	 * if specified. Can be overridden in subclasses to customize any of
	 * {@link DefaultListableBeanFactory}'s settings.
	 *
	 * @param beanFactory the newly created bean factory for this context
	 * @see DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see DefaultListableBeanFactory#setAllowCircularReferences
	 * @see DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 * @see DefaultListableBeanFactory#setAllowEagerClassLoading
	 */
	// 20201211 定制此上下文使用的内部bean工厂 -> 是否允许覆盖BeanDefinition实例、是否允许循环依赖
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		// 20201211 是否允许覆盖BeanDefinition实例
		if (this.allowBeanDefinitionOverriding != null) {
			// 20201208 通过注册具有相同名称的其他定义（自动替换前者）来设置是否应允许它覆盖bean定义。 否则，将引发异常。 这也适用于覆盖别名。
			beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}

		// 20201211 是否允许循环依赖
		if (this.allowCircularReferences != null) {
			// 20201211 设置是否在bean之间允许循环引用 -> 通常建议不要在您的bean之间使用循环引用
			beanFactory.setAllowCircularReferences(this.allowCircularReferences);
		}
	}

	/**
	 * Load bean definitions into the given bean factory, typically through
	 * delegating to one or more bean definition readers.
	 * @param beanFactory the bean factory to load bean definitions into
	 * @throws BeansException if parsing of the bean definitions failed
	 * @throws IOException if loading of bean definition files failed
	 * @see org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	// 20201211 通常通过委派一个或多个bean定义读取器，将bean定义加载到给定的bean工厂中。
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
			throws BeansException, IOException;

}
