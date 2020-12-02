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

package org.springframework.core.env;

import java.util.Collection;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * 20201202
 * A. 由命令行参数支持的{@link PropertySource}实现的抽象基类。参数化类型{@code T}表示命令行选项的底层源。在{@link SimpleCommandLinePropertySource}中，
 *    这可能与字符串数组一样简单；在{@link JOptCommandLinePropertySource}的情况下，这可能是特定于特定API的，例如JOpt的{@code OptionSet}。
 * B. 用途和一般用途: 用于基于Spring的独立应用程序，即通过传统的{@code main}方法从命令行接受{@code String[]}参数引导的应用程序。在许多情况下，直接在{@code main}方法中
 *    处理命令行参数就足够了，但是在其他情况下，可能需要将参数作为值注入springbean中。在后一种情况下，{@code CommandLinePropertySource}就变得有用了。
 *    {@code CommandLinePropertySource}通常会添加到Spring{@code ApplicationContext}的{@link Environment}，此时所有命令行参数都可以通过{@link Environment}
 *    getProperty（String）}系列方法使用。例如：
 *	 		public static void main(String[] args) {
 *     			CommandLinePropertySource clps = ...;
 *     			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 *     			ctx.getEnvironment().getPropertySources().addFirst(clps);
 *     			ctx.register(AppConfig.class);
 *     			ctx.refresh();
 * 			}
 * C. 使用上面的引导逻辑，{@code AppConfig}类可以{@code @Inject}Spring{@code Environment}，并直接查询其属性:
 * 			@Configuration
 * 			public class AppConfig {
 * 				@Inject
 *     			Environment env;
 *
 *     			@Bean
 *     			public void DataSource dataSource() {
 *         			MyVendorDataSource dataSource = new MyVendorDataSource();
 *         			dataSource.setHostname(env.getProperty("db.hostname", "localhost"));
 *         			dataSource.setUsername(env.getRequiredProperty("db.username"));
 *         			dataSource.setPassword(env.getRequiredProperty("db.password"));
 *         			// ...
 *         			return dataSource;
 *     			}
 * 			}
 * D. 对于上面的引导逻辑，由于{@code CommandLinePropertySource}是使用{@code #addFirst}方法添加到{@code Environment}的{@link MutablePropertySources}集合中，
 *    因此它具有最高的搜索优先级，这意味着数据库主机名”和其他属性可能存在于其他属性源（如系统环境）中变量，它将首先从命令行属性源中选择。这是一种合理的方法，因为在命令行中
 *    指定的参数自然比指定为环境的参数更具体变量。{@code AppConfig}类可以{@code@Inject}Spring{@code Environment}并直接查询其属性。
 * E. 作为注入{@code Environment}的替代方法，Spring的{@code @Value}注释可以用来注入这些属性，前提是{@link PropertySourcesPropertyResolver}bean已经注册，
 *    可以直接注册，也可以使用{@code <context:property-placeholder>}元素。例如：
 * 			@Component
 * 			public class MyComponent {
 *
 *     			@Value("my.property:defaultVal")
 *     			private String myProperty;
 *
 *     			public void getMyProperty() {
 *         			return this.myProperty;
 *     			}
 *
 *     			// ...
 * 			}
 * F. 使用选项参数: 单个命令行参数通过常用的{@link PropertySource#getProperty（String）}和{@link PropertySource#containsProperty（String）}方法表示为属性。例如，给定以下命令行：
 * 		a. --o1=v1 --o2
 * 		b. “o1”和“o2”被视为“选项参数”，以下断言的值为true：
 * 			b.1. CommandLinePropertySource<?> ps = ...
 * 			b.2. assert ps.containsProperty("o1") == true;
 * 			b.3. assert ps.containsProperty("o2") == true;
 * 			b.4. assert ps.containsProperty("o3") == false;
 * 			b.5. assert ps.getProperty("o1").equals("v1");
 * 			b.6. assert ps.getProperty("o2").equals("");
 * 			b.7. assert ps.getProperty("o3") == null;
 * G. 注意'o2'选项没有参数，但是{@code getProperty（“o2”）}解析为空字符串（{@code“”}），而{@code getProperty（“o3”）}解析为{@code null}，因为没有指定它。
 *    此行为与所有{@code propertysource}实现遵循的通用约定一致。
 * H. 还请注意，虽然在上面的示例中使用“--”来表示选项参数，但此语法可能因各个命令行参数库而异。例如，基于JOpt或Commons CLI的实现可能允许使用单破折号（“-”）“short”选项参数等。
 * I. 使用非选项参数: 这种抽象也支持非选项参数。提供的任何没有选项样式前缀（如“-”或“--”）的参数都被视为“非选项参数”，可以通过特殊的
 *    {@linkplain #DEFAULT_non_option_ARGS_PROPERTY_NAME“non optionargs”}属性使用。如果指定了多个非选项参数，则此属性的值将是包含所有参数的逗号分隔字符串。
 *    这种方法确保了来自{@code CommandLinePropertySource}的所有属性都有一个简单而一致的返回类型（String），同时，当与Spring{@link Environment}及其内置的
 *    {@code ConversionService}一起使用时，它还可以进行转换。考虑以下示例：
 *    	a. --o1=v1 --o2=v2 /path/to/file1 /path/to/file2
 *      b. 在这个例子中，“o1”和“o2”将被视为“选项参数”，而这两个文件系统路径被限定为“非选项参数”。因此，以下断言的结果为true：
 * 			b.1. CommandLinePropertySource<?> ps = ...
 * 			b.2. assert ps.containsProperty("o1") == true;
 * 			b.3. assert ps.containsProperty("o2") == true;
 * 			b.4. assert ps.containsProperty("nonOptionArgs") == true;
 * 			b.5. assert ps.getProperty("o1").equals("v1");
 * 			b.6. assert ps.getProperty("o2").equals("v2");
 * 			b.7. assert ps.getProperty("nonOptionArgs").equals("/path/to/file1,/path/to/file2");
 * J. 如上所述，当与Spring{@code Environment}抽象结合使用时，这个逗号分隔的字符串可以很容易地转换为字符串数组或列表：
* 			Environment env = applicationContext.getEnvironment();
 * 			String[] nonOptionArgs = env.getProperty("nonOptionArgs", String[].class);
 * 			assert nonOptionArgs[0].equals("/path/to/file1");
 * 			assert nonOptionArgs[1].equals("/path/to/file2");
 * K. 特殊的“非选项参数”属性的名称可以通过{@link #setNonOptionArgsPropertyName（String）}方法自定义。建议这样做，因为它为非选项参数提供了适当的语义值。
 *    例如，如果文件系统路径被指定为非选项参数，则最好将其称为类似于文件.位置”而不是默认的“非选项args”：
 * 			public static void main(String[] args) {
 *     			CommandLinePropertySource clps = ...;
 *     			clps.setNonOptionArgsPropertyName("file.locations");
 *
 *     			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 *     			ctx.getEnvironment().getPropertySources().addFirst(clps);
 *     			ctx.register(AppConfig.class);
 *     			ctx.refresh();
 * 			}
 * L. 局限性: 这个抽象并不打算公开底层命令行解析api（如JOpt或Commons-CLI）的全部功能。它的目的恰恰相反：在命令行参数被解析后，提供最简单的抽象来访问它们。
 *    因此，典型的情况是完全配置底层命令行解析API，解析进入main方法的参数的{@code String[]}，然后简单地将解析结果提供给{@code CommandLinePropertySource}的实现。
 *    在这一点上，所有参数可以被视为“option”或“non-option”参数，如上所述，可以通过普通的{@code PropertySource}和{@code Environment}api来访问。
 */
/**
 * A.
 * Abstract base class for {@link PropertySource} implementations backed by command line
 * arguments. The parameterized type {@code T} represents the underlying source of command
 * line options. This may be as simple as a String array in the case of
 * {@link SimpleCommandLinePropertySource}, or specific to a particular API such as JOpt's
 * {@code OptionSet} in the case of {@link JOptCommandLinePropertySource}.
 *
 * B.
 * <h3>Purpose and General Usage</h3>
 *
 * For use in standalone Spring-based applications, i.e. those that are bootstrapped via
 * a traditional {@code main} method accepting a {@code String[]} of arguments from the
 * command line. In many cases, processing command-line arguments directly within the
 * {@code main} method may be sufficient, but in other cases, it may be desirable to
 * inject arguments as values into Spring beans. It is this latter set of cases in which
 * a {@code CommandLinePropertySource} becomes useful. A {@code CommandLinePropertySource}
 * will typically be added to the {@link Environment} of the Spring
 * {@code ApplicationContext}, at which point all command line arguments become available
 * through the {@link Environment#getProperty(String)} family of methods. For example:
 *
 * <pre class="code">
 * public static void main(String[] args) {
 *     CommandLinePropertySource clps = ...;
 *     AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 *     ctx.getEnvironment().getPropertySources().addFirst(clps);
 *     ctx.register(AppConfig.class);
 *     ctx.refresh();
 * }</pre>
 *
 * C.
 * With the bootstrap logic above, the {@code AppConfig} class may {@code @Inject} the
 * Spring {@code Environment} and query it directly for properties:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Inject Environment env;
 *
 *     &#064;Bean
 *     public void DataSource dataSource() {
 *         MyVendorDataSource dataSource = new MyVendorDataSource();
 *         dataSource.setHostname(env.getProperty("db.hostname", "localhost"));
 *         dataSource.setUsername(env.getRequiredProperty("db.username"));
 *         dataSource.setPassword(env.getRequiredProperty("db.password"));
 *         // ...
 *         return dataSource;
 *     }
 * }</pre>
 *
 * D.
 * Because the {@code CommandLinePropertySource} was added to the {@code Environment}'s
 * set of {@link MutablePropertySources} using the {@code #addFirst} method, it has
 * highest search precedence, meaning that while "db.hostname" and other properties may
 * exist in other property sources such as the system environment variables, it will be
 * chosen from the command line property source first. This is a reasonable approach
 * given that arguments specified on the command line are naturally more specific than
 * those specified as environment variables.
 *
 * E.
 * <p>As an alternative to injecting the {@code Environment}, Spring's {@code @Value}
 * annotation may be used to inject these properties, given that a {@link
 * PropertySourcesPropertyResolver} bean has been registered, either directly or through
 * using the {@code <context:property-placeholder>} element. For example:
 *
 * <pre class="code">
 * &#064;Component
 * public class MyComponent {
 *
 *     &#064;Value("my.property:defaultVal")
 *     private String myProperty;
 *
 *     public void getMyProperty() {
 *         return this.myProperty;
 *     }
 *
 *     // ...
 * }</pre>
 *
 * F.
 * <h3>Working with option arguments</h3>
 *
 * <p>Individual command line arguments are represented as properties through the usual
 * {@link PropertySource#getProperty(String)} and
 * {@link PropertySource#containsProperty(String)} methods. For example, given the
 * following command line:
 *
 * <pre class="code">--o1=v1 --o2</pre>
 *
 * 'o1' and 'o2' are treated as "option arguments", and the following assertions would
 * evaluate true:
 *
 * <pre class="code">
 * CommandLinePropertySource<?> ps = ...
 * assert ps.containsProperty("o1") == true;
 * assert ps.containsProperty("o2") == true;
 * assert ps.containsProperty("o3") == false;
 * assert ps.getProperty("o1").equals("v1");
 * assert ps.getProperty("o2").equals("");
 * assert ps.getProperty("o3") == null;
 * </pre>
 *
 * G.
 * Note that the 'o2' option has no argument, but {@code getProperty("o2")} resolves to
 * empty string ({@code ""}) as opposed to {@code null}, while {@code getProperty("o3")}
 * resolves to {@code null} because it was not specified. This behavior is consistent with
 * the general contract to be followed by all {@code PropertySource} implementations.
 *
 * H.
 * <p>Note also that while "--" was used in the examples above to denote an option
 * argument, this syntax may vary across individual command line argument libraries. For
 * example, a JOpt- or Commons CLI-based implementation may allow for single dash ("-")
 * "short" option arguments, etc.
 *
 * I.
 * <h3>Working with non-option arguments</h3>
 *
 * <p>Non-option arguments are also supported through this abstraction. Any arguments
 * supplied without an option-style prefix such as "-" or "--" are considered "non-option
 * arguments" and available through the special {@linkplain
 * #DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME "nonOptionArgs"} property.  If multiple
 * non-option arguments are specified, the value of this property will be a
 * comma-delimited string containing all of the arguments. This approach ensures a simple
 * and consistent return type (String) for all properties from a {@code
 * CommandLinePropertySource} and at the same time lends itself to conversion when used
 * in conjunction with the Spring {@link Environment} and its built-in {@code
 * ConversionService}. Consider the following example:
 *
 * <pre class="code">--o1=v1 --o2=v2 /path/to/file1 /path/to/file2</pre>
 *
 * In this example, "o1" and "o2" would be considered "option arguments", while the two
 * filesystem paths qualify as "non-option arguments".  As such, the following assertions
 * will evaluate true:
 *
 * <pre class="code">
 * CommandLinePropertySource<?> ps = ...
 * assert ps.containsProperty("o1") == true;
 * assert ps.containsProperty("o2") == true;
 * assert ps.containsProperty("nonOptionArgs") == true;
 * assert ps.getProperty("o1").equals("v1");
 * assert ps.getProperty("o2").equals("v2");
 * assert ps.getProperty("nonOptionArgs").equals("/path/to/file1,/path/to/file2");
 * </pre>
 *
 * J.
 * <p>As mentioned above, when used in conjunction with the Spring {@code Environment}
 * abstraction, this comma-delimited string may easily be converted to a String array or
 * list:
 *
 * <pre class="code">
 * Environment env = applicationContext.getEnvironment();
 * String[] nonOptionArgs = env.getProperty("nonOptionArgs", String[].class);
 * assert nonOptionArgs[0].equals("/path/to/file1");
 * assert nonOptionArgs[1].equals("/path/to/file2");
 * </pre>
 *
 * K.
 * <p>The name of the special "non-option arguments" property may be customized through
 * the {@link #setNonOptionArgsPropertyName(String)} method. Doing so is recommended as
 * it gives proper semantic value to non-option arguments. For example, if filesystem
 * paths are being specified as non-option arguments, it is likely preferable to refer to
 * these as something like "file.locations" than the default of "nonOptionArgs":
 *
 * <pre class="code">
 * public static void main(String[] args) {
 *     CommandLinePropertySource clps = ...;
 *     clps.setNonOptionArgsPropertyName("file.locations");
 *
 *     AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 *     ctx.getEnvironment().getPropertySources().addFirst(clps);
 *     ctx.register(AppConfig.class);
 *     ctx.refresh();
 * }</pre>
 *
 * L.
 * <h3>Limitations</h3>
 *
 * This abstraction is not intended to expose the full power of underlying command line
 * parsing APIs such as JOpt or Commons CLI. It's intent is rather just the opposite: to
 * provide the simplest possible abstraction for accessing command line arguments
 * <em>after</em> they have been parsed. So the typical case will involve fully configuring
 * the underlying command line parsing API, parsing the {@code String[]} of arguments
 * coming into the main method, and then simply providing the parsing results to an
 * implementation of {@code CommandLinePropertySource}. At that point, all arguments can
 * be considered either 'option' or 'non-option' arguments and as described above can be
 * accessed through the normal {@code PropertySource} and {@code Environment} APIs.
 *
 * @author Chris Beams
 * @since 3.1
 * @param <T> the source type
 * @see PropertySource
 * @see SimpleCommandLinePropertySource
 * @see JOptCommandLinePropertySource
 */
// 20201202 由命令行参数支持的{@link PropertySource}实现的抽象基类。参数化类型{@code T}表示命令行选项的底层源。
public abstract class CommandLinePropertySource<T> extends EnumerablePropertySource<T> {

	/** The default name given to {@link CommandLinePropertySource} instances: {@value}. */
	// 20201202 给{@link CommandLinePropertySource}实例的默认名称：{@value}。
	public static final String COMMAND_LINE_PROPERTY_SOURCE_NAME = "commandLineArgs";

	/** The default name of the property representing non-option arguments: {@value}. */
	public static final String DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME = "nonOptionArgs";


	private String nonOptionArgsPropertyName = DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME;


	/**
	 * Create a new {@code CommandLinePropertySource} having the default name
	 * {@value #COMMAND_LINE_PROPERTY_SOURCE_NAME} and backed by the given source object.
	 */
	public CommandLinePropertySource(T source) {
		super(COMMAND_LINE_PROPERTY_SOURCE_NAME, source);
	}

	/**
	 * Create a new {@link CommandLinePropertySource} having the given name
	 * and backed by the given source object.
	 */
	// 20201202 创建一个新的{@link CommandLinePropertySource}，它具有给定的名称并由给定的源对象支持。
	public CommandLinePropertySource(String name, T source) {
		super(name, source);
	}


	/**
	 * Specify the name of the special "non-option arguments" property.
	 * The default is {@value #DEFAULT_NON_OPTION_ARGS_PROPERTY_NAME}.
	 */
	public void setNonOptionArgsPropertyName(String nonOptionArgsPropertyName) {
		this.nonOptionArgsPropertyName = nonOptionArgsPropertyName;
	}

	/**
	 * This implementation first checks to see if the name specified is the special
	 * {@linkplain #setNonOptionArgsPropertyName(String) "non-option arguments" property},
	 * and if so delegates to the abstract {@link #getNonOptionArgs()} method
	 * checking to see whether it returns an empty collection. Otherwise delegates to and
	 * returns the value of the abstract {@link #containsOption(String)} method.
	 */
	@Override
	public final boolean containsProperty(String name) {
		if (this.nonOptionArgsPropertyName.equals(name)) {
			return !this.getNonOptionArgs().isEmpty();
		}
		return this.containsOption(name);
	}

	/**
	 * This implementation first checks to see if the name specified is the special
	 * {@linkplain #setNonOptionArgsPropertyName(String) "non-option arguments" property},
	 * and if so delegates to the abstract {@link #getNonOptionArgs()} method. If so
	 * and the collection of non-option arguments is empty, this method returns {@code
	 * null}. If not empty, it returns a comma-separated String of all non-option
	 * arguments. Otherwise delegates to and returns the result of the abstract {@link
	 * #getOptionValues(String)} method.
	 */
	@Override
	@Nullable
	public final String getProperty(String name) {
		if (this.nonOptionArgsPropertyName.equals(name)) {
			Collection<String> nonOptionArguments = this.getNonOptionArgs();
			if (nonOptionArguments.isEmpty()) {
				return null;
			}
			else {
				return StringUtils.collectionToCommaDelimitedString(nonOptionArguments);
			}
		}
		Collection<String> optionValues = this.getOptionValues(name);
		if (optionValues == null) {
			return null;
		}
		else {
			return StringUtils.collectionToCommaDelimitedString(optionValues);
		}
	}


	/**
	 * Return whether the set of option arguments parsed from the command line contains
	 * an option with the given name.
	 */
	protected abstract boolean containsOption(String name);

	/**
	 * Return the collection of values associated with the command line option having the
	 * given name.
	 * <ul>
	 * <li>if the option is present and has no argument (e.g.: "--foo"), return an empty
	 * collection ({@code []})</li>
	 * <li>if the option is present and has a single value (e.g. "--foo=bar"), return a
	 * collection having one element ({@code ["bar"]})</li>
	 * <li>if the option is present and the underlying command line parsing library
	 * supports multiple arguments (e.g. "--foo=bar --foo=baz"), return a collection
	 * having elements for each value ({@code ["bar", "baz"]})</li>
	 * <li>if the option is not present, return {@code null}</li>
	 * </ul>
	 */
	@Nullable
	protected abstract List<String> getOptionValues(String name);

	/**
	 * Return the collection of non-option arguments parsed from the command line.
	 * Never {@code null}.
	 */
	protected abstract List<String> getNonOptionArgs();

}
