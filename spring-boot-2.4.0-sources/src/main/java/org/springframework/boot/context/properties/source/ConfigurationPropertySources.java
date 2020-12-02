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

package org.springframework.boot.context.properties.source;

import java.util.Collections;
import java.util.stream.Stream;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySource.StubPropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;

/**
 * Provides access to {@link ConfigurationPropertySource ConfigurationPropertySources}.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
// 20201202 提供对{@link ConfigurationPropertySource ConfigurationPropertySources}的访问。
public final class ConfigurationPropertySources {

	/**
	 * The name of the {@link PropertySource} {@link #attach(Environment) adapter}.
	 */
	// 20201202 {@link PropertySource}{@link #attach（Environment）适配器}的名称。
	private static final String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

	private ConfigurationPropertySources() {
	}

	/**
	 * Determines if the specific {@link PropertySource} is the
	 * {@link ConfigurationPropertySource} that was {@link #attach(Environment) attached}
	 * to the {@link Environment}.
	 * @param propertySource the property source to test
	 * @return {@code true} if this is the attached {@link ConfigurationPropertySource}
	 */
	public static boolean isAttachedConfigurationPropertySource(PropertySource<?> propertySource) {
		return ATTACHED_PROPERTY_SOURCE_NAME.equals(propertySource.getName());
	}

	/**
	 * 20201202
	 * A. 将{@link ConfigurationPropertySource}支持附加到指定的{@link Environment}。将环境管理的每个{@link PropertySource}适配为
	 *    {@link ConfigurationPropertySource}，并允许使用{@link ConfigurationPropertyName}配置属性名称解析经典的{@link PropertySourcesPropertyResolver}调用。
	 * B. 附加的解析器将动态跟踪底层{@link Environment}属性源的任何添加或删除。
	 */
	/**
	 * A.
	 * Attach a {@link ConfigurationPropertySource} support to the specified
	 * {@link Environment}. Adapts each {@link PropertySource} managed by the environment
	 * to a {@link ConfigurationPropertySource} and allows classic
	 * {@link PropertySourcesPropertyResolver} calls to resolve using
	 * {@link ConfigurationPropertyName configuration property names}.
	 *
	 * B.
	 * <p>
	 * The attached resolver will dynamically track any additions or removals from the
	 * underlying {@link Environment} property sources.
	 * @param environment the source environment (must be an instance of
	 * {@link ConfigurableEnvironment})
	 * @see #get(Environment)
	 */
	// 20201202 环境绑定属性源
	public static void attach(Environment environment) {
		// 20201202 environment必须是ConfigurableEnvironment.class类型
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment);

		// 20201202 获取环境属性源
		MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();

		// 20201202 获取属性源中的configurationProperties属性 -> 适配器(Spring的属性源)
		PropertySource<?> attached = sources.get(ATTACHED_PROPERTY_SOURCE_NAME);

		// 20201202 如果适配器中的属性源(Spring的属性源)不为该属性源
		if (attached != null && attached.getSource() != sources) {
			// 20201202 则移除该适配器(Spring的属性源)
			sources.remove(ATTACHED_PROPERTY_SOURCE_NAME);

			// 20201202 不绑定任何适配器(Spring的属性源)
			attached = null;
		}

		// 20201202 如果适配器为空
		if (attached == null) {
			// 20201202 添加新的适配器(Spring的属性源)在属性元的末尾
			sources.addFirst(new ConfigurationPropertySourcesPropertySource(ATTACHED_PROPERTY_SOURCE_NAME,
					new SpringConfigurationPropertySources(sources)));
		}
	}

	/**
	 * Return a set of {@link ConfigurationPropertySource} instances that have previously
	 * been {@link #attach(Environment) attached} to the {@link Environment}.
	 * @param environment the source environment (must be an instance of
	 * {@link ConfigurableEnvironment})
	 * @return an iterable set of configuration property sources
	 * @throws IllegalStateException if not configuration property sources have been
	 * attached
	 */
	// 20201202 返回一组{@link ConfigurationPropertySource}实例，这些实例以前是{@link #attach（Environment）}附加到{@link Environment}。
	public static Iterable<ConfigurationPropertySource> get(Environment environment) {
		// 20201202 environment必须是ConfigurableEnvironment.class的实例
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment);

		// 20201202 获取环境的属性源
		MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();

		// 20201202 获取适配器的属性源 -> Spring属性源
		ConfigurationPropertySourcesPropertySource attached = (ConfigurationPropertySourcesPropertySource) sources
				.get(ATTACHED_PROPERTY_SOURCE_NAME);

		// 20201202 如果Spring属性源为空
		if (attached == null) {
			// 20201202 则新构建一个Spring属性源并返回
			return from(sources);
		}

		// 20201202 如果Spring属性源不为空, 则从Spring属性源中获取所有属性源
		return attached.getSource();
	}

	/**
	 * Return {@link Iterable} containing a single new {@link ConfigurationPropertySource}
	 * adapted from the given Spring {@link PropertySource}.
	 * @param source the Spring property source to adapt
	 * @return an {@link Iterable} containing a single newly adapted
	 * {@link SpringConfigurationPropertySource}
	 */
	public static Iterable<ConfigurationPropertySource> from(PropertySource<?> source) {
		return Collections.singleton(ConfigurationPropertySource.from(source));
	}

	/**
	 * 20201202
	 * A. 返回{@link Iterable}，其中包含新的{@link ConfigurationPropertySource}实例，这些实例改编自给定的Spring{@link PropertySource PropertySources}。
	 * B. 此方法将展平所有嵌套的属性源，并过滤所有{@link StubPropertySource stub property sources}。对底层源代码的更新（由其迭代器返回的源代码中的更改标识）将被自动跟踪。
	 *    底层源应该是线程安全的，例如{@link MutablePropertySources}
	 */
	/**
	 * A.
	 * Return {@link Iterable} containing new {@link ConfigurationPropertySource}
	 * instances adapted from the given Spring {@link PropertySource PropertySources}.
	 *
	 * B.
	 * <p>
	 * This method will flatten any nested property sources and will filter all
	 * {@link StubPropertySource stub property sources}. Updates to the underlying source,
	 * identified by changes in the sources returned by its iterator, will be
	 * automatically tracked. The underlying source should be thread safe, for example a
	 * {@link MutablePropertySources}
	 *
	 * @param sources the Spring property sources to adapt
	 * @return an {@link Iterable} containing newly adapted
	 * {@link SpringConfigurationPropertySource} instances
	 */
	// 20201202 展开迭代器中的所有属性源 -> Iterable<PropertySource<?>>是顶层接口
	public static Iterable<ConfigurationPropertySource> from(Iterable<PropertySource<?>> sources) {
		// 20201202 新构建一个Spring属性源
		return new SpringConfigurationPropertySources(sources);
	}

	private static Stream<PropertySource<?>> streamPropertySources(PropertySources sources) {
		return sources.stream().flatMap(ConfigurationPropertySources::flatten)
				.filter(ConfigurationPropertySources::isIncluded);
	}

	private static Stream<PropertySource<?>> flatten(PropertySource<?> source) {
		if (source.getSource() instanceof ConfigurableEnvironment) {
			return streamPropertySources(((ConfigurableEnvironment) source.getSource()).getPropertySources());
		}
		return Stream.of(source);
	}

	private static boolean isIncluded(PropertySource<?> source) {
		return !(source instanceof StubPropertySource)
				&& !(source instanceof ConfigurationPropertySourcesPropertySource);
	}

}
