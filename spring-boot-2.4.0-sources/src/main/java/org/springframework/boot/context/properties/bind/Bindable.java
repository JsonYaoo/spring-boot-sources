/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.context.properties.bind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Source that can be bound by a {@link Binder}.
 *
 * @param <T> the source type
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 * @see Bindable#of(Class)
 * @see Bindable#of(ResolvableType)
 */
// 20201202 可由{@link Binder}绑定的源。
public final class Bindable<T> {

	// 20201202 无注释
	private static final Annotation[] NO_ANNOTATIONS = {};

	// 20201202 ResolvableType实例
	private final ResolvableType type;

	// 20201202 开箱类型
	private final ResolvableType boxedType;

	// 20201202 结果提供者
	private final Supplier<T> value;

	// 20201202 注释数组
	private final Annotation[] annotations;

	// 20201202 构造Bindable实例
	private Bindable(ResolvableType type, ResolvableType boxedType, Supplier<T> value, Annotation[] annotations) {
		// 20201202 注册ResolvableType实例
		this.type = type;

		// 20201202 注册开箱类型
		this.boxedType = boxedType;

		// 20201202 注册结果提供者
		this.value = value;

		// 20201202 注册注释数组
		this.annotations = annotations;
	}

	/**
	 * Return the type of the item to bind.
	 * @return the type being bound
	 */
	public ResolvableType getType() {
		return this.type;
	}

	/**
	 * Return the boxed type of the item to bind.
	 * @return the boxed type for the item being bound
	 */
	public ResolvableType getBoxedType() {
		return this.boxedType;
	}

	/**
	 * Return a supplier that provides the object value or {@code null}.
	 * @return the value or {@code null}
	 */
	public Supplier<T> getValue() {
		return this.value;
	}

	/**
	 * Return any associated annotations that could affect binding.
	 * @return the associated annotations
	 */
	public Annotation[] getAnnotations() {
		return this.annotations;
	}

