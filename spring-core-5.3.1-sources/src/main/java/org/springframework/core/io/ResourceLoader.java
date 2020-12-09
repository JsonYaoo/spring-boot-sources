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

package org.springframework.core.io;

import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * 20201130
 * A. 加载资源的策略接口（e.. 类路径或文件系统资源）。一个{@link org.springframework.context.ApplicationContext}需要提供此功能，外加扩展的
 *    {@link org.springframework.core.io.support.ResourcePatternResolver}支持。
 * B. {@link DefaultResourceLoader}是一个独立的实现，可在ApplicationContext外部使用，也可由{@link resourceditor}使用。
 * C. 在ApplicationContext中运行时，可以使用特定上下文的资源加载策略从字符串填充类型Resource和Resource array的Bean属性。
 */
/**
 * A.
 * Strategy interface for loading resources (e.. class path or file system
 * resources). An {@link org.springframework.context.ApplicationContext}
 * is required to provide this functionality, plus extended
 * {@link org.springframework.core.io.support.ResourcePatternResolver} support.
 *
 * B.
 * <p>{@link DefaultResourceLoader} is a standalone implementation that is
 * usable outside an ApplicationContext, also used by {@link ResourceEditor}.
 *
 * C.
 * <p>Bean properties of type Resource and Resource array can be populated
 * from Strings when running in an ApplicationContext, using the particular
 * context's resource loading strategy.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 */
// 20201130 加载资源的策略接口 => 声明加载资源的方法
public interface ResourceLoader {

	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

	/**
	 * 20201209
	 * A. 返回指定资源位置的资源句柄。
	 * B. 该句柄应始终是可重用的资源描述符，并允许多个{@link Resource＃getInputStream（）}调用:
	 * 		a. 必须支持完全限定的网址，例如 “文件：C：/test.dat”。
	 * 		b. 必须支持classpath伪URL，例如 “ classpath：test.dat”。
	 * 		c. 应该支持相对文件路径，例如 “ WEB-INF / test.dat”。 （这将是特定于实现的，通常由ApplicationContext实现提供。）
	 * C. 请注意，资源句柄并不意味着现有资源； 您需要调用{@link Resource＃exists}来检查是否存在。
	 */
	/**
	 * A.
	 * Return a Resource handle for the specified resource location.
	 *
	 * B.
	 * <p>The handle should always be a reusable resource descriptor,
	 * allowing for multiple {@link Resource#getInputStream()} calls.
	 * <p>
	 * <ul>
	 * a.
	 * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
	 *
	 * b.
	 * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
	 *
	 * c.
	 * <li>Should support relative file paths, e.g. "WEB-INF/test.dat".
	 * (This will be implementation-specific, typically provided by an
	 * ApplicationContext implementation.)
	 * </ul>
	 *
	 * C.
	 * <p>Note that a Resource handle does not imply an existing resource;
	 * you need to invoke {@link Resource#exists} to check for existence.
	 *
	 * @param location the resource location
	 * @return a corresponding Resource handle (never {@code null})
	 * @see #CLASSPATH_URL_PREFIX
	 * @see Resource#exists()
	 * @see Resource#getInputStream()
	 */
	// 20201209 返回指定资源位置的资源句柄 -> 资源句柄并不意味着现有资源； 您需要调用{@link Resource＃exists}来检查是否存在
	Resource getResource(String location);

	/**
	 * 20201205
	 * A. 公开此ResourceLoader使用的ClassLoader。
	 * B. 需要直接访问ClassLoader的客户端可以使用ResourceLoader以统一的方式进行操作，而不是依赖于线程上下文ClassLoader。
	 */
	/**
	 * A.
	 * Expose the ClassLoader used by this ResourceLoader.
	 *
	 * B.
	 * <p>Clients which need to access the ClassLoader directly can do so
	 * in a uniform manner with the ResourceLoader, rather than relying
	 * on the thread context ClassLoader.
	 *
	 * @return the ClassLoader
	 * (only {@code null} if even the system ClassLoader isn't accessible) // 20201205 ClassLoader（即使无法访问系统ClassLoader，也只能使用{@code null}）
	 *
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	// 20201205 获取该ResourceLoader使用的ClassLoader
	@Nullable
	ClassLoader getClassLoader();

}
