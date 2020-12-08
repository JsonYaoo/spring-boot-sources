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

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201204
 * A. 方便的适配器，用于以编程方式注册Bean类。
 * B. 这是{@link ClassPathBeanDefinitionScanner}的替代方法，它应用相同的注释分辨率，但仅适用于显式注册的类。
 */
/**
 * A.
 * Convenient adapter for programmatic registration of bean classes.
 *
 * B.
 * <p>This is an alternative to {@link ClassPathBeanDefinitionScanner}, applying
 * the same resolution of annotations but for explicitly registered classes only.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 3.0
 * @see AnnotationConfigApplicationContext#register
 */
// 20201204 方便的适配器, 用于以编程方式注册Bean类。
public class AnnotatedBeanDefinitionReader {

	private final BeanDefinitionRegistry registry;

	// 20201208 用于为bean定义生成bean名称的策略接口实例, 默认为AnnotationBeanNameGenerator的实例常量, 用于组件扫描
	private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	// 20201206 用于评估{@link Conditional}注解的内部类
	private ConditionEvaluator conditionEvaluator;

	/**
	 * 20201204
	 * A. 为给定的注册表创建一个新的{@code AnnotatedBeanDefinitionReader}。
	 * B. 如果注册表是{@link EnvironmentCapable}，例如 如果是{@code ApplicationContext}，则将继承{@link Environment}，否则将创建并使用新的
	 *    {@link StandardEnvironment}。
	 */
	/**
	 * A.
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry.
	 *
	 * B.
	 * <p>If the registry is {@link EnvironmentCapable}, e.g. is an {@code ApplicationContext},
	 * the {@link Environment} will be inherited, otherwise a new
	 * {@link StandardEnvironment} will be created and used.
	 *
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry} // 20201204 注册{@code BeanFactory}以便以{@code BeanDefinitionRegistry}的形式将bean定义加载到其中
	 *
	 * @see #AnnotatedBeanDefinitionReader(BeanDefinitionRegistry, Environment)
	 * @see #setEnvironment(Environment)
	 */
	// 20201204 为给定的注册表创建一个方便的适配器, 用于以编程方式注册Bean类。
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, getOrCreateEnvironment(registry));
	}

	/**
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry,
	 * using the given {@link Environment}.
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry}
	 * @param environment the {@code Environment} to use when evaluating bean definition
	 * profiles.
	 * @since 3.1
	 */
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
	}


	/**
	 * Get the BeanDefinitionRegistry that this reader operates on.
	 */
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Set the {@code Environment} to use when evaluating whether
	 * {@link Conditional @Conditional}-annotated component classes should be registered.
	 *
	 * <p>The default is a {@link StandardEnvironment}.
	 *
	 * @see #registerBean(Class, String, Class...)
	 */
	// 20201206 设置使用@Conditional注解组件类时的环境。默认值为{@link StandardEnvironment}。
	public void setEnvironment(Environment environment) {
		// 20201206 使用环境构造用于评估{@link Conditional}注解的内部类, 注册bean定义注册的接口实例、bean工厂实例、当前应用程序正在其中运行的环境的接口实例、资源加载器、类加载器到ConditionEvaluator
		this.conditionEvaluator = new ConditionEvaluator(this.registry, environment, null);
	}

	/**
	 * 20201208
	 * A. 设置{@code BeanNameGenerator}以用于检测到的bean类。
	 * B. 默认值为{@link AnnotationBeanNameGenerator}。
	 */
	/**
	 * A.
	 * Set the {@code BeanNameGenerator} to use for detected bean classes.
	 *
	 * B.
	 * <p>The default is a {@link AnnotationBeanNameGenerator}.
	 */
	// 20201208 设置{@code BeanNameGenerator}以用于检测到的bean类, 默认值为{@link AnnotationBeanNameGenerator}。
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		// 20201208 设置用于为bean定义生成bean名称的策略接口实例
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : AnnotationBeanNameGenerator.INSTANCE);
	}

	/**
	 * Set the {@code ScopeMetadataResolver} to use for registered component classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}

	/**
	 * 20201208
	 * A. 注册一个或多个要处理的组件类。
	 * B. 对{@code register}的调用是幂等的； 多次添加同一组件类不会产生任何其他影响。
	 */
	/**
	 * A.
	 * Register one or more component classes to be processed.
	 *
	 * B.
	 * <p>Calls to {@code register} are idempotent; adding the same
	 * component class more than once has no additional effect.
	 *
	 * @param componentClasses one or more component classes,
	 * e.g. {@link Configuration @Configuration} classes // 20201208 一个或多个组件类，例如 {@link Configuration @Configuration}类
	 */
	// 20201208 注册一个或多个要处理的组件类
	public void register(Class<?>... componentClasses) {
		// 20201208 遍历组件bean类列表
		for (Class<?> componentClass : componentClasses) {
			// 20201208 注册每个一个组件bean类
			registerBean(componentClass);
		}
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 */
	// 20201208 从给定的bean类中注册一个bean，并从类声明的注释中派生其元数据。
	public void registerBean(Class<?> beanClass) {
		// 20201208 从给定的bean类中注册一个bean，并从类声明的注解中派生其元数据。
		doRegisterBean(beanClass, null, null, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @since 5.2
	 */
	public void registerBean(Class<?> beanClass, @Nullable String name) {
		doRegisterBean(beanClass, name, null, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> beanClass, Class<? extends Annotation>... qualifiers) {
		doRegisterBean(beanClass, null, qualifiers, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> beanClass, @Nullable String name,
			Class<? extends Annotation>... qualifiers) {

		doRegisterBean(beanClass, name, qualifiers, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param beanClass the class of the bean
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> beanClass, @Nullable Supplier<T> supplier) {
		doRegisterBean(beanClass, null, null, supplier, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> beanClass, @Nullable String name, @Nullable Supplier<T> supplier) {
		doRegisterBean(beanClass, name, null, supplier, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.2
	 */
	public <T> void registerBean(Class<T> beanClass, @Nullable String name, @Nullable Supplier<T> supplier,
			BeanDefinitionCustomizer... customizers) {

		doRegisterBean(beanClass, name, null, supplier, customizers);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 *
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * @param qualifiers specific qualifier annotations to consider, if any,
	 * in addition to qualifiers at the bean class level
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 */
	// 20201208 从给定的bean类中注册一个bean，并从类声明的注解中派生其元数据。
	private <T> void doRegisterBean(Class<T> beanClass,// 20201208 待处理的bean类
									@Nullable String name,// 20201208 Bean的显式名称
									@Nullable Class<? extends Annotation>[] qualifiers,// 20201208 Bean类级别上的限定符，如果有的话，还要考虑特定的限定符注解
									@Nullable Supplier<T> supplier,// 20201208 用于创建bean实例的回调（可以为{@code null}）
									@Nullable BeanDefinitionCustomizer[] customizers// 20201208 一个或多个用于自定义工厂的{@link BeanDefinition}的回调，例如 设置惰性初始或主要标志
	) {
		// 20201208 为给定的bean类创建一个新的注解数据公开类AnnotatedGenericBeanDefinition —> 注册注解实例(但过滤"java.lang", "org.springframework.lang"下的注解)
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);

		// 20201208 用于评估{@link Conditional}注解的内部类
		if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
			return;
		}

		abd.setInstanceSupplier(supplier);
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());
		String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		if (qualifiers != null) {
			for (Class<? extends Annotation> qualifier : qualifiers) {
				if (Primary.class == qualifier) {
					abd.setPrimary(true);
				}
				else if (Lazy.class == qualifier) {
					abd.setLazyInit(true);
				}
				else {
					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
				}
			}
		}
		if (customizers != null) {
			for (BeanDefinitionCustomizer customizer : customizers) {
				customizer.customize(abd);
			}
		}

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
	}


	/**
	 * Get the Environment from the given registry if possible, otherwise return a new
	 * StandardEnvironment.
	 */
	// 20201204 如果可能，从给定的注册表中获取环境，否则返回一个新的StandardEnvironment。
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		// 20201204 注册表不能为null
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

		// 20201204 如果注册表属于顶层环境EnvironmentCapable的子类, 说明为Web环境
		if (registry instanceof EnvironmentCapable) {
			// 20201204 则获取子类的环境实例
			return ((EnvironmentCapable) registry).getEnvironment();
		}

		// 20201204 否则说明不为Web环境, 则初始化一个标准环境(非Web)
		return new StandardEnvironment();
	}

}
