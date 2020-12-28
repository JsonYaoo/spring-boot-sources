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

package org.springframework.boot.web.embedded.tomcat;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContainerInitializer;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot.ResourceSetType;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.FixContextListener;
import org.apache.catalina.util.LifecycleBase;
import org.apache.catalina.webresources.AbstractResourceSet;
import org.apache.catalina.webresources.EmptyResource;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.scan.StandardJarScanFilter;

import org.springframework.boot.util.LambdaSafe;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 20201228
 * A. {@link AbstractServletWebServerFactory}，可用于创建{@link TomcatWebServer}。 可以使用Spring的{@link ServletContextInitializer}或Tomcat {@link LifecycleListener}进行初始化。
 * B. 除非明确配置，否则该工厂将创建在端口8080上侦听HTTP请求的容器。
 */
/**
 * A.
 * {@link AbstractServletWebServerFactory} that can be used to create
 * {@link TomcatWebServer}s. Can be initialized using Spring's
 * {@link ServletContextInitializer}s or Tomcat {@link LifecycleListener}s.
 *
 * B.
 * <p>
 * Unless explicitly configured otherwise this factory will create containers that listen
 * for HTTP requests on port 8080.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Brock Mills
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @author Eddú Meléndez
 * @author Christoffer Sawicki
 * @author Dawid Antecki
 * @since 2.0.0
 * @see #setPort(int)
 * @see #setContextLifecycleListeners(Collection)
 * @see TomcatWebServer
 */
// 20201228 {@link AbstractServletWebServerFactory}，可用于创建{@link TomcatWebServer}。 可以使用Spring的{@link ServletContextInitializer}或Tomcat {@link LifecycleListener}进行初始化
public class TomcatServletWebServerFactory extends AbstractServletWebServerFactory implements ConfigurableTomcatWebServerFactory, ResourceLoaderAware {

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();

	/**
	 * The class name of default protocol used.
	 */
	// 20201228 使用的默认协议的类名称 eg: "org.apache.coyote.http11.Http11NioProtocol"
	public static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";

	private static final boolean IN_NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

	private File baseDirectory;

	private List<Valve> engineValves = new ArrayList<>();

	private List<Valve> contextValves = new ArrayList<>();

	private List<LifecycleListener> contextLifecycleListeners = getDefaultLifecycleListeners();

	private Set<TomcatContextCustomizer> tomcatContextCustomizers = new LinkedHashSet<>();

	private Set<TomcatConnectorCustomizer> tomcatConnectorCustomizers = new LinkedHashSet<>();

	private Set<TomcatProtocolHandlerCustomizer<?>> tomcatProtocolHandlerCustomizers = new LinkedHashSet<>();

	private final List<Connector> additionalTomcatConnectors = new ArrayList<>();

	private ResourceLoader resourceLoader;

	// 20201228 使用的默认协议的类名称 eg: "org.apache.coyote.http11.Http11NioProtocol"
	private String protocol = DEFAULT_PROTOCOL;

	private Set<String> tldSkipPatterns = new LinkedHashSet<>(TldPatterns.DEFAULT_SKIP);

	private Set<String> tldScanPatterns = new LinkedHashSet<>(TldPatterns.DEFAULT_SCAN);

	private Charset uriEncoding = DEFAULT_CHARSET;

	private int backgroundProcessorDelay;

	// 20201228 禁用MBean, 默认为true
	private boolean disableMBeanRegistry = true;

	/**
	 * Create a new {@link TomcatServletWebServerFactory} instance.
	 */
	public TomcatServletWebServerFactory() {
	}

	/**
	 * Create a new {@link TomcatServletWebServerFactory} that listens for requests using
	 * the specified port.
	 * @param port the port to listen on
	 */
	public TomcatServletWebServerFactory(int port) {
		super(port);
	}

	/**
	 * Create a new {@link TomcatServletWebServerFactory} with the specified context path
	 * and port.
	 * @param contextPath the root context path
	 * @param port the port to listen on
	 */
	public TomcatServletWebServerFactory(String contextPath, int port) {
		super(contextPath, port);
	}

	private static List<LifecycleListener> getDefaultLifecycleListeners() {
		ArrayList<LifecycleListener> lifecycleListeners = new ArrayList<>();
		if (!IN_NATIVE_IMAGE) {
			AprLifecycleListener aprLifecycleListener = new AprLifecycleListener();
			if (AprLifecycleListener.isAprAvailable()) {
				lifecycleListeners.add(aprLifecycleListener);
			}
		}
		return lifecycleListeners;
	}

