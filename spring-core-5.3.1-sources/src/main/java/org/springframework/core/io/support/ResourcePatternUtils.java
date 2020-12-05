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

package org.springframework.core.io.support;

import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * 20201205
 * A. 用于确定给定URL是否为可通过{@link ResourcePatternResolver}加载的资源位置的实用工具类。
 * B. 如果{@link #isUrl（String）}方法返回{@code false}，则调用者通常会假设位置是相对路径。
 */
/**
 * A.
 * Utility class for determining whether a given URL is a resource
 * location that can be loaded via a {@link ResourcePatternResolver}.
 *
 * B.
 * <p>Callers will usually assume that a location is a relative path
 * if the {@link #isUrl(String)} method returns {@code false}.
 *
 * @author Juergen Hoeller
 * @since 1.2.3
 */
// 20201205 用于确定给定URL是否为可通过{@link ResourcePatternResolver}加载的资源位置的实用工具类。
public abstract class ResourcePatternUtils {

	/**
	 * Return whether the given resource location is a URL: either a
	 * special "classpath" or "classpath*" pseudo URL or a standard URL.
	 * @param resourceLocation the location String to check
	 * @return whether the location qualifies as a URL
	 * @see ResourcePatternResolver#CLASSPATH_ALL_URL_PREFIX
	 * @see ResourceUtils#CLASSPATH_URL_PREFIX
	 * @see ResourceUtils#isUrl(String)
	 * @see java.net.URL
	 */
	public static boolean isUrl(@Nullable String resourceLocation) {
		return (resourceLocation != null &&
				(resourceLocation.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX) ||
						ResourceUtils.isUrl(resourceLocation)));
	}

	/**
	 * // 20201205 为给定的{@link ResourceLoader}返回默认的{@link ResourcePatternResolver}。
	 * Return a default {@link ResourcePatternResolver} for the given {@link ResourceLoader}.
	 *
	 * // 20201205 如果它实现了{@code ResourcePatternResolver}扩展名，则可能是{@code ResourceLoader}本身，或者是在给定的{@code ResourceLoader}上构建的默认
	 * // 20201205 {@link PathMatchingResourcePatternResolver}。
	 * <p>This might be the {@code ResourceLoader} itself, if it implements the
	 * {@code ResourcePatternResolver} extension, or a default
	 * {@link PathMatchingResourcePatternResolver} built on the given {@code ResourceLoader}.
	 *
	 * // 20201205 ResourceLoader为其构建模式解析器（可以为{@code null}以指示默认的ResourceLoader）
	 * @param resourceLoader the ResourceLoader to build a pattern resolver for
	 * (may be {@code null} to indicate a default ResourceLoader)
	 *
	 * @return the ResourcePatternResolver
	 * @see PathMatchingResourcePatternResolver
	 */
	// 20201205 为给定的{@link ResourceLoader}返回默认的{@link ResourcePatternResolver}。 -> 获取路径模式资源处理器
	public static ResourcePatternResolver getResourcePatternResolver(@Nullable ResourceLoader resourceLoader) {
		// 20201205 如果本身为该类型则直接返回
		if (resourceLoader instanceof ResourcePatternResolver) {
			return (ResourcePatternResolver) resourceLoader;
		}
		// 20201205 否则根据资源加载器构造PathMatchingResourcePatternResolver
		else if (resourceLoader != null) {
			return new PathMatchingResourcePatternResolver(resourceLoader);
		}
		// 20201205 如果资源加载器也为为空, 则使用DefaultResourceLoader创建一个新的PathMatchingResourcePatternResolver
		else {
			return new PathMatchingResourcePatternResolver();
		}
	}

}
