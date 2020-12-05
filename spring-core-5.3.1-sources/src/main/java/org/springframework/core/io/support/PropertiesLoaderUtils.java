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
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.core.SpringProperties;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.ResourceUtils;

/**
 * 20201205
 * A. 方便的实用程序方法，用于加载{@code java.util.Properties}，执行输入流的标准处理。
 * B. 对于更多可配置的属性加载，包括自定义编码的选项，请考虑使用PropertiesLoaderSupport类。
 */
/**
 * A.
 * Convenient utility methods for loading of {@code java.util.Properties},
 * performing standard handling of input streams.
 *
 * B.
 * <p>For more configurable properties loading, including the option of a
 * customized encoding, consider using the PropertiesLoaderSupport class.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sebastien Deleuze
 * @since 2.0
 * @see PropertiesLoaderSupport
 */
// 20201205 方便的实用程序方法，用于加载{@code java.util.Properties}，执行输入流的标准处理。
public abstract class PropertiesLoaderUtils {

	private static final String XML_FILE_EXTENSION = ".xml";

	/**
	 * Boolean flag controlled by a {@code spring.xml.ignore} system property that instructs Spring to
	 * ignore XML, i.e. to not initialize the XML-related infrastructure.
	 * <p>The default is "false".
	 */
	// 20201205 由{@code spring.xml.ignore}系统属性控制的布尔标志，该属性指示Spring忽略XML，即不初始化与XML相关的基础架构 -> 默认为false, 不忽略
	private static final boolean shouldIgnoreXml = SpringProperties.getFlag("spring.xml.ignore");


	/**
	 * Load properties from the given EncodedResource,
	 * potentially defining a specific encoding for the properties file.
	 * @see #fillProperties(Properties, EncodedResource)
	 */
	public static Properties loadProperties(EncodedResource resource) throws IOException {
		Properties props = new Properties();
		fillProperties(props, resource);
		return props;
	}

	/**
	 * Fill the given properties from the given EncodedResource,
	 * potentially defining a specific encoding for the properties file.
	 * @param props the Properties instance to load into
	 * @param resource the resource to load from
	 * @throws IOException in case of I/O errors
	 */
	public static void fillProperties(Properties props, EncodedResource resource)
			throws IOException {

		fillProperties(props, resource, ResourcePropertiesPersister.INSTANCE);
	}

	/**
	 * Actually load properties from the given EncodedResource into the given Properties instance.
	 * @param props the Properties instance to load into
	 * @param resource the resource to load from
	 * @param persister the PropertiesPersister to use
	 * @throws IOException in case of I/O errors
	 */
	static void fillProperties(Properties props, EncodedResource resource, PropertiesPersister persister)
			throws IOException {

		InputStream stream = null;
		Reader reader = null;
		try {
			String filename = resource.getResource().getFilename();
			if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
				if (shouldIgnoreXml) {
					throw new UnsupportedOperationException("XML support disabled");
				}
				stream = resource.getInputStream();
				persister.loadFromXml(props, stream);
			}
			else if (resource.requiresReader()) {
				reader = resource.getReader();
				persister.load(props, reader);
			}
			else {
				stream = resource.getInputStream();
				persister.load(props, stream);
			}
		}
		finally {
			if (stream != null) {
				stream.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Load properties from the given resource (in ISO-8859-1 encoding).
	 * @param resource the resource to load from
	 * @return the populated Properties instance
	 * @throws IOException if loading failed
	 * @see #fillProperties(Properties, Resource)
	 */
	// 20201205 从给定资源加载属性（采用ISO-8859-1编码）。
	public static Properties loadProperties(Resource resource) throws IOException {
		Properties props = new Properties();

		// 20201205 填充给定资源中的给定属性（采用ISO-8859-1编码）。
		fillProperties(props, resource);

		// 20201205 返回资源属性集 -> {@code Properties}类表示一组持久属性。{@code Properties}可以保存到流或从流加载。属性列表中的每个键及其对应值都是一个字符串。
		return props;
	}

	/**
	 * Fill the given properties from the given resource (in ISO-8859-1 encoding).
	 * @param props the Properties instance to fill
	 * @param resource the resource to load from
	 * @throws IOException if loading failed
	 */
	// 20201205 填充给定资源中的给定属性（采用ISO-8859-1编码）。
	public static void fillProperties(Properties props, Resource resource) throws IOException {
		// 20201205 获取资源输入流
		try (InputStream is = resource.getInputStream()) {
			// 20201205 确定此资源的文件名，即通常路径的最后一部分：例如“ myfile.txt”。
			String filename = resource.getFilename();

			// 20201205 如果资源的文件名为“.xml”结尾
			if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
				// 20201205 是否忽略xml配置, 默认false, 不忽略
				if (shouldIgnoreXml) {
					// 20201205 如果忽略则抛出异常, 因为忽略了还获取到了xml资源
					throw new UnsupportedOperationException("XML support disabled");
				}

				// 20201205 将指定输入流上XML文档表示的所有属性加载到此属性表中。
				props.loadFromXML(is);
			}
			else {
				// 20201205 从InputStream / Reader中读取“逻辑行”，跳过所有注释和空白行，并从“自然行”的开头过滤掉那些前导空格字符
				props.load(is);
			}
		}
	}

	/**
	 * Load all properties from the specified class path resource
	 * (in ISO-8859-1 encoding), using the default class loader.
	 * <p>Merges properties if more than one resource of the same name
	 * found in the class path.
	 * @param resourceName the name of the class path resource
	 * @return the populated Properties instance
	 * @throws IOException if loading failed
	 */
	public static Properties loadAllProperties(String resourceName) throws IOException {
		return loadAllProperties(resourceName, null);
	}

	/**
	 * Load all properties from the specified class path resource
	 * (in ISO-8859-1 encoding), using the given class loader.
	 * <p>Merges properties if more than one resource of the same name
	 * found in the class path.
	 * @param resourceName the name of the class path resource
	 * @param classLoader the ClassLoader to use for loading
	 * (or {@code null} to use the default class loader)
	 * @return the populated Properties instance
	 * @throws IOException if loading failed
	 */
	public static Properties loadAllProperties(String resourceName, @Nullable ClassLoader classLoader) throws IOException {
		Assert.notNull(resourceName, "Resource name must not be null");
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = ClassUtils.getDefaultClassLoader();
		}
		Enumeration<URL> urls = (classLoaderToUse != null ? classLoaderToUse.getResources(resourceName) :
				ClassLoader.getSystemResources(resourceName));
		Properties props = new Properties();
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			URLConnection con = url.openConnection();
			ResourceUtils.useCachesIfNecessary(con);
			try (InputStream is = con.getInputStream()) {
				if (resourceName.endsWith(XML_FILE_EXTENSION)) {
					if (shouldIgnoreXml) {
						throw new UnsupportedOperationException("XML support disabled");
					}
					props.loadFromXML(is);
				}
				else {
					props.load(is);
				}
			}
		}
		return props;
	}

}