	// 20201213 【Tomcat源码】获取一个新的完全配置但暂停的{@link WebServer}实例
	@Override
	public WebServer getWebServer(ServletContextInitializer... initializers) {
		// 禁用MBean注册 => eg: true
		if (this.disableMBeanRegistry) {
			Registry.disableRegistry();
		}

		// 20201213 【Tomcat源码】tomcat-embed-core-9.0.39.jar
		// 20201228 此类用于嵌入tomcat的应用程序: Tomcat支持多种样式的配置和启动-最常见和稳定的是基于server.xml的服务器
		Tomcat tomcat = new Tomcat();

		// 20201213 设置Tomcat必要属性
		// 20201228 eg: "C:\Users\14840\AppData\Local\Temp\tomcat.8087.1601608683379134423"
		File baseDir = (this.baseDirectory != null) ? this.baseDirectory : createTempDir("tomcat");

		// 20201228 eg: "C:\Users\14840\AppData\Local\Temp\tomcat.8087.1601608683379134423"
		tomcat.setBaseDir(baseDir.getAbsolutePath());

		// 20201228 Connector@xxxx: protocolHandlerName: "org.apache.coyote.http11.Http11NioProtocol"
		Connector connector = new Connector(this.protocol);

		// 20201228 在重要位置抛出的异常将被重新抛出以供调用者处理或将其记录下来 eg: true
		connector.setThrowOnFailure(true);

		// 20201228 将新的连接器添加到已定义的连接器集，并将其与此服务的容器关联。
		tomcat.getService().addConnector(connector);

		// 20201228 自定义Connector
		customizeConnector(connector);

		// 20201228 在服务中设置指定的连接器（如果尚不存在）。
		tomcat.setConnector(connector);

		// 20201228 设置此主机的自动部署标志值 eg: false
		tomcat.getHost().setAutoDeploy(false);

		// 20201228 配置引擎
		configureEngine(tomcat.getEngine());

		// 20201228 eg: []
		for (Connector additionalConnector : this.additionalTomcatConnectors) {
			tomcat.getService().addConnector(additionalConnector);
		}

		// 20201214 准备上下文环境, 设置其他属性
		prepareContext(tomcat.getHost(), initializers);

		// 20201214 【重点】调用工厂方法来创建{@link TomcatWebServer}。 子类可以重写此方法以返回不同的{@link TomcatWebServer}或对Tomcat服务器进行其他处理。
		return getTomcatWebServer(tomcat);
	}

	// 20201228 配置引擎
	private void configureEngine(Engine engine) {
		// 20201228 设置在此容器及其子容器上调用execute方法之间的延迟 eg: 10
		engine.setBackgroundProcessorDelay(this.backgroundProcessorDelay);
		for (Valve valve : this.engineValves) {
			engine.getPipeline().addValve(valve);
		}
	}

