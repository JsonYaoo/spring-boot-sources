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

/**
 * 20201202
 * A. 接口，表示当前应用程序正在其中运行的环境。profiles的两个重要方面：profiles的应用。与属性访问相关的方法通过{@link PropertyResolver}上层接口公开。
 * B. profile是一个命名的、逻辑的bean定义组，只有在给定的profile处于活动状态时才会注册到容器中。bean可以被分配给一个profile，不管是用XML定义的还是通过注释定义的；
 *   请参阅springbeans3.1模式或{@link org.springframework.context.annotation.Profile @Profile}有关语法详细信息的注释。{@code Environment}对象与profiles
 *   相关的作用是确定哪些profiles（如果有的话）当前是{@linkplain getActiveProfiles active}，哪些profiles（如果有的话）应该是{@linkplain getDefaultProfiles-active。
 * C. Properties在几乎所有的应用程序中都扮演着重要的角色，并且可能来自各种来源：属性文件、JVM系统属性、系统环境变量、JNDI、servlet上下文参数、特殊属性对象、映射等等。
 *    与属性相关的环境对象的作用是为用户提供一个方便的服务接口，用于配置属性源并从中解析属性。
 * D. 在{@code ApplicationContext}中管理的bean可以注册为{@link org.springframework.context.EnvironmentAware EnvironmentAware}或
 *    {@code @Inject}以直接查询配置文件状态或解析属性。
 * E. 但是，在大多数情况下，应用程序级bean不需要直接与{@code Environment}交互，而是可能需要将{@code ${…}}属性值替换为属性占位符配置器，例如
 *    {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurerPropertySourcesPlaceholderConfigurer}，它本身是
 *    {@code EnvironmentAware}，从spring3.1起，在使用默认注册为{@code <context:property-placeholder/>}.
 * F. 环境对象的配置必须通过{@code ConfigurableEnvironment}接口完成，该接口从所有{@code AbstractApplicationContext}子类{@code getEnvironment（）}方法返回。
 *    请参见{@link ConfigurableEnvironment}Javadoc，以获取在应用程序上下文{@code refresh（）}之前操作属性源的用法示例。
 */
/**
 * A.
 * Interface representing the environment in which the current application is running.
 * Models two key aspects of the application environment: <em>profiles</em> and
 * <em>properties</em>. Methods related to property access are exposed via the
 * {@link PropertyResolver} superinterface.
 *
 * B.
 * <p>A <em>profile</em> is a named, logical group of bean definitions to be registered
 * with the container only if the given profile is <em>active</em>. Beans may be assigned
 * to a profile whether defined in XML or via annotations; see the spring-beans 3.1 schema
 * or the {@link org.springframework.context.annotation.Profile @Profile} annotation for
 * syntax details. The role of the {@code Environment} object with relation to profiles is
 * in determining which profiles (if any) are currently {@linkplain #getActiveProfiles
 * active}, and which profiles (if any) should be {@linkplain #getDefaultProfiles active
 * by default}.
 *
 * C.
 * <p><em>Properties</em> play an important role in almost all applications, and may
 * originate from a variety of sources: properties files, JVM system properties, system
 * environment variables, JNDI, servlet context parameters, ad-hoc Properties objects,
 * Maps, and so on. The role of the environment object with relation to properties is to
 * provide the user with a convenient service interface for configuring property sources
 * and resolving properties from them.
 *
 * D.
 * <p>Beans managed within an {@code ApplicationContext} may register to be {@link
 * org.springframework.context.EnvironmentAware EnvironmentAware} or {@code @Inject} the
 * {@code Environment} in order to query profile state or resolve properties directly.
 *
 * E.
 * <p>In most cases, however, application-level beans should not need to interact with the
 * {@code Environment} directly but instead may have to have {@code ${...}} property
 * values replaced by a property placeholder configurer such as
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer}, which itself is {@code EnvironmentAware} and
 * as of Spring 3.1 is registered by default when using
 * {@code <context:property-placeholder/>}.
 *
 * F.
 * <p>Configuration of the environment object must be done through the
 * {@code ConfigurableEnvironment} interface, returned from all
 * {@code AbstractApplicationContext} subclass {@code getEnvironment()} methods. See
 * {@link ConfigurableEnvironment} Javadoc for usage examples demonstrating manipulation
 * of property sources prior to application context {@code refresh()}.
 *
 * @author Chris Beams
 * @since 3.1
 * @see PropertyResolver
 * @see EnvironmentCapable
 * @see ConfigurableEnvironment
 * @see AbstractEnvironment
 * @see StandardEnvironment
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#setEnvironment
 * @see org.springframework.context.support.AbstractApplicationContext#createEnvironment
 */
