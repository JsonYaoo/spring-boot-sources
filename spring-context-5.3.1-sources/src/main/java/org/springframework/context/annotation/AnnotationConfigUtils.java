/*
 * Copyright 2002-2018 the original author or authors.
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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.event.DefaultEventListenerFactory;
import org.springframework.context.event.EventListenerMethodProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Utility class that allows for convenient registration of common
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} and
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}
 * definitions for annotation-based configuration. Also registers a common
 * {@link org.springframework.beans.factory.support.AutowireCandidateResolver}.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 2.5
 * @see ContextAnnotationAutowireCandidateResolver
 * @see ConfigurationClassPostProcessor
 * @see CommonAnnotationBeanPostProcessor
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor
 */
// 20201206 实用程序类，可以方便地注册通用的{@link org.springframework.beans.factory.config.BeanPostProcessor}和
// 20201206 {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}定义，以用于基于注解的配置。 还注册一个通用的
// 20201206 {@link org.springframework.beans.factory.support.AutowireCandidateResolver}。
public abstract class AnnotationConfigUtils {

	/**
	 * The bean name of the internally managed Configuration annotation processor.
	 */
	public static final String CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME =
			"org.springframework.context.annotation.internalConfigurationAnnotationProcessor";

	/**
	 * The bean name of the internally managed BeanNameGenerator for use when processing
	 * {@link Configuration} classes. Set by {@link AnnotationConfigApplicationContext}
	 * and {@code AnnotationConfigWebApplicationContext} during bootstrap in order to make
	 * any custom name generation strategy available to the underlying
	 * {@link ConfigurationClassPostProcessor}.
	 * @since 3.1.1
	 */
	// 20201206 内部管理的BeanNameGenerator的Bean名称，用于在处理{@link Configuration}类时使用。 在引导过程中由{@link AnnotationConfigApplicationContext}和
	// 20201206 {@code AnnotationConfigWebApplicationContext}进行设置，以使任何自定义名称生成策略可用于基础{@link ConfigurationClassPostProcessor}。
	public static final String CONFIGURATION_BEAN_NAME_GENERATOR =
			"org.springframework.context.annotation.internalConfigurationBeanNameGenerator";

	/**
	 * The bean name of the internally managed Autowired annotation processor.
	 */
	public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
			"org.springframework.context.annotation.internalAutowiredAnnotationProcessor";

	/**
	 * The bean name of the internally managed Required annotation processor.
	 * @deprecated as of 5.1, since no Required processor is registered by default anymore
	 */
	@Deprecated
	public static final String REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
			"org.springframework.context.annotation.internalRequiredAnnotationProcessor";

	/**
	 * The bean name of the internally managed JSR-250 annotation processor.
	 */
	public static final String COMMON_ANNOTATION_PROCESSOR_BEAN_NAME =
			"org.springframework.context.annotation.internalCommonAnnotationProcessor";

	/**
	 * The bean name of the internally managed JPA annotation processor.
	 */
	public static final String PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME =
			"org.springframework.context.annotation.internalPersistenceAnnotationProcessor";

	private static final String PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME =
			"org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor";

	/**
	 * The bean name of the internally managed @EventListener annotation processor.
	 */
	public static final String EVENT_LISTENER_PROCESSOR_BEAN_NAME =
			"org.springframework.context.event.internalEventListenerProcessor";

	/**
	 * The bean name of the internally managed EventListenerFactory.
	 */
	public static final String EVENT_LISTENER_FACTORY_BEAN_NAME =
			"org.springframework.context.event.internalEventListenerFactory";

	private static final boolean jsr250Present;

	private static final boolean jpaPresent;

	static {
		ClassLoader classLoader = AnnotationConfigUtils.class.getClassLoader();
		jsr250Present = ClassUtils.isPresent("javax.annotation.Resource", classLoader);
		jpaPresent = ClassUtils.isPresent("javax.persistence.EntityManagerFactory", classLoader) &&
				ClassUtils.isPresent(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, classLoader);
	}