	// 20201214 准备上下文环境, 设置其他属性
	protected void prepareContext(Host host, ServletContextInitializer[] initializers) {
		// 20201228 eg: null
		File documentRoot = getValidDocumentRoot();

		// 20201228 {@link TomcatWebServer}使用的Tomcat {@link StandardContext}支持延迟的初始化:  Context接口的标准实现, 每个子容器必须是Wrapper实现，才能处理针对特定servlet的请求
		// 20201228 eg: TomcatEmbeddedContext@xxxx: "TomcatEmbeddedContext[null]", state: LifecycleState@xxxx: "NEW"
		TomcatEmbeddedContext context = new TomcatEmbeddedContext();
		if (documentRoot != null) {
			context.setResources(new LoaderHidingResourceRoot(context));
		}

		// 20201228 eg: ""
		context.setName(getContextPath());

		// 20201228 eg: "application"
		context.setDisplayName(getDisplayName());

		// 20201228 eg: ""
		context.setPath(getContextPath());

		// 20201228 eg: "C:\Users\14840\AppData\Local\Temp\tomcat-docbase.8087.3533994348268659940"
		File docBase = (documentRoot != null) ? documentRoot : createTempDir("tomcat-docbase");

		// 20201228 eg: "C:\Users\14840\AppData\Local\Temp\tomcat-docbase.8087.3533994348268659940"
		context.setDocBase(docBase.getAbsolutePath());

		// 20201228 添加声明周期监听器 eg: Tomcat$FixContextListener@xxxx
		context.addLifecycleListener(new FixContextListener());

		// 20201228 eg: "org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6b3e12b5, started on Tue Dec 29 00:30:58 CST 2020"
		// 20201228 eg: parentClassLoader-Launcher$AppClassLoader@xxxx
		context.setParentClassLoader((this.resourceLoader != null) ? this.resourceLoader.getClassLoader() : ClassUtils.getDefaultClassLoader());

		// 202012228 覆盖Tomcat的默认语言环境映射以与其他服务器对齐。 请参阅{@code org.apache.catalina.util.CharsetMapperDefault.properties}。
		resetDefaultLocaleMapping(context);
		addLocaleMappings(context);
		try {
			context.setCreateUploadTargets(true);
		}
		catch (NoSuchMethodError ex) {
			// Tomcat is < 8.5.39. Continue.
		}
		configureTldPatterns(context);
		WebappLoader loader = new WebappLoader();
		loader.setLoaderClass(TomcatEmbeddedWebappClassLoader.class.getName());
		loader.setDelegate(true);
		context.setLoader(loader);
		if (isRegisterDefaultServlet()) {
			addDefaultServlet(context);
		}
		if (shouldRegisterJspServlet()) {
			addJspServlet(context);
			addJasperInitializer(context);
		}
		context.addLifecycleListener(new StaticResourceConfigurer(context));

		// 20201228 希望将指定的{@link ServletContextInitializer}参数与此实例中定义的参数组合在一起的子类可以使用的实用程序方法。
		// 20201228 eg: ServletContextInitialzer[3]@xxxx: AbstractServletWebServerFactory$lambda@xxxx、AbstractServletWebServerFactory$SessionConfigurationInitializer@xxxx、ServletWebServerApplicationContext$lambda@xxxx
		ServletContextInitializer[] initializersToUse = mergeInitializers(initializers);
		host.addChild(context);

		// 20201228 配置Tomcat {@link上下文}。
		configureContext(context, initializersToUse);

		// 20201228 eg: do nothing
		postProcessContext(context);
	}

	/**
	 * Override Tomcat's default locale mappings to align with other servers. See
	 * {@code org.apache.catalina.util.CharsetMapperDefault.properties}.
	 * @param context the context to reset
	 */
	// 202012228 覆盖Tomcat的默认语言环境映射以与其他服务器对齐。 请参阅{@code org.apache.catalina.util.CharsetMapperDefault.properties}。
	private void resetDefaultLocaleMapping(TomcatEmbeddedContext context) {
		context.addLocaleEncodingMappingParameter(Locale.ENGLISH.toString(), DEFAULT_CHARSET.displayName());
		context.addLocaleEncodingMappingParameter(Locale.FRENCH.toString(), DEFAULT_CHARSET.displayName());
	}

	private void addLocaleMappings(TomcatEmbeddedContext context) {
		getLocaleCharsetMappings().forEach(
				(locale, charset) -> context.addLocaleEncodingMappingParameter(locale.toString(), charset.toString()));
	}

	private void configureTldPatterns(TomcatEmbeddedContext context) {
		StandardJarScanFilter filter = new StandardJarScanFilter();
		filter.setTldSkip(StringUtils.collectionToCommaDelimitedString(this.tldSkipPatterns));
		filter.setTldScan(StringUtils.collectionToCommaDelimitedString(this.tldScanPatterns));
		context.getJarScanner().setJarScanFilter(filter);
	}

	private void addDefaultServlet(Context context) {
		Wrapper defaultServlet = context.createWrapper();
		defaultServlet.setName("default");
		defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		defaultServlet.addInitParameter("debug", "0");
		defaultServlet.addInitParameter("listings", "false");
		defaultServlet.setLoadOnStartup(1);
		// Otherwise the default location of a Spring DispatcherServlet cannot be set
		defaultServlet.setOverridable(true);
		context.addChild(defaultServlet);
		context.addServletMappingDecoded("/", "default");
	}

	private void addJspServlet(Context context) {
		Wrapper jspServlet = context.createWrapper();
		jspServlet.setName("jsp");
		jspServlet.setServletClass(getJsp().getClassName());
		jspServlet.addInitParameter("fork", "false");
		getJsp().getInitParameters().forEach(jspServlet::addInitParameter);
		jspServlet.setLoadOnStartup(3);
		context.addChild(jspServlet);
		context.addServletMappingDecoded("*.jsp", "jsp");
		context.addServletMappingDecoded("*.jspx", "jsp");
	}

