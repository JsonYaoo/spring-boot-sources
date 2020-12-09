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

package org.springframework.context.annotation;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Delegate factory class used to just introduce an AOP framework dependency
 * when actually creating a scoped proxy.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.aop.scope.ScopedProxyUtils#createScopedProxy
 */
// 20201209 在实际创建作用域代理时，用于仅引入AOP框架依赖关系的委托工厂类。
final class ScopedProxyCreator {

	private ScopedProxyCreator() {
	}

	// 20201209 为提供的目标bean生成作用域代理，用内部名称注册目标bean并在作用域代理上设置“ targetBeanName”。
	public static BeanDefinitionHolder createScopedProxy(
			BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry, boolean proxyTargetClass) {
		// 20201209 为提供的目标bean生成作用域代理，用内部名称注册目标bean并在作用域代理上设置“ targetBeanName”。
		return ScopedProxyUtils.createScopedProxy(definitionHolder, registry, proxyTargetClass);
	}

	public static String getTargetBeanName(String originalBeanName) {
		return ScopedProxyUtils.getTargetBeanName(originalBeanName);
	}

}
