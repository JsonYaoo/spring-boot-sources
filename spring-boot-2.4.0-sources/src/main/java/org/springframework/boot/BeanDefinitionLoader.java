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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import groovy.lang.Closure;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.SpringProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 20201208
 * 从基础源（包括XML和JavaConfig）加载bean定义。 充当{@link AnnotatedBeanDefinitionReader}，{@link XmlBeanDefinitionReader}和
 * {@link ClassPathBeanDefinitionScanner}上的简单外观。 有关受支持的源类型，请参见{@link SpringApplication}。
 */
/**
 * Loads bean definitions from underlying sources, including XML and JavaConfig. Acts as a
 * simple facade over {@link AnnotatedBeanDefinitionReader},
 * {@link XmlBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}. See
 * {@link SpringApplication} for the types of sources that are supported.
 *
 * @author Phillip Webb
 * @author Vladislav Kisel
 * @author Sebastien Deleuze
 * @see #setBeanNameGenerator(BeanNameGenerator)
 */
// 20201208 从基础源（包括XML和JavaConfig）加载bean定义
class BeanDefinitionLoader {

	// 20201208 静态最终字段，以方便Graal删除代码
	// Static final field to facilitate code removal by Graal

	// 20201208 是否启动XML读取
	private static final boolean XML_ENABLED = !SpringProperties.getFlag("spring.xml.ignore");

	// 20201208 所有bean加载源
	private final Object[] sources;

	// 20201208 bean注册适配器
	private final AnnotatedBeanDefinitionReader annotatedReader;

	// 20201208 xml的bean定义阅读器
	private final AbstractBeanDefinitionReader xmlReader;

	// 202012008 基于Groovy的bean定义阅读器
	private final BeanDefinitionReader groovyReader;

	// 20201208 bean定义扫描器
	private final ClassPathBeanDefinitionScanner scanner;

	// 20201208 资源加载器
	private ResourceLoader resourceLoader;

	/**
	 * Create a new {@link BeanDefinitionLoader} that will load beans into the specified
	 * {@link BeanDefinitionRegistry}.
	 * @param registry the bean definition registry that will contain the loaded beans	// 20201208 将包含已加载的bean的bean定义注册表
	 * @param sources the bean sources
	 */
	// 20201208 创建一个新的{@link BeanDefinitionLoader}，它将把bean加载到指定的{@link BeanDefinitionRegistry}中。
	BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
		// 20201208 bean定义注册表不能为空
		Assert.notNull(registry, "Registry must not be null");

		// 20201208 所有bean加载源不能为空
		Assert.notEmpty(sources, "Sources must not be empty");

		// 20201208 注册所有bean加载源
		this.sources = sources;