	private void addJasperInitializer(TomcatEmbeddedContext context) {
		try {
			ServletContainerInitializer initializer = (ServletContainerInitializer) ClassUtils
					.forName("org.apache.jasper.servlet.JasperInitializer", null).getDeclaredConstructor()
					.newInstance();
			context.addServletContainerInitializer(initializer, null);
		}
		catch (Exception ex) {
			// Probably not Tomcat 8
		}
	}

	// Needs to be protected so it can be used by subclasses
	// 20201228 需要受到保护，以便子类可以使用它
	protected void customizeConnector(Connector connector) {
		int port = Math.max(getPort(), 0);
		connector.setPort(port);
		if (StringUtils.hasText(getServerHeader())) {
			connector.setProperty("server", getServerHeader());
		}
		if (connector.getProtocolHandler() instanceof AbstractProtocol) {
			customizeProtocol((AbstractProtocol<?>) connector.getProtocolHandler());
		}
		invokeProtocolHandlerCustomizers(connector.getProtocolHandler());
		if (getUriEncoding() != null) {
			connector.setURIEncoding(getUriEncoding().name());
		}
		// Don't bind to the socket prematurely if ApplicationContext is slow to start
		connector.setProperty("bindOnInit", "false");
		if (getSsl() != null && getSsl().isEnabled()) {
			customizeSsl(connector);
		}
		TomcatConnectorCustomizer compression = new CompressionConnectorCustomizer(getCompression());
		compression.customize(connector);
		for (TomcatConnectorCustomizer customizer : this.tomcatConnectorCustomizers) {
			customizer.customize(connector);
		}
	}

	private void customizeProtocol(AbstractProtocol<?> protocol) {
		if (getAddress() != null) {
			protocol.setAddress(getAddress());
		}
	}

	@SuppressWarnings("unchecked")
	private void invokeProtocolHandlerCustomizers(ProtocolHandler protocolHandler) {
		LambdaSafe.callbacks(TomcatProtocolHandlerCustomizer.class, this.tomcatProtocolHandlerCustomizers,
				protocolHandler).invoke((customizer) -> customizer.customize(protocolHandler));
	}

	private void customizeSsl(Connector connector) {
		new SslConnectorCustomizer(getSsl(), getSslStoreProvider()).customize(connector);
		if (getHttp2() != null && getHttp2().isEnabled()) {
			connector.addUpgradeProtocol(new Http2Protocol());
		}
	}

	/**
	 * Configure the Tomcat {@link Context}.
	 * @param context the Tomcat context
	 * @param initializers initializers to apply
	 */
	// 20201228 配置Tomcat {@link上下文}。
	protected void configureContext(Context context, ServletContextInitializer[] initializers) {
		// 20201228 eg: ServletContextInitialzer[3]@xxxx: AbstractServletWebServerFactory$lambda@xxxx、AbstractServletWebServerFactory$SessionConfigurationInitializer@xxxx、ServletWebServerApplicationContext$lambda@xxxx
		TomcatStarter starter = new TomcatStarter(initializers);
		if (context instanceof TomcatEmbeddedContext) {
			TomcatEmbeddedContext embeddedContext = (TomcatEmbeddedContext) context;
			embeddedContext.setStarter(starter);
			embeddedContext.setFailCtxIfServletStartFails(true);
		}
		context.addServletContainerInitializer(starter, NO_CLASSES);

		// 20201228 eg: []
		for (LifecycleListener lifecycleListener : this.contextLifecycleListeners) {
			context.addLifecycleListener(lifecycleListener);
		}

		// 20201228 eg: []
		for (Valve valve : this.contextValves) {
			context.getPipeline().addValve(valve);
		}

		// 20201228 eg: ErrorPage@xxxx: path: "/error"
		for (ErrorPage errorPage : getErrorPages()) {
			org.apache.tomcat.util.descriptor.web.ErrorPage tomcatErrorPage = new org.apache.tomcat.util.descriptor.web.ErrorPage();
			tomcatErrorPage.setLocation(errorPage.getPath());
			tomcatErrorPage.setErrorCode(errorPage.getStatusCode());
			tomcatErrorPage.setExceptionType(errorPage.getExceptionName());
			context.addErrorPage(tomcatErrorPage);
		}

		// 20201228 eg: size = 176
		for (MimeMappings.Mapping mapping : getMimeMappings()) {
			context.addMimeMapping(mapping.getExtension(), mapping.getMimeType());
		}
		configureSession(context);
		new DisableReferenceClearingContextCustomizer().customize(context);

		// 20201228 eg: []
		for (String webListenerClassName : getWebListenerClassNames()) {
			context.addApplicationListener(webListenerClassName);
		}

		// 20201228 eg: size = 5
		for (TomcatContextCustomizer customizer : this.tomcatContextCustomizers) {
			// 20201228 自定义上下文。
			customizer.customize(context);
		}
	}

