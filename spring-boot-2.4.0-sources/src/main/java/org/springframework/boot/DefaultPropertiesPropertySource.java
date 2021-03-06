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

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

/**
 * {@link MapPropertySource} containing default properties contributed directly to a
 * {@code SpringApplication}. By convention, the {@link DefaultPropertiesPropertySource}
 * is always the last property source in the {@link Environment}.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
// 20201202 {@link mappropertysource}包含直接为{@code springapplication}提供的默认属性。按照惯例，{@link defaultproperties propertySource}始终是
// 20201202 {@link Environment}中的最后一个属性源。
public class DefaultPropertiesPropertySource extends MapPropertySource {

	/**
	 * The name of the 'default properties' property source.
	 */
	public static final String NAME = "defaultProperties";

	/**
	 * Create a new {@link DefaultPropertiesPropertySource} with the given {@code Map}
	 * source.
	 * @param source the source map
	 */
	public DefaultPropertiesPropertySource(Map<String, Object> source) {
		super(NAME, source);
	}

	/**
	 * Return {@code true} if the given source is named 'defaultProperties'.
	 * @param propertySource the property source to check
	 * @return {@code true} if the name matches
	 */
	public static boolean hasMatchingName(PropertySource<?> propertySource) {
		return (propertySource != null) && propertySource.getName().equals(NAME);
	}

	/**
	 * Create a consume a new {@link DefaultPropertiesPropertySource} instance if the
	 * provided source is not empty.
	 * @param source the {@code Map} source
	 * @param action the action used to consume the
	 * {@link DefaultPropertiesPropertySource}
	 */
	// 20201202 如果提供的源不为空，则创建一个consume a new{@link defaultproperties propertySource}实例。
	public static void ifNotEmpty(Map<String, Object> source, Consumer<DefaultPropertiesPropertySource> action) {
		if (!CollectionUtils.isEmpty(source) && action != null) {
			action.accept(new DefaultPropertiesPropertySource(source));
		}
	}

	/**
	 * Move the 'defaultProperties' property source so that it's the last source in the
	 * given {@link ConfigurableEnvironment}.
	 * @param environment the environment to update
	 */
	// 20201202 移动“defaultProperties”属性源，使其成为给定{@link ConfigurableEnvironment}中的最后一个源。
	public static void moveToEnd(ConfigurableEnvironment environment) {
		moveToEnd(environment.getPropertySources());
	}

	/**
	 * Move the 'defaultProperties' property source so that it's the last source in the
	 * given {@link MutablePropertySources}.
	 * @param propertySources the property sources to update
	 */
	// 20201202 移动“defaultProperties”属性源，使其成为给定{@link MutablePropertySources}中的最后一个源。
	public static void moveToEnd(MutablePropertySources propertySources) {
		// 20201202 删除defaultProperties属性源, 然后在末尾添加回来
		PropertySource<?> propertySource = propertySources.remove(NAME);
		if (propertySource != null) {
			propertySources.addLast(propertySource);
		}
	}

}
