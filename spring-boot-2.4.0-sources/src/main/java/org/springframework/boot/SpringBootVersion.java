/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;

/**
 * 20201203
 * A. 类，该类公开springboot版本。通过{@link Package#getImplementationVersion（）}从jar文件获取{@link Name#IMPLEMENTATION_VERSION IMPLEMENTATION VERSION}manifest属性，
 *    返回到包含该类的jar文件并从其清单中读取{@code IMPLEMENTATION VERSION}属性。
 * B. 此类可能无法确定所有环境中的springboot版本。考虑使用基于反射的检查：例如，检查是否存在要调用的特定springboot方法。
 */
/**
 * A.
 * Class that exposes the Spring Boot version. Fetches the
 * {@link Name#IMPLEMENTATION_VERSION Implementation-Version} manifest attribute from the
 * jar file via {@link Package#getImplementationVersion()}, falling back to locating the
 * jar file that contains this class and reading the {@code Implementation-Version}
 * attribute from its manifest.
 *
 * B.
 * <p>
 * This class might not be able to determine the Spring Boot version in all environments.
 * Consider using a reflection-based check instead: For example, checking for the presence
 * of a specific Spring Boot method that you intend to call.
 *
 * @author Drummond Dawson
 * @author Hendrig Sellik
 * @author Andy Wilkinson
 * @since 1.3.0
 */
// 20201203 该类公开springboot版本 => 从MANIFEST.MF读取Implementation-Version属性, 可能会为空
public final class SpringBootVersion {

	private SpringBootVersion() {
	}

	/**
	 * Return the full version string of the present Spring Boot codebase, or {@code null}
	 * if it cannot be determined.
	 * @return the version of Spring Boot or {@code null}
	 * @see Package#getImplementationVersion()
	 */
	// 20201203 返回当前springboot代码库的完整版本字符串，如果无法确定，则返回{@code null}。
	public static String getVersion() {
		return determineSpringBootVersion();
	}

	// 20201203 确定Springboot版本号
	private static String determineSpringBootVersion() {
		// 20201204 获取Springboot的实现版本
		String implementationVersion = SpringBootVersion.class.getPackage().getImplementationVersion();

		// 20201204  如果不为空则直接返回
		if (implementationVersion != null) {
			return implementationVersion;
		}

		// 20201204 获取类的安全组
		CodeSource codeSource = SpringBootVersion.class.getProtectionDomain().getCodeSource();

		// 20201204 如果安全组为空则返回null, 代表类无效
		if (codeSource == null) {
			return null;
		}

		// 20201204 否则返回代码的位置
		URL codeSourceLocation = codeSource.getLocation();
		try {
			// 20201204 根据位置打开URL连接
			URLConnection connection = codeSourceLocation.openConnection();

			// 20201204 如果属于Jar包连接
			if (connection instanceof JarURLConnection) {
				// 20201204 则获取Jar包的实现版本
				return getImplementationVersion(((JarURLConnection) connection).getJarFile());
			}

			// 20201204 否则根据URL内容封装成Jar包然后获取其实现版本
			try (JarFile jarFile = new JarFile(new File(codeSourceLocation.toURI()))) {
				return getImplementationVersion(jarFile);
			}
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static String getImplementationVersion(JarFile jarFile) throws IOException {
		return jarFile.getManifest().getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION);
	}

}
