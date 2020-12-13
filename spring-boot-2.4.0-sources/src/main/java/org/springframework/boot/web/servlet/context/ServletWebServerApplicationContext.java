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

package org.springframework.boot.web.servlet.context;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.ServletContextAwareProcessor;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.ServletContextScope;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 20201204
 * A. 一个{@link WebApplicationContext}，可用于从包含的{@link ServletWebServerFactory} bean中进行自我引导。
 * B. 通过在{@link ApplicationContext}本身内搜索单个{@link ServletWebServerFactory} bean，此上下文将创建，初始化和运行{@link WebServer}。
 *    {@link ServletWebServerFactory}可以自由使用标准的Spring概念（例如依赖项注入，生命周期回调和属性占位符变量）。
 * C. 另外，上下文中定义的任何{@link Servlet}或{@link Filter} bean都将自动在Web服务器上注册。 在单个Servlet bean的情况下，将使用“ /”映射。
 *    如果找到多个Servlet Bean，则将小写的Bean名称用作映射前缀。 任何名为“ dispatcherServlet”的Servlet都将始终映射为“ /”。 过滤的Bean将被映射到所有URL（'/ *'）。
 * D. 对于更高级的配置，上下文可以改为定义实现{@link ServletContextInitializer}接口的bean（最常见的是{@link ServletRegistrationBean}和或
 *    {@link FilterRegistrationBean}）。 为了防止重复注册，使用{@link ServletContextInitializer} bean将禁用自动Servlet和Filter bean注册。
 * E. 尽管可以直接使用此上下文，但是大多数开发人员应考虑使用{@link AnnotationConfigServletWebServerApplicationContext}或
 *    {@link XmlServletWebServerApplicationContext}变体。
 */
/**
 * A.
 * A {@link WebApplicationContext} that can be used to bootstrap itself from a contained
 * {@link ServletWebServerFactory} bean.
 * <p>
 *
 * B.
 * This context will create, initialize and run an {@link WebServer} by searching for a
 * single {@link ServletWebServerFactory} bean within the {@link ApplicationContext}
 * itself. The {@link ServletWebServerFactory} is free to use standard Spring concepts
 * (such as dependency injection, lifecycle callbacks and property placeholder variables).
 *
 * C.
 * <p>
 * In addition, any {@link Servlet} or {@link Filter} beans defined in the context will be
 * automatically registered with the web server. In the case of a single Servlet bean, the
 * '/' mapping will be used. If multiple Servlet beans are found then the lowercase bean
 * name will be used as a mapping prefix. Any Servlet named 'dispatcherServlet' will
 * always be mapped to '/'. Filter beans will be mapped to all URLs ('/*').
 * <p>
 *
 * D.
 * For more advanced configuration, the context can instead define beans that implement
 * the {@link ServletContextInitializer} interface (most often
 * {@link ServletRegistrationBean}s and/or {@link FilterRegistrationBean}s). To prevent
 * double registration, the use of {@link ServletContextInitializer} beans will disable
 * automatic Servlet and Filter bean registration.
 *
 * E.
 * <p>
 * Although this context can be used directly, most developers should consider using the
 * {@link AnnotationConfigServletWebServerApplicationContext} or
 * {@link XmlServletWebServerApplicationContext} variants.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Scott Frederick
 * @since 2.0.0
 * @see AnnotationConfigServletWebServerApplicationContext
 * @see XmlServletWebServerApplicationContext
 * @see ServletWebServerFactory
 */
// 20201204 Servlet Web程序上下文: 允许对其应用任何bean定义读取器、支持Servlet上下文、配置上下文、Web生命周期获取、将资源路径解释为servlet上下文资源
public class ServletWebServerApplicationContext extends GenericWebApplicationContext implements ConfigurableWebServerApplicationContext {

	private static final Log logger = LogFactory.getLog(ServletWebServerApplicationContext.class);

	/**
	 * Constant value for the DispatcherServlet bean name. A Servlet bean with this name
	 * is deemed to be the "main" servlet and is automatically given a mapping of "/" by
	 * default. To change the default behavior you can use a
	 * {@link ServletRegistrationBean} or a different bean name.
	 */
	public static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

	// 20201210 表示完全配置的Web服务器的简单实例（例如Tomcat，Jetty，Netty）
	private volatile WebServer webServer;

	private ServletConfig servletConfig;

	private String serverNamespace;

	/**
	 * Create a new {@link ServletWebServerApplicationContext}.
	 */
	public ServletWebServerApplicationContext() {
	}

