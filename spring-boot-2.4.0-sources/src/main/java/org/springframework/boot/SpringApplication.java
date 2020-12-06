/*
 * Copyright 2012-2020 the original author or authors.
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

import java.lang.reflect.Constructor;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * 20201130
 * A. 该类可用于从javamain方法引导和启动Spring应用程序。默认情况下，类将执行以下步骤来引导应用程序：
 *      a. 创建适当的{@linkapplicationContext}实例（取决于您的类路径）
 *      b. 注册{@link CommandLinePropertySource}以将命令行参数公开为Spring属性
 *      c. 刷新应用程序上下文，加载所有单例bean\
 *      d. 触发任何{@link CommandLineRunner}bean
 * B. 在大多数情况下，可以直接从{@literal main}方法调用静态{@link #run（Class，String[]）}方法来引导应用程序：
 * 		a.
 * 			@Configuration
 *			@EnableAutoConfiguration
 *	 		public class MyApplication  {
 *
 *		   		// ... Bean definitions
 *
 *		   		public static void main(String[] args) {
 *		   			SpringApplication.run(MyApplication.class, args);
 *		   		}
 *	 		}
 * C. 对于更高级的配置，可以在运行{@link SpringApplication}实例之前创建并自定义：
 * 		a.
 * 			public static void main(String[] args) {
 *   			SpringApplication application = new SpringApplication(MyApplication.class);
 *   			// ... customize application settings here
 *   			application.run(args)
 * 			}
 * D. {@link springapplication}可以从各种不同的源读取bean。通常建议使用单个{@code @Configuration}类来引导应用程序，但是，您也可以从以下位置设置
 *    {@link #getSources（）sources}：
 *    	a. {@link AnnotatedBeanDefinitionReader}要加载的完全限定类名
 *    	b. {@link XmlBeanDefinitionReader}要加载的XML资源的位置，或要由{@link GroovyBeanDefinitionReader}加载的groovy脚本的位置
 *    	c. {@link ClassPathBeanDefinitionScanner}扫描的包的名称
 * E. 配置属性也绑定到{@link springapplication}。这使得动态设置{@link springapplication}属性成为可能，就像其他源（“spring.main.sources“即一个CSV列表）
 *    指示web环境的标志（”spring.main.web-“应用程序类型=none”）或用于关闭横幅的标志（“spring.main.banner-模式=off”）。
 */
/**
 * A.
 * Class that can be used to bootstrap and launch a Spring application from a Java main
 * method. By default class will perform the following steps to bootstrap your
 * application:
 *
 * <ul>
 * <li>Create an appropriate {@link ApplicationContext} instance (depending on your
 * classpath)</li>
 * <li>Register a {@link CommandLinePropertySource} to expose command line arguments as
 * Spring properties</li>
 * <li>Refresh the application context, loading all singleton beans</li>
 * <li>Trigger any {@link CommandLineRunner} beans</li>
 * </ul>
 *
 * B.
 * In most circumstances the static {@link #run(Class, String[])} method can be called
 * directly from your {@literal main} method to bootstrap your application:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAutoConfiguration
 * public class MyApplication  {
 *
 *   // ... Bean definitions
 *
 *   public static void main(String[] args) {
 *     SpringApplication.run(MyApplication.class, args);
 *   }
 * }
 * </pre>
 *
 * C.
 * <p>
 * For more advanced configuration a {@link SpringApplication} instance can be created and
 * customized before being run:
 *
 * <pre class="code">
 * public static void main(String[] args) {
 *   SpringApplication application = new SpringApplication(MyApplication.class);
 *   // ... customize application settings here
 *   application.run(args)
 * }
 * </pre>
 *
 * D.
 * {@link SpringApplication}s can read beans from a variety of different sources. It is
 * generally recommended that a single {@code @Configuration} class is used to bootstrap
 * your application, however, you may also set {@link #getSources() sources} from:
 * <ul>
 * <li>The fully qualified class name to be loaded by
 * {@link AnnotatedBeanDefinitionReader}</li>
 * <li>The location of an XML resource to be loaded by {@link XmlBeanDefinitionReader}, or
 * a groovy script to be loaded by {@link GroovyBeanDefinitionReader}</li>
 * <li>The name of a package to be scanned by {@link ClassPathBeanDefinitionScanner}</li>
 * </ul>
 *
 * E.
 * Configuration properties are also bound to the {@link SpringApplication}. This makes it
 * possible to set {@link SpringApplication} properties dynamically, like additional
 * sources ("spring.main.sources" - a CSV list) the flag to indicate a web environment
 * ("spring.main.web-application-type=none") or the flag to switch off the banner
 * ("spring.main.banner-mode=off").
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Christian Dupuis
 * @author Stephane Nicoll
 * @author Jeremy Rickard
 * @author Craig Burke
 * @author Michael Simons
 * @author Madhura Bhave
 * @author Brian Clozel
 * @author Ethan Rubinson
 * @since 1.0.0
 * @see #run(Class, String[])
 * @see #run(Class[], String[])
 * @see #SpringApplication(Class...)
 */
public class SpringApplication {

	/**
	 * The class name of application context that will be used by default for non-web
	 * environments.
	 * @deprecated since 2.4.0 in favour of using a {@link ApplicationContextFactory}
	 */
	@Deprecated
	public static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
			+ "annotation.AnnotationConfigApplicationContext";

	/**
	 * The class name of application context that will be used by default for web
	 * environments.
	 * @deprecated since 2.4.0 in favour of using an {@link ApplicationContextFactory}
	 */
	@Deprecated
	public static final String DEFAULT_SERVLET_WEB_CONTEXT_CLASS = "org.springframework.boot."
			+ "web.servlet.context.AnnotationConfigServletWebServerApplicationContext";

	/**
	 * The class name of application context that will be used by default for reactive web
	 * environments.
	 * @deprecated since 2.4.0 in favour of using an {@link ApplicationContextFactory}
	 */
	@Deprecated
	public static final String DEFAULT_REACTIVE_WEB_CONTEXT_CLASS = "org.springframework."
			+ "boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext";

	/**
	 * Default banner location.
	 */
	public static final String BANNER_LOCATION_PROPERTY_VALUE = SpringApplicationBannerPrinter.DEFAULT_BANNER_LOCATION;

	/**
	 * Banner location property key.
	 */
	public static final String BANNER_LOCATION_PROPERTY = SpringApplicationBannerPrinter.BANNER_LOCATION_PROPERTY;

	// 20201201 awt包
	private static final String SYSTEM_PROPERTY_JAVA_AWT_HEADLESS = "java.awt.headless";

	private static final Log logger = LogFactory.getLog(SpringApplication.class);

