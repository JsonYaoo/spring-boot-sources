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

package org.springframework.core.env;

import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.SpringProperties;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 20201202
 * A. {@link Environment}实现的抽象基类。支持保留的默认配置文件名的概念，并允许通过{@link #active_profiles_PROPERTY_NAME}和{@link #default_profiles_PROPERTY_NAME}属性
 *    指定活动和默认配置文件。
 * B. 具体子类在默认情况下添加的{@link PropertySource}对象主要不同。{@code AbstractEnvironment}不添加任何内容。子类应该通过受保护的
 *    {@link #customizePropertySources（MutablePropertySources）} hook提供属性源，而客户端应该使用{@link ConfigurableEnvironment#getPropertySources（）}并针对
 *    {@link MutablePropertySources}API进行自定义。有关用法示例，请参见{@link ConfigurableEnvironment}javadoc。
 */
/**
 * A.
 * Abstract base class for {@link Environment} implementations. Supports the notion of
 * reserved default profile names and enables specifying active and default profiles
 * through the {@link #ACTIVE_PROFILES_PROPERTY_NAME} and
 * {@link #DEFAULT_PROFILES_PROPERTY_NAME} properties.
 *
 * B.
 * <p>Concrete subclasses differ primarily on which {@link PropertySource} objects they
 * add by default. {@code AbstractEnvironment} adds none. Subclasses should contribute
 * property sources through the protected {@link #customizePropertySources(MutablePropertySources)}
 * hook, while clients should customize using {@link ConfigurableEnvironment#getPropertySources()}
 * and working against the {@link MutablePropertySources} API.
 * See {@link ConfigurableEnvironment} javadoc for usage examples.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see ConfigurableEnvironment
 * @see StandardEnvironment
 */
// 20201202 {@link Environment}实现的抽象基类 => 支持保留的默认配置文件名的概念
public abstract class AbstractEnvironment implements ConfigurableEnvironment {

	/**
	 * System property that instructs Spring to ignore system environment variables,
	 * i.e. to never attempt to retrieve such a variable via {@link System#getenv()}.
	 * <p>The default is "false", falling back to system environment variable checks if a
	 * Spring environment property (e.g. a placeholder in a configuration String) isn't
	 * resolvable otherwise. Consider switching this flag to "true" if you experience
	 * log warnings from {@code getenv} calls coming from Spring, e.g. on WebSphere
	 * with strict SecurityManager settings and AccessControlExceptions warnings.
	 * @see #suppressGetenvAccess()
	 */
	public static final String IGNORE_GETENV_PROPERTY_NAME = "spring.getenv.ignore";

	/**
	 * 20201208
	 * A. 要设置为指定活动配置文件的属性名称：{@value}。 值可以用逗号分隔。
	 * B. 请注意，某些shell环境（例如Bash）不允许在变量名称中使用句点字符。 假设正在使用Spring的{@link SystemEnvironmentPropertySource}，则可以将此属性指定为
	 *    {@code SPRING_PROFILES_ACTIVE}的环境变量。
	 */
	/**
	 * A.
	 * Name of property to set to specify active profiles: {@value}. Value may be comma
	 * delimited.
	 *
	 * B.
	 * <p>Note that certain shell environments such as Bash disallow the use of the period
	 * character in variable names. Assuming that Spring's {@link SystemEnvironmentPropertySource}
	 * is in use, this property may be specified as an environment variable as
	 * {@code SPRING_PROFILES_ACTIVE}.
	 * @see ConfigurableEnvironment#setActiveProfiles
	 */
	// 20201208 要设置为指定活动配置文件的属性名称：{@value}。 值可以用逗号分隔。
	public static final String ACTIVE_PROFILES_PROPERTY_NAME = "spring.profiles.active";

	/**
	 * 20201208
	 * A. 要设置以指定默认情况下处于活动状态的配置文件的属性名称：{@value}。 值可以用逗号分隔。
	 * B. 请注意，某些shell环境（例如Bash）不允许在变量名称中使用句点字符。 假设正在使用Spring的{@link SystemEnvironmentPropertySource}，
	 *    则可以将此属性指定为环境变量，其名称为{@code SPRING_PROFILES_DEFAULT}。
	 */
	/**
	 * A.
	 * Name of property to set to specify profiles active by default: {@value}. Value may
	 * be comma delimited.
	 *
	 * B.
	 * <p>Note that certain shell environments such as Bash disallow the use of the period
	 * character in variable names. Assuming that Spring's {@link SystemEnvironmentPropertySource}
	 * is in use, this property may be specified as an environment variable as
	 * {@code SPRING_PROFILES_DEFAULT}.
	 * @see ConfigurableEnvironment#setDefaultProfiles
	 */
	// 20201208 默认配置文件集名称
	public static final String DEFAULT_PROFILES_PROPERTY_NAME = "spring.profiles.default";

	/**
	 * Name of reserved default profile name: {@value}. If no default profile names are
	 * explicitly and no active profile names are explicitly set, this profile will
	 * automatically be activated by default.
	 * @see #getReservedDefaultProfiles
	 * @see ConfigurableEnvironment#setDefaultProfiles
	 * @see ConfigurableEnvironment#setActiveProfiles
	 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
	 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 */
	// 20201208 保留的默认配置文件名称的名称：{@value}。 如果未明确指定默认配置文件名称，也未明确设置活动配置文件名称，则默认情况下将自动激活此配置文件。
	protected static final String RESERVED_DEFAULT_PROFILE_NAME = "default";

	protected final Log logger = LogFactory.getLog(getClass());

	// 20201203 已激活的配置文件集
	private final Set<String> activeProfiles = new LinkedHashSet<>();

	// 20201208 默认激活的配置文件集
	private final Set<String> defaultProfiles = new LinkedHashSet<>(getReservedDefaultProfiles());

	// 20201203 可变的PropertySource集合
	private final MutablePropertySources propertySources = new MutablePropertySources();

	// 20201203 转换property单例
	private final ConfigurablePropertyResolver propertyResolver =
			// 20201203 针对给定的属性源创建新的冲突解决程序
			new PropertySourcesPropertyResolver(this.propertySources);


	/**
	 * Create a new {@code Environment} instance, calling back to
	 * {@link #customizePropertySources(MutablePropertySources)} during construction to
	 * allow subclasses to contribute or manipulate {@link PropertySource} instances as
	 * appropriate.
	 * @see #customizePropertySources(MutablePropertySources)
	 */
	public AbstractEnvironment() {
		customizePropertySources(this.propertySources);
	}


	/**
	 * Customize the set of {@link PropertySource} objects to be searched by this
	 * {@code Environment} during calls to {@link #getProperty(String)} and related
	 * methods.
	 *
	 * <p>Subclasses that override this method are encouraged to add property
	 * sources using {@link MutablePropertySources#addLast(PropertySource)} such that
	 * further subclasses may call {@code super.customizePropertySources()} with
	 * predictable results. For example:
	 * <pre class="code">
	 * public class Level1Environment extends AbstractEnvironment {
	 *     &#064;Override
	 *     protected void customizePropertySources(MutablePropertySources propertySources) {
	 *         super.customizePropertySources(propertySources); // no-op from base class
	 *         propertySources.addLast(new PropertySourceA(...));
	 *         propertySources.addLast(new PropertySourceB(...));
	 *     }
	 * }
	 *
	 * public class Level2Environment extends Level1Environment {
	 *     &#064;Override
	 *     protected void customizePropertySources(MutablePropertySources propertySources) {
	 *         super.customizePropertySources(propertySources); // add all from superclass
	 *         propertySources.addLast(new PropertySourceC(...));
	 *         propertySources.addLast(new PropertySourceD(...));
	 *     }
	 * }
	 * </pre>
	 * In this arrangement, properties will be resolved against sources A, B, C, D in that
	 * order. That is to say that property source "A" has precedence over property source
	 * "D". If the {@code Level2Environment} subclass wished to give property sources C
	 * and D higher precedence than A and B, it could simply call
	 * {@code super.customizePropertySources} after, rather than before adding its own:
	 * <pre class="code">
	 * public class Level2Environment extends Level1Environment {
	 *     &#064;Override
	 *     protected void customizePropertySources(MutablePropertySources propertySources) {
	 *         propertySources.addLast(new PropertySourceC(...));
	 *         propertySources.addLast(new PropertySourceD(...));
	 *         super.customizePropertySources(propertySources); // add all from superclass
	 *     }
	 * }
	 * </pre>
	 * The search order is now C, D, A, B as desired.
	 *
	 * <p>Beyond these recommendations, subclasses may use any of the {@code add&#42;},
	 * {@code remove}, or {@code replace} methods exposed by {@link MutablePropertySources}
	 * in order to create the exact arrangement of property sources desired.
	 *
	 * <p>The base implementation registers no property sources.
	 *
	 * <p>Note that clients of any {@link ConfigurableEnvironment} may further customize
	 * property sources via the {@link #getPropertySources()} accessor, typically within
	 * an {@link org.springframework.context.ApplicationContextInitializer
	 * ApplicationContextInitializer}. For example:
	 * <pre class="code">
	 * ConfigurableEnvironment env = new StandardEnvironment();
	 * env.getPropertySources().addLast(new PropertySourceX(...));
	 * </pre>
	 *
	 * <h2>A warning about instance variable access</h2>
	 * Instance variables declared in subclasses and having default initial values should
	 * <em>not</em> be accessed from within this method. Due to Java object creation
	 * lifecycle constraints, any initial value will not yet be assigned when this
	 * callback is invoked by the {@link #AbstractEnvironment()} constructor, which may
	 * lead to a {@code NullPointerException} or other problems. If you need to access
	 * default values of instance variables, leave this method as a no-op and perform
	 * property source manipulation and instance variable access directly within the
	 * subclass constructor. Note that <em>assigning</em> values to instance variables is
	 * not problematic; it is only attempting to read default values that must be avoided.
	 *
	 * @see MutablePropertySources
	 * @see PropertySourcesPropertyResolver
	 * @see org.springframework.context.ApplicationContextInitializer
	 */
	protected void customizePropertySources(MutablePropertySources propertySources) {
	}

	/**
	 * Return the set of reserved default profile names. This implementation returns
	 * {@value #RESERVED_DEFAULT_PROFILE_NAME}. Subclasses may override in order to
	 * customize the set of reserved names.
	 * @see #RESERVED_DEFAULT_PROFILE_NAME
	 * @see #doGetDefaultProfiles()
	 */
	// 20201208 返回保留的默认配置文件名称集。 此实现返回{@value #RESERVED_DEFAULT_PROFILE_NAME}。 子类可以重写以自定义保留名称集。
	protected Set<String> getReservedDefaultProfiles() {
		return Collections.singleton(RESERVED_DEFAULT_PROFILE_NAME);
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableEnvironment interface
	//---------------------------------------------------------------------

	// 20201208 获取激活的配置文件集
	@Override
	public String[] getActiveProfiles() {
		// 20201208 获取/激活配置文件集
		return StringUtils.toStringArray(doGetActiveProfiles());
	}

	/**
	 * Return the set of active profiles as explicitly set through
	 * {@link #setActiveProfiles} or if the current set of active profiles
	 * is empty, check for the presence of the {@value #ACTIVE_PROFILES_PROPERTY_NAME}
	 * property and assign its value to the set of active profiles.
	 * @see #getActiveProfiles()
	 * @see #ACTIVE_PROFILES_PROPERTY_NAME
	 */
	// 20201208 返回通过{@link #setActiveProfiles}明确设置的活动配置文件集，或者如果当前活动配置文件集为空，请检查{@value #ACTIVE_PROFILES_PROPERTY_NAME}属性的存在
	// 20201208 并将其值分配给活动配置文件集 -> 即获取/激活配置文件集
	protected Set<String> doGetActiveProfiles() {
		// 20201208 已激活的配置文件集上锁
		synchronized (this.activeProfiles) {
			// 20201208 已激活的配置文件集为空
			if (this.activeProfiles.isEmpty()) {
				// 20201208 获取"spring.profiles.active"属性值
				String profiles = getProperty(ACTIVE_PROFILES_PROPERTY_NAME);

				// 20201208 如果该属性值包含实际文本
				if (StringUtils.hasText(profiles)) {
					// 20201208 设置激活的配置文件集 spring.boot.active
					setActiveProfiles(
							// 20201208 将逗号分隔列表（例如，CSV文件中的一行）转换为字符串数组。
							StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(profiles))
					);
				}
			}

			// 20201208 返回已注册的已激活的配置文件集
			return this.activeProfiles;
		}
	}

	// 20201203 设置激活的配置文件集 spring.boot.active
	@Override
	public void setActiveProfiles(String... profiles) {
		// 20201203 文件集合不能为空
		Assert.notNull(profiles, "Profile array must not be null");

		// 20201203 如果是debugger级别, 则输出debugger日志
		if (logger.isDebugEnabled()) {
			// 20201203 激活配置文件: ..
			logger.debug("Activating profiles " + Arrays.asList(profiles));
		}

		// 20201203 同步激活配置文件集
		synchronized (this.activeProfiles) {
			this.activeProfiles.clear();
			for (String profile : profiles) {
				// 20201203 检验配置文件, 不能为空 & 不能为空格 & 不能!开头
				validateProfile(profile);

				// 20201203 校验通过的添加到激活配置文件集中
				this.activeProfiles.add(profile);
			}
		}
	}

	@Override
	public void addActiveProfile(String profile) {
		if (logger.isDebugEnabled()) {
			logger.debug("Activating profile '" + profile + "'");
		}
		validateProfile(profile);
		doGetActiveProfiles();
		synchronized (this.activeProfiles) {
			this.activeProfiles.add(profile);
		}
	}

	// 20201208 获取默认的配置文件集
	@Override
	public String[] getDefaultProfiles() {
		// 20201208 获取/激活默认配置文件集
		return StringUtils.toStringArray(doGetDefaultProfiles());
	}

	/**
	 * 20201208
	 * 返回通过{@link #setDefaultProfiles（String ...）}显式设置的默认配置文件集，或者如果当前的默认配置文件集仅由
	 * {@linkplain #getReservedDefaultProfiles（）保留的默认配置文件}组成，然后检查是否存在 {@value #DEFAULT_PROFILES_PROPERTY_NAME}属性，
	 * 并将其值（如果有）分配给默认配置文件集。
	 */
	/**
	 * Return the set of default profiles explicitly set via
	 * {@link #setDefaultProfiles(String...)} or if the current set of default profiles
	 * consists only of {@linkplain #getReservedDefaultProfiles() reserved default
	 * profiles}, then check for the presence of the
	 * {@value #DEFAULT_PROFILES_PROPERTY_NAME} property and assign its value (if any)
	 * to the set of default profiles.
	 * @see #AbstractEnvironment()
	 * @see #getDefaultProfiles()
	 * @see #DEFAULT_PROFILES_PROPERTY_NAME
	 * @see #getReservedDefaultProfiles()
	 */
	// 20201208 获取/激活默认配置文件集
	protected Set<String> doGetDefaultProfiles() {
		// 20201208 默认激活的配置文件集上锁
		synchronized (this.defaultProfiles) {
			// 20201208 如果激活的默认文件集与保留的配置文件集相同, 则说明还没进行过赋值
			if (this.defaultProfiles.equals(getReservedDefaultProfiles())) {
				// 20201208 则根据默认配置文件集名称获取属性
				String profiles = getProperty(DEFAULT_PROFILES_PROPERTY_NAME);

				// 20201208 如果该属性包含实际文本
				if (StringUtils.hasText(profiles)) {
					// 20201208 调用此方法将删除覆盖在构造环境期间可能已添加的所有保留的默认配置文件
					setDefaultProfiles(
							// 20201208 将逗号分隔列表（例如，CSV文件中的一行）转换为字符串数组。
							StringUtils.commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(profiles)));
				}
			}

			// 20201208 返回默认配置文件集
			return this.defaultProfiles;
		}
	}

	/**
	 * 20201208
	 * A. 如果没有通过{@link #setActiveProfiles}显式激活其他配置文件，则指定默认情况下将其激活的配置文件集。
	 * B. 调用此方法将删除覆盖在构造环境期间可能已添加的所有保留的默认配置文件。
	 */
	/**
	 * A.
	 * Specify the set of profiles to be made active by default if no other profiles
	 * are explicitly made active through {@link #setActiveProfiles}.
	 *
	 * B.
	 * <p>Calling this method removes overrides any reserved default profiles
	 * that may have been added during construction of the environment.
	 * @see #AbstractEnvironment()
	 * @see #getReservedDefaultProfiles()
	 */
	// 20201208 调用此方法将删除覆盖在构造环境期间可能已添加的所有保留的默认配置文件
	@Override
	public void setDefaultProfiles(String... profiles) {
		// 20201208 配置文件集数组不能为空
		Assert.notNull(profiles, "Profile array must not be null");

		// 20201208 默认激活的配置文件集上锁
		synchronized (this.defaultProfiles) {
			// 20201208 清空之前保留的文件集
			this.defaultProfiles.clear();

			// 20201208 遍历配置文件集数组
			for (String profile : profiles) {
				// 20201208 校验每个配置文件集
				validateProfile(profile);

				// 20201208 通过校验则添加到默认激活的配置文件集
				this.defaultProfiles.add(profile);
			}
		}
	}

	@Override
	@Deprecated
	public boolean acceptsProfiles(String... profiles) {
		Assert.notEmpty(profiles, "Must specify at least one profile");
		for (String profile : profiles) {
			if (StringUtils.hasLength(profile) && profile.charAt(0) == '!') {
				if (!isProfileActive(profile.substring(1))) {
					return true;
				}
			}
			else if (isProfileActive(profile)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean acceptsProfiles(Profiles profiles) {
		Assert.notNull(profiles, "Profiles must not be null");
		return profiles.matches(this::isProfileActive);
	}

	/**
	 * Return whether the given profile is active, or if active profiles are empty
	 * whether the profile should be active by default.
	 * @throws IllegalArgumentException per {@link #validateProfile(String)}
	 */
	protected boolean isProfileActive(String profile) {
		validateProfile(profile);
		Set<String> currentActiveProfiles = doGetActiveProfiles();
		return (currentActiveProfiles.contains(profile) ||
				(currentActiveProfiles.isEmpty() && doGetDefaultProfiles().contains(profile)));
	}

	/**
	 * Validate the given profile, called internally prior to adding to the set of
	 * active or default profiles.
	 * <p>Subclasses may override to impose further restrictions on profile syntax.
	 * @throws IllegalArgumentException if the profile is null, empty, whitespace-only or
	 * begins with the profile NOT operator (!).
	 * @see #acceptsProfiles
	 * @see #addActiveProfile
	 * @see #setDefaultProfiles
	 */
	// 20201203 在添加到活动或默认配置文件集之前，验证给定的配置文件（在内部调用）。子类可以重写以对概要文件语法施加进一步的限制。
	protected void validateProfile(String profile) {
		// 20201203 判断字符串不为空 & 是否为正确的文本
		if (!StringUtils.hasText(profile)) {
			// 20201203 如果不是正确的文本, 则抛出异常
			throw new IllegalArgumentException("Invalid profile [" + profile + "]: must contain text");
		}

		// 20201203 如果配置文件属性第一个字符为!, 则属于非法参数异常
		if (profile.charAt(0) == '!') {
			throw new IllegalArgumentException("Invalid profile [" + profile + "]: must not begin with ! operator");
		}
	}

	@Override
	public MutablePropertySources getPropertySources() {
		return this.propertySources;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Map<String, Object> getSystemProperties() {
		try {
			return (Map) System.getProperties();
		}
		catch (AccessControlException ex) {
			return (Map) new ReadOnlySystemAttributesMap() {
				@Override
				@Nullable
				protected String getSystemAttribute(String attributeName) {
					try {
						return System.getProperty(attributeName);
					}
					catch (AccessControlException ex) {
						if (logger.isInfoEnabled()) {
							logger.info("Caught AccessControlException when accessing system property '" +
									attributeName + "'; its value will be returned [null]. Reason: " + ex.getMessage());
						}
						return null;
					}
				}
			};
		}
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Map<String, Object> getSystemEnvironment() {
		if (suppressGetenvAccess()) {
			return Collections.emptyMap();
		}
		try {
			return (Map) System.getenv();
		}
		catch (AccessControlException ex) {
			return (Map) new ReadOnlySystemAttributesMap() {
				@Override
				@Nullable
				protected String getSystemAttribute(String attributeName) {
					try {
						return System.getenv(attributeName);
					}
					catch (AccessControlException ex) {
						if (logger.isInfoEnabled()) {
							logger.info("Caught AccessControlException when accessing system environment variable '" +
									attributeName + "'; its value will be returned [null]. Reason: " + ex.getMessage());
						}
						return null;
					}
				}
			};
		}
	}

	/**
	 * Determine whether to suppress {@link System#getenv()}/{@link System#getenv(String)}
	 * access for the purposes of {@link #getSystemEnvironment()}.
	 * <p>If this method returns {@code true}, an empty dummy Map will be used instead
	 * of the regular system environment Map, never even trying to call {@code getenv}
	 * and therefore avoiding security manager warnings (if any).
	 * <p>The default implementation checks for the "spring.getenv.ignore" system property,
	 * returning {@code true} if its value equals "true" in any case.
	 * @see #IGNORE_GETENV_PROPERTY_NAME
	 * @see SpringProperties#getFlag
	 */
	protected boolean suppressGetenvAccess() {
		return SpringProperties.getFlag(IGNORE_GETENV_PROPERTY_NAME);
	}

	@Override
	public void merge(ConfigurableEnvironment parent) {
		for (PropertySource<?> ps : parent.getPropertySources()) {
			if (!this.propertySources.contains(ps.getName())) {
				this.propertySources.addLast(ps);
			}
		}
		String[] parentActiveProfiles = parent.getActiveProfiles();
		if (!ObjectUtils.isEmpty(parentActiveProfiles)) {
			synchronized (this.activeProfiles) {
				Collections.addAll(this.activeProfiles, parentActiveProfiles);
			}
		}
		String[] parentDefaultProfiles = parent.getDefaultProfiles();
		if (!ObjectUtils.isEmpty(parentDefaultProfiles)) {
			synchronized (this.defaultProfiles) {
				this.defaultProfiles.remove(RESERVED_DEFAULT_PROFILE_NAME);
				Collections.addAll(this.defaultProfiles, parentDefaultProfiles);
			}
		}
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurablePropertyResolver interface // 20201203 执行配置属性接口
	//---------------------------------------------------------------------

	@Override
	public ConfigurableConversionService getConversionService() {
		return this.propertyResolver.getConversionService();
	}

	// 20201203 设置property转换服务
	@Override
	public void setConversionService(ConfigurableConversionService conversionService) {
		// 20201203 给转换property单例设置property转换服务
		this.propertyResolver.setConversionService(conversionService);
	}

	@Override
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.propertyResolver.setPlaceholderPrefix(placeholderPrefix);
	}

	@Override
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.propertyResolver.setPlaceholderSuffix(placeholderSuffix);
	}

	@Override
	public void setValueSeparator(@Nullable String valueSeparator) {
		this.propertyResolver.setValueSeparator(valueSeparator);
	}

	@Override
	public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
		this.propertyResolver.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
	}

	@Override
	public void setRequiredProperties(String... requiredProperties) {
		this.propertyResolver.setRequiredProperties(requiredProperties);
	}

	@Override
	public void validateRequiredProperties() throws MissingRequiredPropertiesException {
		this.propertyResolver.validateRequiredProperties();
	}


	//---------------------------------------------------------------------
	// Implementation of PropertyResolver interface
	//---------------------------------------------------------------------

	@Override
	public boolean containsProperty(String key) {
		return this.propertyResolver.containsProperty(key);
	}

	@Override
	@Nullable
	public String getProperty(String key) {
		return this.propertyResolver.getProperty(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		return this.propertyResolver.getProperty(key, defaultValue);
	}

	@Override
	@Nullable
	public <T> T getProperty(String key, Class<T> targetType) {
		return this.propertyResolver.getProperty(key, targetType);
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		return this.propertyResolver.getProperty(key, targetType, defaultValue);
	}

	@Override
	public String getRequiredProperty(String key) throws IllegalStateException {
		return this.propertyResolver.getRequiredProperty(key);
	}

	@Override
	public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
		return this.propertyResolver.getRequiredProperty(key, targetType);
	}

	@Override
	public String resolvePlaceholders(String text) {
		return this.propertyResolver.resolvePlaceholders(text);
	}

	@Override
	public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
		return this.propertyResolver.resolveRequiredPlaceholders(text);
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + " {activeProfiles=" + this.activeProfiles +
				", defaultProfiles=" + this.defaultProfiles + ", propertySources=" + this.propertySources + "}";
	}

}
