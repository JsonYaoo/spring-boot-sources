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

package org.springframework.context.annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 20201213
 * A. 由在处理{@link Configuration}类时注册其他bean定义的类型的类型实现的接口。 在bean定义级别（与{@code @Bean}方法/实例级别相对）进行操作时很有用或有必要。
 * B. 与{@code @Configuration}和{@link ImportSelector}一起，可以将此类型的类提供给{@link Import}批注（或也可以从{@code ImportSelector}返回）。
 * C. {@link ImportBeanDefinitionRegistrar}可以实现以下任何{@link org.springframework.beans.factory.Aware Aware}接口，并且它们各自的方法将在
 *    {@link #registerBeanDefinitions}之前被调用：
 *    	a. {@link org.springframework.context.EnvironmentAware EnvironmentAware}
 *    	b. {@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}
 *    	c. {@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}
 *      d. {@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}
 * D. 或者，该类可以为单个构造函数提供以下一种或多种受支持的参数类型：
 * 		a. {@link org.springframework.core.env.Environment Environment}
 * 		b. {@link org.springframework.beans.factory.BeanFactory BeanFactory}
 * 		c. {@link java.lang.ClassLoader ClassLoader}
 * 		d. {@link org.springframework.core.io.ResourceLoader ResourceLoader}
 * E. 有关用法示例，请参见实现和相关的单元测试。
 *
 */
/**
 * A.
 * Interface to be implemented by types that register additional bean definitions when
 * processing @{@link Configuration} classes. Useful when operating at the bean definition
 * level (as opposed to {@code @Bean} method/instance level) is desired or necessary.
 *
 * B.
 * <p>Along with {@code @Configuration} and {@link ImportSelector}, classes of this type
 * may be provided to the @{@link Import} annotation (or may also be returned from an
 * {@code ImportSelector}).
 *
 * C.
 * <p>An {@link ImportBeanDefinitionRegistrar} may implement any of the following
 * {@link org.springframework.beans.factory.Aware Aware} interfaces, and their respective
 * methods will be called prior to {@link #registerBeanDefinitions}:
 * <ul>
 * a.
 * <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware}</li>
 *
 * b.
 * <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}
 *
 * c.
 * <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}
 *
 * d.
 * <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}
 * </ul>
 *
 * D.
 * <p>Alternatively, the class may provide a single constructor with one or more of
 * the following supported parameter types:
 * <ul>
 * a.
 * <li>{@link org.springframework.core.env.Environment Environment}</li>
 *
 * b.
 * <li>{@link org.springframework.beans.factory.BeanFactory BeanFactory}</li>
 *
 * c.
 * <li>{@link java.lang.ClassLoader ClassLoader}</li>
 *
 * d.
 * <li>{@link org.springframework.core.io.ResourceLoader ResourceLoader}</li>
 * </ul>
 *
 * E.
 * <p>See implementations and associated unit tests for usage examples.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see Import
 * @see ImportSelector
 * @see Configuration
 */
// 20201213 @Import BeanDefinition注册器
public interface ImportBeanDefinitionRegistrar {

	/**
	 * 20201213
	 * A. 根据导入的{@code @Configuration}类的给定注释元数据，根据需要注册Bean定义。
	 * B. 请注意，由于与{@code @Configuration}类处理相关的生命周期限制，{@link BeanDefinitionRegistryPostProcessor}类型可能无法在此处注册。
	 * C. 默认实现委托给{@link #registerBeanDefinitions（AnnotationMetadata，BeanDefinitionRegistry）}。
	 */
	/**
	 * A.
	 * Register bean definitions as necessary based on the given annotation metadata of
	 * the importing {@code @Configuration} class.
	 *
	 * B.
	 * <p>Note that {@link BeanDefinitionRegistryPostProcessor} types may <em>not</em> be
	 * registered here, due to lifecycle constraints related to {@code @Configuration}
	 * class processing.
	 *
	 * C.
	 * <p>The default implementation delegates to
	 * {@link #registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)}.
	 *
	 * @param importingClassMetadata annotation metadata of the importing class
	 * @param registry current bean definition registry
	 * @param importBeanNameGenerator the bean name generator strategy for imported beans:
	 * {@link ConfigurationClassPostProcessor#IMPORT_BEAN_NAME_GENERATOR} by default, or a
	 * user-provided one if {@link ConfigurationClassPostProcessor#setBeanNameGenerator}
	 * has been set. In the latter case, the passed-in strategy will be the same used for
	 * component scanning in the containing application context (otherwise, the default
	 * component-scan naming strategy is {@link AnnotationBeanNameGenerator#INSTANCE}).
	 * @since 5.2
	 * @see ConfigurationClassPostProcessor#IMPORT_BEAN_NAME_GENERATOR
	 * @see ConfigurationClassPostProcessor#setBeanNameGenerator
	 */
	// 20201213  根据导入的{@code @Configuration}类的给定注释元数据，根据需要注册Bean定义
	default void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
			BeanNameGenerator importBeanNameGenerator) {
		registerBeanDefinitions(importingClassMetadata, registry);
	}

	/**
	 * 20201213
	 * A. 根据导入的{@code @Configuration}类的给定注释元数据，根据需要注册Bean定义。
	 * B. 请注意，由于与{@code @Configuration}类处理相关的生命周期限制，{@link BeanDefinitionRegistryPostProcessor}类型可能无法在此处注册。
	 * C. 默认实现为空。
	 */
	/**
	 * A.
	 * Register bean definitions as necessary based on the given annotation metadata of
	 * the importing {@code @Configuration} class.
	 *
	 * B.
	 * <p>Note that {@link BeanDefinitionRegistryPostProcessor} types may <em>not</em> be
	 * registered here, due to lifecycle constraints related to {@code @Configuration}
	 * class processing.
	 *
	 * C.
	 * <p>The default implementation is empty.
	 *
	 * @param importingClassMetadata annotation metadata of the importing class
	 * @param registry current bean definition registry
	 */
	// 20201213 根据导入的{@code @Configuration}类的给定注释元数据，根据需要注册Bean定义。
	default void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
	}

}
