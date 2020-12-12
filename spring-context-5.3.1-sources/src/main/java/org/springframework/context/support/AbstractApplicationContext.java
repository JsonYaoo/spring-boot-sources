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

package org.springframework.context.support;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.ResolvableType;
import org.springframework.core.SpringProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 20201204
 * A. org.springframework.context.ApplicationContext接口的抽象实现。不要求用于配置的存储类型；简单地实现通用上下文功能。使用模板方法设计模式，需要具体的子类来实现抽象方法。
 * B. 与普通BeanFactory相比，ApplicationContext应该检测其内部bean工厂中定义的特殊bean：因此，此类自动注册
 *    org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessors，
 *    org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessors和
 *    org.springframework.context.ApplicationListener ApplicationListeners，它们在上下文中定义为bean。
 * C. {@link org.springframework.context.MessageSource}也可以作为Bean在上下文中提供，名称为“ messageSource”。 否则，将消息解析委托给父上下文。
 *    此外，可以在上下文中将用于应用程序事件的多播器作为类型为{@link org.springframework.context.event.ApplicationEventMulticaster}的
 *    “applicationEventMulticaster” bean提供。 否则，将使用{@link org.springframework.context.event.SimpleApplicationEventMulticaster}类型的默认多播器。
 * D. 通过扩展{@link org.springframework.core.io.DefaultResourceLoader}来实现资源加载。 因此，除非在子类中重写{@link #getResourceByPath}方法，
 *    否则将非URL资源路径视为类路径资源（支持包含包路径的完整类路径资源名称，例如“ mypackage / myresource.dat”）。
 */
/**
 * A.
 * Abstract implementation of the {@link org.springframework.context.ApplicationContext}
 * interface. Doesn't mandate the type of storage used for configuration; simply
 * implements common context functionality. Uses the Template Method design pattern,
 * requiring concrete subclasses to implement abstract methods.
 *
 * B.
 * <p>In contrast to a plain BeanFactory, an ApplicationContext is supposed
 * to detect special beans defined in its internal bean factory:
 * Therefore, this class automatically registers
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessors},
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessors},
 * and {@link org.springframework.context.ApplicationListener ApplicationListeners}
 * which are defined as beans in the context.
 *
 * C.
 * <p>A {@link org.springframework.context.MessageSource} may also be supplied
 * as a bean in the context, with the name "messageSource"; otherwise, message
 * resolution is delegated to the parent context. Furthermore, a multicaster
 * for application events can be supplied as an "applicationEventMulticaster" bean
 * of type {@link org.springframework.context.event.ApplicationEventMulticaster}
 * in the context; otherwise, a default multicaster of type
 * {@link org.springframework.context.event.SimpleApplicationEventMulticaster} will be used.
 *
 * D.
 * <p>Implements resource loading by extending
 * {@link org.springframework.core.io.DefaultResourceLoader}.
 * Consequently treats non-URL resource paths as class path resources
 * (supporting full class path resource names that include the package path,
 * e.g. "mypackage/myresource.dat"), unless the {@link #getResourceByPath}
 * method is overridden in a subclass.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @author Sebastien Deleuze
 * @author Brian Clozel
 * @since January 21, 2001
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.MessageSource
 */
// 20201204 ApplicationContext接口的抽象实现。简单地实现通用上下文功能 -> 此类自动注册 BeanFactoryPostProcessor & BeanPostProcessor & ApplicationListener
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

	/**
	 * Name of the MessageSource bean in the factory.
	 * If none is supplied, message resolution is delegated to the parent.
	 * @see MessageSource
	 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

	/**
	 * Name of the LifecycleProcessor bean in the factory.
	 * If none is supplied, a DefaultLifecycleProcessor is used.
	 * @see org.springframework.context.LifecycleProcessor
	 * @see org.springframework.context.support.DefaultLifecycleProcessor
	 */
	public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";

	/**
	 * Name of the ApplicationEventMulticaster bean in the factory.
	 * If none is supplied, a default SimpleApplicationEventMulticaster is used.
	 * @see org.springframework.context.event.ApplicationEventMulticaster
	 * @see org.springframework.context.event.SimpleApplicationEventMulticaster
	 */
	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

	/**
	 * Boolean flag controlled by a {@code spring.spel.ignore} system property that instructs Spring to
	 * ignore SpEL, i.e. to not initialize the SpEL infrastructure.
	 * <p>The default is "false".
	 */
	// 20201210 由{@code spring.spel.ignore}系统属性控制的布尔值标志，指示Spring忽略SpEL，即不初始化SpEL基础架构。 默认值为“ false”。
	private static final boolean shouldIgnoreSpel = SpringProperties.getFlag("spring.spel.ignore");

	/**
	 * Whether this environment lives within a native image.
	 * Exposed as a private static field rather than in a {@code NativeImageDetector.inNativeImage()} static method due to https://github.com/oracle/graal/issues/2594.
	 * @see <a href="https://github.com/oracle/graal/blob/master/sdk/src/org.graalvm.nativeimage/src/org/graalvm/nativeimage/ImageInfo.java">ImageInfo.java</a>
	 */
	// 20201210 此环境是否存在于本地映像中。-> "org.graalvm.nativeimage.imagecode
	// 20201210 由于https://github.com/oracle/graal/issues/2594，它作为一个私有静态字段而不是在{@code NativeImageDetector.inNativeImage（）}静态方法中公开。
	private static final boolean IN_NATIVE_IMAGE = (System.getProperty("org.graalvm.nativeimage.imagecode") != null);


	static {
		// Eagerly load the ContextClosedEvent class to avoid weird classloader issues
		// on application shutdown in WebLogic 8.1. (Reported by Dustin Woods.)
		ContextClosedEvent.class.getName();
	}


	/** Logger used by this class. Available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Unique id for this context, if any. */
	private String id = ObjectUtils.identityToString(this);

	/** Display name. */
	private String displayName = ObjectUtils.identityToString(this);

	// 20201208 父级上下文
	/** Parent context. */
	@Nullable
	private ApplicationContext parent;

	/** Environment used by this context. */
	// 20201204 此上下文使用的环境。
	@Nullable
	private ConfigurableEnvironment environment;

	// 20201208 工厂修改器列表, BeanFactoryPostProcessors应用于刷新。
	/** BeanFactoryPostProcessors to apply on refresh. */
	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

	/** System time in milliseconds when this context started. */
	private long startupDate;

	// 20201210 指示此上下文当前是否处于活动状态的标志。
	/** Flag that indicates whether this context is currently active. */
	private final AtomicBoolean active = new AtomicBoolean();

	// 20201210 指示此上下文是否已经关闭的标志。
	/** Flag that indicates whether this context has been closed already. */
	private final AtomicBoolean closed = new AtomicBoolean();

	// 20201210 同步监视器，用于“刷新”和“销毁”。
	/** Synchronization monitor for the "refresh" and "destroy". */
	private final Object startupShutdownMonitor = new Object();

	// 20201210 虚拟机关闭挂钩 -> 如果已注册，请参考JVM关闭钩子。
	/** Reference to the JVM shutdown hook, if registered. */
	@Nullable
	private Thread shutdownHook;

	/** ResourcePatternResolver used by this context. */
	private ResourcePatternResolver resourcePatternResolver;

	// 20201210 LifecycleProcessor，用于在此上下文中管理bean的生命周期。
	/** LifecycleProcessor for managing the lifecycle of beans within this context. */
	@Nullable
	private LifecycleProcessor lifecycleProcessor;

	/** MessageSource we delegate our implementation of this interface to. */
	@Nullable
	private MessageSource messageSource;

	/** Helper class used in event publishing. */
	@Nullable
	private ApplicationEventMulticaster applicationEventMulticaster;

	/** Application startup metrics. **/
	// 20201205 应用程序启动指标 -> 默认DefaultApplicationStartup
	private ApplicationStartup applicationStartup = ApplicationStartup.DEFAULT;

	/** Statically specified listeners. */
	// 20201210 静态指定的侦听器。
	private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

	// 20201210 刷新之前已注册本地侦听器。
	/** Local listeners registered before refresh. */
	@Nullable
	private Set<ApplicationListener<?>> earlyApplicationListeners;

	/** ApplicationEvents published before the multicaster setup. */
	@Nullable
	private Set<ApplicationEvent> earlyApplicationEvents;


	/**
	 * Create a new AbstractApplicationContext with no parent.
	 */
	public AbstractApplicationContext() {
		this.resourcePatternResolver = getResourcePatternResolver();
	}

	/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractApplicationContext(@Nullable ApplicationContext parent) {
		this();
		setParent(parent);
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContext interface
	//---------------------------------------------------------------------

	/**
	 * Set the unique id of this application context.
	 * <p>Default is the object id of the context instance, or the name
	 * of the context bean if the context is itself defined as a bean.
	 * @param id the unique id of the context
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getApplicationName() {
		return "";
	}

	/**
	 * Set a friendly name for this context.
	 * Typically done during initialization of concrete context implementations.
	 * <p>Default is the object id of the context instance.
	 */
	public void setDisplayName(String displayName) {
		Assert.hasLength(displayName, "Display name must not be empty");
		this.displayName = displayName;
	}

	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context (never {@code null})
	 */
	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * (that is, this context is the root of the context hierarchy).
	 */
	// 20201208 返回父级上下文，如果没有父级，则返回{@code null}（也就是说，此上下文是上下文层次结构的根）。
	@Override
	@Nullable
	public ApplicationContext getParent() {
		return this.parent;
	}

	/**
	 * 20201206
	 * A. 为此应用程序上下文设置{@code Environment}。
	 * B. 默认值由{@link #createEnvironment（）}确定。 用此方法替换默认值是一个选项，但也应考虑通过{@link #getEnvironment（）}进行配置。 无论哪种情况，都应在
	 *    之前使用 {@link #refresh（）}进行此类修改。
	 */
	/**
	 * A.
	 * Set the {@code Environment} for this application context.
	 *
	 * B.
	 * <p>Default value is determined by {@link #createEnvironment()}. Replacing the
	 * default with this method is one option but configuration through {@link
	 * #getEnvironment()} should also be considered. In either case, such modifications
	 * should be performed <em>before</em> {@link #refresh()}.
	 *
	 * @see org.springframework.context.support.AbstractApplicationContext#createEnvironment
	 */
	// 20201206 为此应用程序上下文设置{@code Environment}
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		// 20201206 注册此上下文使用的环境
		this.environment = environment;
	}

	/**
	 * 20201204
	 * A. 以可配置的形式返回此应用程序上下文的{@code Environment}，以便进行进一步的自定义。
	 * B. 如果未指定，则默认环境将通过{@link #createEnvironment（）}初始化。
	 */
	/**
	 * A.
	 * Return the {@code Environment} for this application context in configurable
	 * form, allowing for further customization.
	 *
	 * B.
	 * <p>If none specified, a default environment will be initialized via
	 * {@link #createEnvironment()}.
	 */
	// 20201208 返回当前环境实例
	@Override
	public ConfigurableEnvironment getEnvironment() {
		// 20201204 如果当前环境实例为null
		if (this.environment == null) {
			// 20201204 当需要构建Web环境时, 交由GenericWebApplicationContext重写createEnvironment()方法, 构造标准ServletWeb环境
			this.environment = createEnvironment();
		}

		// 20201204 否则返回当前环境实例
		return this.environment;
	}

	/**
	 * 20201204
	 * A. 创建并返回一个新的{@link StandardEnvironment}。
	 * B. 子类可以重写此方法，以提供自定义的{@link ConfigurableEnvironment}实现。
	 */
	/**
	 * A.
	 * Create and return a new {@link StandardEnvironment}.
	 *
	 * B.
	 * <p>Subclasses may override this method in order to supply
	 * a custom {@link ConfigurableEnvironment} implementation.
	 */
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardEnvironment();
	}

	/**
	 * Return this context's internal bean factory as AutowireCapableBeanFactory,
	 * if already available.
	 * @see #getBeanFactory()
	 */
	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		return getBeanFactory();
	}

	/**
	 * Return the timestamp (ms) when this context was first loaded.
	 */
	@Override
	public long getStartupDate() {
		return this.startupDate;
	}

	/**
	 * 20201210
	 * A. 将给定事件发布给所有侦听器。
	 * B. 注意：侦听器在MessageSource之后初始化，以便能够在侦听器实现中访问它。 因此，MessageSource实现无法发布事件。
	 */
	/**
	 * A.
	 * Publish the given event to all listeners.
	 *
	 * B.
	 * <p>Note: Listeners get initialized after the MessageSource, to be able
	 * to access it within listener implementations. Thus, MessageSource
	 * implementations cannot publish events.
	 * @param event the event to publish (may be application-specific or a
	 * standard framework event)
	 */
	// 20201210 将给定事件发布给所有侦听器
	@Override
	public void publishEvent(ApplicationEvent event) {
		publishEvent(event, null);
	}

	/**
	 * Publish the given event to all listeners.
	 * <p>Note: Listeners get initialized after the MessageSource, to be able
	 * to access it within listener implementations. Thus, MessageSource
	 * implementations cannot publish events.
	 * @param event the event to publish (may be an {@link ApplicationEvent}
	 * or a payload object to be turned into a {@link PayloadApplicationEvent})
	 */
	@Override
	public void publishEvent(Object event) {
		publishEvent(event, null);
	}

	/**
	 * Publish the given event to all listeners.
	 * @param event the event to publish (may be an {@link ApplicationEvent}
	 * or a payload object to be turned into a {@link PayloadApplicationEvent})
	 * @param eventType the resolved event type, if known
	 * @since 4.2
	 */
	protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");

		// Decorate event as an ApplicationEvent if necessary
		ApplicationEvent applicationEvent;
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		}
		else {
			applicationEvent = new PayloadApplicationEvent<>(this, event);
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
			}
		}

		// Multicast right now if possible - or lazily once the multicaster is initialized
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}
		else {
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}

		// Publish event via parent context as well...
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
			}
			else {
				this.parent.publishEvent(event);
			}
		}
	}

	/**
	 * Return the internal ApplicationEventMulticaster used by the context.
	 * @return the internal ApplicationEventMulticaster (never {@code null})
	 * @throws IllegalStateException if the context has not been initialized yet
	 */
	ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}

	// 20201205 注册应用程序启动指标
	@Override
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		// 20201205 applicationStartup不能为空
		Assert.notNull(applicationStartup, "applicationStartup should not be null");

		// 20201205 注册应用程序启动指标
		this.applicationStartup = applicationStartup;
	}

	@Override
	public ApplicationStartup getApplicationStartup() {
		return this.applicationStartup;
	}

	/**
	 * Return the internal LifecycleProcessor used by the context.
	 * @return the internal LifecycleProcessor (never {@code null})
	 * @throws IllegalStateException if the context has not been initialized yet
	 */
	LifecycleProcessor getLifecycleProcessor() throws IllegalStateException {
		if (this.lifecycleProcessor == null) {
			throw new IllegalStateException("LifecycleProcessor not initialized - " +
					"call 'refresh' before invoking lifecycle methods via the context: " + this);
		}
		return this.lifecycleProcessor;
	}

	/**
	 * Return the ResourcePatternResolver to use for resolving location patterns
	 * into Resource instances. Default is a
	 * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
	 * supporting Ant-style location patterns.
	 * <p>Can be overridden in subclasses, for extended resolution strategies,
	 * for example in a web environment.
	 * <p><b>Do not call this when needing to resolve a location pattern.</b>
	 * Call the context's {@code getResources} method instead, which
	 * will delegate to the ResourcePatternResolver.
	 * @return the ResourcePatternResolver for this context
	 * @see #getResources
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PathMatchingResourcePatternResolver(this);
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableApplicationContext interface
	//---------------------------------------------------------------------

	/**
	 * Set the parent of this application context.
	 * <p>The parent {@linkplain ApplicationContext#getEnvironment() environment} is
	 * {@linkplain ConfigurableEnvironment#merge(ConfigurableEnvironment) merged} with
	 * this (child) application context environment if the parent is non-{@code null} and
	 * its environment is an instance of {@link ConfigurableEnvironment}.
	 * @see ConfigurableEnvironment#merge(ConfigurableEnvironment)
	 */
	@Override
	public void setParent(@Nullable ApplicationContext parent) {
		this.parent = parent;
		if (parent != null) {
			Environment parentEnvironment = parent.getEnvironment();
			if (parentEnvironment instanceof ConfigurableEnvironment) {
				getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
			}
		}
	}

	// 20201208 添加一个新的工厂修改器BeanFactoryPostProcessor，应用于刷新
	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		// 20201208 工厂修改器不能为空
		Assert.notNull(postProcessor, "BeanFactoryPostProcessor must not be null");

		// 20201208 注册工厂修改器列表
		this.beanFactoryPostProcessors.add(postProcessor);
	}

	/**
	 * Return the list of BeanFactoryPostProcessors that will get applied
	 * to the internal BeanFactory.
	 */
	public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return this.beanFactoryPostProcessors;
	}

	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		Assert.notNull(listener, "ApplicationListener must not be null");
		if (this.applicationEventMulticaster != null) {
			this.applicationEventMulticaster.addApplicationListener(listener);
		}
		this.applicationListeners.add(listener);
	}

	/**
	 * Return the list of statically specified ApplicationListeners.
	 */
	public Collection<ApplicationListener<?>> getApplicationListeners() {
		return this.applicationListeners;
	}

	// 20201210 加载或刷新配置的持久性表示形式, 如果失败，它应该销毁已创建的单例，以避免悬挂资源
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		// 20201210 同步监视器上锁，用于“刷新”和“销毁”。
		synchronized (this.startupShutdownMonitor) {
			// 20201210 创建新步骤并标记其开始, 步骤名称描述当前操作或阶段 -> "spring.context.refresh"
			StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

			// Prepare this context for refreshing. // 20201212 备此上下文以进行刷新，设置其启动日期和活动标志以及执行属性源的任何初始化
			// 20201212 切换到活动状态、初始化上下文环境中的所有占位符属性源、验证是否存在必要属性、收集早期的ApplicationEvent
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory. // 20201212 告诉子类刷新内部BeanFactory
			// 20201212 获取在GenericApplicationContext注册的DefaultListableBeanFactory
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			// 20201212 准备beanFactory: BeanClassLoader、BeanExpressionResolver、PropertyEditorRegistrar、BeanPostProcessor、注册"environment"、"systemProperties"、"systemEnvironment"、"applicationStartup"单例
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				// 20201210 允许在上下文子类中对bean工厂进行后处理。
				postProcessBeanFactory(beanFactory);

				// 20201210 创建新步骤并标记其开始, 步骤名称描述当前操作或阶段 -> "spring.context.beans.post-process"
				StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");

				// Invoke factory processors registered as beans in the context.
				// 20201210 调用在上下文中注册为bean的工厂处理器。
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				// 20201210 注册拦截Bean创建的Bean处理器。
				registerBeanPostProcessors(beanFactory);
				beanPostProcess.end();

				// Initialize message source for this context.
				// 20201210 为此上下文初始化消息源。
				initMessageSource();

				// Initialize event multicaster for this context.
				// 20201210 为此上下文初始化事件多播器。
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				// 20201210 在特定上下文子类中初始化其他特殊bean。
				onRefresh();

				// Check for listener beans and register them.
				// 20201210 检查侦听器bean并注册它们。
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				// 20201210 实例化所有剩余的（非延迟初始化）单例。
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				// 20201210 最后一步：发布相应的事件。
				finishRefresh();
			}

			// 20201210 如果期间抛出异常
			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				// 20201210 销毁已创建的单例以避免资源悬空。
				destroyBeans();

				// Reset 'active' flag.
				// 20201210 重置“有效”标志。
				cancelRefresh(ex);

				// Propagate exception to caller.
				// 20201210 将异常传播给调用方。
				throw ex;
			}

			// 20201210 最后执行
			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				// 20201210 在Spring的核心中重置常见的自省缓存，因为我们可能不再需要单例bean的元数据...
				// 20201210 重置Spring的公共反射元数据缓存，尤其是{@link ReflectionUtils}，{@ link AnnotationUtils}，{@ link ResolvableType}和{@link CachedIntrospectionResults}缓存。
				resetCommonCaches();

				// 20201210 记录步骤的状态以及可能的其他度量，例如执行时间。
				contextRefresh.end();
			}
		}
	}

	/**
	 * Prepare this context for refreshing, setting its startup date and
	 * active flag as well as performing any initialization of property sources.
	 */
	// 20201210 准备此上下文以进行刷新，设置其启动日期和活动标志以及执行属性源的任何初始化。
	protected void prepareRefresh() {
		// Switch to active.
		// 20201210 切换到活动状态。
		this.startupDate = System.currentTimeMillis();
		this.closed.set(false);
		this.active.set(true);

		// 20201210 打印应用程序刷新日志
		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Refreshing " + this);
			}
			else {
				logger.debug("Refreshing " + getDisplayName());
			}
		}

		// Initialize any placeholder property sources in the context environment.
		// 20201210 初始化上下文环境中的所有占位符属性源, 空实现
		initPropertySources();

		// Validate that all properties marked as required are resolvable:
		// see ConfigurablePropertyResolver#setRequiredProperties
		// 20201211 验证是否存在{@link #setRequiredProperties}指定的每个属性，并将其解析为非{@code null}值 -> 如果存在找不到属性值的属性, 则抛出异常
		getEnvironment().validateRequiredProperties();

		// Store pre-refresh ApplicationListeners...
		// 20201210 存储预刷新的ApplicationListeners ... -> 收集早期的ApplicationEvent
		if (this.earlyApplicationListeners == null) {
			this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
		}
		else {
			// 20201210 将本地应用程序侦听器重置为预刷新状态。
			// Reset local application listeners to pre-refresh state.
			this.applicationListeners.clear();
			this.applicationListeners.addAll(this.earlyApplicationListeners);
		}

		// Allow for the collection of early ApplicationEvents,
		// to be published once the multicaster is available...
		// 20201210 允许收集早期的ApplicationEvent，一旦多播器可用，便会发布...
		this.earlyApplicationEvents = new LinkedHashSet<>();
	}

	/**
	 * <p>Replace any stub property sources with actual instances.
	 * @see org.springframework.core.env.PropertySource.StubPropertySource
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#initServletPropertySources
	 */
	// 20201210 用实际实例替换任何存根属性源。
	protected void initPropertySources() {
		// For subclasses: do nothing by default.
		// 20201210 对于子类：默认情况下不执行任何操作。
	}

	/**
	 * Tell the subclass to refresh the internal bean factory.
	 * @return the fresh BeanFactory instance
	 * @see #refreshBeanFactory()
	 * @see #getBeanFactory()
	 */
	// 20201210 告诉子类刷新内部BeanFactory -> 子类将创建一个新的BeanFactory并保留对其的引用，或者返回它持有的单个BeanFactory实例
	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		// 20201210 子类将创建一个新的BeanFactory并保留对其的引用，或者返回它持有的单个BeanFactory实例。
		refreshBeanFactory();

		// 20201210 子类必须在此处返回其内部BeanFactory。 他们应该有效地实现查找，以便可以重复调用它而不会影响性能。
		return getBeanFactory();
	}

	/**
	 * Configure the factory's standard context characteristics,
	 * such as the context's ClassLoader and post-processors.
	 * @param beanFactory the BeanFactory to configure
	 */
	// 20201210 配置工厂的标准上下文特征，例如上下文的ClassLoader和后处理器。
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// Tell the internal bean factory to use the context's class loader etc.
		// 20201210 告诉内部bean工厂使用上下文的类加载器等。
		// 20201212 设置类加载器以用于加载bean类, 默认值为线程上下文类加载器 -> Bean定义仅带有Bean类名
		beanFactory.setBeanClassLoader(getClassLoader());

		// 20201210 需要初始化SpEL基础架构
		if (!shouldIgnoreSpel) {
			// 20201210 为bean定义值中的表达式指定解析策略 -> SpringEL表达式
			beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
		}

		// 20201210 添加一个PropertyEditorRegistrar以应用于所有bean创建过程 -> 资源编辑注册器
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

		// Configure the bean factory with context callbacks.
		// 20201210 使用上下文回调配置bean工厂。
		// 20201210 添加一个新的上下文加工后处理器BeanPostProcessor，它将应用于该工厂创建的bean。 在出厂配置期间调用 -> 属于BeanPostProcessor
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

		// 20201210 忽略给定的依赖接口进行自动装配 -> 应用程序上下文通常会使用它来注册以其他方式解决的依赖关系
		// 20201212 beanFactory忽略 环境自觉接口 的自动装配
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);

		// 20201212 beanFactory忽略 嵌入式值处理器自觉接口 的自动装配
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);

		// 20201212 beanFactory忽略 资源加载器自觉接口 的自动装配
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);

		// 20201212 beanFactory忽略 ApplicationEventPublisher自觉接口 的自动装配
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);

		// 20201212 beanFactory忽略 MessageSource自觉接口 的自动装配
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);

		// 20201212 beanFactory忽略 ApplicationContext自觉接口 的自动装配
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

		// 20201212 beanFactory忽略 应用程序启动收集器 的自动装配
		beanFactory.ignoreDependencyInterface(ApplicationStartup.class);

		// BeanFactory interface not registered as resolvable type in a plain factory.
		// MessageSource registered (and found for autowiring) as a bean.
		// 20201212 BeanFactory接口未在普通工厂中注册为可解析类型。 MessageSource注册为Bean（并发现用于自动装配）。
		// 20201212 在beanFactory中注册一个BeanFactory类型, 值为当前beanFactory
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);

		// 20201212 在beanFactory中注册一个ResourceLoader类型, 值为当前beanFactory
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);

		// 20201212 在beanFactory中注册一个ApplicationEventPublisher类型, 值为当前beanFactory
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);

		// 20201212 在beanFactory中注册一个ApplicationContext类型, 值为当前beanFactory
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// Register early post-processor for detecting inner beans as ApplicationListeners.
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

		// Detect a LoadTimeWeaver and prepare for weaving, if found.
		// 20201212 如果发现LoadTimeWeaver，请准备编织。
		if (!IN_NATIVE_IMAGE && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			// Set a temporary ClassLoader for type matching.
			// 202012212 设置一个临时的ClassLoader以进行类型匹配。
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}

		// Register default environment beans.
		// 20201212 注册默认环境bean。
		// 20201212 如果beanFactory中不包含"environment", 则注册"environment"单例
		if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
		}

		// 20201212 如果beanFactory中不包含"systemProperties", 则注册"systemProperties"单例
		if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
		}

		// 20201212 如果beanFactory中不包含"systemEnvironment", 则注册"systemEnvironment"单例
		if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
		}

		// 20201212 如果beanFactory中不包含"applicationStartup", 则注册"applicationStartup"单例
		if (!beanFactory.containsLocalBean(APPLICATION_STARTUP_BEAN_NAME)) {
			beanFactory.registerSingleton(APPLICATION_STARTUP_BEAN_NAME, getApplicationStartup());
		}
	}

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for registering special
	 * BeanPostProcessors etc in certain ApplicationContext implementations.
	 * @param beanFactory the bean factory used by the application context
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * <p>Must be called before singleton instantiation.
	 */
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

		// Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
		// (e.g. through an @Bean method registered by ConfigurationClassPostProcessor)
		if (!IN_NATIVE_IMAGE && beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}
	}

	/**
	 * Instantiate and register all BeanPostProcessor beans,
	 * respecting explicit order if given.
	 * <p>Must be called before any instantiation of application beans.
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
	}

	/**
	 * Initialize the MessageSource.
	 * Use parent's if none defined in this context.
	 */
	protected void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// Make MessageSource aware of parent MessageSource.
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
				if (hms.getParentMessageSource() == null) {
					// Only set parent context as parent MessageSource if no parent MessageSource
					// registered already.
					hms.setParentMessageSource(getInternalParentMessageSource());
				}
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Using MessageSource [" + this.messageSource + "]");
			}
		}
		else {
			// Use empty MessageSource to be able to accept getMessage calls.
			DelegatingMessageSource dms = new DelegatingMessageSource();
			dms.setParentMessageSource(getInternalParentMessageSource());
			this.messageSource = dms;
			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + MESSAGE_SOURCE_BEAN_NAME + "' bean, using [" + this.messageSource + "]");
			}
		}
	}

	/**
	 * Initialize the ApplicationEventMulticaster.
	 * Uses SimpleApplicationEventMulticaster if none defined in the context.
	 * @see org.springframework.context.event.SimpleApplicationEventMulticaster
	 */
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster =
					beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
			SimpleApplicationEventMulticaster simpleApplicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			simpleApplicationEventMulticaster.setApplicationStartup(getApplicationStartup());
			this.applicationEventMulticaster = simpleApplicationEventMulticaster;
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "' bean, using " +
						"[" + this.applicationEventMulticaster.getClass().getSimpleName() + "]");
			}
		}
	}

	/**
	 * Initialize the LifecycleProcessor.
	 * Uses DefaultLifecycleProcessor if none defined in the context.
	 * @see org.springframework.context.support.DefaultLifecycleProcessor
	 */
	protected void initLifecycleProcessor() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor =
					beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
			if (logger.isTraceEnabled()) {
				logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
			}
		}
		else {
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
			if (logger.isTraceEnabled()) {
				logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
						"[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
			}
		}
	}

	/**
	 * Template method which can be overridden to add context-specific refresh work.
	 * Called on initialization of special beans, before instantiation of singletons.
	 * <p>This implementation is empty.
	 * @throws BeansException in case of errors
	 * @see #refresh()
	 */
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
	}

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 */
	protected void registerListeners() {
		// Register statically specified listeners first.
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// Publish early application events now that we finally have a multicaster...
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
		this.earlyApplicationEvents = null;
		if (!CollectionUtils.isEmpty(earlyEventsToProcess)) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}
	}

	/**
	 * Finish the initialization of this context's bean factory,
	 * initializing all remaining singleton beans.
	 */
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Initialize conversion service for this context.
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(
					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}

		// Register a default embedded value resolver if no bean post-processor
		// (such as a PropertyPlaceholderConfigurer bean) registered any before:
		// at this point, primarily for resolution in annotation attribute values.
		if (!beanFactory.hasEmbeddedValueResolver()) {
			beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
		}

		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
			getBean(weaverAwareName);
		}

		// Stop using the temporary ClassLoader for type matching.
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
		beanFactory.preInstantiateSingletons();
	}

	/**
	 * Finish the refresh of this context, invoking the LifecycleProcessor's
	 * onRefresh() method and publishing the
	 * {@link org.springframework.context.event.ContextRefreshedEvent}.
	 */
	@SuppressWarnings("deprecation")
	protected void finishRefresh() {
		// Clear context-level resource caches (such as ASM metadata from scanning).
		clearResourceCaches();

		// Initialize lifecycle processor for this context.
		initLifecycleProcessor();

		// Propagate refresh to lifecycle processor first.
		getLifecycleProcessor().onRefresh();

		// Publish the final event.
		publishEvent(new ContextRefreshedEvent(this));

		// Participate in LiveBeansView MBean, if active.
		if (!IN_NATIVE_IMAGE) {
			LiveBeansView.registerApplicationContext(this);
		}
	}

	/**
	 * Cancel this context's refresh attempt, resetting the {@code active} flag
	 * after an exception got thrown.
	 * @param ex the exception that led to the cancellation
	 */
	protected void cancelRefresh(BeansException ex) {
		this.active.set(false);
	}

	/**
	 * Reset Spring's common reflection metadata caches, in particular the
	 * {@link ReflectionUtils}, {@link AnnotationUtils}, {@link ResolvableType}
	 * and {@link CachedIntrospectionResults} caches.
	 * @since 4.2
	 * @see ReflectionUtils#clearCache()
	 * @see AnnotationUtils#clearCache()
	 * @see ResolvableType#clearCache()
	 * @see CachedIntrospectionResults#clearClassLoader(ClassLoader)
	 */
	// 20201210 重置Spring的公共反射元数据缓存，尤其是{@link ReflectionUtils}，{@ link AnnotationUtils}，{@ link ResolvableType}和{@link CachedIntrospectionResults}缓存。
	protected void resetCommonCaches() {
		// 20201210 清除ReflectionUtils相关类的内部方法/字段缓存。
		ReflectionUtils.clearCache();

		// 20201210 清除AnnotationUtils相关类的内部注释元数据缓存。
		AnnotationUtils.clearCache();

		// 20201210 清除内部{@code ResolvableType} / {@ code SerializableTypeWrapper}缓存。
		ResolvableType.clearCache();

		// 20201210 清除给定ClassLoader的自省缓存，删除该ClassLoader下所有类的自省结果，并从接受列表中删除ClassLoader（及其子级）。
		CachedIntrospectionResults.clearClassLoader(getClassLoader());
	}

	/**
	 * 20201210
	 * A. 在JVM运行时中注册一个关闭钩子{@linkplain Thread＃getName（）} {@code SpringContextShutdownHook}，在JVM关闭时关闭该上下文，除非当时尚未关闭它。
	 * B. 代表{@code doClose（）}进行实际的关闭过程。
	 */
	/**
	 * A.
	 * Register a shutdown hook {@linkplain Thread#getName() named}
	 * {@code SpringContextShutdownHook} with the JVM runtime, closing this
	 * context on JVM shutdown unless it has already been closed at that time.
	 *
	 * B.
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 *
	 * @see Runtime#addShutdownHook
	 * @see ConfigurableApplicationContext#SHUTDOWN_HOOK_THREAD_NAME
	 * @see #close()
	 * @see #doClose()
	 */
	// 20201210 在JVM运行时中注册一个关闭钩子{@linkplain Thread＃getName（）} {@code SpringContextShutdownHook}，在JVM关闭时关闭该上下文，除非当时尚未关闭它。
	@Override
	public void registerShutdownHook() {
		// 20201210 如果虚拟机关闭挂钩还没注册
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			// 20201210 构建"SpringContextShutdownHook"线程 -> 该虚拟机关闭挂钩
			this.shutdownHook = new Thread(SHUTDOWN_HOOK_THREAD_NAME) {
				@Override
				public void run() {
					synchronized (startupShutdownMonitor) {
						// 20201210 实际上执行上下文关闭：发布ContextClosedEvent并销毁此应用程序上下文的bean工厂中的单例。
						doClose();
					}
				}
			};

			// 20201210 获取该应用的运行时环境 -> 注册一个新的虚拟机关闭挂钩
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}

	/**
	 * Callback for destruction of this instance, originally attached
	 * to a {@code DisposableBean} implementation (not anymore in 5.0).
	 * <p>The {@link #close()} method is the native way to shut down
	 * an ApplicationContext, which this method simply delegates to.
	 * @deprecated as of Spring Framework 5.0, in favor of {@link #close()}
	 */
	@Deprecated
	public void destroy() {
		close();
	}

	/**
	 * Close this application context, destroying all beans in its bean factory.
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 * Also removes a JVM shutdown hook, if registered, as it's not needed anymore.
	 * @see #doClose()
	 * @see #registerShutdownHook()
	 */
	@Override
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			// If we registered a JVM shutdown hook, we don't need it anymore now:
			// We've already explicitly closed the context.
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				}
				catch (IllegalStateException ex) {
					// ignore - VM is already shutting down
				}
			}
		}
	}

	/**
	 * 20201210
	 * A. 实际上执行上下文关闭：发布ContextClosedEvent并销毁此应用程序上下文的bean工厂中的单例。
	 * B. 由{@code close（）}和JVM关闭钩子（如果有）调用。
	 */
	/**
	 * A.
	 * Actually performs context closing: publishes a ContextClosedEvent and
	 * destroys the singletons in the bean factory of this application context.
	 *
	 * B.
	 * <p>Called by both {@code close()} and a JVM shutdown hook, if any.
	 * @see org.springframework.context.event.ContextClosedEvent
	 * @see #destroyBeans()
	 * @see #close()
	 * @see #registerShutdownHook()
	 */
	// 20201210 实际上执行上下文关闭：发布ContextClosedEvent并销毁此应用程序上下文的bean工厂中的单例
	@SuppressWarnings("deprecation")
	protected void doClose() {
		// 20201210 检查是否需要实际的关闭尝试...
		// Check whether an actual close attempt is necessary...
		// 20201210 更新关闭状态为true
		if (this.active.get() && this.closed.compareAndSet(false, true)) {
			// 20201210 打印关闭日志
			if (logger.isDebugEnabled()) {
				logger.debug("Closing " + this);
			}

			// 20201210 此环境不存在于本地映像中。-> "org.graalvm.nativeimage.imagecode
			if (!IN_NATIVE_IMAGE) {
				// 20201210 注销应用程序上下文, 注销Beans
				LiveBeansView.unregisterApplicationContext(this);
			}

			try {
				// Publish shutdown event.
				// 20201210 发布关闭事件。
				publishEvent(new ContextClosedEvent(this));
			}
			catch (Throwable ex) {
				logger.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", ex);
			}

			// 20201210 停止所有Lifecycle bean，以避免在单个销毁期间造成延迟。
			// Stop all Lifecycle beans, to avoid delays during individual destruction.
			if (this.lifecycleProcessor != null) {
				try {
					// 20201210 上下文关闭阶段的通知，例如 用于自动停止组件。
					this.lifecycleProcessor.onClose();
				}
				catch (Throwable ex) {
					logger.warn("Exception thrown from LifecycleProcessor on context close", ex);
				}
			}

			// Destroy all cached singletons in the context's BeanFactory.
			// 20201210 销毁上下文的BeanFactory中所有缓存的单例。
			destroyBeans();

			// Close the state of this context itself.
			// 20201210 关闭此上下文本身的状态 -> 能释放其内部bean工厂
			closeBeanFactory();

			// Let subclasses do some final clean-up if they wish...
			// 20201210 如果希望的话，让子类做一些最后的清理... -> 默认实现为空。
			onClose();

			// Reset local application listeners to pre-refresh state.
			// 20201210 将本地应用程序侦听器重置为预刷新状态 -> 初始化监听器列表为刷新前的监听器列表
			if (this.earlyApplicationListeners != null) {
				this.applicationListeners.clear();
				this.applicationListeners.addAll(this.earlyApplicationListeners);
			}

			// Switch to inactive.
			// 20201210 切换为非活动状态。
			this.active.set(false);
		}
	}

	/**
	 * 20201210
	 * A. 用于销毁此上下文管理的所有bean的模板方法。 默认实现在此上下文中销毁所有缓存的单例，并调用{@code DisposableBean.destroy（）}和/或指定的“ destroy-method”。
	 * B. 可以重写以在标准单例销毁之前或之后添加特定于上下文的Bean销毁步骤，而上下文的BeanFactory仍处于活动状态。
	 */
	/**
	 * A.
	 * Template method for destroying all beans that this context manages.
	 * The default implementation destroy all cached singletons in this context,
	 * invoking {@code DisposableBean.destroy()} and/or the specified
	 * "destroy-method".
	 *
	 * B.
	 * <p>Can be overridden to add context-specific bean destruction steps
	 * right before or right after standard singleton destruction,
	 * while the context's BeanFactory is still active.
	 * @see #getBeanFactory()
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
	 */
	// 20201210 用于销毁此上下文管理的所有bean的模板方法。 默认实现在此上下文中销毁所有缓存的单例，
	protected void destroyBeans() {
		// 20201210 销毁该工厂中的所有Beans，包括已注册为一次性的Bean。 在工厂关闭时被调用。
		getBeanFactory().destroySingletons();
	}

	/**
	 * 20201210
	 * A. 可以重写的模板方法以添加特定于上下文的关闭工作。 默认实现为空。
	 * B. 在关闭此上下文的BeanFactory之后，在{@link #doClose}的关闭过程结束时调用此方法。 如果在BeanFactory仍处于活动状态时需要执行自定义关闭逻辑，请改用
	 *    {@link #destroyBeans（）}方法。
	 */
	/**
	 * A.
	 * Template method which can be overridden to add context-specific shutdown work.
	 * The default implementation is empty.
	 *
	 * B.
	 * <p>Called at the end of {@link #doClose}'s shutdown procedure, after
	 * this context's BeanFactory has been closed. If custom shutdown logic
	 * needs to execute while the BeanFactory is still active, override
	 * the {@link #destroyBeans()} method instead.
	 */
	// 20201210 可以重写的模板方法以添加特定于上下文的关闭工作。 默认实现为空。
	protected void onClose() {
		// For subclasses: do nothing by default.
		// 20201210 对于子类：默认情况下不执行任何操作。
	}

	@Override
	public boolean isActive() {
		return this.active.get();
	}

	/**
	 * Assert that this context's BeanFactory is currently active,
	 * throwing an {@link IllegalStateException} if it isn't.
	 * <p>Invoked by all {@link BeanFactory} delegation methods that depend
	 * on an active context, i.e. in particular all bean accessor methods.
	 * <p>The default implementation checks the {@link #isActive() 'active'} status
	 * of this context overall. May be overridden for more specific checks, or for a
	 * no-op if {@link #getBeanFactory()} itself throws an exception in such a case.
	 */
	protected void assertBeanFactoryActive() {
		if (!this.active.get()) {
			if (this.closed.get()) {
				throw new IllegalStateException(getDisplayName() + " has been closed already");
			}
			else {
				throw new IllegalStateException(getDisplayName() + " has not been refreshed yet");
			}
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public Object getBean(String name) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name, requiredType);
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name, args);
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(requiredType);
	}

	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(requiredType, args);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType);
	}

	@Override
	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isSingleton(name);
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isPrototype(name);
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isTypeMatch(name, typeToMatch);
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isTypeMatch(name, typeToMatch);
	}

	@Override
	@Nullable
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().getType(name);
	}

	@Override
	@Nullable
	public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().getType(name, allowFactoryBeanInit);
	}

	@Override
	public String[] getAliases(String name) {
		return getBeanFactory().getAliases(name);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public boolean containsBeanDefinition(String beanName) {
		return getBeanFactory().containsBeanDefinition(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType, allowEagerInit);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanProvider(requiredType, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type);
	}

	@Override
	public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBeansOfType(type);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		assertBeanFactoryActive();
		return getBeanFactory().getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForAnnotation(annotationType);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
			throws BeansException {

		assertBeanFactoryActive();
		return getBeanFactory().getBeansWithAnnotation(annotationType);
	}

	@Override
	@Nullable
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {

		assertBeanFactoryActive();
		return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	@Nullable
	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	@Override
	public boolean containsLocalBean(String name) {
		return getBeanFactory().containsLocalBean(name);
	}

	/**
	 * Return the internal bean factory of the parent context if it implements
	 * ConfigurableApplicationContext; else, return the parent context itself.
	 * @see org.springframework.context.ConfigurableApplicationContext#getBeanFactory
	 */
	// 20201211 如果实现了ConfigurableApplicationContext，则返回父上下文的内部bean工厂；否则，返回false。 否则，返回父上下文本身。
	@Nullable
	protected BeanFactory getInternalParentBeanFactory() {
		// 20201211 如果实现了ConfigurableApplicationContext，则返回父上下文的内部bean工厂；否则，返回false。 否则，返回父上下文本身。
		return (getParent() instanceof ConfigurableApplicationContext ?
				((ConfigurableApplicationContext) getParent()).getBeanFactory() : getParent());
	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource interface
	//---------------------------------------------------------------------

	@Override
	public String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
		return getMessageSource().getMessage(code, args, defaultMessage, locale);
	}

	@Override
	public String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(code, args, locale);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(resolvable, locale);
	}

	/**
	 * Return the internal MessageSource used by the context.
	 * @return the internal MessageSource (never {@code null})
	 * @throws IllegalStateException if the context has not been initialized yet
	 */
	private MessageSource getMessageSource() throws IllegalStateException {
		if (this.messageSource == null) {
			throw new IllegalStateException("MessageSource not initialized - " +
					"call 'refresh' before accessing messages via the context: " + this);
		}
		return this.messageSource;
	}

	/**
	 * Return the internal message source of the parent context if it is an
	 * AbstractApplicationContext too; else, return the parent context itself.
	 */
	@Nullable
	protected MessageSource getInternalParentMessageSource() {
		return (getParent() instanceof AbstractApplicationContext ?
				((AbstractApplicationContext) getParent()).messageSource : getParent());
	}


	//---------------------------------------------------------------------
	// Implementation of ResourcePatternResolver interface
	//---------------------------------------------------------------------

	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		return this.resourcePatternResolver.getResources(locationPattern);
	}


	//---------------------------------------------------------------------
	// Implementation of Lifecycle interface
	//---------------------------------------------------------------------

	@Override
	public void start() {
		getLifecycleProcessor().start();
		publishEvent(new ContextStartedEvent(this));
	}

	@Override
	public void stop() {
		getLifecycleProcessor().stop();
		publishEvent(new ContextStoppedEvent(this));
	}

	@Override
	public boolean isRunning() {
		return (this.lifecycleProcessor != null && this.lifecycleProcessor.isRunning());
	}


	//---------------------------------------------------------------------
	// Abstract methods that must be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * 20201210
	 * A. 子类必须实现此方法才能执行实际的配置加载。在进行任何其他初始化工作之前，该方法由{@link #refresh（）}调用。
	 * B. 子类将创建一个新的bean工厂并保留对其的引用，或者返回它持有的单个BeanFactory实例。 在后一种情况下，如果多次刷新上下文，通常会抛出IllegalStateException。
	 */
	/**
	 * A.
	 * Subclasses must implement this method to perform the actual configuration load.
	 * The method is invoked by {@link #refresh()} before any other initialization work.
	 *
	 * B.
	 * <p>A subclass will either create a new bean factory and hold a reference to it,
	 * or return a single BeanFactory instance that it holds. In the latter case, it will
	 * usually throw an IllegalStateException if refreshing the context more than once.
	 *
	 * @throws BeansException if initialization of the bean factory failed
	 * @throws IllegalStateException if already initialized and multiple refresh
	 * attempts are not supported
	 */
	// 20201210 子类将创建一个新的BeanFactory并保留对其的引用，或者返回它持有的单个BeanFactory实例。
	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

	/**
	 * 20201210
	 * A. 子类必须实现此方法才能释放其内部bean工厂。 在所有其他关闭工作之后，{@link #close（）}将调用此方法。
	 * B. 永远不要抛出异常，而应该记录日志关闭失败。
	 */
	/**
	 * A.
	 * Subclasses must implement this method to release their internal bean factory.
	 * This method gets invoked by {@link #close()} after all other shutdown work.
	 *
	 * B.
	 * <p>Should never throw an exception but rather log shutdown failures.
	 */
	// 20201210 子类必须实现此方法才能释放其内部bean工厂。 在所有其他关闭工作之后，{@link #close（）}将调用此方法
	protected abstract void closeBeanFactory();

	/**
	 * 20201206
	 * A. 子类必须在此处返回其内部bean工厂。 他们应该有效地实现查找，以便可以重复调用它而不会影响性能。
	 * B. 注意：子类应在返回内部bean工厂之前检查上下文是否仍处于活动状态。 一旦关闭上下文，通常应将内部工厂视为不可用。
	 */
	/**
	 * A.
	 * Subclasses must return their internal bean factory here. They should implement the
	 * lookup efficiently, so that it can be called repeatedly without a performance penalty.
	 *
	 * B.
	 * <p>Note: Subclasses should check whether the context is still active before
	 * returning the internal bean factory. The internal factory should generally be
	 * considered unavailable once the context has been closed.
	 *
	 * @return this application context's internal bean factory (never {@code null})
	 * @throws IllegalStateException if the context does not hold an internal bean factory yet
	 * (usually if {@link #refresh()} has never been called) or if the context has been
	 * closed already
	 * @see #refreshBeanFactory()
	 * @see #closeBeanFactory()
	 */
	// 20201210 子类必须在此处返回其内部bean工厂。 他们应该有效地实现查找，以便可以重复调用它而不会影响性能。
	@Override
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

	/**
	 * Return information about this context.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getDisplayName());
		sb.append(", started on ").append(new Date(getStartupDate()));
		ApplicationContext parent = getParent();
		if (parent != null) {
			sb.append(", parent: ").append(parent.getDisplayName());
		}
		return sb.toString();
	}

}