	// 20201130 主类Class对象集合
	private Set<Class<?>> primarySources;

	private Set<String> sources = new LinkedHashSet<>();

	// 20201203 主类Class
	private Class<?> mainApplicationClass;

	// 20201203 Banner打印模式 => 控制台打印
	private Mode bannerMode = Mode.CONSOLE;

	private boolean logStartupInfo = true;

	// 20201202 允许读取命令行参数Properties属性
	private boolean addCommandLineProperties = true;

	private boolean addConversionService = true;

	private Banner banner;

	// 20201130 资源加载器
	private ResourceLoader resourceLoader;

	private BeanNameGenerator beanNameGenerator;

	// 20201201 配置环境
	private ConfigurableEnvironment environment;

	// 20201201 服务器类型 NONE、SERVLET、REACTIVE
	private WebApplicationType webApplicationType;

	private boolean headless = true;

	private boolean registerShutdownHook = true;

	private List<ApplicationContextInitializer<?>> initializers;

	private List<ApplicationListener<?>> listeners;

	// 20201202 默认Properties源
	private Map<String, Object> defaultProperties;

	// 20201201 启动引导实例结果集 -> 本质上是个回调接口, 声明了初始化BootstrapRegistry SpringBoot最初注册表实例的方法
	private List<Bootstrapper> bootstrappers;

	// 20201202 额外的配置文件集合
	private Set<String> additionalProfiles = Collections.emptySet();

	private boolean allowBeanDefinitionOverriding;

	// 20201202 是否为定制化的环境
	private boolean isCustomEnvironment = false;

	private boolean lazyInitialization = false;

	// 20201204 默认的{@link ApplicationContextFactory}实现，将为{@link WebApplicationType}创建适当的上下文。
	private ApplicationContextFactory applicationContextFactory = ApplicationContextFactory.DEFAULT;

	// 20201201 默认上下文数据收集器
	private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;

	/**
	 * 20201130
	 * 创建一个新的{@link springapplication}实例。应用程序上下文将从指定的主源加载bean（有关详细信息，请参见{@link springapplication class level}文档）。
	 * 可以在调用之前自定义实例
	 */
	/**
	 * Create a new {@link SpringApplication} instance. The application context will load
	 * beans from the specified primary sources (see {@link SpringApplication class-level}
	 * documentation for details. The instance can be customized before calling
	 * {@link #run(String...)}.
	 * @param primarySources the primary bean sources	// 20201130 主类的Class对象列表
	 * @see #run(Class, String[])
	 * @see #SpringApplication(ResourceLoader, Class...)
	 * @see #setSources(Set)
	 */
	// 20201130 通过指定的主类的Class对象列表构造SpringApplication, 不指定资源加载器
	public SpringApplication(Class<?>... primarySources) {
		this(null, primarySources);
	}

	/**
	 * 20201130
	 * 创建一个新的{@link springapplication}实例。应用程序上下文将从指定的主源加载bean（有关详细信息，请参见{@links pringapplication class level}文档）。
	 * 可以在调用之前自定义实例
	 */
	/**
	 * Create a new {@link SpringApplication} instance. The application context will load
	 * beans from the specified primary sources (see {@link SpringApplication class-level}
	 * documentation for details. The instance can be customized before calling
	 * {@link #run(String...)}.
	 * @param resourceLoader the resource loader to use		// 20201130 要使用的资源加载程序
	 * @param primarySources the primary bean sources	// 20201130 主类的Class对象列表
	 * @see #run(Class, String[])
	 * @see #setSources(Set)
	 */
	// 20201130 指定资源加载器、主类Class列表构造SpringApplication
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		this.resourceLoader = resourceLoader;

		// 20201130 主类的Class对象列表不能为空
		Assert.notNull(primarySources, "PrimarySources must not be null");

