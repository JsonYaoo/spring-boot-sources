/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.context.properties.bind;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.SystemPropertyUtils;

/**
 * {@link PlaceholdersResolver} to resolve placeholders from {@link PropertySources}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 */
// 20201202 {@link PlaceholdersResolver}从{@link PropertySources}解析占位符。
public class PropertySourcesPlaceholdersResolver implements PlaceholdersResolver {

	private final Iterable<PropertySource<?>> sources;

	private final PropertyPlaceholderHelper helper;

	// 20201202 使用默认分隔符解析器${}
	public PropertySourcesPlaceholdersResolver(Environment environment) {
		this(getSources(environment), null);
	}

	public PropertySourcesPlaceholdersResolver(Iterable<PropertySource<?>> sources) {
		this(sources, null);
	}

	// 20201202 构造属性源占位符解析器
	public PropertySourcesPlaceholdersResolver(Iterable<PropertySource<?>> sources, PropertyPlaceholderHelper helper) {
		// 20201202 注册属性源
		this.sources = sources;

		// 20201202 如果助手类为空, 则创建指定占位符的解析器并注册
		this.helper = (helper != null) ? helper : new PropertyPlaceholderHelper(
				// 20201202 指定占位符前缀${
				SystemPropertyUtils.PLACEHOLDER_PREFIX,
				// 20201202 指定占位符后缀}
				SystemPropertyUtils.PLACEHOLDER_SUFFIX,
				// 20201202 指定占位符分隔符:
				SystemPropertyUtils.VALUE_SEPARATOR,
				// 20201202 指定是否能忽略非法占位符true
				true);
	}

	@Override
	public Object resolvePlaceholders(Object value) {
		if (value instanceof String) {
			return this.helper.replacePlaceholders((String) value, this::resolvePlaceholder);
		}
		return value;
	}

	protected String resolvePlaceholder(String placeholder) {
		if (this.sources != null) {
			for (PropertySource<?> source : this.sources) {
				Object value = source.getProperty(placeholder);
				if (value != null) {
					return String.valueOf(value);
				}
			}
		}
		return null;
	}

	// 20201202 根据环境获取属性源
	private static PropertySources getSources(Environment environment) {
		// 20201202 环境不能为null
		Assert.notNull(environment, "Environment must not be null");

		// 20201202 环境必须为ConfigurableEnvironment.class实例
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment,
				"Environment must be a ConfigurableEnvironment");

		// 20201202 获取属性源
		return ((ConfigurableEnvironment) environment).getPropertySources();
	}

}