	/**
	 * Register all relevant annotation post processors in the given registry.
	 * @param registry the registry to operate on
	 */
	// 20201209 在给定的注册表中注册所有相关的注解后处理器。
	public static void registerAnnotationConfigProcessors(BeanDefinitionRegistry registry) {
		registerAnnotationConfigProcessors(registry, null);
	}

	/**
	 * Register all relevant annotation post processors in the given registry.
	 * @param registry the registry to operate on
	 * @param source the configuration source element (already extracted)
	 * that this registration was triggered from. May be {@code null}.
	 * @return a Set of BeanDefinitionHolders, containing all bean definitions
	 * that have actually been registered by this call
	 */
	// 20201211 在给定的注册表中注册所有相关的注释后处理器。
	public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		// 20201211 获取没有包装的DefaultListableBeanFactory -> 此上下文的基础bean工厂，可用于注册bean定义
		DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);

		// 20201211 如果获取得到
		if (beanFactory != null) {
			// 20201211 如果根据注解排序, 在添加AnnotationAwareOrderComparator比较器实例
			if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
				// 20201211 为依赖项列表和数组设置一个{@link Comparator}。
				beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
			}

			// 20201211 如果BeanFactory的自动装配候选解析器为 知道上下文自动装配注解类型组件解析器
			if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
				// 20201211 设置此BeanFactory的自定义自动装配候选解析器，以在决定是否应将bean定义视为自动装配的候选者时使用。
				beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
			}
		}

		Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(8);

		if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		// Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
		if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		// Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
		if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			try {
				def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
						AnnotationConfigUtils.class.getClassLoader()));
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
			}
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
		}

		return beanDefs;
	}

	private static BeanDefinitionHolder registerPostProcessor(
			BeanDefinitionRegistry registry, RootBeanDefinition definition, String beanName) {

		definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		registry.registerBeanDefinition(beanName, definition);
		return new BeanDefinitionHolder(definition, beanName);
	}

	// 20201211 获取没有包装的DefaultListableBeanFactory -> 此上下文的基础bean工厂，可用于注册bean定义
	@Nullable
	private static DefaultListableBeanFactory unwrapDefaultListableBeanFactory(BeanDefinitionRegistry registry) {
		if (registry instanceof DefaultListableBeanFactory) {
			// 20201211 返回此上下文的基础bean工厂，可用于注册bean定义
			return (DefaultListableBeanFactory) registry;
		}
		else if (registry instanceof GenericApplicationContext) {
			// 20201211 返回此上下文的基础bean工厂，可用于注册bean定义
			return ((GenericApplicationContext) registry).getDefaultListableBeanFactory();
		}
		else {
			return null;
		}
	}

	// 20201209 处理默认公共注解定义: Lazy, Primary, DependsOn, Role, Description
	public static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd) {
		processCommonDefinitionAnnotations(abd, abd.getMetadata());
	}

	// 20201209 处理默认公共注解定义: Lazy, Primary, DependsOn, Role, Description
	static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd, AnnotatedTypeMetadata metadata) {
		// 20201209 根据Lazy类型的注解以及元数据获取注解属性键值对
		AnnotationAttributes lazy = attributesFor(metadata, Lazy.class);

		// 20201209 如果需要懒加载
		if (lazy != null) {
			// 20201209 如果指定了懒加载, 则设置是否应延迟初始化此bean
			abd.setLazyInit(lazy.getBoolean("value"));
		}

		// 20201209 否则如果注解元数据相同, 则也设置懒加载
		else if (abd.getMetadata() != metadata) {
			lazy = attributesFor(abd.getMetadata(), Lazy.class);
			if (lazy != null) {
				abd.setLazyInit(lazy.getBoolean("value"));
			}
		}

		// 20201209 如果元数据具有Primary注解
		if (metadata.isAnnotated(Primary.class.getName())) {
			// 20201209 设置此bean为自动装配的主要候选对象
			abd.setPrimary(true);
		}

		// 20201209 根据DependsOn类型的注解以及元数据获取注解属性键值对
		AnnotationAttributes dependsOn = attributesFor(metadata, DependsOn.class);
		if (dependsOn != null) {
			// 20201209 设置该bean依赖于初始化的bean的名称。 Bean工厂将确保首先初始化这些Bean。
			abd.setDependsOn(dependsOn.getStringArray("value"));
		}

		// 20201209 根据Role类型的注解以及元数据获取注解属性键值对
		AnnotationAttributes role = attributesFor(metadata, Role.class);
		if (role != null) {
			// 20201209 设置此{@code BeanDefinition}的角色提示。 角色提示为框架和工具提供了特定{@code BeanDefinition}的角色和重要性的指示。
			abd.setRole(role.getNumber("value").intValue());
		}

		// 20201209 根据Description类型的注解以及元数据获取注解属性键值对
		AnnotationAttributes description = attributesFor(metadata, Description.class);
		if (description != null) {
			// 20201209 设置此bean定义的可读描述。
			abd.setDescription(description.getString("value"));
		}
	}

	// 20201209 根据作用域代理模式创建实例 -> 不要创建作用域代理、创建一个JDK动态代理、创建一个基于类的代理（使用CGLIB）
	static BeanDefinitionHolder applyScopedProxyMode(
			ScopeMetadata metadata, BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {

		// 20201209 获取要应用于作用域实例的代理模式。
		ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();

		// 2020129 如果为默认代理默认, 不要创建作用域代理
		if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
			// 20201209 则直接返回具有名称和别名的BeanDefinition的持有人
			return definition;
		}

		// 20201209 如果是创建一个基于类的代理（使用CGLIB）模式, 则为提供的目标bean生成作用域代理，用内部名称注册目标bean并在作用域代理上设置“ targetBeanName”。
		boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
		return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
	}

	// 20201209 根据指定类型的注解以及元数据获取注解属性键值对
	@Nullable
	static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, Class<?> annotationClass) {
		return attributesFor(metadata, annotationClass.getName());
	}

	// 20201209 根据指定类型的注解以及元数据获取注解属性键值对
	@Nullable
	static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, String annotationClassName) {
		// 20201209 根据给定的映射返回一个{@link AnnotationAttributes}实例。
		return AnnotationAttributes.fromMap(
				// 20201209 检索给定类型的注解的属性（如果有的话）（即，如果在基础元素上定义为直接注解或元注解），也要考虑对组合注解的属性覆盖。
				metadata.getAnnotationAttributes(annotationClassName, false)
		);
	}

	static Set<AnnotationAttributes> attributesForRepeatable(AnnotationMetadata metadata,
			Class<?> containerClass, Class<?> annotationClass) {

		return attributesForRepeatable(metadata, containerClass.getName(), annotationClass.getName());
	}

	@SuppressWarnings("unchecked")
	static Set<AnnotationAttributes> attributesForRepeatable(
			AnnotationMetadata metadata, String containerClassName, String annotationClassName) {

		Set<AnnotationAttributes> result = new LinkedHashSet<>();

		// Direct annotation present?
		addAttributesIfNotNull(result, metadata.getAnnotationAttributes(annotationClassName, false));

		// Container annotation present?
		Map<String, Object> container = metadata.getAnnotationAttributes(containerClassName, false);
		if (container != null && container.containsKey("value")) {
			for (Map<String, Object> containedAttributes : (Map<String, Object>[]) container.get("value")) {
				addAttributesIfNotNull(result, containedAttributes);
			}
		}

		// Return merged result
		return Collections.unmodifiableSet(result);
	}

	private static void addAttributesIfNotNull(
			Set<AnnotationAttributes> result, @Nullable Map<String, Object> attributes) {

		if (attributes != null) {
			result.add(AnnotationAttributes.fromMap(attributes));
		}
	}

}
