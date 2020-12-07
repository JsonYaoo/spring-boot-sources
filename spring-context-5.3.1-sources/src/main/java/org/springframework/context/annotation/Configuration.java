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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * 20201206
 * A. 指示一个类声明了一个或多个{@link Bean @Bean}方法，并且可以由Spring容器进行处理，以在运行时为这些bean生成bean定义和服务请求，例如：
 * 			@Configuration
 * 			public class AppConfig {
 *     			@Bean
 *     			public MyBean myBean() {
 *         			// instantiate, configure and return bean ...
 *     			}
 * 			}
 * B. 通过{@code AnnotationConfigApplicationContext}引导{@code @Configuration}类: {@code @Configuration}类通常使用
 *    {@link AnnotationConfigApplicationContext}或具有Web功能的变体
 *    {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext AnnotationConfigWebApplicationContext}进行引导。
 *    前者的一个简单示例如下：
* 			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * 			ctx.register(AppConfig.class);
 * 			ctx.refresh();
 * 			MyBean myBean = ctx.getBean(MyBean.class);
 * 			// use myBean ...
 * 	  有关更多详细信息，请参见{@link AnnotationConfigApplicationContext} Javadocs，以及在{@code Servlet}容器中，请参见
 * 	  {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext AnnotationConfigWebApplicationContext}以获取Web配置说明。
 * C. 通过Spring {@code <beans>} XML: 作为直接针对{@code AnnotationConfigApplicationContext}注册{@code @Configuration}类的一种替代方法，可以将
 *    {@code @Configuration}类声明为Spring XML文件中的常规{@code <bean>}定义：
 * 			<beans>
 *    			<context:annotation-config/>
 *    			<bean class="com.acme.AppConfig"/>
 * 			</beans>
 * 	  在上面的示例中，需要{@code <context：annotation-config />}，以启用{@link ConfigurationClassPostProcessor}和其他与注解相关的后处理器，以方便处理
 * 	  {@code @Configuration}类。
 * D. 通过组件扫描: {@code @Configuration}用{@link Component @Component}进行元注解，因此{@code @Configuration}类是组件扫描的候选对象（通常使用Spring XML的
 *    {@code <context：component-scan />}元素 ），因此也可以像任何常规的{@code @Component}一样利用{@link Autowired @Autowired} /
 *    {@link javax.inject.Inject @Inject}的优势。 特别是，如果存在单个构造函数，则自动为该构造函数自动应用语义：
 * 			@Configuration
 *	 		public class AppConfig {
 *
 *     			private final SomeBean someBean;
 *
 *     			public AppConfig(SomeBean someBean) {
 *         			this.someBean = someBean;
 *     			}
 *
 *     			// @Bean definition using "SomeBean"
 *
 * 			}
 * 	  {@code @Configuration}类不仅可以使用组件扫描来引导，还可以自己使用{@link ComponentScan @ComponentScan}注释来配置组件扫描：
 * 			@Configuration
 * 			@ComponentScan("com.acme.app.services")
 * 			public class AppConfig {
 *     			// various &#064;Bean definitions ...
 * 			}
 * 	  有关详细信息，请参见{@link ComponentScan @ComponentScan} javadocs。
 * E. 使用外在价值-使用{@code Environment} API: 可以通过将Spring {@link org.springframework.core.env.Environment}注入到{@code @Configuration}类中来查找外部化的值。
 *    例如，使用{@code @Autowired}注释：
 * 			@Configuration
 * 			public class AppConfig {
 *
 *     			@Autowired
 *     			Environment env;
 *
 *     			@Bean
 *     			public MyBean myBean() {
 *         			MyBean myBean = new MyBean();
 *         			myBean.setName(env.getProperty("bean.name"));
 *         			return myBean;
 *     			}
 * 			}
 * 	  通过{@code Environment}解析的属性驻留在一个或多个“属性源”对象中，并且{@code @Configuration}类可以使用{@link PropertySource @PropertySource}注释将属性源
 * 	  贡献给{@code Environment}对象 ：
 * 			@Configuration
 * 			@PropertySource("classpath:/com/acme/app.properties")
 * 				public class AppConfig {
 *
 *     			@Inject
 *     			Environment env;
 *
 *     			@Bean
 *     			public MyBean myBean() {
 *         			return new MyBean(env.getProperty("bean.name"));
 *     			}
 * 			}
 *    有关更多详细信息，请参见{@link org.springframework.core.env.Environment Environment}和{@link PropertySource @PropertySource} Javadocs。
 * F. 使用{@code @Value}注释: 可以使用{@link Value @Value}注释将外部化的值注入到{@code @Configuration}类中：
 * 			@Configuration
 * 			@PropertySource("classpath:/com/acme/app.properties")
 * 			public class AppConfig {
 *
 *     			@Value("${bean.name}")
 *     			String beanName;
 *
 *     			@Bean
 *     			public MyBean myBean() {
 *         			return new MyBean(beanName);
 *     			}
 * 			}
 *    这种方法通常与Spring的{@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer PropertySourcesPlaceholderConfigurer}结合使用，
 *    可以通过{@code <context：property-placeholder />}在XML配置中自动启用，也可以在{@@code {@code @Bean}通过专用的静态方法对code @Configuration}类进行编码
 *    （有关详细信息，请参见{@link Bean @Bean}的javadocs的“关于BeanFactoryPostProcessor-返回{@code @Bean}方法的说明”） ）。但是请注意，通常仅在需要自定义配置
 *    （例如占位符语法等）时才需要通过{@code static} {@code @Bean}方法显式注册{@code PropertySourcesPlaceholderConfigurer}。 Bean后处理器（例如
 *    {@code PropertySourcesPlaceholderConfigurer}）已经为{@code ApplicationContext}注册了一个嵌入式值解析器，Spring将注册一个默认的嵌入式值解析器，该解析器将根据
 *    {@code Environment}中注册的属性源解析占位符。 。请参阅以下有关使用{@code @ImportResource}与Spring XML组合{@code @Configuration}类的部分；参见
 *    {@link Value @Value} javadocs；并查看{@link Bean @Bean} javadocs，以获取有关使用{@code BeanFactoryPostProcessor}类型（例如
 *    {@code PropertySourcesPlaceholderConfigurer}）的详细信息。
 * G. 组成{@code @Configuration}类-带有{@code @Import}注释: {@code @Configuration}类可以使用{@link Import @Import}注释组成，类似于{@code <import>}
 *    在Spring XML中的工作方式。 由于{@code @Configuration}对象作为容器内的Spring Bean管理，因此导入的配置可能会注入＆mdash; 例如，通过构造函数注入：
 *          @Configuration
 *          public class DatabaseConfig {
 *
 *              @Bean
 *              public DataSource dataSource() {
 *                  // instantiate, configure and return DataSource
 *              }
 *          }
 *
 *          @Configuration
 *          @Import(DatabaseConfig.class)
 *          public class AppConfig {
 *
 *              private final DatabaseConfig dataConfig;
 *
 *              public AppConfig(DatabaseConfig dataConfig) {
 *                  this.dataConfig = dataConfig;
 *              }
 *
 *              @Bean
 *              public MyBean myBean() {
 *                  // reference the dataSource() bean method
 *                  return new MyBean(dataConfig.dataSource());
 *              }
 *          }
 *    现在，可以通过仅针对Spring上下文注册{@code AppConfig}来引导{@code AppConfig}和导入的{@code DatabaseConfig}：
 *          new AnnotationConfigApplicationContext(AppConfig.class);
 * H. 带有{@code @Profile}注释: {@code @Configuration}类可以用{@link Profile @Profile}注释标记，以指示仅当给定的一个或多个配置文件为active时才应对其进行处理：
 *          @Profile("development")
 *          @Configuration
 *          public class EmbeddedDatabaseConfig {
 *
 *              @Bean
 *              public DataSource dataSource() {
 *                  // instantiate, configure and return embedded DataSource
 *              }
 *          }
 *
 *          @Profile("production")
 *          @Configuration
 *          public class ProductionDatabaseConfig {
 *
 *              @Bean
 *              public DataSource dataSource() {
 *                  // instantiate, configure and return production DataSource
 *              }
 *          }
 *    另外，您也可以在{@code @Bean}方法级别＆mdash;声明配置文件条件。 例如，对于同一配置类中的替代bean变体：
 *          @Configuration
 *          public class ProfileDatabaseConfig {
 *
 *              @Bean("dataSource")
 *              @Profile("development")
 *              public DataSource embeddedDatabase() { ... }
 *
 *              @Bean("dataSource")
 *              @Profile("production")
 *              public DataSource productionDatabase() { ... }
 *          }
 *    有关更多详细信息，请参见{@link Profile @Profile}和{@link org.springframework.core.env.Environment} javadocs。
 * I. 使用{@code @ImportResource}批注的Spring XML: 如上所述，可以在Spring XML文件中将{@code @Configuration}类声明为常规Spring {@code <bean>}定义。 还可以使用
 *    {@link ImportResource @ImportResource}批注将Spring XML配置文件导入到{@code @Configuration}类中。 从XML导入的Bean定义可以注入＆mdash;。 例如，使用{@code @Inject}批注：
 *          @Configuration
 *          @ImportResource("classpath:/com/acme/database-config.xml")
 *          public class AppConfig {
 *
 *              @Inject
 *              DataSource dataSource; // from XML
 *
 *              @Bean
 *              public MyBean myBean() {
 *                  // inject the XML-defined dataSource bean
 *                  return new MyBean(this.dataSource);
 *              }
 *          }
 * J. 使用嵌套的{@code @Configuration}类: {@code @Configuration}类可以相互嵌套，如下所示：
 *          @Configuration
 *          public class AppConfig {
 *
 *              @Inject DataSource dataSource;
 *
 *              @Bean
 *              public MyBean myBean() {
 *                  return new MyBean(dataSource);
 *              }
 *
 *              @Configuration
 *              static class DatabaseConfig {
 *                  @Bean
 *                  DataSource dataSource() {
 *                      return new EmbeddedDatabaseBuilder().build();
 *                  }
 *              }
 *          }
 *    自举这种安排时，仅需要针对应用程序上下文注册{@code AppConfig}。 由于是嵌套的{@code @Configuration}类，因此将自动注册{@code DatabaseConfig}。 当
 *    {@code AppConfig}和{@code DatabaseConfig}之间的关系已经隐式清除时，这避免了使用{@code @Import}注释的需要。 还请注意，嵌套的{@code @Configuration}类可与
 *    {@code @Profile}注解一起使用，以为封闭的{@code @Configuration}类提供同一bean的两个选项。
 * K. 配置延迟初始化: 默认情况下，{@code @Bean}方法将在容器引导时急切实例化。 为了避免这种情况，可以将{@code @Configuration}与{@link Lazy @Lazy}注释结合使用，
 *    以指示默认情况下对类中声明的所有{@code @Bean}方法进行了延迟初始化。 请注意，{@code @Lazy}也可以用于单独的{@code @Bean}方法。
 * L. 测试对{@code @Configuration}类的支持: {@code spring-test}模块中可用的Spring TestContext框架提供了{@code @ContextConfiguration}注释，该注释可以接受一组组件类
 *    引用＆mdash;。 通常是{@code @Configuration}或{@code @Component}类。
 *          @RunWith(SpringRunner.class)
 *          @ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
 *              public class MyTests {
 *
 *              @Autowired
 *              MyBean myBean;
 *
 *              @Autowired
 *              DataSource dataSource;
 *
 *              @Test
 *              public void test() {
 *                  // assertions against myBean ...
 *              }
 *          }
 *    有关详细信息，请参见<a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-framework"> TestContext框架</a>参考文档。
 * M. 使用{@code @Enable}注解启用内置的Spring功能: 可以使用各自的“ {@code @Enable}”注解从{@code @Configuration}类启用和配置Spring功能，例如异步方法执行，计划任务执行，
 *    注释驱动的事务管理，甚至Spring MVC。 参见{@link org.springframework.scheduling.annotation.EnableAsync @EnableAsync}，
 *    {@link org.springframework.scheduling.annotation.EnableScheduling @EnableScheduling}，
 *    {@link org.springframework.transaction.annotation.EnableTransactionManagement @EnableTransactionManagement}，
 *    有关详细信息，请@link org.springframework.context.annotation.EnableAspectJAutoProxy @EnableAspectJAutoProxy}和
 *    {@link org.springframework.web.servlet.config.annotation.EnableWebMvc @EnableWebMvc}。
 * N. 创作{@code @Configuration}类时的约束:
 *     a. 配置类必须作为类提供（即，不是从工厂方法返回的实例），以允许通过生成的子类增强运行时。
 *     b. 配置类必须是非最终类（允许在运行时提供子类），除非{@link #proxyBeanMethods（）proxyBeanMethods}标志设置为{@code false}，在这种情况下，不需要运行时生成的子类。
 *     c. 配置类必须是非本地的（即不得在方法中声明）。
 *     d. 任何嵌套的配置类都必须声明为{@code static}。
 *     c. {@code @Bean}方法可能不会再创建其他配置类（任何此类实例都将被视为常规Bean，且其配置注释仍未被检测到）。
 */
/**
 * A.
 * Indicates that a class declares one or more {@link Bean @Bean} methods and
 * may be processed by the Spring container to generate bean definitions and
 * service requests for those beans at runtime, for example:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // instantiate, configure and return bean ...
 *     }
 * }</pre>
 *
 * B.
 * <h2>Bootstrapping {@code @Configuration} classes</h2>
 *
 * <h3>Via {@code AnnotationConfigApplicationContext}</h3>
 *
 * <p>{@code @Configuration} classes are typically bootstrapped using either
 * {@link AnnotationConfigApplicationContext} or its web-capable variant,
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext}. A simple example with the former follows:
 *
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(AppConfig.class);
 * ctx.refresh();
 * MyBean myBean = ctx.getBean(MyBean.class);
 * // use myBean ...
 * </pre>
 *
 * <p>See the {@link AnnotationConfigApplicationContext} javadocs for further details, and see
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} for web configuration instructions in a
 * {@code Servlet} container.
 *
 * C.
 * <h3>Via Spring {@code <beans>} XML</h3>
 *
 * <p>As an alternative to registering {@code @Configuration} classes directly against an
 * {@code AnnotationConfigApplicationContext}, {@code @Configuration} classes may be
 * declared as normal {@code <bean>} definitions within Spring XML files:
 *
 * <pre class="code">
 * &lt;beans&gt;
 *    &lt;context:annotation-config/&gt;
 *    &lt;bean class="com.acme.AppConfig"/&gt;
 * &lt;/beans&gt;
 * </pre>
 *
 * <p>In the example above, {@code <context:annotation-config/>} is required in order to
 * enable {@link ConfigurationClassPostProcessor} and other annotation-related
 * post processors that facilitate handling {@code @Configuration} classes.
 *
 * D.
 * <h3>Via component scanning</h3>
 *
 * <p>{@code @Configuration} is meta-annotated with {@link Component @Component}, therefore
 * {@code @Configuration} classes are candidates for component scanning (typically using
 * Spring XML's {@code <context:component-scan/>} element) and therefore may also take
 * advantage of {@link Autowired @Autowired}/{@link javax.inject.Inject @Inject}
 * like any regular {@code @Component}. In particular, if a single constructor is present
 * autowiring semantics will be applied transparently for that constructor:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     private final SomeBean someBean;
 *
 *     public AppConfig(SomeBean someBean) {
 *         this.someBean = someBean;
 *     }
 *
 *     // &#064;Bean definition using "SomeBean"
 *
 * }</pre>
 *
 * <p>{@code @Configuration} classes may not only be bootstrapped using
 * component scanning, but may also themselves <em>configure</em> component scanning using
 * the {@link ComponentScan @ComponentScan} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.acme.app.services")
 * public class AppConfig {
 *     // various &#064;Bean definitions ...
 * }</pre>
 *
 * <p>See the {@link ComponentScan @ComponentScan} javadocs for details.
 *
 * E.
 * <h2>Working with externalized values</h2>
 *
 * <h3>Using the {@code Environment} API</h3>
 *
 * <p>Externalized values may be looked up by injecting the Spring
 * {@link org.springframework.core.env.Environment} into a {@code @Configuration}
 * class &mdash; for example, using the {@code @Autowired} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064Autowired Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         MyBean myBean = new MyBean();
 *         myBean.setName(env.getProperty("bean.name"));
 *         return myBean;
 *     }
 * }</pre>
 *
 * <p>Properties resolved through the {@code Environment} reside in one or more "property
 * source" objects, and {@code @Configuration} classes may contribute property sources to
 * the {@code Environment} object using the {@link PropertySource @PropertySource}
 * annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064Inject Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(env.getProperty("bean.name"));
 *     }
 * }</pre>
 *
 * <p>See the {@link org.springframework.core.env.Environment Environment}
 * and {@link PropertySource @PropertySource} javadocs for further details.
 *
 * F.
 * <h3>Using the {@code @Value} annotation</h3>
 *
 * <p>Externalized values may be injected into {@code @Configuration} classes using
 * the {@link Value @Value} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064Value("${bean.name}") String beanName;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(beanName);
 *     }
 * }</pre>
 *
 * <p>This approach is often used in conjunction with Spring's
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer} that can be enabled <em>automatically</em>
 * in XML configuration via {@code <context:property-placeholder/>} or <em>explicitly</em>
 * in a {@code @Configuration} class via a dedicated {@code static} {@code @Bean} method
 * (see "a note on BeanFactoryPostProcessor-returning {@code @Bean} methods" of
 * {@link Bean @Bean}'s javadocs for details). Note, however, that explicit registration
 * of a {@code PropertySourcesPlaceholderConfigurer} via a {@code static} {@code @Bean}
 * method is typically only required if you need to customize configuration such as the
 * placeholder syntax, etc. Specifically, if no bean post-processor (such as a
 * {@code PropertySourcesPlaceholderConfigurer}) has registered an <em>embedded value
 * resolver</em> for the {@code ApplicationContext}, Spring will register a default
 * <em>embedded value resolver</em> which resolves placeholders against property sources
 * registered in the {@code Environment}. See the section below on composing
 * {@code @Configuration} classes with Spring XML using {@code @ImportResource}; see
 * the {@link Value @Value} javadocs; and see the {@link Bean @Bean} javadocs for details
 * on working with {@code BeanFactoryPostProcessor} types such as
 * {@code PropertySourcesPlaceholderConfigurer}.
 *
 * G.
 * <h2>Composing {@code @Configuration} classes</h2>
 *
 * <h3>With the {@code @Import} annotation</h3>
 *
 * <p>{@code @Configuration} classes may be composed using the {@link Import @Import} annotation,
 * similar to the way that {@code <import>} works in Spring XML. Because
 * {@code @Configuration} objects are managed as Spring beans within the container,
 * imported configurations may be injected &mdash; for example, via constructor injection:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class DatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return DataSource
 *     }
 * }
 *
 * &#064;Configuration
 * &#064;Import(DatabaseConfig.class)
 * public class AppConfig {
 *
 *     private final DatabaseConfig dataConfig;
 *
 *     public AppConfig(DatabaseConfig dataConfig) {
 *         this.dataConfig = dataConfig;
 *     }
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // reference the dataSource() bean method
 *         return new MyBean(dataConfig.dataSource());
 *     }
 * }</pre>
 *
 * <p>Now both {@code AppConfig} and the imported {@code DatabaseConfig} can be bootstrapped
 * by registering only {@code AppConfig} against the Spring context:
 *
 * <pre class="code">
 * new AnnotationConfigApplicationContext(AppConfig.class);</pre>
 *
 * H.
 * <h3>With the {@code @Profile} annotation</h3>
 *
 * <p>{@code @Configuration} classes may be marked with the {@link Profile @Profile} annotation to
 * indicate they should be processed only if a given profile or profiles are <em>active</em>:
 *
 * <pre class="code">
 * &#064;Profile("development")
 * &#064;Configuration
 * public class EmbeddedDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return embedded DataSource
 *     }
 * }
 *
 * &#064;Profile("production")
 * &#064;Configuration
 * public class ProductionDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return production DataSource
 *     }
 * }</pre>
 *
 * <p>Alternatively, you may also declare profile conditions at the {@code @Bean} method level
 * &mdash; for example, for alternative bean variants within the same configuration class:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class ProfileDatabaseConfig {
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("development")
 *     public DataSource embeddedDatabase() { ... }
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("production")
 *     public DataSource productionDatabase() { ... }
 * }</pre>
 *
 * <p>See the {@link Profile @Profile} and {@link org.springframework.core.env.Environment}
 * javadocs for further details.
 *
 * I.
 * <h3>With Spring XML using the {@code @ImportResource} annotation</h3>
 *
 * <p>As mentioned above, {@code @Configuration} classes may be declared as regular Spring
 * {@code <bean>} definitions within Spring XML files. It is also possible to
 * import Spring XML configuration files into {@code @Configuration} classes using
 * the {@link ImportResource @ImportResource} annotation. Bean definitions imported from
 * XML can be injected &mdash; for example, using the {@code @Inject} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ImportResource("classpath:/com/acme/database-config.xml")
 * public class AppConfig {
 *
 *     &#064Inject DataSource dataSource; // from XML
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // inject the XML-defined dataSource bean
 *         return new MyBean(this.dataSource);
 *     }
 * }</pre>
 *
 * J.
 * <h3>With nested {@code @Configuration} classes</h3>
 *
 * <p>{@code @Configuration} classes may be nested within one another as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Inject DataSource dataSource;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(dataSource);
 *     }
 *
 *     &#064;Configuration
 *     static class DatabaseConfig {
 *         &#064;Bean
 *         DataSource dataSource() {
 *             return new EmbeddedDatabaseBuilder().build();
 *         }
 *     }
 * }</pre>
 *
 * <p>When bootstrapping such an arrangement, only {@code AppConfig} need be registered
 * against the application context. By virtue of being a nested {@code @Configuration}
 * class, {@code DatabaseConfig} <em>will be registered automatically</em>. This avoids
 * the need to use an {@code @Import} annotation when the relationship between
 * {@code AppConfig} and {@code DatabaseConfig} is already implicitly clear.
 *
 * <p>Note also that nested {@code @Configuration} classes can be used to good effect
 * with the {@code @Profile} annotation to provide two options of the same bean to the
 * enclosing {@code @Configuration} class.
 *
 * K.
 * <h2>Configuring lazy initialization</h2>
 *
 * <p>By default, {@code @Bean} methods will be <em>eagerly instantiated</em> at container
 * bootstrap time.  To avoid this, {@code @Configuration} may be used in conjunction with
 * the {@link Lazy @Lazy} annotation to indicate that all {@code @Bean} methods declared
 * within the class are by default lazily initialized. Note that {@code @Lazy} may be used
 * on individual {@code @Bean} methods as well.
 *
 * L.
 * <h2>Testing support for {@code @Configuration} classes</h2>
 *
 * <p>The Spring <em>TestContext framework</em> available in the {@code spring-test} module
 * provides the {@code @ContextConfiguration} annotation which can accept an array of
 * <em>component class</em> references &mdash; typically {@code @Configuration} or
 * {@code @Component} classes.
 *
 * <pre class="code">
 * &#064;RunWith(SpringRunner.class)
 * &#064;ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
 * public class MyTests {
 *
 *     &#064;Autowired MyBean myBean;
 *
 *     &#064;Autowired DataSource dataSource;
 *
 *     &#064;Test
 *     public void test() {
 *         // assertions against myBean ...
 *     }
 * }</pre>
 *
 * <p>See the
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-framework">TestContext framework</a>
 * reference documentation for details.
 *
 * M.
 * <h2>Enabling built-in Spring features using {@code @Enable} annotations</h2>
 *
 * <p>Spring features such as asynchronous method execution, scheduled task execution,
 * annotation driven transaction management, and even Spring MVC can be enabled and
 * configured from {@code @Configuration} classes using their respective "{@code @Enable}"
 * annotations. See
 * {@link org.springframework.scheduling.annotation.EnableAsync @EnableAsync},
 * {@link org.springframework.scheduling.annotation.EnableScheduling @EnableScheduling},
 * {@link org.springframework.transaction.annotation.EnableTransactionManagement @EnableTransactionManagement},
 * {@link org.springframework.context.annotation.EnableAspectJAutoProxy @EnableAspectJAutoProxy},
 * and {@link org.springframework.web.servlet.config.annotation.EnableWebMvc @EnableWebMvc}
 * for details.
 *
 * N.
 * <h2>Constraints when authoring {@code @Configuration} classes</h2>
 *
 * <ul>
 * <li>Configuration classes must be provided as classes (i.e. not as instances returned
 * from factory methods), allowing for runtime enhancements through a generated subclass.
 * <li>Configuration classes must be non-final (allowing for subclasses at runtime),
 * unless the {@link #proxyBeanMethods() proxyBeanMethods} flag is set to {@code false}
 * in which case no runtime-generated subclass is necessary.
 * <li>Configuration classes must be non-local (i.e. may not be declared within a method).
 * <li>Any nested configuration classes must be declared as {@code static}.
 * <li>{@code @Bean} methods may not in turn create further configuration classes
 * (any such instances will be treated as regular beans, with their configuration
 * annotations remaining undetected).
 * </ul>
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Bean
 * @see Profile
 * @see Import
 * @see ImportResource
 * @see ComponentScan
 * @see Lazy
 * @see PropertySource
 * @see AnnotationConfigApplicationContext
 * @see ConfigurationClassPostProcessor
 * @see org.springframework.core.env.Environment
 * @see org.springframework.test.context.ContextConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

	/**
	 * Explicitly specify the name of the Spring bean definition associated with the
	 * {@code @Configuration} class. If left unspecified (the common case), a bean
	 * name will be automatically generated.
	 * <p>The custom name applies only if the {@code @Configuration} class is picked
	 * up via component scanning or supplied directly to an
	 * {@link AnnotationConfigApplicationContext}. If the {@code @Configuration} class
	 * is registered as a traditional XML bean definition, the name/id of the bean
	 * element will take precedence.
	 * @return the explicit component name, if any (or empty String otherwise)
	 * @see AnnotationBeanNameGenerator
	 */
	@AliasFor(annotation = Component.class)
	String value() default "";

	/**
	 * Specify whether {@code @Bean} methods should get proxied in order to enforce
	 * bean lifecycle behavior, e.g. to return shared singleton bean instances even
	 * in case of direct {@code @Bean} method calls in user code. This feature
	 * requires method interception, implemented through a runtime-generated CGLIB
	 * subclass which comes with limitations such as the configuration class and
	 * its methods not being allowed to declare {@code final}.
	 * <p>The default is {@code true}, allowing for 'inter-bean references' via direct
	 * method calls within the configuration class as well as for external calls to
	 * this configuration's {@code @Bean} methods, e.g. from another configuration class.
	 * If this is not needed since each of this particular configuration's {@code @Bean}
	 * methods is self-contained and designed as a plain factory method for container use,
	 * switch this flag to {@code false} in order to avoid CGLIB subclass processing.
	 * <p>Turning off bean method interception effectively processes {@code @Bean}
	 * methods individually like when declared on non-{@code @Configuration} classes,
	 * a.k.a. "@Bean Lite Mode" (see {@link Bean @Bean's javadoc}). It is therefore
	 * behaviorally equivalent to removing the {@code @Configuration} stereotype.
	 * @since 5.2
	 */
	boolean proxyBeanMethods() default true;

}
