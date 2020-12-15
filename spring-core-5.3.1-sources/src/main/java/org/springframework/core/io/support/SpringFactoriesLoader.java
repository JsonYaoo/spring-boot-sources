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

package org.springframework.core.io.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 20201130
 * A. 通用工厂加载机制，内部使用框架。
 * B. {@code SpringFactoriesLoader}{@linkplain loadFactories loadFactories}并从类路径中的多个JAR文件中实例化给定类型的工厂。
 *    {@Code spring.factories}文件必须是{@link Properties}格式，其中键是接口或抽象类的完全限定名，值是以逗号分隔的实现类名列表。例如：
 *    		example.MyService=example.MyServiceImpl1,example.MyServiceImpl2
 *    其中{@code example.MyService}是接口的名称，{@code MyServiceImpl1}和{@code MyServiceImpl2}是两个实现。
 */
/**
 * A.
 * General purpose factory loading mechanism for internal use within the framework.
 *
 * B.
 * <p>{@code SpringFactoriesLoader} {@linkplain #loadFactories loads} and instantiates
 * factories of a given type from {@value #FACTORIES_RESOURCE_LOCATION} files which
 * may be present in multiple JAR files in the classpath. The {@code spring.factories}
 * file must be in {@link Properties} format, where the key is the fully qualified
 * name of the interface or abstract class, and the value is a comma-separated list of
 * implementation class names. For example:
 *
 * <pre class="code">example.MyService=example.MyServiceImpl1,example.MyServiceImpl2</pre>
 *
 * where {@code example.MyService} is the name of the interface, and {@code MyServiceImpl1}
 * and {@code MyServiceImpl2} are two implementations.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.2
 */
// 20201130 Spring工厂加载器
public final class SpringFactoriesLoader {

	/**
	 * The location to look for factories.
	 * <p>Can be present in multiple JAR files.
	 */
	public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";


	private static final Log logger = LogFactory.getLog(SpringFactoriesLoader.class);

	// 20201130 Spring工厂缓存 -> 类加载器-
	static final Map<ClassLoader, Map<String, List<String>>> cache = new ConcurrentReferenceHashMap<>();

	private SpringFactoriesLoader() {

	}

