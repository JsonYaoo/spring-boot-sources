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

import java.io.PrintStream;

import org.springframework.core.env.Environment;

/**
 * Interface class for writing a banner programmatically.
 *
 * @author Phillip Webb
 * @author Michael Stummvoll
 * @author Jeremy Rickard
 * @since 1.2.0
 */
// 20201203 用于以编程方式编写横幅的接口类。
@FunctionalInterface
public interface Banner {

	/**
	 * Print the banner to the specified print stream.
	 * @param environment the spring environment
	 * @param sourceClass the source class for the application
	 * @param out the output print stream
	 */
	// 20201203 将横幅打印到指定的打印流。
	void printBanner(Environment environment, Class<?> sourceClass, PrintStream out);

	/**
	 * An enumeration of possible values for configuring the Banner.
	 */
	// 20201203 用于配置横幅的可能值的枚举。
	enum Mode {

		/**
		 * Disable printing of the banner.
		 */
		// 20201203 禁止打印横幅。
		OFF,

		/**
		 * Print the banner to System.out.
		 */
		// 20201203 将横幅打印到系统输出
		CONSOLE,

		/**
		 * Print the banner to the log file.
		 */
		// 20201203 将横幅打印到日志文件。
		LOG

	}

}
