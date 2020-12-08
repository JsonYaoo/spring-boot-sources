/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.aop;

import org.springframework.lang.Nullable;

/**
 * 20201207
 * A. 用于将目标类暴露在代理之后的最小接口。
 * B. 由AOP代理对象和代理工厂（通过{@link org.springframework.aop.framework.Advised}）以及{@link TargetSource TargetSources}实施。
 */
/**
 * A.
 * Minimal interface for exposing the target class behind a proxy.
 *
 * B.
 * <p>Implemented by AOP proxy objects and proxy factories
 * (via {@link org.springframework.aop.framework.Advised})
 * as well as by {@link TargetSource TargetSources}.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)
 */
// 20201207 用于将目标类暴露在代理之后的最小接口
public interface TargetClassAware {

	/**
	 * Return the target class behind the implementing object
	 * (typically a proxy configuration or an actual proxy).
	 *
	 * @return the target Class, or {@code null} if not known	// 20201207 目标类；如果未知，则为{@code null}
	 */
	// 20201207 返回实现对象在代理之后的目标类（通常是代理配置或实际代理）
	@Nullable
	Class<?> getTargetClass();

}
