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

package org.springframework.core.type.filter;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 20201205
 * A. 一个简单的{@link TypeFilter}，它将类与给定的注解匹配，并检查继承的注解。
 * B. 默认情况下，匹配逻辑与{@link AnnotationUtils＃getAnnotation（java.lang.reflect.AnnotatedElement，Class）}的逻辑进行镜像，支持为单个级别的元注解提供或存在的注解。
 *    搜索元注解可能会被禁用。 类似地，可以选择启用对接口的注解的搜索。 有关详细信息，请咨询此类中的各种构造函数。
 */
/**
 * A.
 * A simple {@link TypeFilter} which matches classes with a given annotation,
 * checking inherited annotations as well.
 *
 * B.
 * <p>By default, the matching logic mirrors that of
 * {@link AnnotationUtils#getAnnotation(java.lang.reflect.AnnotatedElement, Class)},
 * supporting annotations that are <em>present</em> or <em>meta-present</em> for a
 * single level of meta-annotations. The search for meta-annotations my be disabled.
 * Similarly, the search for annotations on interfaces may optionally be enabled.
 * Consult the various constructors in this class for details.
 *
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
// 20201205 一个简单的{@link TypeFilter}: 检查类和继承的注解, 可以选择启用对接口的注解的搜索
public class AnnotationTypeFilter extends AbstractTypeHierarchyTraversingFilter {

	// 20201205 注解类型
	private final Class<? extends Annotation> annotationType;

	// 20201205 是否也匹配元注解
	private final boolean considerMetaAnnotations;

	/**
	 * 20201205
	 * A. 为给定的注解类型创建一个新的{@code AnnotationTypeFilter}。
	 * B. 过滤器还将匹配元注解。 要禁用元注解匹配，请使用接受'{@code thinkMetaAnnotations}'参数的构造函数。
	 * C. 过滤器将不匹配接口。
	 */
	/**
	 * A.
	 * Create a new {@code AnnotationTypeFilter} for the given annotation type.
	 *
	 * B.
	 * <p>The filter will also match meta-annotations. To disable the
	 * meta-annotation matching, use the constructor that accepts a
	 * '{@code considerMetaAnnotations}' argument.
	 *
	 * C.
	 * <p>The filter will not match interfaces.
	 *
	 * @param annotationType the annotation type to match
	 */
	public AnnotationTypeFilter(Class<? extends Annotation> annotationType) {
		// 20201205 为给定的注解类型创建一个新的{@code AnnotationTypeFilter} => 匹配元注解、不匹配接口
		this(annotationType, true, false);
	}

	/**
	 * Create a new {@code AnnotationTypeFilter} for the given annotation type.
	 * <p>The filter will not match interfaces.
	 * @param annotationType the annotation type to match
	 * @param considerMetaAnnotations whether to also match on meta-annotations
	 */
	public AnnotationTypeFilter(Class<? extends Annotation> annotationType, boolean considerMetaAnnotations) {
		this(annotationType, considerMetaAnnotations, false);
	}

	/**
	 * Create a new {@code AnnotationTypeFilter} for the given annotation type.
	 * @param annotationType the annotation type to match	// 20201205 匹配的注解类型
	 * @param considerMetaAnnotations whether to also match on meta-annotations // 20201205 是否也匹配元注解
	 * @param considerInterfaces whether to also match interfaces	// 20201205 是否也匹配接口
	 */
	// 20201205 为给定的注解类型创建一个新的{@code AnnotationTypeFilter}。
	public AnnotationTypeFilter(Class<? extends Annotation> annotationType, boolean considerMetaAnnotations, boolean considerInterfaces) {
		super(
				// 20201205 判断此元素上是否存在Inherited.class类型的注解 => Inherited注解仅使注解从超类继承； 已实现的接口上的注解无效，如果超类都不存在注解, 则说明不匹配超类注解
				annotationType.isAnnotationPresent(Inherited.class),

				// 20201205 是否匹配接口
				considerInterfaces
		);

		// 20201205 设置注解类型annotationType
		this.annotationType = annotationType;

		// 20201205 是否也匹配元注解
		this.considerMetaAnnotations = considerMetaAnnotations;
	}

	/**
	 * Return the {@link Annotation} that this instance is using to filter
	 * candidates.
	 * @since 5.0
	 */
	// 20201212 返回此实例用于过滤的{@link Annotation}
	public final Class<? extends Annotation> getAnnotationType() {
		return this.annotationType;
	}

	@Override
	protected boolean matchSelf(MetadataReader metadataReader) {
		AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
		return metadata.hasAnnotation(this.annotationType.getName()) ||
				(this.considerMetaAnnotations && metadata.hasMetaAnnotation(this.annotationType.getName()));
	}

	@Override
	@Nullable
	protected Boolean matchSuperClass(String superClassName) {
		return hasAnnotation(superClassName);
	}

	@Override
	@Nullable
	protected Boolean matchInterface(String interfaceName) {
		return hasAnnotation(interfaceName);
	}

	@Nullable
	protected Boolean hasAnnotation(String typeName) {
		if (Object.class.getName().equals(typeName)) {
			return false;
		}
		else if (typeName.startsWith("java")) {
			if (!this.annotationType.getName().startsWith("java")) {
				// Standard Java types do not have non-standard annotations on them ->
				// skip any load attempt, in particular for Java language interfaces.
				return false;
			}
			try {
				Class<?> clazz = ClassUtils.forName(typeName, getClass().getClassLoader());
				return ((this.considerMetaAnnotations ? AnnotationUtils.getAnnotation(clazz, this.annotationType) :
						clazz.getAnnotation(this.annotationType)) != null);
			}
			catch (Throwable ex) {
				// Class not regularly loadable - can't determine a match that way.
			}
		}
		return null;
	}

}
