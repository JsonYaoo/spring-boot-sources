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

package org.springframework.core.type;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;

/**
 * Interface that defines abstract access to the annotations of a specific
 * class, in a form that does not require that class to be loaded yet.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 2.5
 * @see StandardAnnotationMetadata
 * @see org.springframework.core.type.classreading.MetadataReader#getAnnotationMetadata()
 * @see AnnotatedTypeMetadata
 */
// 20201208 该接口定义了对特定类的注解的抽象访问，该接口的形式不需要加载该类。
public interface AnnotationMetadata extends ClassMetadata, AnnotatedTypeMetadata {

	/**
	 * Get the fully qualified class names of all annotation types that
	 * are <em>present</em> on the underlying class.
	 * @return the annotation type names
	 */
	// 20201209 获取基础类上存在的所有注解类型的全限定类名。
	default Set<String> getAnnotationTypes() {
		// 20201209 获取过滤掉直接出现在源上的注解的类型名称的列表 -> 根据基础元素的直接注解返回注解详细信息
		return getAnnotations().stream()
				// 20201209 过滤掉直接出现在源上的注解
				.filter(MergedAnnotation::isDirectlyPresent)
				// 20201209 获取注解类型名称
				.map(annotation -> annotation.getType().getName())
				// 20201209 收集所有注解类型名称
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Get the fully qualified class names of all meta-annotation types that
	 * are <em>present</em> on the given annotation type on the underlying class.
	 * @param annotationName the fully qualified class name of the meta-annotation
	 * type to look for
	 * @return the meta-annotation type names, or an empty set if none found
	 */
	// 20201209 获取存在于基础类的给定注解类型上的所有元注解类型的完全限定的类名称。
	default Set<String> getMetaAnnotationTypes(String annotationName) {
		MergedAnnotation<?> annotation = getAnnotations().get(annotationName, MergedAnnotation::isDirectlyPresent);
		if (!annotation.isPresent()) {
			return Collections.emptySet();
		}
		return MergedAnnotations.from(annotation.getType(), SearchStrategy.INHERITED_ANNOTATIONS).stream()
				.map(mergedAnnotation -> mergedAnnotation.getType().getName())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Determine whether an annotation of the given type is <em>present</em> on
	 * the underlying class.
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return {@code true} if a matching annotation is present
	 */
	default boolean hasAnnotation(String annotationName) {
		return getAnnotations().isDirectlyPresent(annotationName);
	}

	/**
	 * Determine whether the underlying class has an annotation that is itself
	 * annotated with the meta-annotation of the given type.
	 * @param metaAnnotationName the fully qualified class name of the
	 * meta-annotation type to look for
	 * @return {@code true} if a matching meta-annotation is present
	 */
	default boolean hasMetaAnnotation(String metaAnnotationName) {
		return getAnnotations().get(metaAnnotationName,
				MergedAnnotation::isMetaPresent).isPresent();
	}

	/**
	 * Determine whether the underlying class has any methods that are
	 * annotated (or meta-annotated) with the given annotation type.
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 */
	// 20201208 确定基础类是否具有使用给定注解类型进行注解（或元注解）的任何方法。
	default boolean hasAnnotatedMethods(String annotationName) {
		// 20201208 如果检索到的bean集合为空, 则说明没检索到, 返回false
		return !getAnnotatedMethods(annotationName).isEmpty();
	}

	/**
	 * 20201208
	 * A. 检索使用给定注解类型进行注解（或元注解）的所有方法的方法元数据。
	 * B. 对于任何返回的方法，{@ link MethodMetadata＃isAnnotated}将为给定的注解类型返回{@code true}。
	 */
	/**
	 * A.
	 * Retrieve the method metadata for all methods that are annotated
	 * (or meta-annotated) with the given annotation type.
	 *
	 * B.
	 * <p>For any returned method, {@link MethodMetadata#isAnnotated} will
	 * return {@code true} for the given annotation type.
	 *
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return a set of {@link MethodMetadata} for methods that have a matching
	 * annotation. The return value will be an empty set if no methods match
	 * the annotation type.
	 */
	// 20201208 检索使用给定注解类型进行注解（或元注解）的所有方法的方法元数据。
	Set<MethodMetadata> getAnnotatedMethods(String annotationName);

	/**
	 * Factory method to create a new {@link AnnotationMetadata} instance
	 * for the given class using standard reflection.
	 * @param type the class to introspect
	 * @return a new {@link AnnotationMetadata} instance
	 * @since 5.2
	 */
	// 20201208 使用标准反射为给定类创建新的{@link AnnotationMetadata}实例的工厂方法。
	// 20201208 为给定的类创建一个新的{@link StandardAnnotationMetadata}包装器, 以AnnotationAttributes形式返回任何嵌套注解或注解数组的选项实例
	static AnnotationMetadata introspect(Class<?> type) {
		// 20201208 为给定的类创建一个新的{@link StandardAnnotationMetadata}包装器, 以AnnotationAttributes形式返回任何嵌套注解或注解数组的选项实例
		return StandardAnnotationMetadata.from(type);
	}

}
