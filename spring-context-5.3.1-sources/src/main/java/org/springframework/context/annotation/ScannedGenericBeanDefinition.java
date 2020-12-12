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

package org.springframework.context.annotation;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201212
 * A. 基于ASM ClassReader的{@link org.springframework.beans.factory.support.GenericBeanDefinition}类的扩展，支持通过{@link AnnotatedBeanDefinition}
 *    接口公开的注释元数据。
 * B. 此类不会尽早加载bean {@code Class}。 而是从ASM ClassReader解析的“ .class”文件本身中检索所有相关的元数据。 它在功能上等效于
 *    {@link AnnotatedGenericBeanDefinition＃AnnotatedGenericBeanDefinition（AnnotationMetadata）}，但按类型区分了已扫描的bean与以其他方式注册或检测到的bean。
 */
/**
 * A.
 * Extension of the {@link org.springframework.beans.factory.support.GenericBeanDefinition}
 * class, based on an ASM ClassReader, with support for annotation metadata exposed
 * through the {@link AnnotatedBeanDefinition} interface.
 *
 * B.
 * <p>This class does <i>not</i> load the bean {@code Class} early.
 * It rather retrieves all relevant metadata from the ".class" file itself,
 * parsed with the ASM ClassReader. It is functionally equivalent to
 * {@link AnnotatedGenericBeanDefinition#AnnotatedGenericBeanDefinition(AnnotationMetadata)}
 * but distinguishes by type beans that have been <em>scanned</em> vs those that have
 * been otherwise registered or detected by other means.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see #getMetadata()
 * @see #getBeanClassName()
 * @see org.springframework.core.type.classreading.MetadataReaderFactory
 * @see AnnotatedGenericBeanDefinition
 */
// 20201212 通用BeanDefinition, 但能按类型区分了已扫描的bean与以其他方式注册或检测到的bean
@SuppressWarnings("serial")
public class ScannedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

	private final AnnotationMetadata metadata;


	/**
	 * Create a new ScannedGenericBeanDefinition for the class that the
	 * given MetadataReader describes.
	 * @param metadataReader the MetadataReader for the scanned target class
	 */
	public ScannedGenericBeanDefinition(MetadataReader metadataReader) {
		Assert.notNull(metadataReader, "MetadataReader must not be null");
		this.metadata = metadataReader.getAnnotationMetadata();
		setBeanClassName(this.metadata.getClassName());
		setResource(metadataReader.getResource());
	}


	@Override
	public final AnnotationMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	@Nullable
	public MethodMetadata getFactoryMethodMetadata() {
		return null;
	}

}
