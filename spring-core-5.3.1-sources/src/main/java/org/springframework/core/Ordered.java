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

package org.springframework.core;

/**
 * 20201207
 * A. {@code Ordered}是一个可以由应排序的对象实现的接口，例如在{@code Collection}中。
 * B. 实际的{@link #getOrder（）顺序}可以解释为优先级，第一个对象（具有最小的顺序值）具有最高优先级。
 * C. 请注意，此接口还有一个优先级标记：{@link PriorityOrdered}。 有关{@code PriorityOrdered}对象相对于普通{@link Ordered}对象如何排序的详细信息，请查阅Javadoc。
 * D. 有关非排序对象的排序语义的详细信息，请查阅Javadoc中的{@link OrderComparator}。
 */
/**
 * A.
 * {@code Ordered} is an interface that can be implemented by objects that
 * should be <em>orderable</em>, for example in a {@code Collection}.
 *
 * B.
 * <p>The actual {@link #getOrder() order} can be interpreted as prioritization,
 * with the first object (with the lowest order value) having the highest
 * priority.
 *
 * C.
 * <p>Note that there is also a <em>priority</em> marker for this interface:
 * {@link PriorityOrdered}. Consult the Javadoc for {@code PriorityOrdered} for
 * details on how {@code PriorityOrdered} objects are ordered relative to
 * <em>plain</em> {@link Ordered} objects.
 *
 * D.
 * <p>Consult the Javadoc for {@link OrderComparator} for details on the
 * sort semantics for non-ordered objects.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 07.04.2003
 * @see PriorityOrdered
 * @see OrderComparator
 * @see org.springframework.core.annotation.Order
 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
 */
// 20201207 {@code Ordered}是一个可以由应排序的对象实现的接口，例如在{@code Collection}中。
public interface Ordered {

	/**
	 * Useful constant for the highest precedence value.
	 * @see Integer#MIN_VALUE
	 */
	int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

	/**
	 * Useful constant for the lowest precedence value.
	 * @see Integer#MAX_VALUE
	 */
	int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


	/**
	 * Get the order value of this object.
	 * <p>Higher values are interpreted as lower priority. As a consequence,
	 * the object with the lowest value has the highest priority (somewhat
	 * analogous to Servlet {@code load-on-startup} values).
	 * <p>Same order values will result in arbitrary sort positions for the
	 * affected objects.
	 * @return the order value
	 * @see #HIGHEST_PRECEDENCE
	 * @see #LOWEST_PRECEDENCE
	 */
	int getOrder();

}
