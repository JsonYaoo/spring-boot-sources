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

package org.springframework.core.env;

import org.springframework.lang.Nullable;

/**
 * {@link PropertyResolver} implementation that resolves property values against
 * an underlying set of {@link PropertySources}.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertySource
 * @see PropertySources
 * @see AbstractEnvironment
 */
// 20201203 {@link PropertyResolver}实现，根据一组{@link PropertySources}解析属性值。
public class PropertySourcesPropertyResolver extends AbstractPropertyResolver {

	// 20201211 属性源列表
	@Nullable
	private final PropertySources propertySources;

	/**
	 * Create a new resolver against the given property sources.
	 * @param propertySources the set of {@link PropertySource} objects to use
	 */
	// 20201203 针对给定的属性源创建新的冲突解决程序。
	public PropertySourcesPropertyResolver(@Nullable PropertySources propertySources) {
		this.propertySources = propertySources;
	}


	@Override
	public boolean containsProperty(String key) {
		if (this.propertySources != null) {
			for (PropertySource<?> propertySource : this.propertySources) {
				if (propertySource.containsProperty(key)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	@Nullable
	public String getProperty(String key) {
		return getProperty(key, String.class, true);
	}

	// 20201211 返回与给定键关联的属性值；如果键无法解析，则返回{@code null}。
	@Override
	@Nullable
	public <T> T getProperty(String key, Class<T> targetValueType) {
		return getProperty(key, targetValueType, true);
	}

	@Override
	@Nullable
	protected String getPropertyAsRawString(String key) {
		return getProperty(key, String.class, false);
	}

	// 20201211 返回与给定键关联的属性值；如果键无法解析，则返回{@code null}。
	@Nullable
	protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
		if (this.propertySources != null) {
			// 20201211 遍历属性源列表
			for (PropertySource<?> propertySource : this.propertySources) {
				if (logger.isTraceEnabled()) {
					logger.trace("Searching for key '" + key + "' in PropertySource '" +
							propertySource.getName() + "'");
				}
				// 20201210 返回与给定名称关联的值；如果找不到，则返回{@code null}。
				Object value = propertySource.getProperty(key);
				if (value != null) {
					if (resolveNestedPlaceholders && value instanceof String) {
						// 20201211 解析给定字符串中的占位符
						value = resolveNestedPlaceholders((String) value);
					}

					// 20201211 默认实现会写入带有密钥和源的调试日志消息,  从4.3.3版本开始，此功能不再记录该值，以避免意外记录敏感设置
					logKeyFound(key, propertySource, value);

					// 20201211 如有必要，将给定值转换为指定的目标类型。
					return convertValueIfNecessary(value, targetValueType);
				}
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Could not find key '" + key + "' in any property source");
		}
		return null;
	}

	/**
	 * 20201211
	 * A. 记录在给定{@link PropertySource}中找到的给定密钥，得到给定值。
	 * B. 默认实现会写入带有密钥和源的调试日志消息。 从4.3.3版本开始，此功能不再记录该值，以避免意外记录敏感设置。 子类可以重写此方法以更改日志级别和/或日志消息，
	 *    如果需要，还可以包括属性的值。
	 */
	/**
	 * A.
	 * Log the given key as found in the given {@link PropertySource}, resulting in
	 * the given value.
	 *
	 * B.
	 * <p>The default implementation writes a debug log message with key and source.
	 * As of 4.3.3, this does not log the value anymore in order to avoid accidental
	 * logging of sensitive settings. Subclasses may override this method to change
	 * the log level and/or log message, including the property's value if desired.
	 *
	 * @param key the key found
	 * @param propertySource the {@code PropertySource} that the key has been found in
	 * @param value the corresponding value
	 * @since 4.3.1
	 */
	// 20201211 默认实现会写入带有密钥和源的调试日志消息,  从4.3.3版本开始，此功能不再记录该值，以避免意外记录敏感设置
	protected void logKeyFound(String key, PropertySource<?> propertySource, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("Found key '" + key + "' in PropertySource '" + propertySource.getName() +
					"' with value of type " + value.getClass().getSimpleName());
		}
	}

}
