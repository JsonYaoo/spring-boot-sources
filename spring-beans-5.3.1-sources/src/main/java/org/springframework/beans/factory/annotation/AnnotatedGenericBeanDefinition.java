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

package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201208
 * A. {@link GenericBeanDefinition}类的扩展，增加了对通过{@link AnnotatedBeanDefinition}接口公开的注解元数据的支持。
 * B. 这个GenericBeanDefinition变体主要用于测试希望在AnnotatedBeanDefinition上运行的代码，例如Spring的组件扫描支持中的策略实现（默认定义类为
 *   {@link org.springframework.context.annotation.ScannedGenericBeanDefinition}，该实现也实现了） AnnotatedBeanDefinition接口）。
 */
/**
 * A.
 * Extension of the {@link GenericBeanDefinition}
 * class, adding support for annotation metadata exposed through the
 * {@link AnnotatedBeanDefinition} interface.
 *
 * B.
 * <p>This GenericBeanDefinition variant is mainly useful for testing code that expects
 * to operate on an AnnotatedBeanDefinition, for example strategy implementations
 * in Spring's component scanning support (where the default definition class is
 * {@link org.springframework.context.annotation.ScannedGenericBeanDefinition},
 * which also implements the AnnotatedBeanDefinition interface).
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see AnnotatedBeanDefinition#getMetadata()
 * @see StandardAnnotationMetadata
 */
// 20201208 注解数据公开类: 增加了对通过{@link AnnotatedBeanDefinition}接口公开的注解元数据的支持, 主要用于测试希望在AnnotatedBeanDefinition上运行的代码
@SuppressWarnings("serial")
public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

	// 20201208 该注解实例
	private final AnnotationMetadata metadata;

	@Nullable
	private MethodMetadata factoryMethodMetadata;

	/**
	 * Create a new AnnotatedGenericBeanDefinition for the given bean class.
	 * @param beanClass the loaded bean class // 20201208 加载的Bean类
	 */
	// 20201208 为给定的bean类创建一个新的注解数据公开类AnnotatedGenericBeanDefinition —> 注册注解实例(但过滤"java.lang", "org.springframework.lang"下的注解)
	public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
		// 20201208 注册该注解的bean类
		setBeanClass(beanClass);

		// 20201208 注册注解实例 -> 为给定的类创建一个新的{@link StandardAnnotationMetadata}包装器, 以AnnotationAttributes形式返回任何嵌套注解或注解数组的选项实例(过滤"java.lang", "org.springframework.lang"下的注解)
		this.metadata = AnnotationMetadata.introspect(beanClass);
	}

	/**
	 * Create a new AnnotatedGenericBeanDefinition for the given annotation metadata,
	 * allowing for ASM-based processing and avoidance of early loading of the bean class.
	 * Note that this constructor is functionally equivalent to
	 * {@link org.springframework.context.annotation.ScannedGenericBeanDefinition
	 * ScannedGenericBeanDefinition}, however the semantics of the latter indicate that a
	 * bean was discovered specifically via component-scanning as opposed to other means.
	 * @param metadata the annotation metadata for the bean class in question
	 * @since 3.1.1
	 */
	public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
		Assert.notNull(metadata, "AnnotationMetadata must not be null");
		if (metadata instanceof StandardAnnotationMetadata) {
			setBeanClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
		}
		else {
			setBeanClassName(metadata.getClassName());
		}
		this.metadata = metadata;
	}

	/**
	 * Create a new AnnotatedGenericBeanDefinition for the given annotation metadata,
	 * based on an annotated class and a factory method on that class.
	 * @param metadata the annotation metadata for the bean class in question
	 * @param factoryMethodMetadata metadata for the selected factory method
	 * @since 4.1.1
	 */
	public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata, MethodMetadata factoryMethodMetadata) {
		this(metadata);
		Assert.notNull(factoryMethodMetadata, "MethodMetadata must not be null");
		setFactoryMethodName(factoryMethodMetadata.getMethodName());
		this.factoryMethodMetadata = factoryMethodMetadata;
	}

	// 20201209 获取此bean定义的bean类的注解元数据（以及基本类元数据）。
	@Override
	public final AnnotationMetadata getMetadata() {
		// 20201209 返回该注解实例
		return this.metadata;
	}

	@Override
	@Nullable
	public final MethodMetadata getFactoryMethodMetadata() {
		return this.factoryMethodMetadata;
	}

}