	/**
	 * Create a new {@link ServletWebServerApplicationContext} with the given
	 * {@code DefaultListableBeanFactory}.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public ServletWebServerApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	/**
	 * Register ServletContextAwareProcessor.
	 * @see ServletContextAwareProcessor
	 */
	// 20201212 注册ServletContextAwareProcessor。
	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 20201212 beanFactory添加Web应用程序上下文&Servlet应用程序上下文自觉BeanPostProcessor
		beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));

		// 20201212 忽略ServletContext自觉接口进行自动装配
		beanFactory.ignoreDependencyInterface(ServletContextAware.class);

		// 20201212 注册Web应用程序范围
		registerWebApplicationScopes();
	}

	// 20201210 加载或刷新配置的持久性表示形式, 如果失败，它应该销毁已创建的单例，以避免悬挂资源
	@Override
	public final void refresh() throws BeansException, IllegalStateException {
		try {
			// 20201210 加载或刷新配置的持久性表示形式, 如果失败，它应该销毁已创建的单例，以避免悬挂资源
			super.refresh();
		}
		catch (RuntimeException ex) {
			// 20201210 获取已注册的表示完全配置的Web服务器的简单实例（例如Tomcat，Jetty，Netty）
			WebServer webServer = this.webServer;

			// 20201210 刷新上下文出现异常时, 如果该服务器实例不为空
			if (webServer != null) {
				// 20201210 停止Web服务器。 在已停止的服务器上调用此方法无效。
				webServer.stop();
			}

			// 20201210 继续抛出异常
			throw ex;
		}
	}

	// 20201213 可以重写的模板方法以添加特定于上下文的刷新工作
	@Override
	protected void onRefresh() {
		// 20201213 初始化主题功能。
		super.onRefresh();
		try {
			// 20201213 启动Web服务器, 替换与{@code Servlet}相关的属性源
			createWebServer();
		}
		catch (Throwable ex) {
			throw new ApplicationContextException("Unable to start web server", ex);
		}
	}

	@Override
	protected void doClose() {
		if (isActive()) {
			AvailabilityChangeEvent.publish(this, ReadinessState.REFUSING_TRAFFIC);
		}
		super.doClose();
	}

	// 20201213 启动Web服务器, 替换与{@code Servlet}相关的属性源
	private void createWebServer() {
		WebServer webServer = this.webServer;
		ServletContext servletContext = getServletContext();
		if (webServer == null && servletContext == null) {
			StartupStep createWebServer = this.getApplicationStartup().start("spring.boot.webserver.create");
			// 20201213 【Tomcat源码】获取一个新的完全配置但暂停的{@link WebServer}实例
			ServletWebServerFactory factory = getWebServerFactory();
			createWebServer.tag("factory", factory.getClass().toString());
			this.webServer = factory.getWebServer(getSelfInitializer());
			createWebServer.end();

			// 20201213 注册webServerGracefulShutdown单例
			getBeanFactory().registerSingleton("webServerGracefulShutdown",
					new WebServerGracefulShutdownLifecycle(this.webServer));

			// 20201213 注册webServerStartStop单例
			getBeanFactory().registerSingleton("webServerStartStop",
					new WebServerStartStopLifecycle(this, this.webServer));
		}
		else if (servletContext != null) {
			try {
				// 20201213 使用初始化所需的所有Servlet，过滤器，侦听器上下文参数和属性配置给定的{@link ServletContext}。
				getSelfInitializer().onStartup(servletContext);
			}
			catch (ServletException ex) {
				throw new ApplicationContextException("Cannot initialize servlet context", ex);
			}
		}

		// 20201213 替换与{@code Servlet}相关的属性源。
		initPropertySources();
	}

	/**
	 * Returns the {@link ServletWebServerFactory} that should be used to create the
	 * embedded {@link WebServer}. By default this method searches for a suitable bean in
	 * the context itself.
	 * @return a {@link ServletWebServerFactory} (never {@code null})
	 */
	protected ServletWebServerFactory getWebServerFactory() {
		// Use bean names so that we don't consider the hierarchy
		String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
		if (beanNames.length == 0) {
			throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to missing "
					+ "ServletWebServerFactory bean.");
		}
		if (beanNames.length > 1) {
			throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to multiple "
					+ "ServletWebServerFactory beans : " + StringUtils.arrayToCommaDelimitedString(beanNames));
		}
		return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
	}

	/**
	 * Returns the {@link ServletContextInitializer} that will be used to complete the
	 * setup of this {@link WebApplicationContext}.
	 * @return the self initializer
	 * @see #prepareWebApplicationContext(ServletContext)
	 */
	// 20201213 返回{@link ServletContextInitializer}，它将用于完成此{@link WebApplicationContext}的设置。
	private org.springframework.boot.web.servlet.ServletContextInitializer getSelfInitializer() {
		return this::selfInitialize;
	}

	private void selfInitialize(ServletContext servletContext) throws ServletException {
		prepareWebApplicationContext(servletContext);
		registerApplicationScope(servletContext);
		WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(), servletContext);
		for (ServletContextInitializer beans : getServletContextInitializerBeans()) {
			beans.onStartup(servletContext);
		}
	}

	private void registerApplicationScope(ServletContext servletContext) {
		ServletContextScope appScope = new ServletContextScope(servletContext);
		getBeanFactory().registerScope(WebApplicationContext.SCOPE_APPLICATION, appScope);
		// Register as ServletContext attribute, for ContextCleanupListener to detect it.
		servletContext.setAttribute(ServletContextScope.class.getName(), appScope);
	}

	// 20201212 注册Web应用程序范围
	private void registerWebApplicationScopes() {
		// 20201212 构造Web应用程序作用域实例
		ExistingWebApplicationScopes existingScopes = new ExistingWebApplicationScopes(getBeanFactory());

		// 20201212 根据beanFactory注册特定于Web的范围（“请求”，“会话”，“ globalSession”，“应用程序”）
		WebApplicationContextUtils.registerWebApplicationScopes(getBeanFactory());

		// 20201212 beanFactory重新注册给定范围
		existingScopes.restore();
	}

	/**
	 * Returns {@link ServletContextInitializer}s that should be used with the embedded
	 * web server. By default this method will first attempt to find
	 * {@link ServletContextInitializer}, {@link Servlet}, {@link Filter} and certain
	 * {@link EventListener} beans.
	 * @return the servlet initializer beans
	 */
	protected Collection<ServletContextInitializer> getServletContextInitializerBeans() {
		return new ServletContextInitializerBeans(getBeanFactory());
	}

	/**
	 * Prepare the {@link WebApplicationContext} with the given fully loaded
	 * {@link ServletContext}. This method is usually called from
	 * {@link ServletContextInitializer#onStartup(ServletContext)} and is similar to the
	 * functionality usually provided by a {@link ContextLoaderListener}.
	 * @param servletContext the operational servlet context
	 */
	protected void prepareWebApplicationContext(ServletContext servletContext) {
		Object rootContext = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (rootContext != null) {
			if (rootContext == this) {
				throw new IllegalStateException(
						"Cannot initialize context because there is already a root application context present - "
								+ "check whether you have multiple ServletContextInitializers!");
			}
			return;
		}
		servletContext.log("Initializing Spring embedded WebApplicationContext");
		try {
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this);
			if (logger.isDebugEnabled()) {
				logger.debug("Published root WebApplicationContext as ServletContext attribute with name ["
						+ WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
			}
			setServletContext(servletContext);
			if (logger.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - getStartupDate();
				logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
			}
		}
		catch (RuntimeException | Error ex) {
			logger.error("Context initialization failed", ex);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
	}

	@Override
	protected Resource getResourceByPath(String path) {
		if (getServletContext() == null) {
			return new ClassPathContextResource(path, getClassLoader());
		}
		return new ServletContextResource(getServletContext(), path);
	}

	@Override
	public String getServerNamespace() {
		return this.serverNamespace;
	}

	@Override
	public void setServerNamespace(String serverNamespace) {
		this.serverNamespace = serverNamespace;
	}

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	/**
	 * Returns the {@link WebServer} that was created by the context or {@code null} if
	 * the server has not yet been created.
	 * @return the embedded web server
	 */
	@Override
	public WebServer getWebServer() {
		return this.webServer;
	}

	/**
	 * 20201212
	 * 实用程序类，用于存储和还原任何用户定义的范围。 这允许范围以与在传统的非嵌入式Web应用程序上下文中相同的方式在ApplicationContextInitializer中注册。
	 */
	/**
	 * Utility class to store and restore any user defined scopes. This allow scopes to be
	 * registered in an ApplicationContextInitializer in the same way as they would in a
	 * classic non-embedded web application context.
	 */
	// 20201212 Web应用程序作用域类
	public static class ExistingWebApplicationScopes {

		private static final Set<String> SCOPES;

		static {
			Set<String> scopes = new LinkedHashSet<>();
			scopes.add(WebApplicationContext.SCOPE_REQUEST);
			scopes.add(WebApplicationContext.SCOPE_SESSION);
			SCOPES = Collections.unmodifiableSet(scopes);
		}

		private final ConfigurableListableBeanFactory beanFactory;

		// 20201212 Web应用程序作用域集合: 作用域名称-作用域实现
		private final Map<String, Scope> scopes = new HashMap<>();

		// 20201212 构造Web应用程序作用域实例
		public ExistingWebApplicationScopes(ConfigurableListableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
			for (String scopeName : SCOPES) {
				// 20201212 根据作用域名称返回已显示注册的作用域实现
				Scope scope = beanFactory.getRegisteredScope(scopeName);
				if (scope != null) {
					this.scopes.put(scopeName, scope);
				}
			}
		}

		// 20201212 beanFactory重新注册给定范围
		public void restore() {
			this.scopes.forEach((key, value) -> {
				if (logger.isInfoEnabled()) {
					logger.info("Restoring user defined scope " + key);
				}
				this.beanFactory.registerScope(key, value);
			});
		}

	}

}
