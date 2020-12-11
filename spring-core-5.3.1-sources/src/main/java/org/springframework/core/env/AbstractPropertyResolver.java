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

package org.springframework.core.env;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.SystemPropertyUtils;

/**
 * Abstract base class for resolving properties against any underlying source.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
// 20201203 用于针对任何基础源解析属性的抽象基类。
public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	// 20201211 配置服务转换
	@Nullable
	private volatile ConfigurableConversionService conversionService;

	// 20201211 用于处理包含占位符值的字符串的实用程序类, 忽略不可解析的占位符
	@Nullable
	private PropertyPlaceholderHelper nonStrictHelper;

	// 20201211 用于处理包含占位符值的字符串的实用程序类, 不忽略不可解析的占位符, 抛出异常
	@Nullable
	private PropertyPlaceholderHelper strictHelper;

	// 20201211 是否忽略不可解析的占位符, 默认否
	private boolean ignoreUnresolvableNestedPlaceholders = false;

	private String placeholderPrefix = SystemPropertyUtils.PLACEHOLDER_PREFIX;

	private String placeholderSuffix = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

	@Nullable
	private String valueSeparator = SystemPropertyUtils.VALUE_SEPARATOR;

	// 20201211 必须的属性集合
	private final Set<String> requiredProperties = new LinkedHashSet<>();

	@Override
	public ConfigurableConversionService getConversionService() {
		// Need to provide an independent DefaultConversionService, not the
		// shared DefaultConversionService used by PropertySourcesPropertyResolver.
		ConfigurableConversionService cs = this.conversionService;
		if (cs == null) {
			synchronized (this) {
				cs = this.conversionService;
				if (cs == null) {
					cs = new DefaultConversionService();
					this.conversionService = cs;
				}
			}
		}
		return cs;
	}

	@Override
	public void setConversionService(ConfigurableConversionService conversionService) {
		Assert.notNull(conversionService, "ConversionService must not be null");
		this.conversionService = conversionService;
	}

	/**
	 * Set the prefix that placeholders replaced by this resolver must begin with.
	 * <p>The default is "${".
	 * @see SystemPropertyUtils#PLACEHOLDER_PREFIX
	 */
	@Override
	public void setPlaceholderPrefix(String placeholderPrefix) {
		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * Set the suffix that placeholders replaced by this resolver must end with.
	 * <p>The default is "}".
	 * @see SystemPropertyUtils#PLACEHOLDER_SUFFIX
	 */
	@Override
	public void setPlaceholderSuffix(String placeholderSuffix) {
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderSuffix = placeholderSuffix;
	}

	/**
	 * Specify the separating character between the placeholders replaced by this
	 * resolver and their associated default value, or {@code null} if no such
	 * special character should be processed as a value separator.
	 * <p>The default is ":".
	 * @see SystemPropertyUtils#VALUE_SEPARATOR
	 */
	@Override
	public void setValueSeparator(@Nullable String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	/**
	 * Set whether to throw an exception when encountering an unresolvable placeholder
	 * nested within the value of a given property. A {@code false} value indicates strict
	 * resolution, i.e. that an exception will be thrown. A {@code true} value indicates
	 * that unresolvable nested placeholders should be passed through in their unresolved
	 * ${...} form.
	 * <p>The default is {@code false}.
	 * @since 3.2
	 */
	@Override
	public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
		this.ignoreUnresolvableNestedPlaceholders = ignoreUnresolvableNestedPlaceholders;
	}

	@Override
	public void setRequiredProperties(String... requiredProperties) {
		Collections.addAll(this.requiredProperties, requiredProperties);
	}

	// 20201211 验证是否存在{@link #setRequiredProperties}指定的每个属性，并将其解析为非{@code null}值 -> 如果存在找不到属性值的属性, 则抛出异常
	@Override
	public void validateRequiredProperties() {
		// 20201211 未找到所需属性时引发异常。
		MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();

		// 20201211 遍历必须的属性集合
		for (String key : this.requiredProperties) {
			// 20201211 返回与给定键关联的属性值；如果键无法解析，则返回{@code null}。
			if (this.getProperty(key) == null) {
				// 20201211 标记找不到属性值的属性
				ex.addMissingRequiredProperty(key);
			}
		}

		// 20201211 如果存在找不到属性值的属性, 则抛出异常
		if (!ex.getMissingRequiredProperties().isEmpty()) {
			throw ex;
		}
	}

	@Override
	public boolean containsProperty(String key) {
		return (getProperty(key) != null);
	}

	// 20201211 返回与给定键关联的属性值；如果键无法解析，则返回{@code null}。
	@Override
	@Nullable
	public String getProperty(String key) {
		// 20201211 返回与给定键关联的属性值；如果键无法解析，则返回{@code null}。
		return getProperty(key, String.class);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return (value != null ? value : defaultValue);
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		T value = getProperty(key, targetType);
		return (value != null ? value : defaultValue);
	}

	@Override
	public String getRequiredProperty(String key) throws IllegalStateException {
		String value = getProperty(key);
		if (value == null) {
			throw new IllegalStateException("Required key '" + key + "' not found");
		}
		return value;
	}

	@Override
	public <T> T getRequiredProperty(String key, Class<T> valueType) throws IllegalStateException {
		T value = getProperty(key, valueType);
		if (value == null) {
			throw new IllegalStateException("Required key '" + key + "' not found");
		}
		return value;
	}

	// 20201211 忽略不可解析的占位符-> 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值
	@Override
	public String resolvePlaceholders(String text) {
		if (this.nonStrictHelper == null) {
			// 20201211  创建一个新的{@code PropertyPlaceholderHelper}，它使用提供的前缀和后缀, 忽略不可解析的占位符
			this.nonStrictHelper = createPlaceholderHelper(true);
		}

		// 20201211 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值。
		return doResolvePlaceholders(text, this.nonStrictHelper);
	}

	// 20201211 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值。
	@Override
	public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
		if (this.strictHelper == null) {
			// 20201211 用于处理包含占位符值的字符串的实用程序类, 不忽略不可解析的占位符, 抛出异常
			this.strictHelper = createPlaceholderHelper(false);
		}
		// 20201211 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值。
		return doResolvePlaceholders(text, this.strictHelper);
	}

	/**
	 * 20201211
	 * A. 解析给定字符串中的占位符，并根据{@link #setIgnoreUnresolvableNestedPlaceholders}的值来确定是否存在任何无法解析的占位符是否引发异常或被忽略。
	 * B. 从{@link #getProperty}及其变体调用，隐式解析嵌套占位符。 相反，{@link #resolvePlaceholders}和{@link #resolveRequiredPlaceholders}并不委托该方法，
	 *    而是执行它们各自对不可解析的占位符的处理。
	 */
	/**
	 * A.
	 * Resolve placeholders within the given string, deferring to the value of
	 * {@link #setIgnoreUnresolvableNestedPlaceholders} to determine whether any
	 * unresolvable placeholders should raise an exception or be ignored.
	 *
	 * B.
	 * <p>Invoked from {@link #getProperty} and its variants, implicitly resolving
	 * nested placeholders. In contrast, {@link #resolvePlaceholders} and
	 * {@link #resolveRequiredPlaceholders} do <i>not</i> delegate
	 * to this method but rather perform their own handling of unresolvable
	 * placeholders, as specified by each of those methods.
	 * @since 3.2
	 * @see #setIgnoreUnresolvableNestedPlaceholders
	 */
	// 20201211 解析给定字符串中的占位符
	protected String resolveNestedPlaceholders(String value) {
		if (value.isEmpty()) {
			return value;
		}

		// 20201211 是否忽略不可解析的占位符, 默认否
		return (this.ignoreUnresolvableNestedPlaceholders ?
				// 20201211 忽略不可解析的占位符-> 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值
				resolvePlaceholders(value) :

				// 20201211 不忽略不可解析的占位符, 抛出异常 -> 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值
				resolveRequiredPlaceholders(value));
	}

	// 20201211  创建一个新的{@code PropertyPlaceholderHelper}，它使用提供的前缀和后缀。
	private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
		// 20201211  创建一个新的{@code PropertyPlaceholderHelper}，它使用提供的前缀和后缀。
		return new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix,
				this.valueSeparator, ignoreUnresolvablePlaceholders);
	}

	// 20201211 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值。
	private String doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {
		// 20201211 将格式为{@code $ {name}}的所有占位符替换为提供的{@link PlaceholderResolver}返回的值。
		return helper.replacePlaceholders(text, this::getPropertyAsRawString);
	}

	/**
	 * Convert the given value to the specified target type, if necessary.
	 * @param value the original property value
	 * @param targetType the specified target type for property retrieval
	 * @return the converted value, or the original value if no conversion
	 * is necessary
	 * @since 4.3.5
	 */
	// 20201211 如有必要，将给定值转换为指定的目标类型。
	@SuppressWarnings("unchecked")
	@Nullable
	protected <T> T convertValueIfNecessary(Object value, @Nullable Class<T> targetType) {
		if (targetType == null) {
			return (T) value;
		}

		// 20201211 获取配置服务转换
		ConversionService conversionServiceToUse = this.conversionService;
		if (conversionServiceToUse == null) {
			// Avoid initialization of shared DefaultConversionService if
			// no standard type conversion is needed in the first place...
			// 20201211 如果首先不需要标准类型转换，请避免初始化共享的DefaultConversionService。
			if (ClassUtils.isAssignableValue(targetType, value)) {
				// 20201211 如果类型是可从值分配的, 则直接返回
				return (T) value;
			}

			// 20201211 返回共享的默认{@code ConversionService}实例，并在需要时延迟构建它
			conversionServiceToUse = DefaultConversionService.getSharedInstance();
		}

		// 20201211 将给定的{@code source}转换为指定的{@code targetType}。
		return conversionServiceToUse.convert(value, targetType);
	}


	/**
	 * Retrieve the specified property as a raw String,
	 * i.e. without resolution of nested placeholders.
	 * @param key the property name to resolve
	 * @return the property value or {@code null} if none found
	 */
	// 20201211 以原始String检索指定的属性，即不解析嵌套占位符。
	@Nullable
	protected abstract String getPropertyAsRawString(String key);

}
