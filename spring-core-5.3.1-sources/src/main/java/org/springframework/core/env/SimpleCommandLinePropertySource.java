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

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 20201202
 * A. {@link CommandLinePropertySource}实现由一个简单的字符串数组支持。
 * B. 目的: 这个{@code CommandLinePropertySource}实现的目的是提供最简单的方法来解析命令行参数。在下面的两个参数中，propertem被复制到linerm的参数中
 * C. 使用选项参数:
 * 		a. 选项参数必须符合确切的语法：--optName[=optValue]
 * 			a.1. 也就是说，选项必须以“{@code--}”为前缀，并且可以指定值，也可以不指定值。如果指定了一个值，则名称和值必须用等号（“=”）分隔而不留空格。值可以是空字符串（可选）。
 * 		b. 选项参数的有效示例:
 * 			b.1. --foo
 * 			b.2. --foo=
 * 			b.3. --foo=""
 * 			b.4. --foo=bar
 * 			b.5. --foo="bar then baz"
 * 			b.6. --foo=bar,baz,biz
 * 		c. 选项参数的示例无效:
 * 			c.1. -foo
 * 			c.2. --foo bar
 * 			c.3. --foo = bar
 * 			c.4. --foo=bar --foo=baz --foo=biz
 * D. 使用非选项参数:
 * 		a. 在命令行中指定的没有{@code--}选项前缀的任何和所有参数都将被视为“非选项参数”，并通过{@link CommandLineArgs#getNonOptionArgs（）}方法提供。
 * E. 典型用法:
 * 		a. public static void main(String[] args) {
 *     			PropertySource<?> ps = new SimpleCommandLinePropertySource(args);
 *     			// ...
 * 		   }
 * 		b. 请参见{@link CommandLinePropertySource}以获取完整的一般用法示例。
 * F. 超出基本要求:
 * 		a. 当需要更全面的命令行解析时，请考虑使用提供的{@link JOptCommandLinePropertySource}，或者根据您选择的命令行解析库实现您自己的{@code CommandLinePropertySource}。
 *
 */
/**
 * A.
 * {@link CommandLinePropertySource} implementation backed by a simple String array.
 *
 * B.
 * <h3>Purpose</h3>
 * <p>This {@code CommandLinePropertySource} implementation aims to provide the simplest
 * possible approach to parsing command line arguments. As with all {@code
 * CommandLinePropertySource} implementations, command line arguments are broken into two
 * distinct groups: <em>option arguments</em> and <em>non-option arguments</em>, as
 * described below <em>(some sections copied from Javadoc for
 * {@link SimpleCommandLineArgsParser})</em>:
 *
 * C.
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
 * <h4>Valid examples of option arguments</h4>
 * <pre class="code">
 * --foo
 * --foo=
 * --foo=""
 * --foo=bar
 * --foo="bar then baz"
 * --foo=bar,baz,biz</pre>
 *
 * <h4>Invalid examples of option arguments</h4>
 * <pre class="code">
 * -foo
 * --foo bar
 * --foo = bar
 * --foo=bar --foo=baz --foo=biz</pre>
 *
 * D.
 * <h3>Working with non-option arguments</h3>
 * <p>Any and all arguments specified at the command line without the "{@code --}"
 * option prefix will be considered as "non-option arguments" and made available
 * through the {@link CommandLineArgs#getNonOptionArgs()} method.
 *
 * E.
 * <h3>Typical usage</h3>
 * <pre class="code">
 * public static void main(String[] args) {
 *     PropertySource<?> ps = new SimpleCommandLinePropertySource(args);
 *     // ...
 * }</pre>
 *
 * See {@link CommandLinePropertySource} for complete general usage examples.
 *
 * F.
 * <h3>Beyond the basics</h3>
 *
 * <p>When more fully-featured command line parsing is necessary, consider using
 * the provided {@link JOptCommandLinePropertySource}, or implement your own
 * {@code CommandLinePropertySource} against the command line parsing library of your
 * choice.
 *
 * @author Chris Beams
 * @since 3.1
 * @see CommandLinePropertySource
 * @see JOptCommandLinePropertySource
 */
// 20201202 由一个简单的字符串数组支持实现CommandLinePropertySource, 提供最简单的方法来解析命令行参数
public class SimpleCommandLinePropertySource extends CommandLinePropertySource<CommandLineArgs> {

	/**
	 * Create a new {@code SimpleCommandLinePropertySource} having the default name
	 * and backed by the given {@code String[]} of command line arguments.
	 * @see CommandLinePropertySource#COMMAND_LINE_PROPERTY_SOURCE_NAME
	 * @see CommandLinePropertySource#CommandLinePropertySource(Object)
	 */
	// 20201202 创建一个新的{@code simplecommandlinepropertySource}，它具有默认名称，并由命令行参数的给定{@code String[]}作为后盾。
	public SimpleCommandLinePropertySource(String... args) {
		super(new SimpleCommandLineArgsParser().parse(args));
	}

	/**
	 * Create a new {@code SimpleCommandLinePropertySource} having the given name
	 * and backed by the given {@code String[]} of command line arguments.
	 */
	// 20201202 创建一个新的{@code simplecommandlinepropertySource}，它具有给定的名称，并由命令行参数的给定{@code String[]}作为后盾。
	public SimpleCommandLinePropertySource(String name, String[] args) {
		// 20201202 使用命令行参数解析器解析args参数, 构造CommandLineArgs property源
		super(name, new SimpleCommandLineArgsParser().parse(args));
	}

	/**
	 * Get the property names for the option arguments.
	 */
	@Override
	public String[] getPropertyNames() {
		return StringUtils.toStringArray(this.source.getOptionNames());
	}

	@Override
	protected boolean containsOption(String name) {
		return this.source.containsOption(name);
	}

	@Override
	@Nullable
	protected List<String> getOptionValues(String name) {
		return this.source.getOptionValues(name);
	}

	@Override
	protected List<String> getNonOptionArgs() {
		return this.source.getNonOptionArgs();
	}

}
