/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.factory.Aware;
import org.springframework.util.StringValueResolver;

/**
 * 20201202
 * A. 接口，该接口由任何希望收到{@code StringValueResolver}通知的对象实现，用于解析嵌入的定义值。
 * B. 这是通过{@code ApplicationContextAware}/{@code BeanFactoryAware}接口实现的完全可配置BeanFactory依赖项的替代方案。
 */
/**
 * A.
 * Interface to be implemented by any object that wishes to be notified of a
 * {@code StringValueResolver} for the resolution of embedded definition values.
 *
 * B.
 * <p>This is an alternative to a full ConfigurableBeanFactory dependency via the
 * {@code ApplicationContextAware}/{@code BeanFactoryAware} interfaces.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0.3
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#resolveEmbeddedValue(String)
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getBeanExpressionResolver()
 * @see org.springframework.beans.factory.config.EmbeddedValueResolver
 */
// 20201202 该接口由任何希望收到{@code StringValueResolver}通知的对象实现
public interface EmbeddedValueResolverAware extends Aware {

	/**
	 * Set the StringValueResolver to use for resolving embedded definition values.
	 */
	void setEmbeddedValueResolver(StringValueResolver resolver);

}
