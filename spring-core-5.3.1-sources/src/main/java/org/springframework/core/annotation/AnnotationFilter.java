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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;

/**
 * 20201208
 * A. 可以用于过滤特定注释类型的回调接口。
 * B. 请注意，{@link MergedAnnotations}模型（为此接口设计的模型）始终根据{@link #PLAIN}过滤器忽略lang注释（出于效率考虑）。
 *    任何其他过滤器，甚至自定义过滤器实现都在此边界内应用，并且可能仅在此处缩小。
 */
/**
 * A.
 * Callback interface that can be used to filter specific annotation types.
 *
 * B.
 * <p>Note that the {@link MergedAnnotations} model (which this interface has been
 * designed for) always ignores lang annotations according to the {@link #PLAIN}
 * filter (for efficiency reasons). Any additional filters and even custom filter
 * implementations apply within this boundary and may only narrow further from here.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 5.2
 * @see MergedAnnotations
 */
// 20201208 可以用于过滤特定注释类型的回调接口
@FunctionalInterface
public interface AnnotationFilter {

	/**
	 * 20201208
	 * A. {@link AnnotationFilter}与{@code java.lang}和{@code org.springframework.lang}包及其子包中的注释匹配。
	 * B. 这是{@link MergedAnnotations}模型中的默认过滤器。
	 */
	/**
	 * A.
	 * {@link AnnotationFilter} that matches annotations in the
	 * {@code java.lang} and {@code org.springframework.lang} packages
	 * and their subpackages.
	 *
	 * B.
	 * <p>This is the default filter in the {@link MergedAnnotations} model.
	 */
	// 20201208 {@link MergedAnnotations}模型中的默认过滤器, 构建含包路径数组的包路径注释过滤器
	AnnotationFilter PLAIN = packages("java.lang", "org.springframework.lang");

	/**
	 * {@link AnnotationFilter} that matches annotations in the
	 * {@code java} and {@code javax} packages and their subpackages.
	 */
	AnnotationFilter JAVA = packages("java", "javax");

	/**
	 * {@link AnnotationFilter} that always matches and can be used when no
	 * relevant annotation types are expected to be present at all.
	 */
	// 20201208 {@link AnnotationFilter}始终匹配，可以在根本不存在任何相关注释类型时使用。
	AnnotationFilter ALL = new AnnotationFilter() {
		@Override
		public boolean matches(Annotation annotation) {
			return true;
		}
		@Override
		public boolean matches(Class<?> type) {
			return true;
		}
		@Override
		public boolean matches(String typeName) {
			return true;
		}
		@Override
		public String toString() {
			return "All annotations filtered";
		}
	};

	/**
	 * {@link AnnotationFilter} that never matches and can be used when no
	 * filtering is needed (allowing for any annotation types to be present).
	 * @deprecated as of 5.2.6 since the {@link MergedAnnotations} model
	 * always ignores lang annotations according to the {@link #PLAIN} filter
	 * (for efficiency reasons)
	 * @see #PLAIN
	 */
	@Deprecated
	AnnotationFilter NONE = new AnnotationFilter() {
		@Override
		public boolean matches(Annotation annotation) {
			return false;
		}
		@Override
		public boolean matches(Class<?> type) {
			return false;
		}
		@Override
		public boolean matches(String typeName) {
			return false;
		}
		@Override
		public String toString() {
			return "No annotation filtering";
		}
	};


	/**
	 * Test if the given annotation matches the filter.
	 * @param annotation the annotation to test
	 * @return {@code true} if the annotation matches
	 */
	default boolean matches(Annotation annotation) {
		return matches(annotation.annotationType());
	}

	/**
	 * Test if the given type matches the filter.
	 * @param type the annotation type to test
	 * @return {@code true} if the annotation matches
	 */
	default boolean matches(Class<?> type) {
		return matches(type.getName());
	}

	/**
	 * Test if the given type name matches the filter.
	 * @param typeName the fully qualified class name of the annotation type to test
	 * @return {@code true} if the annotation matches
	 */
	boolean matches(String typeName);


	/**
	 * Create a new {@link AnnotationFilter} that matches annotations in the
	 * specified packages.
	 * @param packages the annotation packages that should match // 20201208 应该匹配的注释包
	 * @return a new {@link AnnotationFilter} instance
	 */
	// 20201208 创建一个新的{@link AnnotationFilter}来匹配指定程序包中的注释。
	static AnnotationFilter packages(String... packages) {
		// 20201208 构造包路径注释过滤器
		return new PackagesAnnotationFilter(packages);
	}

}