	/**
	 * Return a single associated annotations that could affect binding.
	 * @param <A> the annotation type
	 * @param type annotation type
	 * @return the associated annotation or {@code null}
	 */
	@SuppressWarnings("unchecked")
	public <A extends Annotation> A getAnnotation(Class<A> type) {
		for (Annotation annotation : this.annotations) {
			if (type.isInstance(annotation)) {
				return (A) annotation;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Bindable<?> other = (Bindable<?>) obj;
		boolean result = true;
		result = result && nullSafeEquals(this.type.resolve(), other.type.resolve());
		result = result && nullSafeEquals(this.annotations, other.annotations);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ObjectUtils.nullSafeHashCode(this.type);
		result = prime * result + ObjectUtils.nullSafeHashCode(this.annotations);
		return result;
	}

	@Override
	public String toString() {
		ToStringCreator creator = new ToStringCreator(this);
		creator.append("type", this.type);
		creator.append("value", (this.value != null) ? "provided" : "none");
		creator.append("annotations", this.annotations);
		return creator.toString();
	}

	private boolean nullSafeEquals(Object o1, Object o2) {
		return ObjectUtils.nullSafeEquals(o1, o2);
	}

	/**
	 * Create an updated {@link Bindable} instance with the specified annotations.
	 * @param annotations the annotations
	 * @return an updated {@link Bindable}
	 */
	public Bindable<T> withAnnotations(Annotation... annotations) {
		return new Bindable<>(this.type, this.boxedType, this.value,
				(annotations != null) ? annotations : NO_ANNOTATIONS);
	}

	/**
	 * Create an updated {@link Bindable} instance with an existing value.
	 * @param existingValue the existing value
	 * @return an updated {@link Bindable}
	 */
	// 20201202 使用现有值创建更新的{@linkbindable}实例。
	public Bindable<T> withExistingValue(T existingValue) {
		// 20201202 实例为空 | 实例为数组类型 | 实例为装箱类型, 则抛出异常
		Assert.isTrue(
				existingValue == null || this.type.isArray() || this.boxedType.resolve().isInstance(existingValue),
				() -> "ExistingValue must be an instance of " + this.type);

		// 20201202 声明为结果提供者接口类型
		Supplier<T> value = (existingValue != null) ? () -> existingValue : null;
		return new Bindable<>(this.type, this.boxedType, value, this.annotations);
	}

	/**
	 * Create an updated {@link Bindable} instance with a value supplier.
	 * @param suppliedValue the supplier for the value
	 * @return an updated {@link Bindable}
	 */
	public Bindable<T> withSuppliedValue(Supplier<T> suppliedValue) {
		return new Bindable<>(this.type, this.boxedType, suppliedValue, this.annotations);
	}

	/**
	 * Create a new {@link Bindable} of the type of the specified instance with an
	 * existing value equal to the instance.
	 * @param <T> the source type
	 * @param instance the instance (must not be {@code null})
	 * @return a {@link Bindable} instance
	 * @see #of(ResolvableType)
	 * @see #withExistingValue(Object)
	 */
	// 20201202 创建指定实例类型的新{@link Bindable}，该实例的现有值等于该实例。
	@SuppressWarnings("unchecked")
	public static <T> Bindable<T> ofInstance(T instance) {
		// 20201202 实例不能为空
		Assert.notNull(instance, "Instance must not be null");

		// 20201202 获取实例的Class对象
		Class<T> type = (Class<T>) instance.getClass();

		// 20201202 使用现有值创建更新的{@linkbindable}实例。
		return of(type).withExistingValue(instance);
	}

	/**
	 * Create a new {@link Bindable} of the specified type.
	 * @param <T> the source type
	 * @param type the type (must not be {@code null})
	 * @return a {@link Bindable} instance
	 * @see #of(ResolvableType)
	 */
	// 20201202 创建指定类型的新{@link Bindable}。
	public static <T> Bindable<T> of(Class<T> type) {
		// 20201202 Class不能为空
		Assert.notNull(type, "Type must not be null");

		// 20201202 创建指定类型的新{@link Bindable}
		return of(
				// 20201202 使用Type类型构造ResolvableType实例
				ResolvableType.forClass(type)
		);
	}

	/**
	 * Create a new {@link Bindable} {@link List} of the specified element type.
	 * @param <E> the element type
	 * @param elementType the list element type
	 * @return a {@link Bindable} instance
	 */
	public static <E> Bindable<List<E>> listOf(Class<E> elementType) {
		return of(ResolvableType.forClassWithGenerics(List.class, elementType));
	}

	/**
	 * Create a new {@link Bindable} {@link Set} of the specified element type.
	 * @param <E> the element type
	 * @param elementType the set element type
	 * @return a {@link Bindable} instance
	 */
	public static <E> Bindable<Set<E>> setOf(Class<E> elementType) {
		return of(ResolvableType.forClassWithGenerics(Set.class, elementType));
	}

	/**
	 * Create a new {@link Bindable} {@link Map} of the specified key and value type.
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param keyType the map key type
	 * @param valueType the map value type
	 * @return a {@link Bindable} instance
	 */
	public static <K, V> Bindable<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
		return of(ResolvableType.forClassWithGenerics(Map.class, keyType, valueType));
	}

	/**
	 * Create a new {@link Bindable} of the specified type.
	 * @param <T> the source type
	 * @param type the type (must not be {@code null})
	 * @return a {@link Bindable} instance
	 * @see #of(Class)
	 */
	// 20201202 创建指定类型的新{@link Bindable}。
	public static <T> Bindable<T> of(ResolvableType type) {
		// 20201202 ResolvableType实例不能为空
		Assert.notNull(type, "Type must not be null");

		// 20201202 获取ResolvableType实例的开箱类型 -> 装箱类型则开箱, 数组类型则取数组的组件类型
		ResolvableType boxedType = box(type);

		// 20201202 构造Bindable实例 -> ResolvableType实例 & 开箱类型 & 无注释
		return new Bindable<>(type, boxedType, null, NO_ANNOTATIONS);
	}

	// 20201202 获取ResolvableType实例的开箱类型 -> 装箱类型则开箱, 数组类型则取数组的组件类型
	private static ResolvableType box(ResolvableType type) {
		// 20201202 获取已解析的类型
		Class<?> resolved = type.resolve();

		// 20201202 如果该类型是基础类型的装箱类型
		if (resolved != null && resolved.isPrimitive()) {
			// 20201202 则对基础类型进行拆箱
			Object array = Array.newInstance(resolved, 1);
			Class<?> wrapperType = Array.get(array, 0).getClass();

			// 20201202 返回该类型的ResolvableType实例
			return ResolvableType.forClass(wrapperType);
		}

		// 20201202 如果该类型是数组类型
		if (resolved != null && resolved.isArray()) {
			// 20201202 则构造数组类型的组件类型的ResolvableType实例
			return ResolvableType.forArrayComponent(box(type.getComponentType()));
		}
		return type;
	}

}
