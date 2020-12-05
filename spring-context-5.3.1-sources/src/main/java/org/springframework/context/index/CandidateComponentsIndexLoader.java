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

package org.springframework.context.index;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.SpringProperties;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Candidate components index loading mechanism for internal use within the framework.
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
// 20201205 候选组件索引加载机制供框架内部使用。
public final class CandidateComponentsIndexLoader {

	/**
	 * The location to look for components.
	 * <p>Can be present in multiple JAR files.
	 */
	// 20201205 查找组件的位置, 可以存在于多个JAR文件中。
	public static final String COMPONENTS_RESOURCE_LOCATION = "META-INF/spring.components";

	/**
	 * System property that instructs Spring to ignore the index, i.e.
	 * to always return {@code null} from {@link #loadIndex(ClassLoader)}.
	 * <p>The default is "false", allowing for regular use of the index. Switching this
	 * flag to {@code true} fulfills a corner case scenario when an index is partially
	 * available for some libraries (or use cases) but couldn't be built for the whole
	 * application. In this case, the application context fallbacks to a regular
	 * classpath arrangement (i.e. as no index was present at all).
	 */
	public static final String IGNORE_INDEX = "spring.index.ignore";

	// 20201205 从本地Spring属性的静态持有者获取"spring.index.ignore"对应的属性值, 如果属性设置为“ true”，则为true，否则为{@code} false
	private static final boolean shouldIgnoreIndex = SpringProperties.getFlag(IGNORE_INDEX);

	private static final Log logger = LogFactory.getLog(CandidateComponentsIndexLoader.class);

	// 20201205 候选者组件索引实例缓存: 类加载器-候选者组件索引实例
	private static final ConcurrentMap<ClassLoader, CandidateComponentsIndex> cache = new ConcurrentReferenceHashMap<>();

	private CandidateComponentsIndexLoader() {
	}

	/**
	 * // 20201205 使用给定的类加载器，从{@value #COMPONENTS_RESOURCE_LOCATION}加载并实例化{@link CandidateComponentsIndex}。 如果没有可用的索引，则返回{@code null}。
	 * Load and instantiate the {@link CandidateComponentsIndex} from
	 * {@value #COMPONENTS_RESOURCE_LOCATION}, using the given class loader. If no
	 * index is available, return {@code null}.
	 *
	 * // 20201205 用于加载的ClassLoader（可以为{@code null}以使用默认值）
	 * @param classLoader the ClassLoader to use for loading (can be {@code null} to use the default)
	 *
	 * // 20201205 要使用的索引；如果找不到索引，则为{@code null}
	 * @return the index to use or {@code null} if no index was found
	 *
	 * @throws IllegalArgumentException if any module index cannot
	 * be loaded or if an error occurs while creating {@link CandidateComponentsIndex}
	 */
	// 20201205 使用给定的类加载器，从"META-INF/spring.components"加载并实例化候选者组件索引实例
	@Nullable
	public static CandidateComponentsIndex loadIndex(@Nullable ClassLoader classLoader) {
		ClassLoader classLoaderToUse = classLoader;

		// 20201205 如果指定的类加载器为空
		if (classLoaderToUse == null) {
			// 20201205 则获取本类的类加载器
			classLoaderToUse = CandidateComponentsIndexLoader.class.getClassLoader();
		}

		// 20201205 根据类加载器从候选者组件索引实例缓存获取候选者组件索引实例, 如果获取不到则重新构建候选者组件索引实例, 建立属性键值对Map索引
		return cache.computeIfAbsent(classLoaderToUse, CandidateComponentsIndexLoader::doLoadIndex);
	}

	// 20201205 构建候选者组件索引实例, 建立属性键值对Map索引
	@Nullable
	private static CandidateComponentsIndex doLoadIndex(ClassLoader classLoader) {
		// 20201205 如果从本地Spring属性的静态持有者获取"spring.index.ignore"标志, 如果标志为true, 则直接返回空
		if (shouldIgnoreIndex) {
			return null;
		}

		// 20201205 否则代表标志为false
		try {
			// 20201205 获取"META-INF/spring.components"资源
			Enumeration<URL> urls = classLoader.getResources(COMPONENTS_RESOURCE_LOCATION);

			// 20201205 如果此枚举不包含元素 -> 包含至少一个以上要提供的元素才为true, 否则为false
			if (!urls.hasMoreElements()) {
				// 20201205 则返回空
				return null;
			}

			// 20201205 否则构建Properties结果列表
			List<Properties> result = new ArrayList<>();

			// 20201205 遍历此枚举的每个元素
			while (urls.hasMoreElements()) {
				// 20201205 获取此枚举的下一个元素。
				URL url = urls.nextElement();

				// 20201205 从给定资源加载属性（采用ISO-8859-1编码）。
				Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));

				// 20201205 添加属性集到Properties结果列表中
				result.add(properties);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + result.size() + "] index(es)");
			}

			// 20201205 统计属性键值对的个数
			int totalCount = result.stream().mapToInt(Properties::size).sum();

			// 20201205 如果键值对个数大于0, 则构建候选者组件索引实例, 建立属性键值对Map索引
			return (totalCount > 0 ? new CandidateComponentsIndex(result) : null);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to load indexes from location [" +
					COMPONENTS_RESOURCE_LOCATION + "]", ex);
		}
	}

}