		// 20201208 设置bean注册适配器
		this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);

		// 20201208 如果启动XML读取, 则为给定的bean工厂创建新的XmlBeanDefinitionReader, 并设置为xml的bean定义阅读器
		this.xmlReader = (XML_ENABLED ? new XmlBeanDefinitionReader(registry) : null);

		// 20201208 如果groovy.lang.MetaClass可以加载, 则设置基于Groovy的bean定义阅读器
		this.groovyReader = (isGroovyPresent() ? new GroovyBeanDefinitionReader(registry) : null);

		// 20201208 设置bean定义扫描器
		this.scanner = new ClassPathBeanDefinitionScanner(registry);

		// 20201208 将排除类型过滤器添加到排除列表的最前面
		this.scanner.addExcludeFilter(
				// 20201208 构造简单的{@link TypeFilter}, 用于确保在扫描过程中不会意外地重新添加sources源。
				new ClassExcludeFilter(sources)
		);
	}

	/**
	 * Set the bean name generator to be used by the underlying readers and scanner.
	 * @param beanNameGenerator the bean name generator	//	20201208 Bean名称生成器
	 */
	// 20201208 设置要由基础阅读器和扫描器使用的bean名称生成器。
	void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		// 20201208 bean注册适配器设置{@code BeanNameGenerator}以用于检测到的bean类, 默认值为{@link AnnotationBeanNameGenerator}。
		this.annotatedReader.setBeanNameGenerator(beanNameGenerator);

		// 20201208 bean定义扫描器设置BeanNameGenerator以用于检测到的Bean类。
		this.scanner.setBeanNameGenerator(beanNameGenerator);

		// 20201208 如果xml的bean定义阅读器存在
		if (this.xmlReader != null) {
			// 20201208 xml的bean定义阅读器设置BeanNameGenerator以用于匿名Bean（未指定显式Bean名称）
			this.xmlReader.setBeanNameGenerator(beanNameGenerator);
		}
	}

	/**
	 * Set the resource loader to be used by the underlying readers and scanner.
	 * @param resourceLoader the resource loader
	 */
	// 20201208 设置要由基础读取器和扫描器使用的资源加载器。
	void setResourceLoader(ResourceLoader resourceLoader) {
		// 20201208 注册资源加载器
		this.resourceLoader = resourceLoader;

		// 20201208 bean定义扫描器设置资源加载器以用于资源位置
		this.scanner.setResourceLoader(resourceLoader);

		// 20201208 如果xml的bean定义阅读器存在
		if (this.xmlReader != null) {
			// 20201208 xml的bean定义阅读器设置ResourceLoader以用于资源位置
			this.xmlReader.setResourceLoader(resourceLoader);
		}
	}

	/**
	 * Set the environment to be used by the underlying readers and scanner.
	 * @param environment the environment
	 */
	// 20201208 设置基础阅读器和扫描仪要使用的环境。
	void setEnvironment(ConfigurableEnvironment environment) {
		// 20201208 bean注册适配器设置使用@Conditional注解组件类时的环境
		this.annotatedReader.setEnvironment(environment);

		// 20201208 bean定义扫描器设置@Conditional注解的组件类时使用的环境
		this.scanner.setEnvironment(environment);

		// 20201208 如果存在xml的bean定义阅读器
		if (this.xmlReader != null) {
			// 20201208 xml的bean定义阅读器设置在读取bean定义时要使用的环境
			this.xmlReader.setEnvironment(environment);
		}
	}

	/**
	 * Load the sources into the reader.
	 */
	// 20201208 将源加载到阅读器中 -> lass类型, Resource类型, Package类型, CharSequence类型
	void load() {
		// 20201208 遍历所有源
		for (Object source : this.sources) {
			// 20201208 加载每个源到阅读器中 -> lass类型, Resource类型, Package类型, CharSequence类型
			load(source);
		}
	}

	// 20201208 加载每个源到阅读器中 -> lass类型, Resource类型, Package类型, CharSequence类型
	private void load(Object source) {
		// 20201208 源不能为空
		Assert.notNull(source, "Source must not be null");

		// 20201208 如果源为Class类型
		if (source instanceof Class<?>) {
			// 20201029 加载bean源, 为从给定的bean类中注册一个bean，并从类声明的注释中派生其元数据。
			load((Class<?>) source);
			return;
		}

		// 20201209 如果源为Resource类型
		if (source instanceof Resource) {
			// 20201209 加载Resource源 -> 基于Groovy的bean定义阅读器 | xml的bean定义阅读器
			load((Resource) source);
			return;
		}

		// 20201209 如果源为Package类型
		if (source instanceof Package) {
			// 20201209 加载Package源 -> 使用bean定义扫描器在指定的基本程序包中执行扫描 -> 返回已注册的bean数目
			load((Package) source);
			return;
		}

		// 20201209 如果源属于CharSequence类型
		if (source instanceof CharSequence) {
			// 20201209 加载CharSequence源 -> 尝试加载bean源, 尝试加载Resources源, 尝试加载Package源
			load((CharSequence) source);
			return;
		}

		// 20201209 如果是其他类型, 则抛出异常
		throw new IllegalArgumentException("Invalid source type " + source.getClass());
	}

	// 20201029 加载bean源, 为从给定的bean类中注册一个bean，并从类声明的注释中派生其元数据。
	private void load(Class<?> source) {
		// 20201208 如果groovy.lang.MetaClass能够加载, 且如果Groovy中定义的Bean定义的来源类型与source源类型相同
		if (isGroovyPresent() && GroovyBeanDefinitionSource.class.isAssignableFrom(source)) {
			// Any GroovyLoaders added in beans{} DSL can contribute beans here
			// 20201208 在bean {} DSL中添加的任何GroovyLoader都可以在此处贡献bean
			// 20201208 使用其“主要”构造函数（对于Kotlin类，可能声明了默认参数）或其缺省构造函数（对于常规Java类，需要标准无参数设置）实例化一个类
			GroovyBeanDefinitionSource loader = BeanUtils.instantiateClass(source, GroovyBeanDefinitionSource.class);

			// 20201208 注册bean源 -> 注册表添加bean源的属性
			((GroovyBeanDefinitionReader) this.groovyReader).beans(loader.getBeans());
		}

		// 20201208 如果Bean符合注册条件, 即既不是常规的闭包也不是匿名类
		if (isEligible(source)) {
			// 20201208 bean注册适配器
			this.annotatedReader.register(source);
		}
	}

	// 20201209 加载Resource源 -> 基于Groovy的bean定义阅读器 | xml的bean定义阅读器
	private void load(Resource source) {
		// 20201209 文件名以".groovy结尾
		if (source.getFilename().endsWith(".groovy")) {
			// 20201209 如果没有基于Groovy的bean定义阅读器, 则抛出异常
			if (this.groovyReader == null) {
				throw new BeanDefinitionStoreException("Cannot load Groovy beans without Groovy on classpath");
			}

			// 20201209 否则从指定的资源加载bean定义。
			this.groovyReader.loadBeanDefinitions(source);
		}

		// 20201209 如果为xml资源
		else {
			// 20201209 如果xml的bean定义阅读器为空, 则抛出异常
			if (this.xmlReader == null) {
				throw new BeanDefinitionStoreException("Cannot load XML bean definitions when XML support is disabled");
			}

			// 20201209 否则从指定的资源加载bean定义
			this.xmlReader.loadBeanDefinitions(source);
		}
	}

	// 20201209 加载Package源 -> 使用bean定义扫描器在指定的基本程序包中执行扫描 -> 返回已注册的bean数目
	private void load(Package source) {
		// 20201209 使用bean定义扫描器在指定的基本程序包中执行扫描 -> 返回已注册的bean数目
		this.scanner.scan(source.getName());
	}

	// 20201209 加载CharSequence源 -> 尝试加载bean源, 尝试加载Resources源, 尝试加载Package源
	private void load(CharSequence source) {
		// 20201209 在给定的文本中解析$ {...}占位符，将其替换为{@link #getProperty}解析的相应属性值。 没有默认值的无法解析的占位符将被忽略，并按原样传递。
		String resolvedSource = this.scanner.getEnvironment().resolvePlaceholders(source.toString());

		// 20201209 尝试加载bean源
		// Attempt as a Class
		try {
			// 20201029 根据解析出的属性值进行加载bean源, 为从给定的bean类中注册一个bean，并从类声明的注释中派生其元数据。
			load(ClassUtils.forName(resolvedSource, null));
			return;
		}
		catch (IllegalArgumentException | ClassNotFoundException ex) {
			// swallow exception and continue
			// 20201209 如果Bean源加载异常, 则吞下异常并继续
		}

		// 20201209 尝试加载Resources源
		// Attempt as Resources
		// 20201209 根据字符串加载Resouce源, 如果加载得到则返回true
		if (loadAsResources(resolvedSource)) {
			return;
		}

		// 20201209 尝试加载Package源
		// Attempt as package
		// 20201209 查找源的Package对象
		Package packageResource = findPackage(resolvedSource);

		// 20201209 如果找得到Package对象
		if (packageResource != null) {
			// 20201209 加载Package源 -> 使用bean定义扫描器在指定的基本程序包中执行扫描 -> 返回已注册的bean数目
			load(packageResource);
			return;
		}
		throw new IllegalArgumentException("Invalid source '" + resolvedSource + "'");
	}

	// 20201209 根据字符串加载Resouce源, 如果加载得到则返回true
	private boolean loadAsResources(String resolvedSource) {
		// 20201209 是否发现候选组件
		boolean foundCandidate = false;

		// 20201209 根据源字符串, 查找资源句柄数组
		Resource[] resources = findResources(resolvedSource);

		// 20201209 遍历资源句柄数组
		for (Resource resource : resources) {
			// 20201209 是否存在资源句柄 -> 存在则返回true
			if (isLoadCandidate(resource)) {
				// 20201209 标记确实发现了候选组件
				foundCandidate = true;

				// 20201209 加载Resource源 -> 基于Groovy的bean定义阅读器 | xml的bean定义阅读器
				load(resource);
			}
		}

		// 20201209 返回是否发现候选组件
		return foundCandidate;
	}

	// 20201208 确定由提供的名称标识的groovy.lang.MetaClass是否存在并且可以加载
	private boolean isGroovyPresent() {
		return ClassUtils.isPresent("groovy.lang.MetaClass", null);
	}

	// 20201209 根据源字符串, 查找资源句柄数组
	private Resource[] findResources(String source) {
		// 20201209 获取资源加载器
		ResourceLoader loader = (this.resourceLoader != null) ? this.resourceLoader : new PathMatchingResourcePatternResolver();
		try {
			// 20201029 如果为ResourcePatternResolver类型
			if (loader instanceof ResourcePatternResolver) {
				// 20201209 则将给定的位置模式解析为Resource对象
				return ((ResourcePatternResolver) loader).getResources(source);
			}

			// 20201209 否则属于其他类型的话, 则获取指定资源位置的资源句柄并返回
			return new Resource[] { loader.getResource(source) };
		}
		catch (IOException ex) {
			throw new IllegalStateException("Error reading source '" + source + "'");
		}
	}

	// 20201209 是否存在资源句柄 -> 存在则返回true
	private boolean isLoadCandidate(Resource resource) {
		// 20201209 如果资源句柄不存在, 则返回false
		if (resource == null || !resource.exists()) {
			return false;
		}

		// 20201209 如果资源句柄为ClassPathResource类型
		if (resource instanceof ClassPathResource) {
			// A simple package without a '.' may accidentally get loaded as an XML
			// document if we're not careful. The result of getInputStream() will be
			// a file list of the package content. We double check here that it's not
			// actually a package.
			// 20201209 没有“。”的简单包装。 如果我们不小心，可能会意外地将其加载为XML文档。 getInputStream（）的结果将是包内容的文件列表。 我们在这里再次检查它实际上不是软件包。
			// 20201209 获取资源路径
			String path = ((ClassPathResource) resource).getPath();
			if (path.indexOf('.') == -1) {
				try {
					// 20201209 根据资源路径查找包对象, 如果不为空则返回true
					return Package.getPackage(path) == null;
				}
				catch (Exception ex) {
					// Ignore
				}
			}
		}

		// 20201209 如果是其他类型则返回true
		return true;
	}

	// 20201209 查找源的Package对象
	private Package findPackage(CharSequence source) {
		// 20201209 获取Package对象
		Package pkg = Package.getPackage(source.toString());

		// 20201209 如果存在则直接返回
		if (pkg != null) {
			return pkg;
		}
		// 20201209 否则尝试在此包中找到一个类
		try {
			// Attempt to find a class in this package
			// 20201209 根据类加载器构造路径模式资源处理器
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());

			// 20201209 根据Class全类名获取资源句柄数组
			Resource[] resources = resolver.getResources(
					// 20201209 将基于“。”的完全限定的类名称转换为基于“ /”的资源路径 -> 拼凑Class全类名
					ClassUtils.convertClassNameToResourcePath(source.toString()) + "/*.class");

			// 20201209 遍历资源句柄数组
			for (Resource resource : resources) {
				// 20201209 获取从给定的Java资源路径中删除文件扩展名的路径，例如 “ mypath / myfile.txt”->“ mypath / myfile”。
				String className = StringUtils.stripFilenameExtension(resource.getFilename());

				// 20201029 拼凑类的全类名 -> 加载该类的bean源, 为从给定的bean类中注册一个bean，并从类声明的注释中派生其元数据。
				load(Class.forName(source.toString() + "." + className));
				break;
			}
		}
		catch (Exception ex) {
			// swallow exception and continue
			// 20201209 吞下异常并继续
		}

		// 20201209 根据该类获取Package对象并返回
		return Package.getPackage(source.toString());
	}

	/**
	 * Check whether the bean is eligible for registration.
	 * @param type candidate bean type	// 20201208 候选bean类型
	 * @return true if the given bean type is eligible for registration, i.e. not a groovy
	 * closure nor an anonymous class
	 */
	// 20201208 检查Bean是否符合注册条件 -> 如果给定的bean类型符合注册条件，则为true，即既不是常规的闭包也不是匿名类
	private boolean isEligible(Class<?> type) {
		// 20201208 如果给定的bean类型符合注册条件，则为true，即既不是常规的闭包也不是匿名类
		return !(type.isAnonymousClass() || isGroovyClosure(type) || hasNoConstructors(type));
	}

	private boolean isGroovyClosure(Class<?> type) {
		return type.getName().matches(".*\\$_.*closure.*");
	}

	private boolean hasNoConstructors(Class<?> type) {
		Constructor<?>[] constructors = type.getDeclaredConstructors();
		return ObjectUtils.isEmpty(constructors);
	}

	/**
	 * Simple {@link TypeFilter} used to ensure that specified {@link Class} sources are
	 * not accidentally re-added during scanning.
	 */
	// 20201208 简单的{@link TypeFilter}用于确保在扫描过程中不会意外地重新添加指定的{@link Class}源。
	private static class ClassExcludeFilter extends AbstractTypeHierarchyTraversingFilter {

		// 20201208 不会重新添加的Class源
		private final Set<String> classNames = new HashSet<>();

		// 20201208 构造简单的{@link TypeFilter}, 用于确保在扫描过程中不会意外地重新添加sources源。
		ClassExcludeFilter(Object... sources) {
			// 20201208 构造AbstractTypeHierarchyTraversingFilter
			super(false, false);

			// 20201208 遍历指定源
			for (Object source : sources) {
				// 20201208 如果该源为Class类型
				if (source instanceof Class<?>) {
					// 20201208 则把Class名称添加到Class源中
					this.classNames.add(((Class<?>) source).getName());
				}
			}
		}

		@Override
		protected boolean matchClassName(String className) {
			return this.classNames.contains(className);
		}

	}

	/**
	 * Source for Bean definitions defined in Groovy.
	 */
	// 20201208 Groovy中定义的Bean定义的来源。
	@FunctionalInterface
	protected interface GroovyBeanDefinitionSource {

		Closure<?> getBeans();

	}

}
