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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

/**
 * Internal class used to evaluate {@link Conditional} annotations.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 4.0
 */
// 20201205 用于评估{@link Conditional}注解的内部类。
class ConditionEvaluator {

	// 20201206 ConditionContext实现: bean定义注册的接口实例、bean工厂实例、当前应用程序正在其中运行的环境的接口实例、资源加载器、类加载器
	private final ConditionContextImpl context;

	/**
	 * Create a new {@link ConditionEvaluator} instance.
	 */
	// 20201206 创建一个新的{@link ConditionEvaluator}实例。
	public ConditionEvaluator(@Nullable BeanDefinitionRegistry registry, @Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {
		// 20201206 注册bean定义注册的接口实例、bean工厂实例、当前应用程序正在其中运行的环境的接口实例、资源加载器、类加载器
		this.context = new ConditionContextImpl(registry, environment, resourceLoader);
	}


	/**
	 * Determine if an item should be skipped based on {@code @Conditional} annotations.
	 * The {@link ConfigurationPhase} will be deduced from the type of item (i.e. a
	 * {@code @Configuration} class will be {@link ConfigurationPhase#PARSE_CONFIGURATION})
	 * @param metadata the meta data
	 * @return if the item should be skipped
	 */
	public boolean shouldSkip(AnnotatedTypeMetadata metadata) {
		return shouldSkip(metadata, null);
	}

	/**
	 * Determine if an item should be skipped based on {@code @Conditional} annotations.
	 * @param metadata the meta data
	 * @param phase the phase of the call
	 * @return if the item should be skipped
	 */
	public boolean shouldSkip(@Nullable AnnotatedTypeMetadata metadata, @Nullable ConfigurationPhase phase) {
		if (metadata == null || !metadata.isAnnotated(Conditional.class.getName())) {
			return false;
		}

		if (phase == null) {
			if (metadata instanceof AnnotationMetadata &&
					ConfigurationClassUtils.isConfigurationCandidate((AnnotationMetadata) metadata)) {
				return shouldSkip(metadata, ConfigurationPhase.PARSE_CONFIGURATION);
			}
			return shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN);
		}

		List<Condition> conditions = new ArrayList<>();
		for (String[] conditionClasses : getConditionClasses(metadata)) {
			for (String conditionClass : conditionClasses) {
				Condition condition = getCondition(conditionClass, this.context.getClassLoader());
				conditions.add(condition);
			}
		}

		AnnotationAwareOrderComparator.sort(conditions);

		for (Condition condition : conditions) {
			ConfigurationPhase requiredPhase = null;
			if (condition instanceof ConfigurationCondition) {
				requiredPhase = ((ConfigurationCondition) condition).getConfigurationPhase();
			}
			if ((requiredPhase == null || requiredPhase == phase) && !condition.matches(this.context, metadata)) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private List<String[]> getConditionClasses(AnnotatedTypeMetadata metadata) {
		MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(Conditional.class.getName(), true);
		Object values = (attributes != null ? attributes.get("value") : null);
		return (List<String[]>) (values != null ? values : Collections.emptyList());
	}

	private Condition getCondition(String conditionClassName, @Nullable ClassLoader classloader) {
		Class<?> conditionClass = ClassUtils.resolveClassName(conditionClassName, classloader);
		return (Condition) BeanUtils.instantiateClass(conditionClass);
	}


	/**
	 * Implementation of a {@link ConditionContext}.
	 */
	// 20201206 {@link ConditionContext}的实现。
	private static class ConditionContextImpl implements ConditionContext {

		// 20201206 bean定义注册的接口实例
		@Nullable
		private final BeanDefinitionRegistry registry;

		// 20201206 bean工厂实例
		@Nullable
		private final ConfigurableListableBeanFactory beanFactory;

		// 20201206 当前应用程序正在其中运行的环境的接口实例
		private final Environment environment;

		// 20201206 资源加载器
		private final ResourceLoader resourceLoader;

		// 20201206 类加载器
		@Nullable
		private final ClassLoader classLoader;

		// 20201206 构造ConditionContext实现
		public ConditionContextImpl(@Nullable BeanDefinitionRegistry registry, @Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {
			// 20201206 设置bean定义注册的接口实例
			this.registry = registry;

			// 20201206 根据注册表实例推断并设置Bean工厂实例
			this.beanFactory = deduceBeanFactory(registry);

			// 20201206 根据注册表推断并注册当前环境实例
			this.environment = (environment != null ? environment : deduceEnvironment(registry));

			// 20201206 根据注册表推断并注册资源加载器
			this.resourceLoader = (resourceLoader != null ? resourceLoader : deduceResourceLoader(registry));

			// 20201206 根据注册表推断并注册类加载器
			this.classLoader = deduceClassLoader(resourceLoader, this.beanFactory);
		}

		// 20201206 根据注册表实例推断Bean工厂实例
		@Nullable
		private ConfigurableListableBeanFactory deduceBeanFactory(@Nullable BeanDefinitionRegistry source) {
			// 20201206 如果注册表属于DefaultListableBeanFactory则直接强转返回
			if (source instanceof ConfigurableListableBeanFactory) {
				return (ConfigurableListableBeanFactory) source;
			}

			// 20201206 否则根据注册表上下文获取
			if (source instanceof ConfigurableApplicationContext) {
				return (((ConfigurableApplicationContext) source).getBeanFactory());
			}

			// 20201206 如果获取不到则为null
			return null;
		}

		// 20201206 根据注册表推断当前环境实例
		private Environment deduceEnvironment(@Nullable BeanDefinitionRegistry source) {
			// 20201206 如果能直接拿到环境实例则返回
			if (source instanceof EnvironmentCapable) {
				return ((EnvironmentCapable) source).getEnvironment();
			}

			// 20201206 否则返回标准环境(非Web)实例
			return new StandardEnvironment();
		}

		// 20201206 根据注册表推断资源加载器
		private ResourceLoader deduceResourceLoader(@Nullable BeanDefinitionRegistry source) {
			// 20201206 如果本身就是个资源加载器, 则强转后返回
			if (source instanceof ResourceLoader) {
				return (ResourceLoader) source;
			}

			// 20201206 否则返回新建的DefaultResourceLoader
			return new DefaultResourceLoader();
		}

		// 20201206 根据注册表推断类加载器
		@Nullable
		private ClassLoader deduceClassLoader(@Nullable ResourceLoader resourceLoader, @Nullable ConfigurableListableBeanFactory beanFactory) {
			// 20201206 如果资源加载器不为空, 则返回资源加载器的类加载器
			if (resourceLoader != null) {
				ClassLoader classLoader = resourceLoader.getClassLoader();
				if (classLoader != null) {
					return classLoader;
				}
			}

			// 20201206 否则获取bean工厂实例的类加载器
			if (beanFactory != null) {
				return beanFactory.getBeanClassLoader();
			}

			// 20201206 再否则返回默认的类加载器: 获取默认类加载器, 线程上下文类加载器 -> ClassUtils类加载器 -> 系统加载器
			return ClassUtils.getDefaultClassLoader();
		}

		@Override
		public BeanDefinitionRegistry getRegistry() {
			Assert.state(this.registry != null, "No BeanDefinitionRegistry available");
			return this.registry;
		}

		@Override
		@Nullable
		public ConfigurableListableBeanFactory getBeanFactory() {
			return this.beanFactory;
		}

		@Override
		public Environment getEnvironment() {
			return this.environment;
		}

		@Override
		public ResourceLoader getResourceLoader() {
			return this.resourceLoader;
		}

		@Override
		@Nullable
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

}