		// 20201130 去重 => 注册主类Class集合
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));

		// 20201130 从Classpath推断出服务器类型 => 注册服务器类型
		this.webApplicationType = WebApplicationType.deduceFromClasspath();

		// 20201130 根据Bootstrapper获取SpringFactories实例 => 注册启动引导实例结果集
		this.bootstrappers = new ArrayList<>(getSpringFactoriesInstances(Bootstrapper.class));

		// 20201130 根据上下文初始化器获取SpringFactories实例 => 注册上下文初始化器实例结果集
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));

		// 20201130 根据应用程序事件监听器获取SpringFactories实例 => 注册应用程序事件监听器实例结果集
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));

		// 20201130 判断获取主类的中main方法对象 => 注册主类main方法
		this.mainApplicationClass = deduceMainApplicationClass();
	}

	// 20201130 判断获取主类的中main方法对象
	private Class<?> deduceMainApplicationClass() {
		try {
			StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				if ("main".equals(stackTraceElement.getMethodName())) {
					return Class.forName(stackTraceElement.getClassName());
				}
			}
		}
		catch (ClassNotFoundException ex) {
			// Swallow and continue
		}
		return null;
	}

	/**
	 * Run the Spring application, creating and refreshing a new
	 * {@link ApplicationContext}.
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return a running {@link ApplicationContext} // 20201201 返回一个应用程序上下文对象
	 */
	// 20201201 运行Spring应用程序，创建并刷新一个新的{@link ApplicationContext}.
	public ConfigurableApplicationContext run(String... args) {
		// 20201201 构造纳秒计时器
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		// 20201201 构造Springboot最初上下文对象实例
		DefaultBootstrapContext bootstrapContext = createBootstrapContext();

		// 20201201 上下文配置接口实例初始化
		ConfigurableApplicationContext context = null;

		// 20201201 无界面时的配置
		configureHeadlessProperty();

		// 20201201 获取Spring上下文启动监听器实例集合
		SpringApplicationRunListeners listeners = getRunListeners(args);

		// 20201201 启动Springboot, 记录每步操作, 每步启动监听器, 启动主类
		listeners.starting(bootstrapContext, this.mainApplicationClass);
		try {
			// 20201201 构造默认上下文参数访问器
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);

			// 20201201 准备环境
			ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);

			// 20201203 设置spring.beaninfo.ignore属性值为true, 跳过对{@code beaninfo}类的搜索
			configureIgnoreBeanInfo(environment);

			// 20201203 打印Springboot横幅
			Banner printedBanner = printBanner(environment);

			// 20201205 创建 AnnotationConfigServletWebServerApplicationContext ServletWeb应用程序配置上下文
			context = createApplicationContext();

			// 20201206 GenericApplicationContext.setApplicationStartup(), 为ServletWeb上下文设置应用程序启动指标
			context.setApplicationStartup(this.applicationStartup);

			prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}

	// 20201201 构造Springboot最初上下文对象实例
	private DefaultBootstrapContext createBootstrapContext() {
		DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();

		// 20201201 遍历启动引导实例结果集, 初始化pringboot最初注册表
		this.bootstrappers.forEach((initializer) -> initializer.intitialize(bootstrapContext));

		// 20201201 返回注册好的Springboot最初上下文对象
		return bootstrapContext;
	}

	// 20201201 准备环境
	private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
			DefaultBootstrapContext bootstrapContext, ApplicationArguments applicationArguments) {
		// Create and configure the environment
		// 20201201 创建和配置环境
		ConfigurableEnvironment environment = getOrCreateEnvironment();

		// 20201202 配置环境 -> 命令行环境 & profile属性对应的环境
		configureEnvironment(environment, applicationArguments.getSourceArgs());

		// 20201202 环境绑定属性源 -> 添加Spring属性源
		ConfigurationPropertySources.attach(environment);

		// 20201202 监听器执行环境准备完毕事件
		listeners.environmentPrepared(bootstrapContext, environment);

		// 20201202 移动“defaultProperties”属性源，使其成为给定{@link ConfigurableEnvironment}中的最后一个源。
		DefaultPropertiesPropertySource.moveToEnd(environment);

		// 20201202 为环境配置额外的配置文件 -> profiles: spring.profiles.active
		configureAdditionalProfiles(environment);

		// 20201202 环境绑定"spring.main"属性, 绑定失败会抛出绑定异常
		bindToSpringApplication(environment);

		// 20201202 如果是定制化的环境
		if (!this.isCustomEnvironment) {
			// 20201203 这里则转换为Servlet类型的环境
			environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment, deduceEnvironmentClass());
		}

		// 20201203 环境绑定属性源 -> 添加Spring属性源
		ConfigurationPropertySources.attach(environment);

		// 20201203 返回准备好的环境
		return environment;
	}

	// 20201203 获取环境的Class类型
	private Class<? extends StandardEnvironment> deduceEnvironmentClass() {
		switch (this.webApplicationType) {
		case SERVLET:
			// 20201203 SERVLET类型则返回StandardServletEnvironment.clas
			return StandardServletEnvironment.class;
		case REACTIVE:
			return StandardReactiveWebEnvironment.class;
		default:
			return StandardEnvironment.class;
		}
	}

	private void prepareContext(DefaultBootstrapContext bootstrapContext, ConfigurableApplicationContext context,
			ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments, Banner printedBanner) {
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
		applyInitializers(context);
		listeners.contextPrepared(context);
		bootstrapContext.close(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		// Add boot specific singleton beans
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		if (this.lazyInitialization) {
			context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
		}
		// Load the sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
		listeners.contextLoaded(context);
	}

	private void refreshContext(ConfigurableApplicationContext context) {
		if (this.registerShutdownHook) {
			try {
				context.registerShutdownHook();
			}
			catch (AccessControlException ex) {
				// Not allowed in some environments.
			}
		}
		refresh((ApplicationContext) context);
	}

	private void configureHeadlessProperty() {
		System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS,
				System.getProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
	}

	// 20201201 获取Spring上下文启动监听器实例集合
	private SpringApplicationRunListeners getRunListeners(String[] args) {
		Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };

		// 20201201 构造Spring上下文启动监听器实例集合
		return new SpringApplicationRunListeners(logger,

				// 20201201 获取org.springframework.boot.context.event.EventPublishingRunListener Springboot启动监听器实例
				getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args),

				// 20201201 默认上下文数据收集器
				this.applicationStartup);
	}

	// 20201130 根据Bootstrapper获取SpringFactories实例
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
		return getSpringFactoriesInstances(type, new Class<?>[] {});
	}

	// 20201130 根据类型、Class列表、参数列表获取SpringFactories实例
	// 20201130 构造SpringApplication的factoryTypeName类型: Bootstrapper、ApplicationContextInitializer、ApplicationListener
	private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		// 20201130 获取类加载器
		ClassLoader classLoader = getClassLoader();

		// Use names and ensure unique to protect against duplicates // 20201130 使用名称并确保唯一以防止重复, 加载工厂类实例名称
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));

		// 20201130 创建工厂类实例 -> 获取工厂类实例结果集
		List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);

		// 20201130 对SpringFactories实例进行注解排序
		AnnotationAwareOrderComparator.sort(instances);

		// 20201130 返回排序后的SpringFactories实例结果集
		return instances;
	}

	// 20201130 创建工厂类实例
	@SuppressWarnings("unchecked")
	private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
			ClassLoader classLoader, Object[] args, Set<String> names) {
		// 20201201 工厂类实例集合
		List<T> instances = new ArrayList<>(names.size());

		// 20201201 遍历每个工厂类名称
		for (String name : names) {
			try {
				// 20201201 获取工厂类Class对象
				Class<?> instanceClass = ClassUtils.forName(name, classLoader);

				// 20201201 如果instanceClass不是type的子类型, 则抛出异常
				Assert.isAssignable(type, instanceClass);

				// 20201201 根据main方法参数获取工厂类对应的构造方法
				Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);

				// 20201201 使用main方法参数值进行实例化工厂, 没指定时使用基础类型默认值
				T instance = (T) BeanUtils.instantiateClass(constructor, args);

				// 20201201 实例添加到结果集合中
				instances.add(instance);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Cannot instantiate " + type + " : " + name, ex);
			}
		}

		// 20201201 返回实例结果集
		return instances;
	}

	// 20201201 创建和配置环境
	private ConfigurableEnvironment getOrCreateEnvironment() {
		if (this.environment != null) {
			// 20201201 注册配置环境
			return this.environment;
		}

		// 20201201 根据服务器类型启动环境
		switch (this.webApplicationType) {
		case SERVLET:
			// 20201201 启动标准Servlet环境 => 提供Servlet所有实例 & 配置环境
			return new StandardServletEnvironment();
		case REACTIVE:
			return new StandardReactiveWebEnvironment();
		default:
			return new StandardEnvironment();
		}
	}

	/**
	 * Template method delegating to
	 * {@link #configurePropertySources(ConfigurableEnvironment, String[])} and
	 * {@link #configureProfiles(ConfigurableEnvironment, String[])} in that order.
	 * Override this method for complete control over Environment customization, or one of
	 * the above for fine-grained control over property sources or profiles, respectively.
	 * @param environment this application's environment
	 * @param args arguments passed to the {@code run} method
	 * @see #configureProfiles(ConfigurableEnvironment, String[])
	 * @see #configurePropertySources(ConfigurableEnvironment, String[])
	 */
	// 20201202 模板方法委托给{@link #configurePropertySources（ConfigurableEnvironment，String[]）
	// 20201202 和{@link #configureProfiles（ConfigurableEnvironment，String[]）}的模板方法。重写此方法以实现对环境自定义的完全控制，
	// 20201202 或重写上述方法之一以分别对属性源或配置文件进行细粒度控制。 => 配置环境: 命令行环境 & profile属性对应的环境
	protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
		// 20201202 默认为true, 需要添加property转换服务
		if (this.addConversionService) {
			// 20201202 应用程序注册表注册服务 => 配置、注册、回调通知 -> 单例, 双重检查锁
			ConversionService conversionService = ApplicationConversionService.getSharedInstance();

			// 20201202 给环境指定property转换服务
			environment.setConversionService((ConfigurableConversionService) conversionService);
		}

		// 20201202 根据args配置命令行property对象
		configurePropertySources(environment, args);

		// 20201202 配置{@code spring.profiles.active} profile环境属性
		configureProfiles(environment, args);
	}

	/**
	 * Add, remove or re-order any {@link PropertySource}s in this application's
	 * environment.
	 * @param environment this application's environment
	 * @param args arguments passed to the {@code run} method
	 * @see #configureEnvironment(ConfigurableEnvironment, String[])
	 */
	// 20201202 添加、删除或重新排序此应用程序环境中的任何{@link PropertySource}。 => 根据args配置命令行property对象
	protected void configurePropertySources(ConfigurableEnvironment environment, String[] args) {
		// 20201202 获取可变的PropertySource集合
		MutablePropertySources sources = environment.getPropertySources();

		// 20201202 如果存在默认Properties源, 则创建map类型PropertySource集合
		DefaultPropertiesPropertySource.ifNotEmpty(this.defaultProperties, sources::addLast);

		// 20201202 如果允许读取命令行参数Properties属性
		if (this.addCommandLineProperties && args.length > 0) {
			//20201202 获取{@link CommandLinePropertySource}实例的默认名称 => commandLineArgs
			String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;

			// 20201202 如果PropertySource源集合包含CommandLinePropertySource实例
			if (sources.contains(name)) {
				// 20201202 则获取该CommandLinePropertySource实例
				PropertySource<?> source = sources.get(name);

				// 20201202 初始化一个混合PropertySource对象
				CompositePropertySource composite = new CompositePropertySource(name);

				// 20201202 添加SimpleCommandLinePropertySource实例添加到链的末尾。
				composite.addPropertySource(
						// 20201202 根据main参数构造命令行参数propertySource
						new SimpleCommandLinePropertySource("springApplicationCommandLineArgs", args));

				// 20201202 将该CommandLinePropertySource实例添加到链的末尾。
				composite.addPropertySource(source);

				// 20201202 根据名称替换掉PropertySource源集合中对应的CommandLinePropertySource实例
				sources.replace(name, composite);
			}
			else {
				// 20201202 如果PropertySource源集合没包含CommandLinePropertySource实例, 则根据参数添加SimpleCommandLinePropertySource实例添加到链的末尾。
				sources.addFirst(new SimpleCommandLinePropertySource(args));
			}
		}
	}

	/**
	 * Configure which profiles are active (or active by default) for this application
	 * environment. Additional profiles may be activated during configuration file
	 * processing via the {@code spring.profiles.active} property.
	 * @param environment this application's environment
	 * @param args arguments passed to the {@code run} method
	 * @see #configureEnvironment(ConfigurableEnvironment, String[])
	 * @see org.springframework.boot.context.config.ConfigFileApplicationListener
	 */
	// 20201202 配置此应用程序环境的活动（或默认情况下是活动的）配置文件。在配置期间，可以通过{@code spring.profiles.active}属性进行profile处理。
	protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
	}

	// 20201202 配置额外的配置文件
	private void configureAdditionalProfiles(ConfigurableEnvironment environment) {
		// 20201202 额外的配置文件集合为空
		if (!CollectionUtils.isEmpty(this.additionalProfiles)) {
			// 20201202 获取有序的激活的配置文件Set集合 => spring.profiles.active
			Set<String> profiles = new LinkedHashSet<>(Arrays.asList(environment.getActiveProfiles()));

			// 20201202 如果这些配置文件没有包含那些额外的配置文件集
			if (!profiles.containsAll(this.additionalProfiles)) {
				// 20201202 则全部追加进去
				profiles.addAll(this.additionalProfiles);

				// 20201202 替换现有的配置文件集
				environment.setActiveProfiles(StringUtils.toStringArray(profiles));
			}
		}
	}

	// 20201203 设置spring.beaninfo.ignore属性值为true, 跳过对{@code beaninfo}类的搜索
	private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
		// 20201203 获取spring.beaninfo.ignore系统属性 => 如果没有配置跳过对{@code beaninfo}类的搜索
		if (System.getProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME) == null) {
			// 20201203 获取"spring.beaninfo.ignore属性值, 如果没有则设置为true
			Boolean ignore = environment.getProperty("spring.beaninfo.ignore", Boolean.class, Boolean.TRUE);

			// 20201203 设置spring.beaninfo.ignore属性值为true, 跳过对{@code beaninfo}类的搜索
			System.setProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME, ignore.toString());
		}
	}

	/**
	 * Bind the environment to the {@link SpringApplication}.
	 * @param environment the environment to bind
	 */
	// 20201202 将环境绑定到{@link springapplication}。
	protected void bindToSpringApplication(ConfigurableEnvironment environment) {
		try {
			// 20201202 构建${}占位符解析器binder, 即为"spring.main"属性绑定一个binder, 如果绑定失败则抛出异常
			Binder.get(environment).bind("spring.main",
					// 20201202 构造Bindable实例 -> ResolvableType实例 & 开箱类型 & 无注释
					Bindable.ofInstance(this)
			);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
	}

	// 20201203 打印Springboot横幅
	private Banner printBanner(ConfigurableEnvironment environment) {
		// 20201203 如果Banner打印模式为关闭状态, 则直接返回不做处理
		if (this.bannerMode == Mode.OFF) {
			return null;
		}

		// 20201203 否则获取资源加载器
		ResourceLoader resourceLoader = (this.resourceLoader != null) ? this.resourceLoader : new DefaultResourceLoader(null);

		// 20201203 构建打印横幅实现类
		SpringApplicationBannerPrinter bannerPrinter = new SpringApplicationBannerPrinter(resourceLoader, this.banner);

		// 20201203 如果Banner打印模式为日志级别
		if (this.bannerMode == Mode.LOG) {
			// 20201203 获取日志Banner, 同时输出spring.banner.charset="UFT-8"
			return bannerPrinter.print(environment, this.mainApplicationClass, logger);
		}

		// 20201203 否则控制台模式打印Springboot横幅
		return bannerPrinter.print(environment, this.mainApplicationClass, System.out);
	}

	/**
	 * Strategy method used to create the {@link ApplicationContext}. By default this
	 * method will respect any explicitly set application context class or factory before
	 * falling back to a suitable default.
	 * @return the application context (not yet refreshed)	// 20201204 应用程序上下文（尚未刷新）
	 * @see #setApplicationContextClass(Class)
	 * @see #setApplicationContextFactory(ApplicationContextFactory)
	 */
	// 20201204 用于创建{@link ApplicationContext}的策略方法。默认情况下，在返回到合适的默认值之前，此方法将尊重任何显式设置的应用程序上下文类或工厂。
	protected ConfigurableApplicationContext createApplicationContext() {
		// 20201205 遵循给定的{@code webApplicationType}，为{@link SpringApplication}创建{@link ConfigurableApplicationContext}应用程序上下文
		// 20201205 -> 这里指的是AnnotationConfigServletWebServerApplicationContext
		return this.applicationContextFactory.create(this.webApplicationType);
	}

	/**
	 * Apply any relevant post processing the {@link ApplicationContext}. Subclasses can
	 * apply additional processing as required.
	 * @param context the application context
	 */
	protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
		if (this.beanNameGenerator != null) {
			context.getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
					this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			if (context instanceof GenericApplicationContext) {
				((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
			}
			if (context instanceof DefaultResourceLoader) {
				((DefaultResourceLoader) context).setClassLoader(this.resourceLoader.getClassLoader());
			}
		}
		if (this.addConversionService) {
			context.getBeanFactory().setConversionService(ApplicationConversionService.getSharedInstance());
		}
	}

	/**
	 * Apply any {@link ApplicationContextInitializer}s to the context before it is
	 * refreshed.
	 * @param context the configured ApplicationContext (not refreshed yet)
	 * @see ConfigurableApplicationContext#refresh()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void applyInitializers(ConfigurableApplicationContext context) {
		for (ApplicationContextInitializer initializer : getInitializers()) {
			Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
					ApplicationContextInitializer.class);
			Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
			initializer.initialize(context);
		}
	}

	/**
	 * Called to log startup information, subclasses may override to add additional
	 * logging.
	 * @param isRoot true if this application is the root of a context hierarchy
	 */
	protected void logStartupInfo(boolean isRoot) {
		if (isRoot) {
			new StartupInfoLogger(this.mainApplicationClass).logStarting(getApplicationLog());
		}
	}

	/**
	 * Called to log active profile information.
	 * @param context the application context
	 */
	protected void logStartupProfileInfo(ConfigurableApplicationContext context) {
		Log log = getApplicationLog();
		if (log.isInfoEnabled()) {
			String[] activeProfiles = context.getEnvironment().getActiveProfiles();
			if (ObjectUtils.isEmpty(activeProfiles)) {
				String[] defaultProfiles = context.getEnvironment().getDefaultProfiles();
				log.info("No active profile set, falling back to default profiles: "
						+ StringUtils.arrayToCommaDelimitedString(defaultProfiles));
			}
			else {
				log.info("The following profiles are active: "
						+ StringUtils.arrayToCommaDelimitedString(activeProfiles));
			}
		}
	}

	/**
	 * Returns the {@link Log} for the application. By default will be deduced.
	 * @return the application log
	 */
	protected Log getApplicationLog() {
		if (this.mainApplicationClass == null) {
			return logger;
		}
		return LogFactory.getLog(this.mainApplicationClass);
	}

	/**
	 * Load beans into the application context.
	 * @param context the context to load beans into
	 * @param sources the sources to load
	 */
	protected void load(ApplicationContext context, Object[] sources) {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
		}
		BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
		if (this.beanNameGenerator != null) {
			loader.setBeanNameGenerator(this.beanNameGenerator);
		}
		if (this.resourceLoader != null) {
			loader.setResourceLoader(this.resourceLoader);
		}
		if (this.environment != null) {
			loader.setEnvironment(this.environment);
		}
		loader.load();
	}

	/**
	 * The ResourceLoader that will be used in the ApplicationContext.
	 * @return the resourceLoader the resource loader that will be used in the
	 * ApplicationContext (or null if the default)
	 */
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * Either the ClassLoader that will be used in the ApplicationContext (if
	 * {@link #setResourceLoader(ResourceLoader) resourceLoader} is set, or the context
	 * class loader (if not null), or the loader of the Spring {@link ClassUtils} class.
	 * @return a ClassLoader (never null)
	 */
	// 20201130 将在ApplicationContext中使用的类加载器（如果设置了{@link #setResourceLoader（ResourceLoader）ResourceLoader}，
	// 20201130 或者上下文类加载器（如果不是null），或者Spring{@link ClassUtils}类的加载器。@返回类加载器（从不为空）
	public ClassLoader getClassLoader() {
		if (this.resourceLoader != null) {
			// 20201130 如果存在资源加载器则使用资源加载器
			return this.resourceLoader.getClassLoader();
		}

		// 20201130 否则获取默认的类加载器, 线程上下文类加载器 -> ClassUtils类加载器 -> 系统加载器
		return ClassUtils.getDefaultClassLoader();
	}

	/**
	 * Get the bean definition registry.
	 * @param context the application context
	 * @return the BeanDefinitionRegistry if it can be determined
	 */
	private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
		if (context instanceof BeanDefinitionRegistry) {
			return (BeanDefinitionRegistry) context;
		}
		if (context instanceof AbstractApplicationContext) {
			return (BeanDefinitionRegistry) ((AbstractApplicationContext) context).getBeanFactory();
		}
		throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
	}

	/**
	 * Factory method used to create the {@link BeanDefinitionLoader}.
	 * @param registry the bean definition registry
	 * @param sources the sources to load
	 * @return the {@link BeanDefinitionLoader} that will be used to load beans
	 */
	protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
		return new BeanDefinitionLoader(registry, sources);
	}

	/**
	 * Refresh the underlying {@link ApplicationContext}.
	 * @param applicationContext the application context to refresh
	 * @deprecated since 2.3.0 in favor of
	 * {@link #refresh(ConfigurableApplicationContext)}
	 */
	@Deprecated
	protected void refresh(ApplicationContext applicationContext) {
		Assert.isInstanceOf(ConfigurableApplicationContext.class, applicationContext);
		refresh((ConfigurableApplicationContext) applicationContext);
	}

	/**
	 * Refresh the underlying {@link ApplicationContext}.
	 * @param applicationContext the application context to refresh
	 */
	protected void refresh(ConfigurableApplicationContext applicationContext) {
		applicationContext.refresh();
	}

	/**
	 * Called after the context has been refreshed.
	 * @param context the application context
	 * @param args the application arguments
	 */
	protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
	}

	private void callRunners(ApplicationContext context, ApplicationArguments args) {
		List<Object> runners = new ArrayList<>();
		runners.addAll(context.getBeansOfType(ApplicationRunner.class).values());
		runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
		AnnotationAwareOrderComparator.sort(runners);
		for (Object runner : new LinkedHashSet<>(runners)) {
			if (runner instanceof ApplicationRunner) {
				callRunner((ApplicationRunner) runner, args);
			}
			if (runner instanceof CommandLineRunner) {
				callRunner((CommandLineRunner) runner, args);
			}
		}
	}

	private void callRunner(ApplicationRunner runner, ApplicationArguments args) {
		try {
			(runner).run(args);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to execute ApplicationRunner", ex);
		}
	}

	private void callRunner(CommandLineRunner runner, ApplicationArguments args) {
		try {
			(runner).run(args.getSourceArgs());
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to execute CommandLineRunner", ex);
		}
	}

	private void handleRunFailure(ConfigurableApplicationContext context, Throwable exception,
			SpringApplicationRunListeners listeners) {
		try {
			try {
				handleExitCode(context, exception);
				if (listeners != null) {
					listeners.failed(context, exception);
				}
			}
			finally {
				reportFailure(getExceptionReporters(context), exception);
				if (context != null) {
					context.close();
				}
			}
		}
		catch (Exception ex) {
			logger.warn("Unable to close ApplicationContext", ex);
		}
		ReflectionUtils.rethrowRuntimeException(exception);
	}

	private Collection<SpringBootExceptionReporter> getExceptionReporters(ConfigurableApplicationContext context) {
		try {
			return getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class<?>[] { ConfigurableApplicationContext.class }, context);
		}
		catch (Throwable ex) {
			return Collections.emptyList();
		}
	}

	private void reportFailure(Collection<SpringBootExceptionReporter> exceptionReporters, Throwable failure) {
		try {
			for (SpringBootExceptionReporter reporter : exceptionReporters) {
				if (reporter.reportException(failure)) {
					registerLoggedException(failure);
					return;
				}
			}
		}
		catch (Throwable ex) {
			// Continue with normal handling of the original failure
		}
		if (logger.isErrorEnabled()) {
			logger.error("Application run failed", failure);
			registerLoggedException(failure);
		}
	}

	/**
	 * Register that the given exception has been logged. By default, if the running in
	 * the main thread, this method will suppress additional printing of the stacktrace.
	 * @param exception the exception that was logged
	 */
	protected void registerLoggedException(Throwable exception) {
		SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
		if (handler != null) {
			handler.registerLoggedException(exception);
		}
	}

	private void handleExitCode(ConfigurableApplicationContext context, Throwable exception) {
		int exitCode = getExitCodeFromException(context, exception);
		if (exitCode != 0) {
			if (context != null) {
				context.publishEvent(new ExitCodeEvent(context, exitCode));
			}
			SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
			if (handler != null) {
				handler.registerExitCode(exitCode);
			}
		}
	}

	private int getExitCodeFromException(ConfigurableApplicationContext context, Throwable exception) {
		int exitCode = getExitCodeFromMappedException(context, exception);
		if (exitCode == 0) {
			exitCode = getExitCodeFromExitCodeGeneratorException(exception);
		}
		return exitCode;
	}

	private int getExitCodeFromMappedException(ConfigurableApplicationContext context, Throwable exception) {
		if (context == null || !context.isActive()) {
			return 0;
		}
		ExitCodeGenerators generators = new ExitCodeGenerators();
		Collection<ExitCodeExceptionMapper> beans = context.getBeansOfType(ExitCodeExceptionMapper.class).values();
		generators.addAll(exception, beans);
		return generators.getExitCode();
	}

	private int getExitCodeFromExitCodeGeneratorException(Throwable exception) {
		if (exception == null) {
			return 0;
		}
		if (exception instanceof ExitCodeGenerator) {
			return ((ExitCodeGenerator) exception).getExitCode();
		}
		return getExitCodeFromExitCodeGeneratorException(exception.getCause());
	}

	SpringBootExceptionHandler getSpringBootExceptionHandler() {
		if (isMainThread(Thread.currentThread())) {
			return SpringBootExceptionHandler.forCurrentThread();
		}
		return null;
	}

	private boolean isMainThread(Thread currentThread) {
		return ("main".equals(currentThread.getName()) || "restartedMain".equals(currentThread.getName()))
				&& "main".equals(currentThread.getThreadGroup().getName());
	}

	/**
	 * Returns the main application class that has been deduced or explicitly configured.
	 * @return the main application class or {@code null}
	 */
	public Class<?> getMainApplicationClass() {
		return this.mainApplicationClass;
	}

	/**
	 * Set a specific main application class that will be used as a log source and to
	 * obtain version information. By default the main application class will be deduced.
	 * Can be set to {@code null} if there is no explicit application class.
	 * @param mainApplicationClass the mainApplicationClass to set or {@code null}
	 */
	public void setMainApplicationClass(Class<?> mainApplicationClass) {
		this.mainApplicationClass = mainApplicationClass;
	}

	/**
	 * Returns the type of web application that is being run.
	 * @return the type of web application
	 * @since 2.0.0
	 */
	public WebApplicationType getWebApplicationType() {
		return this.webApplicationType;
	}

	/**
	 * Sets the type of web application to be run. If not explicitly set the type of web
	 * application will be deduced based on the classpath.
	 * @param webApplicationType the web application type
	 * @since 2.0.0
	 */
	public void setWebApplicationType(WebApplicationType webApplicationType) {
		Assert.notNull(webApplicationType, "WebApplicationType must not be null");
		this.webApplicationType = webApplicationType;
	}

	/**
	 * Sets if bean definition overriding, by registering a definition with the same name
	 * as an existing definition, should be allowed. Defaults to {@code false}.
	 * @param allowBeanDefinitionOverriding if overriding is allowed
	 * @since 2.1.0
	 * @see DefaultListableBeanFactory#setAllowBeanDefinitionOverriding(boolean)
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Sets if beans should be initialized lazily. Defaults to {@code false}.
	 * @param lazyInitialization if initialization should be lazy
	 * @since 2.2
	 * @see BeanDefinition#setLazyInit(boolean)
	 */
	public void setLazyInitialization(boolean lazyInitialization) {
		this.lazyInitialization = lazyInitialization;
	}

	/**
	 * Sets if the application is headless and should not instantiate AWT. Defaults to
	 * {@code true} to prevent java icons appearing.
	 * @param headless if the application is headless
	 */
	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	/**
	 * Sets if the created {@link ApplicationContext} should have a shutdown hook
	 * registered. Defaults to {@code true} to ensure that JVM shutdowns are handled
	 * gracefully.
	 * @param registerShutdownHook if the shutdown hook should be registered
	 */
	public void setRegisterShutdownHook(boolean registerShutdownHook) {
		this.registerShutdownHook = registerShutdownHook;
	}

	/**
	 * Sets the {@link Banner} instance which will be used to print the banner when no
	 * static banner file is provided.
	 * @param banner the Banner instance to use
	 */
	public void setBanner(Banner banner) {
		this.banner = banner;
	}

	/**
	 * Sets the mode used to display the banner when the application runs. Defaults to
	 * {@code Banner.Mode.CONSOLE}.
	 * @param bannerMode the mode used to display the banner
	 */
	public void setBannerMode(Mode bannerMode) {
		this.bannerMode = bannerMode;
	}

	/**
	 * Sets if the application information should be logged when the application starts.
	 * Defaults to {@code true}.
	 * @param logStartupInfo if startup info should be logged.
	 */
	public void setLogStartupInfo(boolean logStartupInfo) {
		this.logStartupInfo = logStartupInfo;
	}

	/**
	 * Sets if a {@link CommandLinePropertySource} should be added to the application
	 * context in order to expose arguments. Defaults to {@code true}.
	 * @param addCommandLineProperties if command line arguments should be exposed
	 */
	public void setAddCommandLineProperties(boolean addCommandLineProperties) {
		this.addCommandLineProperties = addCommandLineProperties;
	}

	/**
	 * Sets if the {@link ApplicationConversionService} should be added to the application
	 * context's {@link Environment}.
	 * @param addConversionService if the application conversion service should be added
	 * @since 2.1.0
	 */
	public void setAddConversionService(boolean addConversionService) {
		this.addConversionService = addConversionService;
	}

	/**
	 * Adds a {@link Bootstrapper} that can be used to initialize the
	 * {@link BootstrapRegistry}.
	 * @param bootstrapper the bootstraper
	 * @since 2.4.0
	 */
	public void addBootstrapper(Bootstrapper bootstrapper) {
		Assert.notNull(bootstrapper, "Bootstrapper must not be null");
		this.bootstrappers.add(bootstrapper);
	}

	/**
	 * Set default environment properties which will be used in addition to those in the
	 * existing {@link Environment}.
	 * @param defaultProperties the additional properties to set
	 */
	public void setDefaultProperties(Map<String, Object> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	/**
	 * Convenient alternative to {@link #setDefaultProperties(Map)}.
	 * @param defaultProperties some {@link Properties}
	 */
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = new HashMap<>();
		for (Object key : Collections.list(defaultProperties.propertyNames())) {
			this.defaultProperties.put((String) key, defaultProperties.get(key));
		}
	}

	/**
	 * Set additional profile values to use (on top of those set in system or command line
	 * properties).
	 * @param profiles the additional profiles to set
	 */
	public void setAdditionalProfiles(String... profiles) {
		this.additionalProfiles = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(profiles)));
	}

	/**
	 * Return an immutable set of any additional profiles in use.
	 * @return the additional profiles
	 */
	public Set<String> getAdditionalProfiles() {
		return this.additionalProfiles;
	}

	/**
	 * Sets the bean name generator that should be used when generating bean names.
	 * @param beanNameGenerator the bean name generator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	/**
	 * Sets the underlying environment that should be used with the created application
	 * context.
	 * @param environment the environment
	 */
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.isCustomEnvironment = true;
		this.environment = environment;
	}

	/**
	 * Add additional items to the primary sources that will be added to an
	 * ApplicationContext when {@link #run(String...)} is called.
	 * <p>
	 * The sources here are added to those that were set in the constructor. Most users
	 * should consider using {@link #getSources()}/{@link #setSources(Set)} rather than
	 * calling this method.
	 * @param additionalPrimarySources the additional primary sources to add
	 * @see #SpringApplication(Class...)
	 * @see #getSources()
	 * @see #setSources(Set)
	 * @see #getAllSources()
	 */
	public void addPrimarySources(Collection<Class<?>> additionalPrimarySources) {
		this.primarySources.addAll(additionalPrimarySources);
	}

	/**
	 * Returns a mutable set of the sources that will be added to an ApplicationContext
	 * when {@link #run(String...)} is called.
	 * <p>
	 * Sources set here will be used in addition to any primary sources set in the
	 * constructor.
	 * @return the application sources.
	 * @see #SpringApplication(Class...)
	 * @see #getAllSources()
	 */
	public Set<String> getSources() {
		return this.sources;
	}

	/**
	 * Set additional sources that will be used to create an ApplicationContext. A source
	 * can be: a class name, package name, or an XML resource location.
	 * <p>
	 * Sources set here will be used in addition to any primary sources set in the
	 * constructor.
	 * @param sources the application sources to set
	 * @see #SpringApplication(Class...)
	 * @see #getAllSources()
	 */
	public void setSources(Set<String> sources) {
		Assert.notNull(sources, "Sources must not be null");
		this.sources = new LinkedHashSet<>(sources);
	}

	/**
	 * Return an immutable set of all the sources that will be added to an
	 * ApplicationContext when {@link #run(String...)} is called. This method combines any
	 * primary sources specified in the constructor with any additional ones that have
	 * been {@link #setSources(Set) explicitly set}.
	 * @return an immutable set of all sources
	 */
	public Set<Object> getAllSources() {
		Set<Object> allSources = new LinkedHashSet<>();
		if (!CollectionUtils.isEmpty(this.primarySources)) {
			allSources.addAll(this.primarySources);
		}
		if (!CollectionUtils.isEmpty(this.sources)) {
			allSources.addAll(this.sources);
		}
		return Collections.unmodifiableSet(allSources);
	}

	/**
	 * Sets the {@link ResourceLoader} that should be used when loading resources.
	 * @param resourceLoader the resource loader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Sets the type of Spring {@link ApplicationContext} that will be created. If not
	 * specified defaults to {@link #DEFAULT_SERVLET_WEB_CONTEXT_CLASS} for web based
	 * applications or {@link AnnotationConfigApplicationContext} for non web based
	 * applications.
	 * @param applicationContextClass the context class to set
	 * @deprecated since 2.4.0 in favor of
	 * {@link #setApplicationContextFactory(ApplicationContextFactory)}
	 */
	@Deprecated
	public void setApplicationContextClass(Class<? extends ConfigurableApplicationContext> applicationContextClass) {
		this.webApplicationType = WebApplicationType.deduceFromApplicationContext(applicationContextClass);
		this.applicationContextFactory = ApplicationContextFactory.ofContextClass(applicationContextClass);
	}

	/**
	 * Sets the factory that will be called to create the application context. If not set,
	 * defaults to a factory that will create
	 * {@link AnnotationConfigServletWebServerApplicationContext} for servlet web
	 * applications, {@link AnnotationConfigReactiveWebServerApplicationContext} for
	 * reactive web applications, and {@link AnnotationConfigApplicationContext} for
	 * non-web applications.
	 * @param applicationContextFactory the factory for the context
	 * @since 2.4.0
	 */
	public void setApplicationContextFactory(ApplicationContextFactory applicationContextFactory) {
		this.applicationContextFactory = (applicationContextFactory != null) ? applicationContextFactory
				: ApplicationContextFactory.DEFAULT;
	}

	/**
	 * Sets the {@link ApplicationContextInitializer} that will be applied to the Spring
	 * {@link ApplicationContext}.
	 * @param initializers the initializers to set
	 */
	public void setInitializers(Collection<? extends ApplicationContextInitializer<?>> initializers) {
		this.initializers = new ArrayList<>(initializers);
	}

	/**
	 * Add {@link ApplicationContextInitializer}s to be applied to the Spring
	 * {@link ApplicationContext}.
	 * @param initializers the initializers to add
	 */
	public void addInitializers(ApplicationContextInitializer<?>... initializers) {
		this.initializers.addAll(Arrays.asList(initializers));
	}

	/**
	 * Returns read-only ordered Set of the {@link ApplicationContextInitializer}s that
	 * will be applied to the Spring {@link ApplicationContext}.
	 * @return the initializers
	 */
	public Set<ApplicationContextInitializer<?>> getInitializers() {
		return asUnmodifiableOrderedSet(this.initializers);
	}

	/**
	 * Sets the {@link ApplicationListener}s that will be applied to the SpringApplication
	 * and registered with the {@link ApplicationContext}.
	 * @param listeners the listeners to set
	 */
	public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
		this.listeners = new ArrayList<>(listeners);
	}

	/**
	 * Add {@link ApplicationListener}s to be applied to the SpringApplication and
	 * registered with the {@link ApplicationContext}.
	 * @param listeners the listeners to add
	 */
	public void addListeners(ApplicationListener<?>... listeners) {
		this.listeners.addAll(Arrays.asList(listeners));
	}

	/**
	 * Returns read-only ordered Set of the {@link ApplicationListener}s that will be
	 * applied to the SpringApplication and registered with the {@link ApplicationContext}
	 * .
	 * @return the listeners
	 */
	public Set<ApplicationListener<?>> getListeners() {
		return asUnmodifiableOrderedSet(this.listeners);
	}

	/**
	 * Set the {@link ApplicationStartup} to use for collecting startup metrics.
	 * @param applicationStartup the application startup to use
	 */
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		this.applicationStartup = (applicationStartup != null) ? applicationStartup : ApplicationStartup.DEFAULT;
	}

	/**
	 * Returns the {@link ApplicationStartup} used for collecting startup metrics.
	 * @return the application startup
	 */
	public ApplicationStartup getApplicationStartup() {
		return this.applicationStartup;
	}

	/**
	 * Static helper that can be used to run a {@link SpringApplication} from the
	 * specified source using default settings.
	 * @param primarySource the primary source to load  // 20201130 要加载的主源
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext} // 20201130 返回一个正在运行的{@link ApplicationContext}
	 */
	// 20201130 静态帮助器，可用于使用默认设置从指定源运行{@linkspringapplication}。
	public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
		// 20201130 args封装成args数组
		return run(new Class<?>[] { primarySource }, args);
	}

	/**
	 * Static helper that can be used to run a {@link SpringApplication} from the
	 * specified sources using default settings and user supplied arguments.
	 * @param primarySources the primary sources to load    // 20201130 要加载的主源
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return the running {@link ApplicationContext}   // 20201130 返回一个正在运行的{@link ApplicationContext}
	 */
	// 20201130 静态帮助器，可用于使用默认设置和用户提供的参数从指定的源运行{@linkspringapplication}。 => args数组参数
	public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
		// 20201130 使用args参数启动
		return new SpringApplication(primarySources).run(args);
	}

	/**
	 * A basic main that can be used to launch an application. This method is useful when
	 * application sources are defined via a {@literal --spring.main.sources} command line
	 * argument.
	 * <p>
	 * Most developers will want to define their own main method and call the
	 * {@link #run(Class, String...) run} method instead.
	 * @param args command line arguments
	 * @throws Exception if the application cannot be started
	 * @see SpringApplication#run(Class[], String[])
	 * @see SpringApplication#run(Class, String...)
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(new Class<?>[0], args);
	}

	/**
	 * Static helper that can be used to exit a {@link SpringApplication} and obtain a
	 * code indicating success (0) or otherwise. Does not throw exceptions but should
	 * print stack traces of any encountered. Applies the specified
	 * {@link ExitCodeGenerator} in addition to any Spring beans that implement
	 * {@link ExitCodeGenerator}. In the case of multiple exit codes the highest value
	 * will be used (or if all values are negative, the lowest value will be used)
	 * @param context the context to close if possible
	 * @param exitCodeGenerators exist code generators
	 * @return the outcome (0 if successful)
	 */
	public static int exit(ApplicationContext context, ExitCodeGenerator... exitCodeGenerators) {
		Assert.notNull(context, "Context must not be null");
		int exitCode = 0;
		try {
			try {
				ExitCodeGenerators generators = new ExitCodeGenerators();
				Collection<ExitCodeGenerator> beans = context.getBeansOfType(ExitCodeGenerator.class).values();
				generators.addAll(exitCodeGenerators);
				generators.addAll(beans);
				exitCode = generators.getExitCode();
				if (exitCode != 0) {
					context.publishEvent(new ExitCodeEvent(context, exitCode));
				}
			}
			finally {
				close(context);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			exitCode = (exitCode != 0) ? exitCode : 1;
		}
		return exitCode;
	}

	private static void close(ApplicationContext context) {
		if (context instanceof ConfigurableApplicationContext) {
			ConfigurableApplicationContext closable = (ConfigurableApplicationContext) context;
			closable.close();
		}
	}

	private static <E> Set<E> asUnmodifiableOrderedSet(Collection<E> elements) {
		List<E> list = new ArrayList<>(elements);
		list.sort(AnnotationAwareOrderComparator.INSTANCE);
		return new LinkedHashSet<>(list);
	}

}
