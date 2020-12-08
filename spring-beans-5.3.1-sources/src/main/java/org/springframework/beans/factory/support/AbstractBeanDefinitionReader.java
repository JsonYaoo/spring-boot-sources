/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans.factory.support;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201205
 * A. 实现{@link BeanDefinitionReader}接口的bean定义阅读器的抽象基类。
 * B. 提供常用属性，例如要使用的bean工厂以及用于加载bean类的类加载器。
 */
/**
 * A.
 * Abstract base class for bean definition readers which implement
 * the {@link BeanDefinitionReader} interface.
 *
 * B.
 * <p>Provides common properties like the bean factory to work on
 * and the class loader to use for loading bean classes.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 11.12.2003
 * @see BeanDefinitionReaderUtils
 */
// 20201205 bean定义阅读器的抽象基类: 提供常用属性, 即要使用的bean工厂以及用于加载bean类的类加载器, 环境获取
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader, EnvironmentCapable {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	// 20201208 bean定义的注册表
	private final BeanDefinitionRegistry registry;

	// 20201208 资源加载器
	@Nullable
	private ResourceLoader resourceLoader;

	@Nullable
	private ClassLoader beanClassLoader;

	// 20201208 当前环境
	private Environment environment;

	// 20201208 用于为bean定义生成bean名称的策略接口实例
	private BeanNameGenerator beanNameGenerator = DefaultBeanNameGenerator.INSTANCE;

	/**
	 * 20201208
	 * A. 为给定的bean工厂创建一个新的AbstractBeanDefinitionReader。
	 * B. 如果传入的bean工厂不仅实现BeanDefinitionRegistry接口，还实现ResourceLoader接口，则它将也用作默认ResourceLoader。
	 *    {@link org.springframework.context.ApplicationContext}实现通常是这种情况。
	 * C. 如果给出普通的BeanDefinitionRegistry，则默认的ResourceLoader将为{@link PathMatchingResourcePatternResolver}。
	 * D. 如果传入的bean工厂也实现了{@link EnvironmentCapable}，则此阅读器将使用其环境。 否则，读者将初始化并使用{@link StandardEnvironment}。
	 *    所有ApplicationContext实现都具有EnvironmentCapable功能，而普通BeanFactory实现则不是。
	 */
	/**
	 * A.
	 * Create a new AbstractBeanDefinitionReader for the given bean factory.
	 *
	 * B.
	 * <p>If the passed-in bean factory does not only implement the BeanDefinitionRegistry
	 * interface but also the ResourceLoader interface, it will be used as default
	 * ResourceLoader as well. This will usually be the case for
	 * {@link org.springframework.context.ApplicationContext} implementations.
	 *
	 * C.
	 * <p>If given a plain BeanDefinitionRegistry, the default ResourceLoader will be a
	 * {@link PathMatchingResourcePatternResolver}.
	 *
	 * D.
	 * <p>If the passed-in bean factory also implements {@link EnvironmentCapable} its
	 * environment will be used by this reader.  Otherwise, the reader will initialize and
	 * use a {@link StandardEnvironment}. All ApplicationContext implementations are
	 * EnvironmentCapable, while normal BeanFactory implementations are not.
	 *
	 * @param registry the BeanFactory to load bean definitions into,
	 * in the form of a BeanDefinitionRegistry // 20201208 BeanFactory以BeanDefinitionRegistry的形式将Bean定义加载到其中
	 * @see #setResourceLoader
	 * @see #setEnvironment
	 */
	// 20201208 为给定的bean工厂创建一个新的AbstractBeanDefinitionReader
	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		// 20201208 bean定义的注册表不能为空
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

		// 20201208 设置bean定义的注册表
		this.registry = registry;

		// Determine ResourceLoader to use. // 20201208 确定要使用的ResourceLoader。
		// 20201208 如果bean定义的注册表也为ResourceLoader
		if (this.registry instanceof ResourceLoader) {
			// 20201208 则设置资源加载器为该bean定义的注册表
			this.resourceLoader = (ResourceLoader) this.registry;
		}

		// 20201208 否则设置资源加载器为PathMatchingResourcePatternResolver
		else {
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}

		// Inherit Environment if possible // 20201208 如果可能，继承环境
		// 20201208 如果bean定义的注册表也为EnvironmentCapable
		if (this.registry instanceof EnvironmentCapable) {
			// 20201208 则设置当前环境为该bean定义的注册表
			this.environment = ((EnvironmentCapable) this.registry).getEnvironment();
		}

		// 20201208 否则设置当前环境为标准环境(非Web)环境
		else {
			this.environment = new StandardEnvironment();
		}
	}


	public final BeanDefinitionRegistry getBeanFactory() {
		return this.registry;
	}

	@Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * 20201208
	 * A. 设置ResourceLoader以用于资源位置。
	 * B. 如果指定ResourcePatternResolver，则Bean定义读取器将能够将资源模式解析为Resource数组。
	 * C. 默认值为PathMatchingResourcePatternResolver，它也能够通过ResourcePatternResolver接口解析资源模式。
	 * D. 将其设置为{@code null}表示此bean定义阅读器不支持绝对资源加载。
	 */
	/**
	 * A.
	 * Set the ResourceLoader to use for resource locations.
	 *
	 * B.
	 * If specifying a ResourcePatternResolver, the bean definition reader
	 * will be capable of resolving resource patterns to Resource arrays.
	 *
	 * C.
	 * <p>Default is PathMatchingResourcePatternResolver, also capable of
	 * resource pattern resolving through the ResourcePatternResolver interface.
	 *
	 * D.
	 * <p>Setting this to {@code null} suggests that absolute resource loading
	 * is not available for this bean definition reader.
	 *
	 * @see ResourcePatternResolver
	 * @see PathMatchingResourcePatternResolver
	 */
	// 20201208 设置ResourceLoader以用于资源位置
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		// 20201208 注册资源加载器
		this.resourceLoader = resourceLoader;
	}

	@Override
	@Nullable
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	/**
	 * Set the ClassLoader to use for bean classes.
	 * <p>Default is {@code null}, which suggests to not load bean classes
	 * eagerly but rather to just register bean definitions with class names,
	 * with the corresponding Classes to be resolved later (or never).
	 * @see Thread#getContextClassLoader()
	 */
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Override
	@Nullable
	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	/**
	 * Set the Environment to use when reading bean definitions. Most often used
	 * for evaluating profile information to determine which bean definitions
	 * should be read and which should be omitted.
	 */
	// 20201208 设置在读取bean定义时要使用的环境。 最常用于评估概要文件信息，以确定应读取哪些bean定义，以及应省略哪些。
	public void setEnvironment(Environment environment) {
		// 20201208 环境不能为空
		Assert.notNull(environment, "Environment must not be null");

		// 20201208 注册当前环境
		this.environment = environment;
	}

	@Override
	public Environment getEnvironment() {
		return this.environment;
	}

	/**
	 * 20201208
	 * A. 设置BeanNameGenerator以用于匿名Bean（未指定显式Bean名称）。
	 * B. 默认值为{@link DefaultBeanNameGenerator}。
	 */
	/**
	 * A.
	 * Set the BeanNameGenerator to use for anonymous beans
	 * (without explicit bean name specified).
	 *
	 * B.
	 * <p>Default is a {@link DefaultBeanNameGenerator}.
	 */
	// 20201208 设置BeanNameGenerator以用于匿名Bean（未指定显式Bean名称）
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		// 20201208 设置用于为bean定义生成bean名称的策略接口实例
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : DefaultBeanNameGenerator.INSTANCE);
	}

	@Override
	public BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}


	@Override
	public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
		Assert.notNull(resources, "Resource array must not be null");
		int count = 0;
		for (Resource resource : resources) {
			count += loadBeanDefinitions(resource);
		}
		return count;
	}

	@Override
	public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(location, null);
	}

	/**
	 * Load bean definitions from the specified resource location.
	 * <p>The location can also be a location pattern, provided that the
	 * ResourceLoader of this bean definition reader is a ResourcePatternResolver.
	 * @param location the resource location, to be loaded with the ResourceLoader
	 * (or ResourcePatternResolver) of this bean definition reader
	 * @param actualResources a Set to be filled with the actual Resource objects
	 * that have been resolved during the loading process. May be {@code null}
	 * to indicate that the caller is not interested in those Resource objects.
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 * @see #getResourceLoader()
	 * @see #loadBeanDefinitions(Resource)
	 * @see #loadBeanDefinitions(Resource[])
	 */
	public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new BeanDefinitionStoreException(
					"Cannot load bean definitions from location [" + location + "]: no ResourceLoader available");
		}

		if (resourceLoader instanceof ResourcePatternResolver) {
			// Resource pattern matching available.
			try {
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
				int count = loadBeanDefinitions(resources);
				if (actualResources != null) {
					Collections.addAll(actualResources, resources);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Loaded " + count + " bean definitions from location pattern [" + location + "]");
				}
				return count;
			}
			catch (IOException ex) {
				throw new BeanDefinitionStoreException(
						"Could not resolve bean definition resource pattern [" + location + "]", ex);
			}
		}
		else {
			// Can only load single resources by absolute URL.
			Resource resource = resourceLoader.getResource(location);
			int count = loadBeanDefinitions(resource);
			if (actualResources != null) {
				actualResources.add(resource);
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Loaded " + count + " bean definitions from location [" + location + "]");
			}
			return count;
		}
	}

	@Override
	public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
		Assert.notNull(locations, "Location array must not be null");
		int count = 0;
		for (String location : locations) {
			count += loadBeanDefinitions(location);
		}
		return count;
	}

}
