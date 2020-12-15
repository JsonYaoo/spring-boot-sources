/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.autoconfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 20201215
 * {@link DeferredImportSelector}处理{@link EnableAutoConfiguration自动配置}。
 * 如果需要{@link EnableAutoConfiguration @EnableAutoConfiguration}的自定义变体，则也可以将该类作为子类。
 */
/**
 * {@link DeferredImportSelector} to handle {@link EnableAutoConfiguration
 * auto-configuration}. This class can also be subclassed if a custom variant of
 * {@link EnableAutoConfiguration @EnableAutoConfiguration} is needed.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Madhura Bhave
 * @since 1.3.0
 * @see EnableAutoConfiguration
 */
// 20201215 EnableAutoConfiguration自动配置：提供一个{@link #getImportGroup（）导入组}，它可以在不同的选择器之间提供附加的排序和过滤逻辑, 能够BeanClassLoader、ResourceLoader、BeanFactory、Environment自省, 能够排序
public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered {

	// 20201215 空的自动装配属性Entry
	private static final AutoConfigurationEntry EMPTY_ENTRY = new AutoConfigurationEntry();

	private static final String[] NO_IMPORTS = {};

	private static final Log logger = LogFactory.getLog(AutoConfigurationImportSelector.class);

	private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

	private ConfigurableListableBeanFactory beanFactory;

	private Environment environment;

	private ClassLoader beanClassLoader;

	private ResourceLoader resourceLoader;

	// 20201215 已注册的配置项过滤器
	private ConfigurationClassFilter configurationClassFilter;

	@Override
	public String[] selectImports(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return NO_IMPORTS;
		}
		AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(annotationMetadata);
		return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
	}

	@Override
	public Predicate<String> getExclusionFilter() {
		return this::shouldExclude;
	}

	private boolean shouldExclude(String configurationClassName) {
		return getConfigurationClassFilter().filter(Collections.singletonList(configurationClassName)).isEmpty();
	}

	/**
	 * // 20201215 根据导入的{@link Configuration @Configuration}类的{@link AnnotationMetadata}返回{@link AutoConfigurationEntry}。
	 * Return the {@link AutoConfigurationEntry} based on the {@link AnnotationMetadata}
	 * of the importing {@link Configuration @Configuration} class.
	 * @param annotationMetadata the annotation metadata of the configuration class
	 * @return the auto-configurations that should be imported
	 */
	// 20201215 【自动装配重点】根据导入的{@link Configuration @Configuration}类的{@link AnnotationMetadata}返回{@link AutoConfigurationEntry} -> 已实现排除
	protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
		// 20201215 判断注解元数据是否开启了自动装配, 默认返回true, 表示开启
		if (!isEnabled(annotationMetadata)) {
			// 20201215 则返回空的自动装配属性Entry
			return EMPTY_ENTRY;
		}

		// 20201215 获取AnnotationMetadata注解元数据的属性
		AnnotationAttributes attributes = getAttributes(annotationMetadata);

		// 20201215【自动装配重点】 返回EnableAutoConfiguration类型工厂实现类的完全限定类名列表 -> 使用给定的类加载器从"META-INF/spring.factories"加载EnableAutoConfiguration类型工厂实现的完全限定类名。
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);

		// 20201215 通过Set集合去除重复的完全限定类名
		configurations = removeDuplicates(configurations);

		// 20201215 返回限制候选配置的所有排除项: 获取"exclude"、"excludeName"、"spring.autoconfigure.exclude"属性列表
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);

		// 20201215 处理所有无效的排除项 -> 抛出非法参数异常
		checkExcludedClasses(configurations, exclusions);

		// 20201215 然后配置项列表移除所有指定的排除项
		configurations.removeAll(exclusions);

		// 20201215 获取配置项过滤器, 配置项过滤器执行过滤所有配置类的完全限定类名 -> 如果不包含ConditionalOnBean、ConditionalOnSingleCandidate、ConditionalOnClass、ConditionalOnWebApplication则被过滤掉
		configurations = getConfigurationClassFilter().filter(configurations);

		// 20201215 处理自动配置导入事件 -> 记录候选者和排除项的类的名称
		fireAutoConfigurationImportEvents(configurations, exclusions);

		// 20201215 使用已贡献的配置及其配置创建一个条目, 并返回
		return new AutoConfigurationEntry(configurations, exclusions);
	}

	// 20201215 返回自动配置导入组Class
	@Override
	public Class<? extends Group> getImportGroup() {
		// 20201215 返回自动配置导入组Class
		return AutoConfigurationGroup.class;
	}

	// 20201215 判断注解元数据是否开启了自动装配, 默认返回true, 表示开启
	protected boolean isEnabled(AnnotationMetadata metadata) {
		// 20201215 如果为AutoConfigurationImportSelector类型
		if (getClass() == AutoConfigurationImportSelector.class) {
			// 20201215 则获取"spring.boot.enableautoconfiguration"键关联的属性值，如果无法解析该键，则返回true。
			return getEnvironment().getProperty(EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY, Boolean.class, true);
		}

		// 20201215 默认返回true, 表示开启
		return true;
	}

	/**
	 * // 20201215 从{@link AnnotationMetadata}返回相应的{@link AnnotationAttributes}。 默认情况下，此方法将返回{@link #getAnnotationClass（）}的属性。
	 * Return the appropriate {@link AnnotationAttributes} from the
	 * {@link AnnotationMetadata}. By default this method will return attributes for
	 * {@link #getAnnotationClass()}.
	 * @param metadata the annotation metadata
	 * @return annotation attributes
	 */
	// 20201215 获取AnnotationMetadata注解元数据的属性
	protected AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
		// 20201215 获取注解Class名称
		String name = getAnnotationClass().getName();

		// 20201209 根据给定的映射返回一个{@link AnnotationAttributes}实例。
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(
				// 20201209 检索name注解的属性（如果有的话）（即，如果在基础元素上定义为直接注解或元注解），也要考虑对组合注解的属性覆盖。
				metadata.getAnnotationAttributes(name, true)
		);
		Assert.notNull(attributes, () -> "No auto-configuration attributes found. Is " + metadata.getClassName()
				+ " annotated with " + ClassUtils.getShortName(name) + "?");
		return attributes;
	}

	/**
	 * Return the source annotation class used by the selector.
	 * @return the annotation class
	 */
	protected Class<?> getAnnotationClass() {
		return EnableAutoConfiguration.class;
	}

	/**
	 * // 20201215 返回应考虑的自动配置类名称。 默认情况下，此方法将使用{@link SpringFactoriesLoader}和{@link #getSpringFactoriesLoaderFactoryClass（）}来加载候选。
	 * Return the auto-configuration class names that should be considered. By default
	 * this method will load candidates using {@link SpringFactoriesLoader} with
	 * {@link #getSpringFactoriesLoaderFactoryClass()}.
	 * @param metadata the source metadata
	 * @param attributes the {@link #getAttributes(AnnotationMetadata) annotation
	 * attributes}
	 * @return a list of candidate configurations
	 */
	// 20201215 【自动装配重点】返回EnableAutoConfiguration类型工厂实现类的完全限定类名列表 -> 使用给定的类加载器从"META-INF/spring.factories"加载EnableAutoConfiguration类型工厂实现的完全限定类名。
	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		// 20201130 使用给定的类加载器从"META-INF/spring.factories"加载EnableAutoConfiguration类型工厂实现的完全限定类名。
		List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
				// 20201215 返回{@link SpringFactoriesLoader}使用的类EnableAutoConfiguration，以加载配置候选者。
				getSpringFactoriesLoaderFactoryClass(),
				getBeanClassLoader()
		);
		Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
				+ "are using a custom packaging, make sure that file is correct.");

		// 20201215 返回EnableAutoConfiguration类型工厂实现类的完全限定类名列表
		return configurations;
	}

	/**
	 * Return the class used by {@link SpringFactoriesLoader} to load configuration
	 * candidates.
	 * @return the factory class
	 */
	// 20201215 返回{@link SpringFactoriesLoader}使用的类，以加载配置候选者。
	protected Class<?> getSpringFactoriesLoaderFactoryClass() {
		return EnableAutoConfiguration.class;
	}

	// 20201215 处理已指定的所有无效排除 -> 抛出非法参数异常
	private void checkExcludedClasses(List<String> configurations, Set<String> exclusions) {
		// 20201215 初始化非法排除项列表
		List<String> invalidExcludes = new ArrayList<>(exclusions.size());

		// 20201215 遍历这些排除项
		for (String exclusion : exclusions) {
			// 20201215 如果排除项又被加载, 但却没有在配置项中出现
			if (ClassUtils.isPresent(exclusion, getClass().getClassLoader()) && !configurations.contains(exclusion)) {
				// 20201215 则添加到非法排除项列表中
				invalidExcludes.add(exclusion);
			}
		}

		// 20201215 如果非法排除项列表不为空
		if (!invalidExcludes.isEmpty()) {
			// 20201215 处理所有无效的排除项 -> 抛出非法参数异常
			handleInvalidExcludes(invalidExcludes);
		}
	}

	/**
	 * Handle any invalid excludes that have been specified.
	 * @param invalidExcludes the list of invalid excludes (will always have at least one
	 * element)
	 */
	// 20201215 处理所有无效的排除项 -> 抛出非法参数异常
	protected void handleInvalidExcludes(List<String> invalidExcludes) {
		StringBuilder message = new StringBuilder();
		for (String exclude : invalidExcludes) {
			message.append("\t- ").append(exclude).append(String.format("%n"));
		}
		throw new IllegalStateException(String.format(
				"The following classes could not be excluded because they are not auto-configuration classes:%n%s",
				message));
	}

	/**
	 * // 20201215 返回限制候选配置的所有排除项。
	 * Return any exclusions that limit the candidate configurations.
	 * @param metadata the source metadata
	 * @param attributes the {@link #getAttributes(AnnotationMetadata) annotation
	 * attributes}
	 * @return exclusions or an empty set
	 */
	// 20201215 返回限制候选配置的所有排除项: 获取"exclude"、"excludeName"、"spring.autoconfigure.exclude"属性列表
	protected Set<String> getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		Set<String> excluded = new LinkedHashSet<>();

		// 20021215 获取"exclude"属性列表
		excluded.addAll(asList(attributes, "exclude"));

		// 20021215 获取"excludeName"属性列表
		excluded.addAll(
				// 20201215 获取以字符串数组形式存储在指定的{@code attributeName}下的值, 然后封装成List列表
				Arrays.asList(attributes.getStringArray("excludeName"))
		);

		// 20201215 获取"spring.autoconfigure.exclude"属性列表
		excluded.addAll(
				// 20201215 使用"spring.autoconfigure.exclude"属性源绑定String[].class, 然后封装成List列表并返回
				getExcludeAutoConfigurationsProperty()
		);
		return excluded;
	}

	/**
	 * // 20201215 返回{@code spring.autoconfigure.exclude}属性所排除的自动配置。
	 * Returns the auto-configurations excluded by the
	 * {@code spring.autoconfigure.exclude} property.
	 * @return excluded auto-configurations
	 * @since 2.3.2
	 */
	// 20201215 使用"spring.autoconfigure.exclude"属性源绑定String[].class, 然后封装成List列表并返回
	protected List<String> getExcludeAutoConfigurationsProperty() {
		// 20201215 获取环境
		Environment environment = getEnvironment();

		// 20201215 环境为空则返回空的集合
		if (environment == null) {
			return Collections.emptyList();
		}

		// 20201215 如果属于配置环境
		if (environment instanceof ConfigurableEnvironment) {
			// 20201202 从指定的环境创建一个新的{@linkbinder}实例。
			Binder binder = Binder.get(environment);

			// 20201215 使用"spring.autoconfigure.exclude"属性源绑定String[].class, 然后封装成List列表并返回
			return binder.bind(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class).map(Arrays::asList).orElse(Collections.emptyList());
		}

		String[] excludes = environment.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class);
		return (excludes != null) ? Arrays.asList(excludes) : Collections.emptyList();
	}

	// 20201215 获取自动装配配置项过滤器 -> 实例化OnBeanCondition、OnClassCondition、OnWebApplicationCondition过滤器
	protected List<AutoConfigurationImportFilter> getAutoConfigurationImportFilters() {
		// 20201215 使用给定的类加载器，从"META-INF/spring.factories"加载并实例化给定类型的工厂实现, 并通过AnnotationAwareOrderComparator进行排序
		// 2020125 org.springframework.boot.autoconfigure.condition.OnBeanCondition
		// 2020125 org.springframework.boot.autoconfigure.condition.OnClassCondition
		// 2020125 org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition
		return SpringFactoriesLoader.loadFactories(AutoConfigurationImportFilter.class, this.beanClassLoader);
	}

	// 20201215 获取配置项过滤器 -> 实例化OnBeanCondition、OnClassCondition、OnWebApplicationCondition过滤器
	private ConfigurationClassFilter getConfigurationClassFilter() {
		// 20201215 如果已注册的配置项过滤器不存在
		if (this.configurationClassFilter == null) {
			// 20201215 获取自动装配配置项过滤器 -> 实例化OnBeanCondition、OnClassCondition、OnWebApplicationCondition过滤器
			List<AutoConfigurationImportFilter> filters = getAutoConfigurationImportFilters();

			// 20201215 遍历过滤器列表
			for (AutoConfigurationImportFilter filter : filters) {
				// 20201215 为过滤器实例绑定意识: BeanClassLoader、BeanFactory、Environment、ResourceLoader
				invokeAwareMethods(filter);
			}

			// 20201215 构建配置项过滤器
			this.configurationClassFilter = new ConfigurationClassFilter(this.beanClassLoader, filters);
		}

		// 20201215 返回配置项过滤器
		return this.configurationClassFilter;
	}

	// 20201215 通过Set集合去除重复的完全限定类名
	protected final <T> List<T> removeDuplicates(List<T> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	protected final List<String> asList(AnnotationAttributes attributes, String name) {
		String[] value = attributes.getStringArray(name);
		return Arrays.asList(value);
	}

	// 20201215 处理自动配置导入事件 -> 记录候选者和排除项的类的名称
	private void fireAutoConfigurationImportEvents(List<String> configurations, Set<String> exclusions) {
		// 20201215 加载org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener实例
		List<AutoConfigurationImportListener> listeners = getAutoConfigurationImportListeners();

		// 20201215 如果自动配置监听器实例不为空
		if (!listeners.isEmpty()) {
			// 20201215 构建导入自动配置类时触发事件
			AutoConfigurationImportEvent event = new AutoConfigurationImportEvent(this, configurations, exclusions);

			// 20201215 遍历所有的自动配置监听器
			for (AutoConfigurationImportListener listener : listeners) {
				// 20201215 为监听器实例绑定意识: BeanClassLoader、BeanFactory、Environment、ResourceLoader
				invokeAwareMethods(listener);

				// 20201215 处理自动配置导入事件 -> 记录候选者和排除项的类的名称
				listener.onAutoConfigurationImportEvent(event);
			}
		}
	}

	// 20201215 加载org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener实例
	protected List<AutoConfigurationImportListener> getAutoConfigurationImportListeners() {
		// 20201215 加载org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener实例
		return SpringFactoriesLoader.loadFactories(AutoConfigurationImportListener.class, this.beanClassLoader);
	}

	// 20201215 为实例绑定意识: BeanClassLoader、BeanFactory、Environment、ResourceLoader
	private void invokeAwareMethods(Object instance) {
		if (instance instanceof Aware) {
			// 20201215 绑定BeanClassLoader意识
			if (instance instanceof BeanClassLoaderAware) {
				((BeanClassLoaderAware) instance).setBeanClassLoader(this.beanClassLoader);
			}

			// 20201215 绑定BeanFactory意识
			if (instance instanceof BeanFactoryAware) {
				((BeanFactoryAware) instance).setBeanFactory(this.beanFactory);
			}

			// 20201215 绑定Environment意识
			if (instance instanceof EnvironmentAware) {
				((EnvironmentAware) instance).setEnvironment(this.environment);
			}

			// 20201215 绑定ResourceLoader意识
			if (instance instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) instance).setResourceLoader(this.resourceLoader);
			}
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	protected final ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	protected ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	protected final Environment getEnvironment() {
		return this.environment;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	protected final ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

	// 20201215 配置项过滤器
	private static class ConfigurationClassFilter {

		// 20201215 配置项注解元数据
		private final AutoConfigurationMetadata autoConfigurationMetadata;

		// 20201215 "spring.factories"过滤器列表
		private final List<AutoConfigurationImportFilter> filters;

		// 20201215 构建配置项过滤器
		ConfigurationClassFilter(ClassLoader classLoader, List<AutoConfigurationImportFilter> filters) {
			this.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(classLoader);
			this.filters = filters;
		}

		// 20201215 配置项过滤器执行过滤所有配置类的完全限定类名
		List<String> filter(List<String> configurations) {
			long startTime = System.nanoTime();
			String[] candidates = StringUtils.toStringArray(configurations);
			boolean skipped = false;

			// 20201215 遍历"spring.factories"过滤器列表
			for (AutoConfigurationImportFilter filter : this.filters) {
				// 20201215 将过滤器应用于给定的自动配置类候选对象 -> 返回匹配结果 -> OnBeanCondition、OnClassCondition、OnWebApplicationCondition
				// 20201215 根据当前配置类的元数据和配置类的完全限定类名列表, 如果不包含ConditionalOnBean、ConditionalOnSingleCandidate、ConditionalOnClass、ConditionalOnWebApplication则返回“不匹配”结果实例
				boolean[] match = filter.match(candidates, this.autoConfigurationMetadata);

				// 20201215 遍历匹配结果, 标记应该跳过的候选组件
				for (int i = 0; i < match.length; i++) {
					// 20201215 过滤掉不匹配的结果
					if (!match[i]) {
						candidates[i] = null;
						skipped = true;
					}
				}
			}

			// 20201215 如果不用跳过, 代表全都通过过滤, 则直接返回所有配置类的完全限定类名
			if (!skipped) {
				return configurations;
			}

			// 20201215 否则只返回剩余的完全限定类名
			List<String> result = new ArrayList<>(candidates.length);
			for (String candidate : candidates) {
				if (candidate != null) {
					result.add(candidate);
				}
			}
			if (logger.isTraceEnabled()) {
				int numberFiltered = configurations.size() - result.size();
				logger.trace("Filtered " + numberFiltered + " auto configuration class in "
						+ TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
			}
			return result;
		}
	}

	// 20201215 自动配置导入组: 用于对来自不同导入选择器的结果进行分组的接口
	private static class AutoConfigurationGroup implements DeferredImportSelector.Group, BeanClassLoaderAware, BeanFactoryAware, ResourceLoaderAware {

		// 20201215 配置类元数据集合
		private final Map<String, AnnotationMetadata> entries = new LinkedHashMap<>();

		// 20201215 自动装配属性Entry列表
		private final List<AutoConfigurationEntry> autoConfigurationEntries = new ArrayList<>();

		private ClassLoader beanClassLoader;

		private BeanFactory beanFactory;

		private ResourceLoader resourceLoader;

		private AutoConfigurationMetadata autoConfigurationMetadata;

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.beanClassLoader = classLoader;
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}

		// 20201215 【自动装配重点】使用指定的{@link DeferredImportSelector}处理导入的{@link Configuration}类的{@link AnnotationMetadata}
		@Override
		public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
			// 20201215 处理器必须为AutoConfigurationImportSelector处理器, 才能处理自动装配注解
			Assert.state(deferredImportSelector instanceof AutoConfigurationImportSelector,
					() -> String.format("Only %s implementations are supported, got %s",
							AutoConfigurationImportSelector.class.getSimpleName(),
							deferredImportSelector.getClass().getName()));

			// 20201215 【自动装配重点】根据导入的{@link Configuration @Configuration}类的{@link AnnotationMetadata}返回{@link AutoConfigurationEntry} -> 已实现排除
			AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector).getAutoConfigurationEntry(annotationMetadata);

			// 20201215 添加到自动装配属性Entry列表
			this.autoConfigurationEntries.add(autoConfigurationEntry);

			// 20201215 遍历每个自动装配属性Entry的配置类的全限定类名列表
			for (String importClassName : autoConfigurationEntry.getConfigurations()) {
				// 20201215 设置配置类元数据集合: 配置类的全限定类名-当前配置类的元数据
				this.entries.putIfAbsent(importClassName, annotationMetadata);
			}
		}

		// 20201215 返回应为此组导入哪个类的{@link Entry条目} -> 对剩余的自动配置类的全限定类名列表进行排序, 并返回排序后的结果列表
		@Override
		public Iterable<Entry> selectImports() {
			// 20201215 如果自动装配属性Entry列表为空, 则返回空列表
			if (this.autoConfigurationEntries.isEmpty()) {
				return Collections.emptyList();
			}

			// 20201215 获取所有排除项的集合
			Set<String> allExclusions = this.autoConfigurationEntries.stream()
					.map(AutoConfigurationEntry::getExclusions).flatMap(Collection::stream).collect(Collectors.toSet());

			// 20201215 获取配置项的集合
			Set<String> processedConfigurations = this.autoConfigurationEntries.stream()
					.map(AutoConfigurationEntry::getConfigurations).flatMap(Collection::stream)
					.collect(Collectors.toCollection(LinkedHashSet::new));

			// 20201215 移除配置项中所有的排除项(一般不会有的了)
			processedConfigurations.removeAll(allExclusions);

			// 20201215 对剩余的自动配置类的全限定类名列表进行排序, 并返回排序后的结果列表
			return sortAutoConfigurations(processedConfigurations, getAutoConfigurationMetadata()).stream()
					.map((importClassName) -> new Entry(this.entries.get(importClassName), importClassName))
					.collect(Collectors.toList());
		}

		private AutoConfigurationMetadata getAutoConfigurationMetadata() {
			if (this.autoConfigurationMetadata == null) {
				this.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
			}
			return this.autoConfigurationMetadata;
		}

		// 20201215 对自动配置类进行排序
		private List<String> sortAutoConfigurations(Set<String> configurations, AutoConfigurationMetadata autoConfigurationMetadata) {
			return new AutoConfigurationSorter(getMetadataReaderFactory(), autoConfigurationMetadata).getInPriorityOrder(configurations);
		}

		private MetadataReaderFactory getMetadataReaderFactory() {
			try {
				return this.beanFactory.getBean(SharedMetadataReaderFactoryContextInitializer.BEAN_NAME,
						MetadataReaderFactory.class);
			}
			catch (NoSuchBeanDefinitionException ex) {
				return new CachingMetadataReaderFactory(this.resourceLoader);
			}
		}

	}

	// 20201215 自动装配属性Entry
	protected static class AutoConfigurationEntry {

		// 20201215 配置类的全限定类名列表
		private final List<String> configurations;

		// 20201215 配置累的排除项的全限定类名列表
		private final Set<String> exclusions;

		private AutoConfigurationEntry() {
			this.configurations = Collections.emptyList();
			this.exclusions = Collections.emptySet();
		}

		/**
		 * Create an entry with the configurations that were contributed and their
		 * exclusions.
		 * @param configurations the configurations that should be imported
		 * @param exclusions the exclusions that were applied to the original list
		 */
		// 20201215 使用已贡献的配置及其配置创建一个条目
		AutoConfigurationEntry(Collection<String> configurations, Collection<String> exclusions) {
			// 20201215 配置类的全限定类名列表
			this.configurations = new ArrayList<>(configurations);

			// 20201215 配置累的排除项的全限定类名列表
			this.exclusions = new HashSet<>(exclusions);
		}

		public List<String> getConfigurations() {
			return this.configurations;
		}

		public Set<String> getExclusions() {
			return this.exclusions;
		}

	}

}