// 20201202 表示当前应用程序正在其中运行的环境的接口 => 与属性访问相关的方法通过{@link PropertyResolver}上层接口公开
public interface Environment extends PropertyResolver {

	/**
	 * 20201202
	 * A. 返回对此环境显式激活的配置文件集。Profiles用于创建要有条件注册的bean定义的逻辑分组，例如基于部署环境。配置文件可以通过设置
	 *    {@linkplain AbstractEnvironment#ACTIVE_Profiles_PROPERTY_NAME}来激活配置文件“spring.profiles.active“作为系统属性，
	 *    或调用{@link ConfigurableEnvironment#setActiveProfiles（String…）}。
	 * B. 如果没有明确指定为活动的配置文件，则任何{@linkplain getDefaultProfiles（）defaultprofiles}都将自动激活。
	 */
	/**
	 * A.
	 * Return the set of profiles explicitly made active for this environment. Profiles
	 * are used for creating logical groupings of bean definitions to be registered
	 * conditionally, for example based on deployment environment. Profiles can be
	 * activated by setting {@linkplain AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 * "spring.profiles.active"} as a system property or by calling
	 * {@link ConfigurableEnvironment#setActiveProfiles(String...)}.
	 *
	 * B.
	 * <p>If no profiles have explicitly been specified as active, then any
	 * {@linkplain #getDefaultProfiles() default profiles} will automatically be activated.
	 * @see #getDefaultProfiles
	 * @see ConfigurableEnvironment#setActiveProfiles
	 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 */
	// 20201202 获取激活的配置文件集
	String[] getActiveProfiles();

	/**
	 * Return the set of profiles to be active by default when no active profiles have
	 * been set explicitly.
	 * @see #getActiveProfiles
	 * @see ConfigurableEnvironment#setDefaultProfiles
	 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
	 */
	// 20201208 当未显式设置活动配置文件时，将默认情况下返回一组配置文件为活动状态。
	String[] getDefaultProfiles();

	/**
	 * Return whether one or more of the given profiles is active or, in the case of no
	 * explicit active profiles, whether one or more of the given profiles is included in
	 * the set of default profiles. If a profile begins with '!' the logic is inverted,
	 * i.e. the method will return {@code true} if the given profile is <em>not</em> active.
	 * For example, {@code env.acceptsProfiles("p1", "!p2")} will return {@code true} if
	 * profile 'p1' is active or 'p2' is not active.
	 * @throws IllegalArgumentException if called with zero arguments
	 * or if any profile is {@code null}, empty, or whitespace only
	 * @see #getActiveProfiles
	 * @see #getDefaultProfiles
	 * @see #acceptsProfiles(Profiles)
	 * @deprecated as of 5.1 in favor of {@link #acceptsProfiles(Profiles)}
	 */
	@Deprecated
	boolean acceptsProfiles(String... profiles);

	/**
	 * Return whether the {@linkplain #getActiveProfiles() active profiles}
	 * match the given {@link Profiles} predicate.
	 */
	// 20201209 返回{@linkplain #getActiveProfiles（）活动配置文件}是否与给定的{@link Profiles}谓词匹配。
	boolean acceptsProfiles(Profiles profiles);

}
