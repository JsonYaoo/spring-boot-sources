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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

/**
 * Provides a quick way to access the attribute methods of an {@link Annotation}
 * with consistent ordering as well as a few useful utility methods.
 *
 * @author Phillip Webb
 * @since 5.2
 */
// 20201208 提供一种以一致的顺序访问{@link Annotation}的属性方法的快速方法，以及一些有用的实用程序方法。
final class AttributeMethods {
	// 20201208 构造空的属性访问方法
	static final AttributeMethods NONE = new AttributeMethods(null, new Method[0]);

	// 20201208 注解类-属性访问方法缓存
	private static final Map<Class<? extends Annotation>, AttributeMethods> cache = new ConcurrentReferenceHashMap<>();

	// 20201208 方法按名称排序器
	private static final Comparator<Method> methodComparator = (m1, m2) -> {
		if (m1 != null && m2 != null) {
			return m1.getName().compareTo(m2.getName());
		}
		return m1 != null ? -1 : 1;
	};

	// 20201208 注解类型
	@Nullable
	private final Class<? extends Annotation> annotationType;

	// 20201208 方法数组
	private final Method[] attributeMethods;

	// 20201208 异常数组
	private final boolean[] canThrowTypeNotPresentException;

	private final boolean hasDefaultValueMethod;

	private final boolean hasNestedAnnotation;

	// 20201208 构造属性访问方法
	private AttributeMethods(@Nullable Class<? extends Annotation> annotationType, Method[] attributeMethods) {
		// 20201208 注册注解类型
		this.annotationType = annotationType;

		// 20201208 注册方法数组
		this.attributeMethods = attributeMethods;

		// 20201208 注册异常数组
		this.canThrowTypeNotPresentException = new boolean[attributeMethods.length];

		// 20201208 发现默认值方法, 默认为false
		boolean foundDefaultValueMethod = false;

		// 20201208 发现嵌套注解, 默认为false
		boolean foundNestedAnnotation = false;

		// 20201208 遍历方法数组
		for (int i = 0; i < attributeMethods.length; i++) {
			Method method = this.attributeMethods[i];
			// 20201208 获取方法返回值
			Class<?> type = method.getReturnType();

			// 20201208
			if (method.getDefaultValue() != null) {
				foundDefaultValueMethod = true;
			}
			if (type.isAnnotation() || (type.isArray() && type.getComponentType().isAnnotation())) {
				foundNestedAnnotation = true;
			}
			ReflectionUtils.makeAccessible(method);
			this.canThrowTypeNotPresentException[i] = (type == Class.class || type == Class[].class || type.isEnum());
		}
		this.hasDefaultValueMethod = foundDefaultValueMethod;
		this.hasNestedAnnotation = foundNestedAnnotation;
	}


	/**
	 * Determine if this instance only contains a single attribute named
	 * {@code value}.
	 * @return {@code true} if there is only a value attribute
	 */
	boolean hasOnlyValueAttribute() {
		return (this.attributeMethods.length == 1 &&
				MergedAnnotation.VALUE.equals(this.attributeMethods[0].getName()));
	}


	/**
	 * Determine if values from the given annotation can be safely accessed without
	 * causing any {@link TypeNotPresentException TypeNotPresentExceptions}.
	 * @param annotation the annotation to check
	 * @return {@code true} if all values are present
	 * @see #validate(Annotation)
	 */
	// 20201208 确定是否可以安全地访问给定注解中的值，而不会引起任何{@link TypeNotPresentException TypeNotPresentExceptions}。
	boolean isValid(Annotation annotation) {
		// 20201208 断言是个注解
		assertAnnotation(annotation);

		// 20201208 遍历访问每个属性
		for (int i = 0; i < size(); i++) {
			if (canThrowTypeNotPresentException(i)) {
				try {
					get(i).invoke(annotation);
				}
				catch (Throwable ex) {
					// 20201208 如果访问抛出异常, 则说明不安全
					return false;
				}
			}
		}

		// 20201208 否则返回true, 说明安全
		return true;
	}

	/**
	 * Check if values from the given annotation can be safely accessed without causing
	 * any {@link TypeNotPresentException TypeNotPresentExceptions}. In particular,
	 * this method is designed to cover Google App Engine's late arrival of such
	 * exceptions for {@code Class} values (instead of the more typical early
	 * {@code Class.getAnnotations() failure}.
	 * @param annotation the annotation to validate
	 * @throws IllegalStateException if a declared {@code Class} attribute could not be read
	 * @see #isValid(Annotation)
	 */
	void validate(Annotation annotation) {
		assertAnnotation(annotation);
		for (int i = 0; i < size(); i++) {
			if (canThrowTypeNotPresentException(i)) {
				try {
					get(i).invoke(annotation);
				}
				catch (Throwable ex) {
					throw new IllegalStateException("Could not obtain annotation attribute value for " +
							get(i).getName() + " declared on " + annotation.annotationType(), ex);
				}
			}
		}
	}

	// 20201208 断言是个注解
	private void assertAnnotation(Annotation annotation) {
		Assert.notNull(annotation, "Annotation must not be null");
		if (this.annotationType != null) {
			Assert.isInstanceOf(this.annotationType, annotation);
		}
	}

