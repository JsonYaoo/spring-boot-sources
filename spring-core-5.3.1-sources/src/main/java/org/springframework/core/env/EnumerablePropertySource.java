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

package org.springframework.core.env;

import org.springframework.util.ObjectUtils;

/**
 * 20201202
 * A. 一个{@link PropertySource}实现，能够询问其底层源对象以枚举所有可能的属性名/值对。公开{@link #getPropertyNames（）}方法，允许调用方在不访问基础源对象的情况下
 *    检查可用属性。这也有助于{@link #containsProperty（String）}的更高效实现，因为它可以调用{@link #getPropertyNames（）}并迭代返回的数组，而不是尝试调用
 *    {@link #getProperty（String）}，这可能会花费更多的成本。实现可以考虑缓存{@link #getPropertyNames（）}的结果，以充分利用这一性能机会。
 *  B. 大多数框架提供的{@code propertysource}实现都是可枚举的；一个反例是{@code jndipropertysource}，其中，由于JNDI的性质，不可能在任何给定的时间确定所有可能的属性名称；
 *     相反，只有尝试访问属性（通过{@link #getProperty（String）}）来评估它是否存在。
 */
/**
 * A.
 * A {@link PropertySource} implementation capable of interrogating its
 * underlying source object to enumerate all possible property name/value
 * pairs. Exposes the {@link #getPropertyNames()} method to allow callers
 * to introspect available properties without having to access the underlying
 * source object. This also facilitates a more efficient implementation of
 * {@link #containsProperty(String)}, in that it can call {@link #getPropertyNames()}
 * and iterate through the returned array rather than attempting a call to
 * {@link #getProperty(String)} which may be more expensive. Implementations may
 * consider caching the result of {@link #getPropertyNames()} to fully exploit this
 * performance opportunity.
 *
 * B.
 * <p>Most framework-provided {@code PropertySource} implementations are enumerable;
 * a counter-example would be {@code JndiPropertySource} where, due to the
 * nature of JNDI it is not possible to determine all possible property names at
 * any given time; rather it is only possible to try to access a property
 * (via {@link #getProperty(String)}) in order to evaluate whether it is present
 * or not.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @param <T> the source type
 */
// 20201202 一个{@link PropertySource}实现，能够询问其底层源对象以枚举所有可能的属性名/值对
public abstract class EnumerablePropertySource<T> extends PropertySource<T> {

	/**
	 * Create a new {@code EnumerablePropertySource} with the given name and source object.
	 * @param name the associated name
	 * @param source the source object
	 */
	public EnumerablePropertySource(String name, T source) {
		super(name, source);
	}

	/**
	 * Create a new {@code EnumerablePropertySource} with the given name and with a new
	 * {@code Object} instance as the underlying source.
	 * @param name the associated name
	 */
	protected EnumerablePropertySource(String name) {
		super(name);
	}


	/**
	 * Return whether this {@code PropertySource} contains a property with the given name.
	 * <p>This implementation checks for the presence of the given name within the
	 * {@link #getPropertyNames()} array.
	 * @param name the name of the property to find
	 */
	@Override
	public boolean containsProperty(String name) {
		return ObjectUtils.containsElement(getPropertyNames(), name);
	}

	/**
	 * Return the names of all properties contained by the
	 * {@linkplain #getSource() source} object (never {@code null}).
	 */
	public abstract String[] getPropertyNames();

}
