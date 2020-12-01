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

package org.springframework.core.metrics;

/**
 * Instruments the application startup phase using {@link StartupStep steps}.
 * <p>The core container and its infrastructure components can use the {@code ApplicationStartup}
 * to mark steps during the application startup and collect data about the execution context
 * or their processing time.
 *
 * @author Brian Clozel
 * @since 5.3
 */
// 20201201 使用{@link startupbesteps}指令应用程序启动阶段。核心容器及其基础结构组件可以使用{@code applicationstartup}标记应用程序启动期间的步骤，
// 20201201 并收集有关执行上下文或其处理时间的数据。
public interface ApplicationStartup {

	/**
	 * Default "no op" {@code ApplicationStartup} implementation.
	 * <p>This variant is designed for minimal overhead and does not record data.
	 */
	// 20201201 默认的“no-op”{@code ApplicationStartup}实现。这个变体是为最小的开销而设计的，不记录数据。
	ApplicationStartup DEFAULT = new DefaultApplicationStartup();

	/**
	 * 20201201
	 * A. 创建新步骤并标记其开始。
	 * B. 步骤名称描述当前操作或阶段。此技术名称应使用“.”名称空间，并可在应用程序启动期间重用以描述同一步骤的其他实例。
	 */
	/**
	 * A.
	 * Create a new step and marks its beginning.
	 *
	 * B.
	 * <p>A step name describes the current action or phase. This technical
	 * name should be "." namespaced and can be reused to describe other instances of
	 * the same step during application startup.
	 * @param name the step name
	 */
	StartupStep start(String name);

}
