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

package org.springframework.core.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.annotation.RepeatableContainers;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

/**
 * {@link AnnotationMetadata} implementation that uses standard reflection
 * to introspect a given {@link Class}.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 2.5
 */
// 20201208 {@link AnnotationMetadata}实现，该实现使用标准反射内省给定的{@link Class}, 该Class实例包装了注解数据。
public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

	// 20201208 所有注解和元注解的合并注解
	private final MergedAnnotations mergedAnnotations;

	// 20201208 是否把嵌套的注解转换成Map
	private final boolean nestedAnnotationsAsMap;

	@Nullable
	private Set<String> annotationTypes;


	/**
	 * Create a new {@code StandardAnnotationMetadata} wrapper for the given Class.
	 * @param introspectedClass the Class to introspect
	 * @see #StandardAnnotationMetadata(Class, boolean)
	 * @deprecated since 5.2 in favor of the factory method {@link AnnotationMetadata#introspect(Class)}
	 */
	@Deprecated
	public StandardAnnotationMetadata(Class<?> introspectedClass) {
		this(introspectedClass, false);
	}

	/**
	 * Create a new {@link StandardAnnotationMetadata} wrapper for the given Class,
	 * providing the option to return any nested annotations or annotation arrays in the
	 * form of {@link org.springframework.core.annotation.AnnotationAttributes} instead
	 * of actual {@link Annotation} instances.
	 *
	 * @param introspectedClass the Class to introspect // 20201208 需要加载的bean Class
	 *
	 * // 20201208 以{@link org.springframework.core.annotation.AnnotationAttributes}的形式返回嵌套的注解和注解数组，以与基于ASM的
	 * // 20201208 {@link AnnotationMetadata}实现兼容
	 * @param nestedAnnotationsAsMap return nested annotations and annotation arrays as
	 * {@link org.springframework.core.annotation.AnnotationAttributes} for compatibility
	 * with ASM-based {@link AnnotationMetadata} implementations
	 * @since 3.1.1
	 * @deprecated since 5.2 in favor of the factory method {@link AnnotationMetadata#introspect(Class)}.
	 * Use {@link MergedAnnotation#asMap(MergedAnnotation.Adapt...) MergedAnnotation.asMap}
	 * from {@link #getAnnotations()} rather than {@link #getAnnotationAttributes(String)}
	 * if {@code nestedAnnotationsAsMap} is {@code false}
	 */
	// 20201208 为给定的类创建一个新的{@link StandardAnnotationMetadata}包装器，并提供以
	// 20201208 {@link org.springframework.core.annotation.AnnotationAttributes}的形式而不是实际的{@link Annotation}形式返回任何嵌套注解或注解数组的选项实例。
	@Deprecated
	public StandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
		// 20201208 为给定的类创建一个新的StandardClassMetadata包装器。
		super(introspectedClass);

		// 20201208 注册所有注解和元注解的合并注解 -> 过滤"java.lang", "org.springframework.lang"下的注解
		this.mergedAnnotations = MergedAnnotations.from(
				// 20201208 给定的类
				introspectedClass,
				// 20201208 继承的注释查找
				SearchStrategy.INHERITED_ANNOTATIONS,
				// 20201208 获取NoRepeatableContainers单例
				RepeatableContainers.none());

		// 20201208 注册是否把嵌套的注解转换成Map -> Springboot启动时为true
		this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
	}


	@Override
	public MergedAnnotations getAnnotations() {
		return this.mergedAnnotations;
	}

	@Override
	public Set<String> getAnnotationTypes() {
		Set<String> annotationTypes = this.annotationTypes;
		if (annotationTypes == null) {
			annotationTypes = Collections.unmodifiableSet(AnnotationMetadata.super.getAnnotationTypes());
			this.annotationTypes = annotationTypes;
		}
		return annotationTypes;
	}

	@Override
	@Nullable
	public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
		if (this.nestedAnnotationsAsMap) {
			return AnnotationMetadata.super.getAnnotationAttributes(annotationName, classValuesAsString);
		}
		return AnnotatedElementUtils.getMergedAnnotationAttributes(
				getIntrospectedClass(), annotationName, classValuesAsString, false);
	}

	@Override
	@Nullable
	public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName, boolean classValuesAsString) {
		if (this.nestedAnnotationsAsMap) {
			return AnnotationMetadata.super.getAllAnnotationAttributes(annotationName, classValuesAsString);
		}
		return AnnotatedElementUtils.getAllAnnotationAttributes(
				getIntrospectedClass(), annotationName, classValuesAsString, false);
	}

	@Override
	public boolean hasAnnotatedMethods(String annotationName) {
		if (AnnotationUtils.isCandidateClass(getIntrospectedClass(), annotationName)) {
			try {
				Method[] methods = ReflectionUtils.getDeclaredMethods(getIntrospectedClass());
				for (Method method : methods) {
					if (isAnnotatedMethod(method, annotationName)) {
						return true;
					}
				}
			}
			catch (Throwable ex) {
				throw new IllegalStateException("Failed to introspect annotated methods on " + getIntrospectedClass(), ex);
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
		Set<MethodMetadata> annotatedMethods = null;
		if (AnnotationUtils.isCandidateClass(getIntrospectedClass(), annotationName)) {
			try {
				Method[] methods = ReflectionUtils.getDeclaredMethods(getIntrospectedClass());
				for (Method method : methods) {
					if (isAnnotatedMethod(method, annotationName)) {
						if (annotatedMethods == null) {
							annotatedMethods = new LinkedHashSet<>(4);
						}
						annotatedMethods.add(new StandardMethodMetadata(method, this.nestedAnnotationsAsMap));
					}
				}
			}
			catch (Throwable ex) {
				throw new IllegalStateException("Failed to introspect annotated methods on " + getIntrospectedClass(), ex);
			}
		}
		return annotatedMethods != null ? annotatedMethods : Collections.emptySet();
	}

	private boolean isAnnotatedMethod(Method method, String annotationName) {
		return !method.isBridge() && method.getAnnotations().length > 0 &&
				AnnotatedElementUtils.isAnnotated(method, annotationName);
	}

	// 20201208 为给定的类创建一个新的{@link StandardAnnotationMetadata}包装器, 以AnnotationAttributes形式返回任何嵌套注解或注解数组的选项实例
	static AnnotationMetadata from(Class<?> introspectedClass) {
		// 20201208 为给定的类创建一个新的{@link StandardAnnotationMetadata}包装器, 以AnnotationAttributes形式返回任何嵌套注解或注解数组的选项实例
		return new StandardAnnotationMetadata(introspectedClass, true);
	}

}