    /**
     * 20201215
     * A. 使用给定的类加载器，从"META-INF/spring.factories"加载并实例化给定类型的工厂实现
     * B. 返回的工厂通过{@link AnnotationAwareOrderComparator}进行排序。
     * C. 如果需要自定义实例化策略，请使用{@link #loadFactoryNames}获取所有注册的工厂名称。
     * D. 从Spring Framework 5.3开始，如果为给定的工厂类型发现了重复的实现类名称，则仅实例化重复的实现类型的一个实例。
     */
	/**
     * A.
	 * Load and instantiate the factory implementations of the given type from
	 * {@value #FACTORIES_RESOURCE_LOCATION}, using the given class loader.
     *
     * B.
	 * <p>The returned factories are sorted through {@link AnnotationAwareOrderComparator}.
     *
     * C.
	 * <p>If a custom instantiation strategy is required, use {@link #loadFactoryNames}
	 * to obtain all registered factory names.
     *
     * D.
	 * <p>As of Spring Framework 5.3, if duplicate implementation class names are
	 * discovered for a given factory type, only one instance of the duplicated
	 * implementation type will be instantiated.
     *
	 * @param factoryType the interface or abstract class representing the factory
	 * @param classLoader the ClassLoader to use for loading (can be {@code null} to use the default)
	 * @throws IllegalArgumentException if any factory implementation class cannot
	 * be loaded or if an error occurs while instantiating any factory
	 * @see #loadFactoryNames
	 */
	// 20201215 使用给定的类加载器，从"META-INF/spring.factories"加载并实例化给定类型的工厂实现, 并通过AnnotationAwareOrderComparator进行排序
	public static <T> List<T> loadFactories(Class<T> factoryType, @Nullable ClassLoader classLoader) {
		Assert.notNull(factoryType, "'factoryType' must not be null");
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
		}
		List<String> factoryImplementationNames = loadFactoryNames(factoryType, classLoaderToUse);
		if (logger.isTraceEnabled()) {
			logger.trace("Loaded [" + factoryType.getName() + "] names: " + factoryImplementationNames);
		}
		List<T> result = new ArrayList<>(factoryImplementationNames.size());
		for (String factoryImplementationName : factoryImplementationNames) {
			result.add(instantiateFactory(factoryImplementationName, factoryType, classLoaderToUse));
		}
		AnnotationAwareOrderComparator.sort(result);
		return result;
	}

	/**
	 * Load the fully qualified class names of factory implementations of the
	 * given type from {@value #FACTORIES_RESOURCE_LOCATION}, using the given
	 * class loader.
	 * <p>As of Spring Framework 5.3, if a particular implementation class name
	 * is discovered more than once for the given factory type, duplicates will
	 * be ignored.
	 * @param factoryType the interface or abstract class representing the factory	// 20201215 代表工厂的接口或抽象类
	 * @param classLoader the ClassLoader to use for loading resources; can be
	 * {@code null} to use the default
	 * @throws IllegalArgumentException if an error occurs while loading factory names
	 * @see #loadFactories
	 */
	// 20201130 使用给定的类加载器从"META-INF/spring.factories"加载给定类型的工厂实现的完全限定类名。
	// 20201130 从springframework5.3开始，如果一个特定的实现类名对于给定的工厂类型被发现不止一次，那么重复的类名将被忽略。
	public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			// 20201130 如果没有指定类加载器, 则使用Spring工厂加载器
			classLoaderToUse = SpringFactoriesLoader.class.getClassLoader();
		}

		// 20201130 获取工厂类型名称
		String factoryTypeName = factoryType.getName();

		// 20201130 根据类加载器(启动时当前线程没有加载器, 即默认使用Spring工厂加载器)加载指定类型的工厂实例名称 -> 只是全类名, 还没实例化
		// 20201130 构造SpringApplication的factoryTypeName类型: Bootstrapper、ApplicationContextInitializer、ApplicationListener
		return loadSpringFactories(classLoaderToUse).getOrDefault(factoryTypeName, Collections.emptyList());
	}

	// 20201130 根据构造器加载工厂实例名称 -> 只是全类名, 还没实例化
	private static Map<String, List<String>> loadSpringFactories(ClassLoader classLoader) {
		// 20201130 从Spring工厂缓存中获取
		Map<String, List<String>> result = cache.get(classLoader);
		if (result != null) {
			return result;
		}

		// 20201130 获取不到则需要进行工厂实例化
		result = new HashMap<>();
		try {
			// 20201130 获取"META-INF/spring.factories"所有资源 -> 里面存放着各种工厂的全类名
			Enumeration<URL> urls = classLoader.getResources(FACTORIES_RESOURCE_LOCATION);

			// 20201130 遍历所有资源
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				UrlResource resource = new UrlResource(url);
				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				for (Map.Entry<?, ?> entry : properties.entrySet()) {
					// 20201130 获取工厂名称
					String factoryTypeName = ((String) entry.getKey()).trim();

					// 20201130 转换为工厂类名称列表
					String[] factoryImplementationNames =
							StringUtils.commaDelimitedListToStringArray((String) entry.getValue());

					// 20201130 遍历所有工厂类名称
					for (String factoryImplementationName : factoryImplementationNames) {
						// 20201130 将每个工厂类名称注册到result结果中
						result.computeIfAbsent(factoryTypeName, key -> new ArrayList<>())
								.add(factoryImplementationName.trim());
					}
				}
			}

			// Replace all lists with unmodifiable lists containing unique elements // 20201130 将结果列表替换为包含唯一元素的不可修改列表
			result.replaceAll((factoryType, implementations) -> implementations.stream().distinct()
					.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)));

			// 20201130 把结果添加到缓存中
			cache.put(classLoader, result);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T instantiateFactory(String factoryImplementationName, Class<T> factoryType, ClassLoader classLoader) {
		try {
			Class<?> factoryImplementationClass = ClassUtils.forName(factoryImplementationName, classLoader);
			if (!factoryType.isAssignableFrom(factoryImplementationClass)) {
				throw new IllegalArgumentException(
						"Class [" + factoryImplementationName + "] is not assignable to factory type [" + factoryType.getName() + "]");
			}
			return (T) ReflectionUtils.accessibleConstructor(factoryImplementationClass).newInstance();
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
				"Unable to instantiate factory class [" + factoryImplementationName + "] for factory type [" + factoryType.getName() + "]",
				ex);
		}
	}

}