	/**
	 * Get the attribute with the specified name or {@code null} if no
	 * matching attribute exists.
	 * @param name the attribute name to find
	 * @return the attribute method or {@code null}
	 */
	@Nullable
	Method get(String name) {
		int index = indexOf(name);
		return index != -1 ? this.attributeMethods[index] : null;
	}

	/**
	 * Get the attribute at the specified index.
	 * @param index the index of the attribute to return
	 * @return the attribute method
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * (<tt>index &lt; 0 || index &gt;= size()</tt>)
	 */
	// 20201208 获取指定索引处的属性。
	Method get(int index) {
		return this.attributeMethods[index];
	}

	/**
	 * Determine if the attribute at the specified index could throw a
	 * {@link TypeNotPresentException} when accessed.
	 * @param index the index of the attribute to check
	 * @return {@code true} if the attribute can throw a
	 * {@link TypeNotPresentException}
	 */
	boolean canThrowTypeNotPresentException(int index) {
		return this.canThrowTypeNotPresentException[index];
	}

	/**
	 * Get the index of the attribute with the specified name, or {@code -1}
	 * if there is no attribute with the name.
	 * @param name the name to find
	 * @return the index of the attribute, or {@code -1}
	 */
	int indexOf(String name) {
		for (int i = 0; i < this.attributeMethods.length; i++) {
			if (this.attributeMethods[i].getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the index of the specified attribute, or {@code -1} if the
	 * attribute is not in this collection.
	 * @param attribute the attribute to find
	 * @return the index of the attribute, or {@code -1}
	 */
	int indexOf(Method attribute) {
		for (int i = 0; i < this.attributeMethods.length; i++) {
			if (this.attributeMethods[i].equals(attribute)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the number of attributes in this collection.
	 * @return the number of attributes
	 */
	int size() {
		return this.attributeMethods.length;
	}

	/**
	 * Determine if at least one of the attribute methods has a default value.
	 * @return {@code true} if there is at least one attribute method with a default value
	 */
	boolean hasDefaultValueMethod() {
		return this.hasDefaultValueMethod;
	}

	/**
	 * Determine if at least one of the attribute methods is a nested annotation.
	 * @return {@code true} if there is at least one attribute method with a nested
	 * annotation type
	 */
	boolean hasNestedAnnotation() {
		return this.hasNestedAnnotation;
	}


	/**
	 * Get the attribute methods for the given annotation type.
	 * @param annotationType the annotation type
	 * @return the attribute methods for the annotation type
	 */
	// 20201208 获取给定注释类型的属性方法。
	static AttributeMethods forAnnotationType(@Nullable Class<? extends Annotation> annotationType) {
		// 20201208 如果属性类型为空
		if (annotationType == null) {
			// 20201208 则返回空的属性访问方法
			return NONE;
		}

		// 20201208 根据注解Class从注解类-属性访问方法缓存中获取属性访问方法 如果获取不到则根据注解类型计算属性访问方法
		return cache.computeIfAbsent(annotationType, AttributeMethods::compute);
	}

	// 20201208 根据注解类型计算属性访问方法
	private static AttributeMethods compute(Class<? extends Annotation> annotationType) {
		// 20201208 获取注解属性的本类方法对象数组
		Method[] methods = annotationType.getDeclaredMethods();

		// 20201208 遍历该数组
		int size = methods.length;
		for (int i = 0; i < methods.length; i++) {
			// 20201208 判断是否为属性方法 -> 如果参数个树为0 且返回值不为void类型, 则说明为属性方法
			if (!isAttributeMethod(methods[i])) {
				// 20201208 如果不是, 则剔除该方法
				methods[i] = null;
				size--;
			}
		}

		// 20201208 如果剔除后的数组长度为0
		if (size == 0) {
			// 20201208 则返回空的属性访问方法
			return NONE;
		}

		// 20201208 如果不为0, 则对根据方法名称进行数组排序
		Arrays.sort(methods, methodComparator);

		// 202001208 深复制该方法数组
		Method[] attributeMethods = Arrays.copyOf(methods, size);

		// 20201208 构造属性访问方法并返回
		return new AttributeMethods(annotationType, attributeMethods);
	}

	// 20201208 判断是否为属性方法 -> 如果参数个树为0 且返回值不为void类型, 则说明为属性方法
	private static boolean isAttributeMethod(Method method) {
		return (method.getParameterCount() == 0 && method.getReturnType() != void.class);
	}

	/**
	 * Create a description for the given attribute method suitable to use in
	 * exception messages and logs.
	 * @param attribute the attribute to describe
	 * @return a description of the attribute
	 */
	static String describe(@Nullable Method attribute) {
		if (attribute == null) {
			return "(none)";
		}
		return describe(attribute.getDeclaringClass(), attribute.getName());
	}

	/**
	 * Create a description for the given attribute method suitable to use in
	 * exception messages and logs.
	 * @param annotationType the annotation type
	 * @param attributeName the attribute name
	 * @return a description of the attribute
	 */
	static String describe(@Nullable Class<?> annotationType, @Nullable String attributeName) {
		if (attributeName == null) {
			return "(none)";
		}
		String in = (annotationType != null ? " in annotation [" + annotationType.getName() + "]" : "");
		return "attribute '" + attributeName + "'" + in;
	}

}
