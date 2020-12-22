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

package org.springframework.core;

/**
 * 20201222
 * A. {@link ParameterNameDiscoverer}策略接口的默认实现，使用Java 8标准反射机制（如果可用），并回退到基于ASM的{@link LocalVariableTableParameterNameNameDiscoverer}，
 *    以检查类文件中的调试信息。
 * B. 如果存在Kotlin反射实现，则将{@link KotlinReflectionParameterNameDiscoverer}首先添加到列表中，并将其用于Kotlin类和接口。 当编译或作为GraalVM本机映像运行时，不使用
 *    {@code KotlinReflectionParameterNameNameDiscoverer}。
 * C. 可以通过{@link #addDiscoverer（ParameterNameDiscoverer）}添加更多发现者。
 */
/**
 * A.
 * Default implementation of the {@link ParameterNameDiscoverer} strategy interface,
 * using the Java 8 standard reflection mechanism (if available), and falling back
 * to the ASM-based {@link LocalVariableTableParameterNameDiscoverer} for checking
 * debug information in the class file.
 *
 * B.
 * <p>If a Kotlin reflection implementation is present,
 * {@link KotlinReflectionParameterNameDiscoverer} is added first in the list and
 * used for Kotlin classes and interfaces. When compiling or running as a GraalVM
 * native image, the {@code KotlinReflectionParameterNameDiscoverer} is not used.
 *
 * C.
 * <p>Further discoverers may be added through {@link #addDiscoverer(ParameterNameDiscoverer)}.
 *
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @since 4.0
 * @see StandardReflectionParameterNameDiscoverer
 * @see LocalVariableTableParameterNameDiscoverer
 * @see KotlinReflectionParameterNameDiscoverer
 */
// 20201222 {@link ParameterNameDiscoverer}策略接口的默认实现
public class DefaultParameterNameDiscoverer extends PrioritizedParameterNameDiscoverer {

	/**
	 * Whether this environment lives within a native image.
	 * Exposed as a private static field rather than in a {@code NativeImageDetector.inNativeImage()} static method due to https://github.com/oracle/graal/issues/2594.
	 * @see <a href="https://github.com/oracle/graal/blob/master/sdk/src/org.graalvm.nativeimage/src/org/graalvm/nativeimage/ImageInfo.java">ImageInfo.java</a>
	 */
	private static final boolean IN_NATIVE_IMAGE = (System.getProperty("org.graalvm.nativeimage.imagecode") != null);

	public DefaultParameterNameDiscoverer() {
		if (KotlinDetector.isKotlinReflectPresent() && !IN_NATIVE_IMAGE) {
			addDiscoverer(new KotlinReflectionParameterNameDiscoverer());
		}
		addDiscoverer(new StandardReflectionParameterNameDiscoverer());
		addDiscoverer(new LocalVariableTableParameterNameDiscoverer());
	}

}
