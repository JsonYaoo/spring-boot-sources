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

package org.springframework.core.env;

/**
 * 20201202
 * A. 解析命令行参数的{@code String[]}，以便填充{@link CommandLineArgs}对象。
 * B. 使用选项参数:
 * 		a. 选项参数必须符合确切的语法：--optName[=optValue]
 * 			a.1. 也就是说，选项必须以“{@code--}”为前缀，并且可以指定值，也可以不指定值。如果指定了一个值，则名称和值必须用等号（“=”）分隔而不带空格。值可以是空字符串（可选）。
 * 		b. 选项参数的有效示例:
 * 			b.1. --foo
 * 			b.2. --foo=
 * 			b.3. --foo=""
 * 			b.4. --foo=bar
 * 			b.5. --foo="bar then baz"
 * 			b.6. --foo=bar,baz,biz
 * 		c. 选项参数的无效示例:
 * 			c.1. -foo
 * 			c.2. --foo bar
 * 			c.3. --foo = bar
 * 			c.4. --foo=bar --foo=baz --foo=biz
 * C. 使用非选项参数:
 * 		a. 在命令行中指定的没有{@code--}选项前缀的任何和所有参数都将被视为“非选项参数”，并通过{@link CommandLineArgs#getNonOptionArgs（）}方法提供。
 */
/**
 * A.
 * Parses a {@code String[]} of command line arguments in order to populate a
 * {@link CommandLineArgs} object.
 *
 * B.
 * <h3>Working with option arguments</h3>
 * <p>Option arguments must adhere to the exact syntax:
 *
 * <pre class="code">--optName[=optValue]</pre>
 *
 * <p>That is, options must be prefixed with "{@code --}" and may or may not
 * specify a value. If a value is specified, the name and value must be separated
 * <em>without spaces</em> by an equals sign ("="). The value may optionally be
 * an empty string.
 *
 * C.
 * <h4>Valid examples of option arguments</h4>
 * <pre class="code">
 * --foo
 * --foo=
 * --foo=""
 * --foo=bar
 * --foo="bar then baz"
 * --foo=bar,baz,biz</pre>
 *
 * D.
 * <h4>Invalid examples of option arguments</h4>
 * <pre class="code">
 * -foo
 * --foo bar
 * --foo = bar
 * --foo=bar --foo=baz --foo=biz</pre>
 *
 * E.
 * <h3>Working with non-option arguments</h3>
 * <p>Any and all arguments specified at the command line without the "{@code --}"
 * option prefix will be considered as "non-option arguments" and made available
 * through the {@link CommandLineArgs#getNonOptionArgs()} method.
 *
 * @author Chris Beams
 * @author Sam Brannen
 * @since 3.1
 */
// 20201202 命令行参数解析器 -> 解析命令行参数的{@code String[]}，以便填充{@link CommandLineArgs}对象。
class SimpleCommandLineArgsParser {

	/**
	 * Parse the given {@code String} array based on the rules described {@linkplain
	 * SimpleCommandLineArgsParser above}, returning a fully-populated
	 * {@link CommandLineArgs} object.
	 * @param args command line arguments, typically from a {@code main()} method
	 */
	// 20201202 根据上面描述的规则{@linkplain simplecommandlineargsparser}解析给定的{@code String}数组，返回一个完全填充的{@link CommandLineArgs}对象。
	public CommandLineArgs parse(String... args) {
		CommandLineArgs commandLineArgs = new CommandLineArgs();
		// 20201202 遍历args数组
		for (String arg : args) {
			// 20201202 如果args是选项参数
			if (arg.startsWith("--")) {
				// 20201202 则获取参数内容
				String optionText = arg.substring(2);
				String optionName;
				String optionValue = null;

				// 20201202 获取参数内容中的键 -> 参数名称和值 -> 参数值
				int indexOfEqualsSign = optionText.indexOf('=');
				if (indexOfEqualsSign > -1) {
					optionName = optionText.substring(0, indexOfEqualsSign);
					optionValue = optionText.substring(indexOfEqualsSign + 1);
				}

				// 20201202 如果没指定键值对, 则参数名称就为该内容
				else {
					optionName = optionText;
				}

				// 20201202 如果参数名称为空, 则抛出参数非法异常
				if (optionName.isEmpty()) {
					throw new IllegalArgumentException("Invalid argument syntax: " + arg);
				}

				// 20201202 命令行参数对象添加选项参数
				commandLineArgs.addOptionArg(optionName, optionValue);
			}
			else {
				// 20201202 命令行参数对象添加非选项参数
				commandLineArgs.addNonOptionArg(arg);
			}
		}
		return commandLineArgs;
	}

}