	private void configureSession(Context context) {
		long sessionTimeout = getSessionTimeoutInMinutes();
		context.setSessionTimeout((int) sessionTimeout);
		Boolean httpOnly = getSession().getCookie().getHttpOnly();
		if (httpOnly != null) {
			context.setUseHttpOnly(httpOnly);
		}
		if (getSession().isPersistent()) {
			Manager manager = context.getManager();
			if (manager == null) {
				manager = new StandardManager();
				context.setManager(manager);
			}
			configurePersistSession(manager);
		}
		else {
			context.addLifecycleListener(new DisablePersistSessionListener());
		}
	}

	private void configurePersistSession(Manager manager) {
		Assert.state(manager instanceof StandardManager,
				() -> "Unable to persist HTTP session state using manager type " + manager.getClass().getName());
		File dir = getValidSessionStoreDir();
		File file = new File(dir, "SESSIONS.ser");
		((StandardManager) manager).setPathname(file.getAbsolutePath());
	}

	private long getSessionTimeoutInMinutes() {
		Duration sessionTimeout = getSession().getTimeout();
		if (isZeroOrLess(sessionTimeout)) {
			return 0;
		}
		return Math.max(sessionTimeout.toMinutes(), 1);
	}

	private boolean isZeroOrLess(Duration sessionTimeout) {
		return sessionTimeout == null || sessionTimeout.isNegative() || sessionTimeout.isZero();
	}

	/**
	 * Post process the Tomcat {@link Context} before it's used with the Tomcat Server.
	 * Subclasses can override this method to apply additional processing to the
	 * {@link Context}.
	 * @param context the Tomcat {@link Context}
	 */
	// 20201228 在将Tomcat {@link Context}与Tomcat服务器一起使用之前，请对其进行后期处理。 子类可以重写此方法，以对{@link Context}进行其他处理。
	protected void postProcessContext(Context context) {
	}

	/**
	 * Factory method called to create the {@link TomcatWebServer}. Subclasses can
	 * override this method to return a different {@link TomcatWebServer} or apply
	 * additional processing to the Tomcat server.
	 * @param tomcat the Tomcat server.
	 * @return a new {@link TomcatWebServer} instance
	 */
	// 20201214 【重点】调用工厂方法来创建{@link TomcatWebServer}。 子类可以重写此方法以返回不同的{@link TomcatWebServer}或对Tomcat服务器进行其他处理。
	protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
		// 20201214 【重点】创建一个新的{@link TomcatWebServer}实例。
		return new TomcatWebServer(tomcat, getPort() >= 0, getShutdown());
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * Returns a mutable set of the patterns that match jars to ignore for TLD scanning.
	 * @return the list of jars to ignore for TLD scanning
	 */
	public Set<String> getTldSkipPatterns() {
		return this.tldSkipPatterns;
	}

	/**
	 * Set the patterns that match jars to ignore for TLD scanning. See Tomcat's
	 * catalina.properties for typical values. Defaults to a list drawn from that source.
	 * @param patterns the jar patterns to skip when scanning for TLDs etc
	 */
	public void setTldSkipPatterns(Collection<String> patterns) {
		Assert.notNull(patterns, "Patterns must not be null");
		this.tldSkipPatterns = new LinkedHashSet<>(patterns);
	}

	/**
	 * Add patterns that match jars to ignore for TLD scanning. See Tomcat's
	 * catalina.properties for typical values.
	 * @param patterns the additional jar patterns to skip when scanning for TLDs etc
	 */
	public void addTldSkipPatterns(String... patterns) {
		Assert.notNull(patterns, "Patterns must not be null");
		this.tldSkipPatterns.addAll(Arrays.asList(patterns));
	}

	/**
	 * The Tomcat protocol to use when create the {@link Connector}.
	 * @param protocol the protocol
	 * @see Connector#Connector(String)
	 */
	public void setProtocol(String protocol) {
		Assert.hasLength(protocol, "Protocol must not be empty");
		this.protocol = protocol;
	}

