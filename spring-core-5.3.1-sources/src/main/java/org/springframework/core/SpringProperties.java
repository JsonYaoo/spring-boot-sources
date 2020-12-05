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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;

/**
 * 20201205
 * A. 本地Spring属性的静态持有者，即在Spring库级别定义的。
 * B. 从Spring库类路径的根目录读取{@code spring.properties}文件，并允许通过{@link #setProperty}以编程方式设置属性。 检查属性时，首先要检查本地条目，然后通过
 *    {@link System＃getProperty}检查回退到JVM级别的系统属性。
 * C. 这是设置与Spring相关的系统属性（例如“ spring.getenv.ignore”和“ spring.beaninfo.ignore”）的另一种方法，特别是对于将JVM系统属性锁定在目标平台（例如WebSphere）上
 *    的场景而言。 有关在本地将此类标志设置为“ true”的便捷方法，请参见{@link #setFlag}。
 */
/**
 * A.
 * Static holder for local Spring properties, i.e. defined at the Spring library level.
 *
 * B.
 * <p>Reads a {@code spring.properties} file from the root of the Spring library classpath,
 * and also allows for programmatically setting properties through {@link #setProperty}.
 * When checking a property, local entries are being checked first, then falling back
 * to JVM-level system properties through a {@link System#getProperty} check.
 *
 * C.
 * <p>This is an alternative way to set Spring-related system properties such as
 * "spring.getenv.ignore" and "spring.beaninfo.ignore", in particular for scenarios
 * where JVM system properties are locked on the target platform (e.g. WebSphere).
 * See {@link #setFlag} for a convenient way to locally set such flags to "true".
 *
 * @author Juergen Hoeller
 * @since 3.2.7
 * @see org.springframework.core.env.AbstractEnvironment#IGNORE_GETENV_PROPERTY_NAME
 * @see org.springframework.beans.CachedIntrospectionResults#IGNORE_BEANINFO_PROPERTY_NAME
 * @see org.springframework.jdbc.core.StatementCreatorUtils#IGNORE_GETPARAMETERTYPE_PROPERTY_NAME
 * @see org.springframework.test.context.cache.ContextCache#MAX_CONTEXT_CACHE_SIZE_PROPERTY_NAME
 */
// 20201205 本地Spring属性的静态持有者 -> 从Spring库类路径的根目录读取{@code spring.properties}文件，并允许通过{@link #setProperty}以编程方式设置属性
public final class SpringProperties {

	private static final String PROPERTIES_RESOURCE_LOCATION = "spring.properties";

	private static final Log logger = LogFactory.getLog(SpringProperties.class);

	// 20201205 属性列表 -> 创建一个没有默认值的空属性列表。
	private static final Properties localProperties = new Properties();


	static {
		try {
			ClassLoader cl = SpringProperties.class.getClassLoader();
			URL url = (cl != null ? cl.getResource(PROPERTIES_RESOURCE_LOCATION) :
					ClassLoader.getSystemResource(PROPERTIES_RESOURCE_LOCATION));
			if (url != null) {
				logger.debug("Found 'spring.properties' file in local classpath");
				try (InputStream is = url.openStream()) {
					localProperties.load(is);
				}
			}
		}
		catch (IOException ex) {
			if (logger.isInfoEnabled()) {
				logger.info("Could not load 'spring.properties' file from local classpath: " + ex);
			}
		}
	}


	private SpringProperties() {
	}


	/**
	 * Programmatically set a local property, overriding an entry in the
	 * {@code spring.properties} file (if any).
	 * @param key the property key
	 * @param value the associated property value, or {@code null} to reset it
	 */
	public static void setProperty(String key, @Nullable String value) {
		if (value != null) {
			localProperties.setProperty(key, value);
		}
		else {
			localProperties.remove(key);
		}
	}

	/**
	 * Retrieve the property value for the given key, checking local Spring
	 * properties first and falling back to JVM-level system properties.
	 *
	 * @param key the property key
	 * @return the associated property value, or {@code null} if none found	// 20201205 关联的属性值；如果找不到，则为{@code null}
	 */
	// 20201205 检索给定键的属性值，首先检查本地Spring属性，然后回退到JVM级别的系统属性。
	@Nullable
	public static String getProperty(String key) {
		// 20201205 根据属性键从属性列表获取属性值
		String value = localProperties.getProperty(key);

		// 20201205 如果属性值为null
		if (value == null) {
			try {
				// 20201205 根据属性键则获取从系统属性列表获取属性值
				value = System.getProperty(key);
			}
			catch (Throwable ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not retrieve system property '" + key + "': " + ex);
				}
			}
		}

		// 20201205 返回找到的属性值
		return value;
	}

	/**
	 * Programmatically set a local flag to "true", overriding an
	 * entry in the {@code spring.properties} file (if any).
	 * @param key the property key
	 */
	public static void setFlag(String key) {
		localProperties.put(key, Boolean.TRUE.toString());
	}

	/**
	 * Retrieve the flag for the given property key.
	 *
	 * @param key the property key
	 *
	 * @return {@code true} if the property is set to "true",
	 * {@code} false otherwise // 20201205如果属性设置为“ true”，则为{@code true}，否则为{@code} false
	 */
	// 20201205 检索给定属性键的标志。
	public static boolean getFlag(String key) {
		return Boolean.parseBoolean(getProperty(key));
	}

}
