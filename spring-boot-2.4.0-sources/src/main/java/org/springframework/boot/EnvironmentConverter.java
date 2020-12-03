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

package org.springframework.boot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * Utility class for converting one type of {@link Environment} to another.
 *
 * @author Ethan Rubinson
 * @author Andy Wilkinson
 * @author Madhura Bhave
 */
// 20201203 用于将{@link Environment}的一种类型转换为另一种类型的实用程序类。
final class EnvironmentConverter {

	private static final String CONFIGURABLE_WEB_ENVIRONMENT_CLASS = "org.springframework.web.context.ConfigurableWebEnvironment";

	private static final Set<String> SERVLET_ENVIRONMENT_SOURCE_NAMES;

	static {
		Set<String> names = new HashSet<>();
		names.add(StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME);
		names.add(StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME);
		names.add(StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME);
		SERVLET_ENVIRONMENT_SOURCE_NAMES = Collections.unmodifiableSet(names);
	}

	private final ClassLoader classLoader;

	/**
	 * Creates a new {@link EnvironmentConverter} that will use the given
	 * {@code classLoader} during conversion.
	 * @param classLoader the class loader to use
	 */
	// 20201203 创建一个新的{@link EnvironmentConverter}，它将在转换期间使用给定的{@code classLoader}。
	EnvironmentConverter(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Converts the given {@code environment} to the given {@link StandardEnvironment}
	 * type. If the environment is already of the same type, no conversion is performed
	 * and it is returned unchanged.
	 * @param environment the Environment to convert	// 20201203 要转换的环境
	 * @param type the type to convert the Environment to	// 20201203 要将环境转换为的类型
	 * @return the converted Environment // 20201203 转换后的环境
	 */
	// 20201203 将给定的{@code environment}转换为给定的{@link StandardEnvironment}类型。如果环境已经是同一类型，则不执行任何转换，并且返回原样。
	StandardEnvironment convertEnvironmentIfNecessary(ConfigurableEnvironment environment, Class<? extends StandardEnvironment> type) {
		// 20201203 如果原来环境已经属于目标类型
		if (type.equals(environment.getClass())) {
			// 20201203 则强转后直接返回即可
			return (StandardEnvironment) environment;
		}

		// 20201203 否则需要进行环境转换
		return convertEnvironment(environment, type);
	}

	// 20201203 执行环境转换
	private StandardEnvironment convertEnvironment(ConfigurableEnvironment environment, Class<? extends StandardEnvironment> type) {
		// 20201203 实例化目标类型的环境
		StandardEnvironment result = createEnvironment(type);

		// 20201203 设置激活的配置文件集 spring.boot.active
		result.setActiveProfiles(environment.getActiveProfiles());

		// 20201203 设置property转换服务
		result.setConversionService(
				// 20201203 返回对属性执行类型转换时使用的{@link ConfigurableConversionService}。
				environment.getConversionService()
		);

		// 20201203 拷贝Servlet属性源到目标环境result中
		copyPropertySources(environment, result);

		// 20201203 返回构建好的目标环境
		return result;
	}

	// 20201203 构建目标类型的环境
	private StandardEnvironment createEnvironment(Class<? extends StandardEnvironment> type) {
		try {
			// 20201203 通过构造器构造一个环境实例
			return type.getDeclaredConstructor().newInstance();
		}
		catch (Exception ex) {
			return new StandardEnvironment();
		}
	}

	// 20201203 拷贝Servlet属性源到目标环境
	private void copyPropertySources(ConfigurableEnvironment source, StandardEnvironment target) {
		// 20201203 删除target的Servlet属性源
		removePropertySources(target.getPropertySources(), isServletEnvironment(target.getClass(), this.classLoader));

		// 20201203 为target设置Servlet属性源
		for (PropertySource<?> propertySource : source.getPropertySources()) {
			if (!SERVLET_ENVIRONMENT_SOURCE_NAMES.contains(propertySource.getName())) {
				target.getPropertySources().addLast(propertySource);
			}
		}
	}

	private boolean isServletEnvironment(Class<?> conversionType, ClassLoader classLoader) {
		try {
			Class<?> webEnvironmentClass = ClassUtils.forName(CONFIGURABLE_WEB_ENVIRONMENT_CLASS, classLoader);
			return webEnvironmentClass.isAssignableFrom(conversionType);
		}
		catch (Throwable ex) {
			return false;
		}
	}

	// 20201203 删除Servlet属性源
	private void removePropertySources(MutablePropertySources propertySources, boolean isServletEnvironment) {
		// 20201203 获取属性源名称集合
		Set<String> names = new HashSet<>();
		for (PropertySource<?> propertySource : propertySources) {
			names.add(propertySource.getName());
		}

		// 20201203 遍历每个属性源的名称
		for (String name : names) {
			// 20201203 如果是servlet环境, 则根据名称删除属性源
			if (!isServletEnvironment || !SERVLET_ENVIRONMENT_SOURCE_NAMES.contains(name)) {
				propertySources.remove(name);
			}
		}
	}

}