	/**
	 * Set {@link Valve}s that should be applied to the Tomcat {@link Engine}. Calling
	 * this method will replace any existing valves.
	 * @param engineValves the valves to set
	 */
	public void setEngineValves(Collection<? extends Valve> engineValves) {
		Assert.notNull(engineValves, "Valves must not be null");
		this.engineValves = new ArrayList<>(engineValves);
	}

	/**
	 * Returns a mutable collection of the {@link Valve}s that will be applied to the
	 * Tomcat {@link Engine}.
	 * @return the engine valves that will be applied
	 */
	public Collection<Valve> getEngineValves() {
		return this.engineValves;
	}

	@Override
	public void addEngineValves(Valve... engineValves) {
		Assert.notNull(engineValves, "Valves must not be null");
		this.engineValves.addAll(Arrays.asList(engineValves));
	}

	/**
	 * Set {@link Valve}s that should be applied to the Tomcat {@link Context}. Calling
	 * this method will replace any existing valves.
	 * @param contextValves the valves to set
	 */
	public void setContextValves(Collection<? extends Valve> contextValves) {
		Assert.notNull(contextValves, "Valves must not be null");
		this.contextValves = new ArrayList<>(contextValves);
	}

	/**
	 * Returns a mutable collection of the {@link Valve}s that will be applied to the
	 * Tomcat {@link Context}.
	 * @return the context valves that will be applied
	 * @see #getEngineValves()
	 */
	public Collection<Valve> getContextValves() {
		return this.contextValves;
	}

	/**
	 * Add {@link Valve}s that should be applied to the Tomcat {@link Context}.
	 * @param contextValves the valves to add
	 */
	public void addContextValves(Valve... contextValves) {
		Assert.notNull(contextValves, "Valves must not be null");
		this.contextValves.addAll(Arrays.asList(contextValves));
	}

	/**
	 * Set {@link LifecycleListener}s that should be applied to the Tomcat
	 * {@link Context}. Calling this method will replace any existing listeners.
	 * @param contextLifecycleListeners the listeners to set
	 */
	public void setContextLifecycleListeners(Collection<? extends LifecycleListener> contextLifecycleListeners) {
		Assert.notNull(contextLifecycleListeners, "ContextLifecycleListeners must not be null");
		this.contextLifecycleListeners = new ArrayList<>(contextLifecycleListeners);
	}

	/**
	 * Returns a mutable collection of the {@link LifecycleListener}s that will be applied
	 * to the Tomcat {@link Context}.
	 * @return the context lifecycle listeners that will be applied
	 */
	public Collection<LifecycleListener> getContextLifecycleListeners() {
		return this.contextLifecycleListeners;
	}

	/**
	 * Add {@link LifecycleListener}s that should be added to the Tomcat {@link Context}.
	 * @param contextLifecycleListeners the listeners to add
	 */
	public void addContextLifecycleListeners(LifecycleListener... contextLifecycleListeners) {
		Assert.notNull(contextLifecycleListeners, "ContextLifecycleListeners must not be null");
		this.contextLifecycleListeners.addAll(Arrays.asList(contextLifecycleListeners));
	}

	/**
	 * Set {@link TomcatContextCustomizer}s that should be applied to the Tomcat
	 * {@link Context}. Calling this method will replace any existing customizers.
	 * @param tomcatContextCustomizers the customizers to set
	 */
	public void setTomcatContextCustomizers(Collection<? extends TomcatContextCustomizer> tomcatContextCustomizers) {
		Assert.notNull(tomcatContextCustomizers, "TomcatContextCustomizers must not be null");
		this.tomcatContextCustomizers = new LinkedHashSet<>(tomcatContextCustomizers);
	}

	/**
	 * Returns a mutable collection of the {@link TomcatContextCustomizer}s that will be
	 * applied to the Tomcat {@link Context}.
	 * @return the listeners that will be applied
	 */
	public Collection<TomcatContextCustomizer> getTomcatContextCustomizers() {
		return this.tomcatContextCustomizers;
	}

	@Override
	public void addContextCustomizers(TomcatContextCustomizer... tomcatContextCustomizers) {
		Assert.notNull(tomcatContextCustomizers, "TomcatContextCustomizers must not be null");
		this.tomcatContextCustomizers.addAll(Arrays.asList(tomcatContextCustomizers));
	}

