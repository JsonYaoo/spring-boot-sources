/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.lang.Nullable;

/**
 * 20201207
 * A. 任何对象都可以实现此接口以提供其实际的{@link ResolvableType}。
 * B. 当确定实例是否与通用签名匹配时，此类信息非常有用，因为Java在运行时不会传递签名。
 * C. 使用此接口的用户在复杂的层次结构方案中应格外小心，尤其是当类的通用类型签名在子类中更改时。 始终可以通过默认行为将{@code null}返回到后备状态。
 */
/**
 * A.
 * Any object can implement this interface to provide its actual {@link ResolvableType}.
 *
 * B.
 * <p>Such information is very useful when figuring out if the instance matches a generic
 * signature as Java does not convey the signature at runtime.
 *
 * C.
 * <p>Users of this interface should be careful in complex hierarchy scenarios, especially
 * when the generic type signature of the class changes in sub-classes. It is always
 * possible to return {@code null} to fallback on a default behavior.
 *
 * @author Stephane Nicoll
 * @since 4.2
 */
// 20201207 ResolvableType提供者: 任何对象都可以实现此接口以提供其实际的{@link ResolvableType}
public interface ResolvableTypeProvider {

	/**
	 * Return the {@link ResolvableType} describing this instance
	 * (or {@code null} if some sort of default should be applied instead).
	 */
	// 20201207 返回描述此实例的{@link ResolvableType}（如果应采用某种默认值，则返回{@code null}）。
	@Nullable
	ResolvableType getResolvableType();

}
