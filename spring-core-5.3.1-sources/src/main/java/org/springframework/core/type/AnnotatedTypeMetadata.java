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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotationCollectors;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotationSelectors;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

/**
 * Defines access to the annotations of a specific type ({@link AnnotationMetadata class}
 * or {@link MethodMetadata method}), in a form that does not necessarily require the
 * class-loading.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Mark Pollack
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see AnnotationMetadata
 * @see MethodMetadata
 */
// 20201208 定义对特定类型（{@link AnnotationMetadata类}或{@link MethodMetadata方法}）的注解的访问，其形式不一定需要加载类。
public interface AnnotatedTypeMetadata {

	/**
	 * Return annotation details based on the direct annotations of the
	 * underlying element.
	 * @return merged annotations based on the direct annotations
	 * @since 5.2
	 */
	// 20201208 根据基础元素的直接注解返回注解详细信息。
	MergedAnnotations getAnnotations();

	/**
	 * 20201208
	 * A. 确定基础元素是否具有已定义的给定类型的注解或元注解。
	 * B. 如果此方法返回{@code true}，则{@link #getAnnotationAttributes}将返回非null的Map。
	 */
	/**
	 * A.
	 * Determine whether the underlying element has an annotation or meta-annotation
	 * of the given type defined.
	 *
	 * B.
	 * <p>If this method returns {@code true}, then
	 * {@link #getAnnotationAttributes} will return a non-null Map.
	 *
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return whether a matching annotation is defined
	 */
	// 20201208 确定基础元素是否具有已定义的给定类型的注解或元注解
	default boolean isAnnotated(String annotationName) {
		// 20201208 返回注解详细信息确定指定的注解是直接存在还是元存在
		return getAnnotations().isPresent(annotationName);
	}

	/**
	 * Retrieve the attributes of the annotation of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation),
	 * also taking attribute overrides on composed annotations into account.
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return a Map of attributes, with the attribute name as key (e.g. "value")
	 * and the defined attribute value as Map value. This return value will be
	 * {@code null} if no matching annotation is defined.
	 */
	@Nullable
	default Map<String, Object> getAnnotationAttributes(String annotationName) {
		return getAnnotationAttributes(annotationName, false);
	}

	/**
	 * Retrieve the attributes of the annotation of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation),
	 * also taking attribute overrides on composed annotations into account.
	 *
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @param classValuesAsString whether to convert class references to String
	 * class names for exposure as values in the returned Map, instead of Class
	 * references which might potentially have to be loaded first
	 * @return a Map of attributes, with the attribute name as key (e.g. "value")
	 * and the defined attribute value as Map value. This return value will be
	 * {@code null} if no matching annotation is defined.
	 */
	// 20201209 检索给定类型的注解的属性（如果有的话）（即，如果在基础元素上定义为直接注解或元注解），也要考虑对组合注解的属性覆盖。
	@Nullable
	default Map<String, Object> getAnnotationAttributes(String annotationName,
                                                        boolean classValuesAsString) {

		// 20201209 获取合并的注解
		MergedAnnotation<Annotation> annotation = getAnnotations().get(
				annotationName,

				null,
				// 20201209 尽可能选择第一个直接声明的注释。 如果未声明直接注释，则选择最近的注释。
				MergedAnnotationSelectors.firstDirectlyDeclared());

		// 20201209 如果该注解是直接注解, 则直接返回
		if (!annotation.isPresent()) {
			return null;
		}

		// 20201209 从此合并的注解中创建一个新的可变{@link AnnotationAttributes}实例。
		return annotation.asAnnotationAttributes(Adapt.values(classValuesAsString, true));
	}

	/**
	 * Retrieve all attributes of all annotations of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * Note that this variant does <i>not</i> take attribute overrides into account.
	 *
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @return a MultiMap of attributes, with the attribute name as key (e.g. "value")
	 * and a list of the defined attribute values as Map value. This return value will
	 * be {@code null} if no matching annotation is defined.
	 * @see #getAllAnnotationAttributes(String, boolean)
	 */
	// 20201209 检索给定类型的所有注解的所有属性（如果有）（即，如果在基础元素上定义为直接注解或元注解）。 请注意，此变体不考虑属性替代。
	@Nullable
	default MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
		// 20201209 检索Conditional类型的所有注解的所有属性（如果有）（即，如果在基础元素上定义为直接注解或元注解）
		return getAllAnnotationAttributes(annotationName, false);
	}

	/**
	 * Retrieve all attributes of all annotations of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * Note that this variant does <i>not</i> take attribute overrides into account.
	 *
	 * @param annotationName the fully qualified class name of the annotation
	 * type to look for
	 * @param classValuesAsString  whether to convert class references to String
	 *
	 * // 20201208 属性的MultiMap，属性名称为键（例如“值”），定义的属性值列表为Map值。 如果未定义匹配的注解，则此返回值为{@code null}。
	 * @return a MultiMap of attributes, with the attribute name as key (e.g. "value")
	 * and a list of the defined attribute values as Map value. This return value will
	 * be {@code null} if no matching annotation is defined.
	 * @see #getAllAnnotationAttributes(String)
	 */
	// 20201208 检索给定类型的所有注解的所有属性（如果有）（即，如果在基础元素上定义为直接注解或元注解）。 请注意，此变体不考虑属性替代。
	@Nullable
	default MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName, boolean classValuesAsString) {
		// 20201209 获取Adapt数组: 可以应用于属性值的调整 -> 指定使类或类数组属性适应字符串为Conditional名称, 且指定使嵌套的注解或注解数组适合于映射，而不是合成值为true
		Adapt[] adaptations = Adapt.values(classValuesAsString, true);

		// 20201209 收集那些没有层次结构的, 非合并属性的注解属性集合 -> 根据基础元素的直接注解返回注解详细信息
		return getAnnotations().stream(annotationName)
				// 20201209 过滤掉那些提取到的键唯一的注解, 其注解是到{@link #getRoot（）根}的注解层次结构中注解类型的完整列表
				.filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
				// 20201209 获取剩下的注解中非合并的属性值 -> 创建注解的新视图，以显示非合并的属性值
				.map(MergedAnnotation::withNonMergedAttributes)
				// 20201209 {@link Collector}收集注解并将其合成为{@link LinkedMultiValueMap}
				.collect(MergedAnnotationCollectors.toMultiValueMap(map ->
						map.isEmpty() ? null : map, adaptations));
	}

}