	/**
	 * Set {@link TomcatConnectorCustomizer}s that should be applied to the Tomcat
	 * {@link Connector}. Calling this method will replace any existing customizers.
	 * @param tomcatConnectorCustomizers the customizers to set
	 */
	public void setTomcatConnectorCustomizers(
			Collection<? extends TomcatConnectorCustomizer> tomcatConnectorCustomizers) {
		Assert.notNull(tomcatConnectorCustomizers, "TomcatConnectorCustomizers must not be null");
		this.tomcatConnectorCustomizers = new LinkedHashSet<>(tomcatConnectorCustomizers);
	}

	@Override
	public void addConnectorCustomizers(TomcatConnectorCustomizer... tomcatConnectorCustomizers) {
		Assert.notNull(tomcatConnectorCustomizers, "TomcatConnectorCustomizers must not be null");
		this.tomcatConnectorCustomizers.addAll(Arrays.asList(tomcatConnectorCustomizers));
	}

	/**
	 * Returns a mutable collection of the {@link TomcatConnectorCustomizer}s that will be
	 * applied to the Tomcat {@link Connector}.
	 * @return the customizers that will be applied
	 */
	public Collection<TomcatConnectorCustomizer> getTomcatConnectorCustomizers() {
		return this.tomcatConnectorCustomizers;
	}

	/**
	 * Set {@link TomcatProtocolHandlerCustomizer}s that should be applied to the Tomcat
	 * {@link Connector}. Calling this method will replace any existing customizers.
	 * @param tomcatProtocolHandlerCustomizer the customizers to set
	 * @since 2.2.0
	 */
	public void setTomcatProtocolHandlerCustomizers(
			Collection<? extends TomcatProtocolHandlerCustomizer<?>> tomcatProtocolHandlerCustomizer) {
		Assert.notNull(tomcatProtocolHandlerCustomizer, "TomcatProtocolHandlerCustomizers must not be null");
		this.tomcatProtocolHandlerCustomizers = new LinkedHashSet<>(tomcatProtocolHandlerCustomizer);
	}

	/**
	 * Add {@link TomcatProtocolHandlerCustomizer}s that should be added to the Tomcat
	 * {@link Connector}.
	 * @param tomcatProtocolHandlerCustomizers the customizers to add
	 * @since 2.2.0
	 */
	@Override
	public void addProtocolHandlerCustomizers(TomcatProtocolHandlerCustomizer<?>... tomcatProtocolHandlerCustomizers) {
		Assert.notNull(tomcatProtocolHandlerCustomizers, "TomcatProtocolHandlerCustomizers must not be null");
		this.tomcatProtocolHandlerCustomizers.addAll(Arrays.asList(tomcatProtocolHandlerCustomizers));
	}

	/**
	 * Returns a mutable collection of the {@link TomcatProtocolHandlerCustomizer}s that
	 * will be applied to the Tomcat {@link Connector}.
	 * @return the customizers that will be applied
	 * @since 2.2.0
	 */
	public Collection<TomcatProtocolHandlerCustomizer<?>> getTomcatProtocolHandlerCustomizers() {
		return this.tomcatProtocolHandlerCustomizers;
	}

	/**
	 * Add {@link Connector}s in addition to the default connector, e.g. for SSL or AJP
	 * @param connectors the connectors to add
	 */
	public void addAdditionalTomcatConnectors(Connector... connectors) {
		Assert.notNull(connectors, "Connectors must not be null");
		this.additionalTomcatConnectors.addAll(Arrays.asList(connectors));
	}

	/**
	 * Returns a mutable collection of the {@link Connector}s that will be added to the
	 * Tomcat.
	 * @return the additionalTomcatConnectors
	 */
	public List<Connector> getAdditionalTomcatConnectors() {
		return this.additionalTomcatConnectors;
	}

	@Override
	public void setUriEncoding(Charset uriEncoding) {
		this.uriEncoding = uriEncoding;
	}

	/**
	 * Returns the character encoding to use for URL decoding.
	 * @return the URI encoding
	 */
	public Charset getUriEncoding() {
		return this.uriEncoding;
	}

	@Override
	public void setBackgroundProcessorDelay(int delay) {
		this.backgroundProcessorDelay = delay;
	}

	/**
	 * Set whether the factory should disable Tomcat's MBean registry prior to creating
	 * the server.
	 * @param disableMBeanRegistry whether to disable the MBean registry
	 * @since 2.2.0
	 */
	public void setDisableMBeanRegistry(boolean disableMBeanRegistry) {
		this.disableMBeanRegistry = disableMBeanRegistry;
	}

	/**
	 * {@link LifecycleListener} to disable persistence in the {@link StandardManager}. A
	 * {@link LifecycleListener} is used so not to interfere with Tomcat's default manager
	 * creation logic.
	 */
	private static class DisablePersistSessionListener implements LifecycleListener {

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (event.getType().equals(Lifecycle.START_EVENT)) {
				Context context = (Context) event.getLifecycle();
				Manager manager = context.getManager();
				if (manager instanceof StandardManager) {
					((StandardManager) manager).setPathname(null);
				}
			}
		}

	}

	private final class StaticResourceConfigurer implements LifecycleListener {

		private final Context context;

		private StaticResourceConfigurer(Context context) {
			this.context = context;
		}

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
				addResourceJars(getUrlsOfJarsWithMetaInfResources());
			}
		}

		private void addResourceJars(List<URL> resourceJarUrls) {
			for (URL url : resourceJarUrls) {
				String path = url.getPath();
				if (path.endsWith(".jar") || path.endsWith(".jar!/")) {
					String jar = url.toString();
					if (!jar.startsWith("jar:")) {
						// A jar file in the file system. Convert to Jar URL.
						jar = "jar:" + jar + "!/";
					}
					addResourceSet(jar);
				}
				else {
					addResourceSet(url.toString());
				}
			}
		}

		private void addResourceSet(String resource) {
			try {
				if (isInsideNestedJar(resource)) {
					// It's a nested jar but we now don't want the suffix because Tomcat
					// is going to try and locate it as a root URL (not the resource
					// inside it)
					resource = resource.substring(0, resource.length() - 2);
				}
				URL url = new URL(resource);
				String path = "/META-INF/resources";
				this.context.getResources().createWebResourceSet(ResourceSetType.RESOURCE_JAR, "/", url, path);
			}
			catch (Exception ex) {
				// Ignore (probably not a directory)
			}
		}

		private boolean isInsideNestedJar(String dir) {
			return dir.indexOf("!/") < dir.lastIndexOf("!/");
		}

	}

	private static final class LoaderHidingResourceRoot extends StandardRoot {

		private LoaderHidingResourceRoot(TomcatEmbeddedContext context) {
			super(context);
		}

		@Override
		protected WebResourceSet createMainResourceSet() {
			return new LoaderHidingWebResourceSet(super.createMainResourceSet());
		}

	}

	private static final class LoaderHidingWebResourceSet extends AbstractResourceSet {

		private final WebResourceSet delegate;

		private final Method initInternal;

		private LoaderHidingWebResourceSet(WebResourceSet delegate) {
			this.delegate = delegate;
			try {
				this.initInternal = LifecycleBase.class.getDeclaredMethod("initInternal");
				this.initInternal.setAccessible(true);
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		public WebResource getResource(String path) {
			if (path.startsWith("/org/springframework/boot")) {
				return new EmptyResource(getRoot(), path);
			}
			return this.delegate.getResource(path);
		}

		@Override
		public String[] list(String path) {
			return this.delegate.list(path);
		}

		@Override
		public Set<String> listWebAppPaths(String path) {
			return this.delegate.listWebAppPaths(path).stream()
					.filter((webAppPath) -> !webAppPath.startsWith("/org/springframework/boot"))
					.collect(Collectors.toSet());
		}

		@Override
		public boolean mkdir(String path) {
			return this.delegate.mkdir(path);
		}

		@Override
		public boolean write(String path, InputStream is, boolean overwrite) {
			return this.delegate.write(path, is, overwrite);
		}

		@Override
		public URL getBaseUrl() {
			return this.delegate.getBaseUrl();
		}

		@Override
		public void setReadOnly(boolean readOnly) {
			this.delegate.setReadOnly(readOnly);
		}

		@Override
		public boolean isReadOnly() {
			return this.delegate.isReadOnly();
		}

		@Override
		public void gc() {
			this.delegate.gc();
		}

		@Override
		protected void initInternal() throws LifecycleException {
			if (this.delegate instanceof LifecycleBase) {
				try {
					ReflectionUtils.invokeMethod(this.initInternal, this.delegate);
				}
				catch (Exception ex) {
					throw new LifecycleException(ex);
				}
			}
		}

	}

}
