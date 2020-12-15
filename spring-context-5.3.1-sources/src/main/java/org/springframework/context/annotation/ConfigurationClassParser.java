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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.context.annotation.DeferredImportSelector.Group;
import org.springframework.core.NestedIOException;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 20201214
 * A. 解析{@link Configuration}类定义，填充{@link ConfigurationClass}对象的集合（解析单个Configuration类可能会产生任意数量的ConfigurationClass对象，
 *    因为一个配置类可以使用{@link Import}注释导入另一个） 。
 * B. 此类有助于将解析Configuration类的结构的关注与基于该模型的内容注册BeanDefinition对象的关注（需要立即注册的{@code @ComponentScan}注释除外）分开。
 * C. 这种基于ASM的实现避免了反射和渴望的类加载，以便与Spring ApplicationContext中的惰性类加载有效地互操作。
 */
/**
 * A.
 * Parses a {@link Configuration} class definition, populating a collection of
 * {@link ConfigurationClass} objects (parsing a single Configuration class may result in
 * any number of ConfigurationClass objects because one Configuration class may import
 * another using the {@link Import} annotation).
 *
 * B.
 * <p>This class helps separate the concern of parsing the structure of a Configuration
 * class from the concern of registering BeanDefinition objects based on the content of
 * that model (with the exception of {@code @ComponentScan} annotations which need to be
 * registered immediately).
 *
 * C.
 * <p>This ASM-based implementation avoids reflection and eager class loading in order to
 * interoperate effectively with lazy class loading in a Spring ApplicationContext.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 3.0
 * @see ConfigurationClassBeanDefinitionReader
 */
// 20201214 @Configuration解析器: 解析{@link Configuration}类定义，填充{@link ConfigurationClass}对象的集合
class ConfigurationClassParser {

	private static final PropertySourceFactory DEFAULT_PROPERTY_SOURCE_FACTORY = new DefaultPropertySourceFactory();

	// 20201214 默认过滤"java.lang.annotation." 以及 "org.springframework.stereotype."类
	private static final Predicate<String> DEFAULT_EXCLUSION_FILTER = className ->
			(className.startsWith("java.lang.annotation.") || className.startsWith("org.springframework.stereotype."));

    // 20201215 使用{@code AnnotationAwareOrderComparator}的共享默认实例进行排序
	private static final Comparator<DeferredImportSelectorHolder> DEFERRED_IMPORT_COMPARATOR =
			(o1, o2) -> AnnotationAwareOrderComparator.INSTANCE.compare(o1.getImportSelector(), o2.getImportSelector());


	private final Log logger = LogFactory.getLog(getClass());

	// 20201214 元数据读取工厂
	private final MetadataReaderFactory metadataReaderFactory;

	// 20201214 beanDefinition解析报告器
	private final ProblemReporter problemReporter;

	private final Environment environment;

	// 20201214 资源加载器
	private final ResourceLoader resourceLoader;

	// 20201214 注册表
	private final BeanDefinitionRegistry registry;

	// 20201214 @ComponentScan解析器
	private final ComponentScanAnnotationParser componentScanParser;

	// 20201214 Conditional条件判断器
	private final ConditionEvaluator conditionEvaluator;

	// 20021214 配置组件Class缓存
	private final Map<ConfigurationClass, ConfigurationClass> configurationClasses = new LinkedHashMap<>();

	private final Map<String, ConfigurationClass> knownSuperclasses = new HashMap<>();

	private final List<String> propertySourceNames = new ArrayList<>();

	// 20201215 Import栈
	private final ImportStack importStack = new ImportStack();

    // 20201215 DeferredImportSelector处理器
	private final DeferredImportSelectorHandler deferredImportSelectorHandler = new DeferredImportSelectorHandler();

	private final SourceClass objectSourceClass = new SourceClass(Object.class);

	/**
	 * Create a new {@link ConfigurationClassParser} instance that will be used
	 * to populate the set of configuration classes.
	 */
	// 20201214 创建一个新的{@link ConfigurationClassParser}实例，该实例将用于填充配置类集。
	public ConfigurationClassParser(MetadataReaderFactory metadataReaderFactory,
									ProblemReporter problemReporter,
									Environment environment,
									ResourceLoader resourceLoader,
									BeanNameGenerator componentScanBeanNameGenerator,
									BeanDefinitionRegistry registry) {

		// 20201214 元数据读取工厂
		this.metadataReaderFactory = metadataReaderFactory;

		// 20201214 beanDefinition解析报告器
		this.problemReporter = problemReporter;

		// 20201214 环境实例
		this.environment = environment;

		// 20201214 资源加载器
		this.resourceLoader = resourceLoader;

		// 20201214 注册表
		this.registry = registry;

		// 20201214 @ComponentScan解析器
		this.componentScanParser = new ComponentScanAnnotationParser(
				environment, resourceLoader, componentScanBeanNameGenerator, registry);

		// 20201214 Conditional条件判断器
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, resourceLoader);
	}

	// 20201214 【自动装配重点】解析配置组件集合
	public void parse(Set<BeanDefinitionHolder> configCandidates) {
		// 20201214 遍历配置组件集合
		for (BeanDefinitionHolder holder : configCandidates) {
			// 20201214 返回包装的BeanDefinition。
			BeanDefinition bd = holder.getBeanDefinition();
			try {
				// 20201214 @Configuration属于AnnotatedBeanDefinition
				if (bd instanceof AnnotatedBeanDefinition) {
					// 20201214 【自动装配重点】解析@Configuration Class的BeanDefinition -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
					parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
				}
				else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
					parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
				}
				else {
					parse(bd.getBeanClassName(), holder.getBeanName());
				}
			}
			catch (BeanDefinitionStoreException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new BeanDefinitionStoreException(
						"Failed to parse configuration class [" + bd.getBeanClassName() + "]", ex);
			}
		}

	    // 20201215 DeferredImportSelector处理器 -> 对spring.factories配置类全限定类名列表再过滤器一次
		this.deferredImportSelectorHandler.process();
	}

	protected final void parse(@Nullable String className, String beanName) throws IOException {
		Assert.notNull(className, "No bean class name for configuration class bean definition");
		MetadataReader reader = this.metadataReaderFactory.getMetadataReader(className);
		processConfigurationClass(new ConfigurationClass(reader, beanName), DEFAULT_EXCLUSION_FILTER);
	}

	protected final void parse(Class<?> clazz, String beanName) throws IOException {
		processConfigurationClass(new ConfigurationClass(clazz, beanName), DEFAULT_EXCLUSION_FILTER);
	}

	// 20201214 【自动装配重点】解析@Configuration Class的BeanDefinition -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
	protected final void parse(AnnotationMetadata metadata, String beanName) throws IOException {
        // 20201214 【自动装配重点】处理@Configuration Class -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
		processConfigurationClass(new ConfigurationClass(metadata, beanName), DEFAULT_EXCLUSION_FILTER);
	}

	/**
	 * Validate each {@link ConfigurationClass} object.
	 * @see ConfigurationClass#validate
	 */
	// 20201215 验证每个{@link ConfigurationClass}对象。
	public void validate() {
		for (ConfigurationClass configClass : this.configurationClasses.keySet()) {
			configClass.validate(this.problemReporter);
		}
	}

	// 20201215 获取过滤、排序、验证过后的{@link ConfigurationClass}对象集合
	public Set<ConfigurationClass> getConfigurationClasses() {
		return this.configurationClasses.keySet();
	}

	// 20201214 【自动装配重点】处理@Configuration Class -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
	protected void processConfigurationClass(ConfigurationClass configClass, Predicate<String> filter) throws IOException {
		// 20201214 注解评估: 如果@Configuration应跳过该项注解的bean注册
		if (this.conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.PARSE_CONFIGURATION)) {
			// 20201214 则直接返回无需处理, 第一次自动配置, 不会进到这里
			return;
		}

		// 20021214 从配置组件Class缓存中获取该@Configuration Class
		ConfigurationClass existingClass = this.configurationClasses.get(configClass);

		// 20201214 如果已存在
		if (existingClass != null) {
			// 20201213 判断此配置类是通过{@link Import}注册还是由于嵌套在另一个配置类中而自动注册。
			if (configClass.isImported()) {
				if (existingClass.isImported()) {
					// 20201214 是的话, 则将给定配置类的import-by声明合并到此声明中。
					existingClass.mergeImportedBy(configClass);
				}

				// 20201214 否则，忽略新导入的配置类； 现有的非导入类将覆盖它。
				// Otherwise ignore new imported config class; existing non-imported class overrides it.
				return;
			}
			else {
				// 20201214 找到明确的bean定义，可能替换了导入。 让我们删除旧的，然后使用新的。
				// Explicit bean definition found, probably replacing an import.
				// Let's remove the old one and go with the new one.
				this.configurationClasses.remove(configClass);
				this.knownSuperclasses.values().removeIf(configClass::equals);
			}
		}

		// Recursively process the configuration class and its superclass hierarchy. // 20201214 递归处理配置类及其超类层次结构。
		// 20201214 对注解类型进行包装: 使带注释的源类可以以统一的方式处理，而不管它们如何加载
		SourceClass sourceClass = asSourceClass(configClass, filter);
		do {
			// 20201214 【自动装配重点】通过阅读源类中的注释，成员和方法，应用处理并构建完整的{@link ConfigurationClass} -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
			sourceClass = doProcessConfigurationClass(configClass, sourceClass, filter);
		}
		while (sourceClass != null);

		// 20201215 在配置组件Class缓存中注册该配置类 -> MyApplication
		this.configurationClasses.put(configClass, configClass);
	}

	/**
	 * // 20201214 通过阅读源类中的注释，成员和方法，应用处理并构建完整的{@link ConfigurationClass}。 发现相关来源后，可以多次调用此方法。
	 * Apply processing and build a complete {@link ConfigurationClass} by reading the
	 * annotations, members and methods from the source class. This method can be called
	 * multiple times as relevant sources are discovered.
	 * @param configClass the configuration class being build
	 * @param sourceClass a source class
	 * @return the superclass, or {@code null} if none found or previously processed
	 */
	// 20201214 【自动装配重点】通过阅读源类中的注释，成员和方法，应用处理并构建完整的{@link ConfigurationClass}
	@Nullable
	protected final SourceClass doProcessConfigurationClass(
			ConfigurationClass configClass, SourceClass sourceClass, Predicate<String> filter)
			throws IOException {

		// 20201214 如果该Class上存在@Component注解
		if (configClass.getMetadata().isAnnotated(Component.class.getName())) {
			// Recursively process any member (nested) classes first // 20201214 首先递归处理任何成员（嵌套）类
			// 20201214 注册碰巧是本身的成员（嵌套）类也是配置类
			processMemberClasses(configClass, sourceClass, filter);
		}

		// Process any @PropertySource annotations // 20201214 处理任何@PropertySource批注
		// 20201214 获取@PropertySources、@PropertySource注解属性
		for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
				sourceClass.getMetadata(),
				PropertySources.class,
				org.springframework.context.annotation.PropertySource.class)) {
			if (this.environment instanceof ConfigurableEnvironment) {
				// 20201214 处理给定的@PropertySource批注元数据。
				processPropertySource(propertySource);
			}
			else {
				logger.info("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
						"]. Reason: Environment must implement ConfigurableEnvironment");
			}
		}

		// Process any @ComponentScan annotations // 20201214 处理任何@ComponentScan批注
		// 20201214 获取@ComponentScans、@ComponentScan注解属性
		Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
				sourceClass.getMetadata(),
				ComponentScans.class,
				ComponentScan.class);

		// 20201214 如果@Configuration不该跳过 且 @ComponentScans、@ComponentScan注解属性不为空
		if (!componentScans.isEmpty() && !this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
			// 20201214 遍历所有@ComponentScans、@ComponentScan注解属性
			for (AnnotationAttributes componentScan : componentScans) {
				// 20201214 使用@ComponentScan注释配置类->立即执行扫描
				// The config class is annotated with @ComponentScan -> perform the scan immediately
				// 20201214 解析@ComponentScans、@ComponentScan注解属性
				Set<BeanDefinitionHolder> scannedBeanDefinitions =
						this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());

				// 20201215 检查扫描的定义集是否有其他配置类，并在需要时递归解析
				// Check the set of scanned definitions for any further config classes and parse recursively if needed
				for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
					BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
					if (bdCand == null) {
						bdCand = holder.getBeanDefinition();
					}
					if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
						parse(bdCand.getBeanClassName(), holder.getBeanName());
					}
				}
			}
		}

		// Process any @Import annotations // 20201215 处理任何@Import批注
        // 20201215 【自动装配重点】处理@Import注解 -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
		processImports(
				configClass,
				sourceClass,
				// 20201215【自动装配重点】递归解析@Import注解 -> 返回收集到的import值的集合
				getImports(sourceClass),
				filter,
				true
		);

		// Process any @ImportResource annotations	// 20201215 处理任何@ImportResource批注
		AnnotationAttributes importResource =
				AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
		if (importResource != null) {
			String[] resources = importResource.getStringArray("locations");
			Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
			for (String resource : resources) {
				String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
				configClass.addImportedResource(resolvedResource, readerClass);
			}
		}

		// Process individual @Bean methods // 20201215 处理单个@Bean方法
		Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
		for (MethodMetadata methodMetadata : beanMethods) {
			configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
		}

		// Process default methods on interfaces // 20201215 处理接口上的默认方法
		processInterfaces(configClass, sourceClass);

		// Process superclass, if any // 20201215 处理超类（如果有）
		if (sourceClass.getMetadata().hasSuperClass()) {
			String superclass = sourceClass.getMetadata().getSuperClassName();
			if (superclass != null && !superclass.startsWith("java") &&
					!this.knownSuperclasses.containsKey(superclass)) {
				this.knownSuperclasses.put(superclass, configClass);
				// Superclass found, return its annotation metadata and recurse // 20201215 找到超类，返回其注释元数据并递归
				return sourceClass.getSuperClass();
			}
		}

		// No superclass -> processing is complete // 20201215 没有超类->处理完成
		return null;
	}

	/**
	 * Register member (nested) classes that happen to be configuration classes themselves.
	 */
	// 20201214 注册碰巧是本身的成员（嵌套）类也是配置类
	private void processMemberClasses(ConfigurationClass configClass, SourceClass sourceClass,
			Predicate<String> filter) throws IOException {

		// 20201214 获取嵌套注解的Class -> 主类时返回空的Class成员列表
		Collection<SourceClass> memberClasses = sourceClass.getMemberClasses();
		if (!memberClasses.isEmpty()) {
			List<SourceClass> candidates = new ArrayList<>(memberClasses.size());
			for (SourceClass memberClass : memberClasses) {
				if (ConfigurationClassUtils.isConfigurationCandidate(memberClass.getMetadata()) &&
						!memberClass.getMetadata().getClassName().equals(configClass.getMetadata().getClassName())) {
					candidates.add(memberClass);
				}
			}
			OrderComparator.sort(candidates);
			for (SourceClass candidate : candidates) {
				if (this.importStack.contains(configClass)) {
					this.problemReporter.error(new CircularImportProblem(configClass, this.importStack));
				}
				else {
					this.importStack.push(configClass);
					try {
						processConfigurationClass(candidate.asConfigClass(configClass), filter);
					}
					finally {
						this.importStack.pop();
					}
				}
			}
		}
	}

	/**
	 * Register default methods on interfaces implemented by the configuration class.
	 */
	private void processInterfaces(ConfigurationClass configClass, SourceClass sourceClass) throws IOException {
		for (SourceClass ifc : sourceClass.getInterfaces()) {
			Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(ifc);
			for (MethodMetadata methodMetadata : beanMethods) {
				if (!methodMetadata.isAbstract()) {
					// A default method or other concrete method on a Java 8+ interface...
					configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
				}
			}
			processInterfaces(configClass, ifc);
		}
	}

	/**
	 * Retrieve the metadata for all <code>@Bean</code> methods.
	 */
	private Set<MethodMetadata> retrieveBeanMethodMetadata(SourceClass sourceClass) {
		AnnotationMetadata original = sourceClass.getMetadata();
		Set<MethodMetadata> beanMethods = original.getAnnotatedMethods(Bean.class.getName());
		if (beanMethods.size() > 1 && original instanceof StandardAnnotationMetadata) {
			// Try reading the class file via ASM for deterministic declaration order...
			// Unfortunately, the JVM's standard reflection returns methods in arbitrary
			// order, even between different runs of the same application on the same JVM.
			try {
				AnnotationMetadata asm =
						this.metadataReaderFactory.getMetadataReader(original.getClassName()).getAnnotationMetadata();
				Set<MethodMetadata> asmMethods = asm.getAnnotatedMethods(Bean.class.getName());
				if (asmMethods.size() >= beanMethods.size()) {
					Set<MethodMetadata> selectedMethods = new LinkedHashSet<>(asmMethods.size());
					for (MethodMetadata asmMethod : asmMethods) {
						for (MethodMetadata beanMethod : beanMethods) {
							if (beanMethod.getMethodName().equals(asmMethod.getMethodName())) {
								selectedMethods.add(beanMethod);
								break;
							}
						}
					}
					if (selectedMethods.size() == beanMethods.size()) {
						// All reflection-detected methods found in ASM method set -> proceed
						beanMethods = selectedMethods;
					}
				}
			}
			catch (IOException ex) {
				logger.debug("Failed to read class file via ASM for determining @Bean method order", ex);
				// No worries, let's continue with the reflection metadata we started with...
			}
		}
		return beanMethods;
	}

	/**
	 * Process the given <code>@PropertySource</code> annotation metadata.
	 * @param propertySource metadata for the <code>@PropertySource</code> annotation found
	 * @throws IOException if loading a property source failed
	 */
	// 20201214 处理给定的@PropertySource批注元数据。
	private void processPropertySource(AnnotationAttributes propertySource) throws IOException {
		String name = propertySource.getString("name");
		if (!StringUtils.hasLength(name)) {
			name = null;
		}
		String encoding = propertySource.getString("encoding");
		if (!StringUtils.hasLength(encoding)) {
			encoding = null;
		}
		String[] locations = propertySource.getStringArray("value");
		Assert.isTrue(locations.length > 0, "At least one @PropertySource(value) location is required");
		boolean ignoreResourceNotFound = propertySource.getBoolean("ignoreResourceNotFound");

		Class<? extends PropertySourceFactory> factoryClass = propertySource.getClass("factory");
		PropertySourceFactory factory = (factoryClass == PropertySourceFactory.class ?
				DEFAULT_PROPERTY_SOURCE_FACTORY : BeanUtils.instantiateClass(factoryClass));

		for (String location : locations) {
			try {
				String resolvedLocation = this.environment.resolveRequiredPlaceholders(location);
				Resource resource = this.resourceLoader.getResource(resolvedLocation);
				addPropertySource(factory.createPropertySource(name, new EncodedResource(resource, encoding)));
			}
			catch (IllegalArgumentException | FileNotFoundException | UnknownHostException | SocketException ex) {
				// Placeholders not resolvable or resource not found when trying to open it
				if (ignoreResourceNotFound) {
					if (logger.isInfoEnabled()) {
						logger.info("Properties location [" + location + "] not resolvable: " + ex.getMessage());
					}
				}
				else {
					throw ex;
				}
			}
		}
	}

	private void addPropertySource(PropertySource<?> propertySource) {
		String name = propertySource.getName();
		MutablePropertySources propertySources = ((ConfigurableEnvironment) this.environment).getPropertySources();

		if (this.propertySourceNames.contains(name)) {
			// We've already added a version, we need to extend it
			PropertySource<?> existing = propertySources.get(name);
			if (existing != null) {
				PropertySource<?> newSource = (propertySource instanceof ResourcePropertySource ?
						((ResourcePropertySource) propertySource).withResourceName() : propertySource);
				if (existing instanceof CompositePropertySource) {
					((CompositePropertySource) existing).addFirstPropertySource(newSource);
				}
				else {
					if (existing instanceof ResourcePropertySource) {
						existing = ((ResourcePropertySource) existing).withResourceName();
					}
					CompositePropertySource composite = new CompositePropertySource(name);
					composite.addPropertySource(newSource);
					composite.addPropertySource(existing);
					propertySources.replace(name, composite);
				}
				return;
			}
		}

		if (this.propertySourceNames.isEmpty()) {
			propertySources.addLast(propertySource);
		}
		else {
			String firstProcessed = this.propertySourceNames.get(this.propertySourceNames.size() - 1);
			propertySources.addBefore(firstProcessed, propertySource);
		}
		this.propertySourceNames.add(name);
	}


	/**
	 * Returns {@code @Import} class, considering all meta-annotations.
	 */
	// 20201215【自动装配重点】考虑所有元注释，返回{@code @Import}类 -> 返回收集到的import值的集合
	private Set<SourceClass> getImports(SourceClass sourceClass) throws IOException {
		// 20201215 初始化imports、visited集合
		Set<SourceClass> imports = new LinkedHashSet<>();
		Set<SourceClass> visited = new LinkedHashSet<>();

		// 20201215 递归收集所有声明的{@code @Import}值 -> 收集到imports结果集中
		collectImports(sourceClass, imports, visited);

		// 20201215 返回收集到的import值的集合
		return imports;
	}

	/**
	 * 20201215
	 * A. 递归收集所有声明的{@code @Import}值。 与大多数元注释不同，使用多个声明为不同值的{@code @Import}是有效的。 从类的第一个元注释返回值的常规过程还不够。
	 * B. 例如，除了源自{@code @Enable}注释的元导入之外，{@code @Configuration}类通常还声明直接{@code @Import}。
	 */
	/**
	 * A.
	 * Recursively collect all declared {@code @Import} values. Unlike most
	 * meta-annotations it is valid to have several {@code @Import}s declared with
	 * different values; the usual process of returning values from the first
	 * meta-annotation on a class is not sufficient.
	 *
	 * B.
	 * <p>For example, it is common for a {@code @Configuration} class to declare direct
	 * {@code @Import}s in addition to meta-imports originating from an {@code @Enable}
	 * annotation.
	 * @param sourceClass the class to search	// 20201215 要搜索的Class
	 * @param imports the imports collected so far	// 20201215 到目前为止收集的进口
	 * @param visited used to track visited classes to prevent infinite recursion	// 20201215 用于跟踪访问的类以防止无限递归
	 * @throws IOException if there is any problem reading metadata from the named class
	 */
	// 20201215 递归收集所有声明的{@code @Import}值 -> 收集到imports结果集中
	private void collectImports(SourceClass sourceClass, Set<SourceClass> imports, Set<SourceClass> visited)
			throws IOException {
		// 20201215 该Class添加到已访问列表中
		if (visited.add(sourceClass)) {
			// 20201215 遍历当前源Class的所有注解
			for (SourceClass annotation : sourceClass.getAnnotations()) {
				// 20201215 获取注解类型
				String annName = annotation.getMetadata().getClassName();

				// 20201215 如果当前注解不为Import类型
				if (!annName.equals(Import.class.getName())) {
					// 20201215 则递归遍历当前注解
					collectImports(annotation, imports, visited);
				}
			}

			// 20201215 遍历完当前类的所有注解, 添加Import注解的值进imports结果集中
			imports.addAll(
					// 20201215 source.getAnnotationAttributes(): 获取指定注解类型的指定注解属性值 -> SourceClass列表
					sourceClass.getAnnotationAttributes(Import.class.getName(),
					"value")
			);
		}
	}

	// 20201215【自动装配重点】处理@Import注解 -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
	private void processImports(ConfigurationClass configClass, // 20201215 当前配置类
								SourceClass currentSourceClass, // 20201215 当前封装好的配置类
								Collection<SourceClass> importCandidates, // 20201215 收集到的所有@Import值的集合
								Predicate<String> exclusionFilter, // 20201215 默认过滤"java.lang.annotation." 以及 "org.springframework.stereotype."类
								boolean checkForCircularImports// 20201215 是否检查常规的Imports: 默认为true
	) {
		// 20201215 收集到的所有@Import值的集合为空时, 则直接返回
		if (importCandidates.isEmpty()) {
			return;
		}

		// 20201215 如果要检查常规Imports, 且当前配置类是否出现在Import栈上
		if (checkForCircularImports && isChainedImportOnStack(configClass)) {
			// 20201215 则beanDefinition解析报告器注册@Import注解异常问题
			this.problemReporter.error(new CircularImportProblem(configClass, this.importStack));
		}
		else {
			// 20201215 如果@Import注解校验通过, 则添加当前配置类到Import栈
			this.importStack.push(configClass);
			try {
				// 20201215 遍历收集到的所有@Import值的集合
				for (SourceClass candidate : importCandidates) {
					// 20201215 如果当前@Import值候选组件属于ImportSelector类型
					if (candidate.isAssignable(ImportSelector.class)) {
						// Candidate class is an ImportSelector -> delegate to it to determine imports // 20201215 候选类是一个ImportSelector->委托给它以确定导入
						// 20201215 加载当前候选组件Class文件
						Class<?> candidateClass = candidate.loadClass();

						// 20201215 使用适当的构造函数实例化一个类，并以指定的可分配类型返回新实例, 且当前实例具有某些意识功能: BeanClassLoader、BeanFactory、Environment、ResourceLoader
						ImportSelector selector = ParserStrategyUtils.instantiateClass(candidateClass, ImportSelector.class,
								this.environment, this.resourceLoader, this.registry);

						// 202012125 获取注解排除过滤操作 -> 空实现
						Predicate<String> selectorFilter = selector.getExclusionFilter();

						// 20201215 如果不为空, 则对当前传入的过滤器进行或运算, 得到过滤器并集
						if (selectorFilter != null) {
							exclusionFilter = exclusionFilter.or(selectorFilter);
						}

						// 20201215 AutoConfigurationImportSelector本身就是DeferredImportSelector
						if (selector instanceof DeferredImportSelector) {
                            // 20201215 【自动装配重点】处理指定的{@link DeferredImportSelector} -> 添加到DeferredImportSelector列表中
							this.deferredImportSelectorHandler.handle(configClass, (DeferredImportSelector) selector);
						}
						else {
							String[] importClassNames = selector.selectImports(currentSourceClass.getMetadata());
							Collection<SourceClass> importSourceClasses = asSourceClasses(importClassNames, exclusionFilter);
							processImports(configClass, currentSourceClass, importSourceClasses, exclusionFilter, false);
						}
					}
					else if (candidate.isAssignable(ImportBeanDefinitionRegistrar.class)) {
						// Candidate class is an ImportBeanDefinitionRegistrar ->
						// delegate to it to register additional bean definitions
						Class<?> candidateClass = candidate.loadClass();
						ImportBeanDefinitionRegistrar registrar =
								ParserStrategyUtils.instantiateClass(candidateClass, ImportBeanDefinitionRegistrar.class,
										this.environment, this.resourceLoader, this.registry);
						configClass.addImportBeanDefinitionRegistrar(registrar, currentSourceClass.getMetadata());
					}
					else {
						// Candidate class not an ImportSelector or ImportBeanDefinitionRegistrar ->
						// process it as an @Configuration class
						this.importStack.registerImport(
								currentSourceClass.getMetadata(), candidate.getMetadata().getClassName());
						processConfigurationClass(candidate.asConfigClass(configClass), exclusionFilter);
					}
				}
			}
			catch (BeanDefinitionStoreException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new BeanDefinitionStoreException(
						"Failed to process import candidates for configuration class [" +
						configClass.getMetadata().getClassName() + "]", ex);
			}
			finally {
				this.importStack.pop();
			}
		}
	}

	// 20201215 判断当前配置类是否出现在Import栈上
	private boolean isChainedImportOnStack(ConfigurationClass configClass) {
		// 20201215 如果Import栈包含当前配置类, 代表当前配置类已经检查过了
		if (this.importStack.contains(configClass)) {
			// 20201215 获取当前配置类的Class名称
			String configClassName = configClass.getMetadata().getClassName();

			// 20201215 根据注解类型, 获取已经Import进来的注解元数据集合中元数据
			AnnotationMetadata importingClass = this.importStack.getImportingClassFor(configClassName);

			// 20201215 如果元数据存在
			while (importingClass != null) {
				// 20201215 注解元数据基础类型等于当前配置类型
				if (configClassName.equals(importingClass.getClassName())) {
					// 20201215 则说明当前配置类确实import栈上
					return true;
				}

				// 20201215 继续根据注解类型, 获取已经Import进来的注解元数据集合中元数据
				importingClass = this.importStack.getImportingClassFor(importingClass.getClassName());
			}
		}

		// 20201215 如果一层层找都没找到, 则返回false, 说明当前配置不在import栈上
		return false;
	}

	ImportRegistry getImportRegistry() {
		return this.importStack;
	}


	/**
	 * Factory method to obtain a {@link SourceClass} from a {@link ConfigurationClass}.
	 */
	// 20201214 对注解类型进行包装: 使带注释的源类可以以统一的方式处理，而不管它们如何加载
	private SourceClass asSourceClass(ConfigurationClass configurationClass, Predicate<String> filter) throws IOException {
		// 20201214 获取@Configuration类注解元数据
		AnnotationMetadata metadata = configurationClass.getMetadata();

		// 20201214 @Configuration为StandardAnnotationMetadata类型
		if (metadata instanceof StandardAnnotationMetadata) {
			// 20201214 对注解类型进行包装: 使带注释的源类可以以统一的方式处理，而不管它们如何加载
			return asSourceClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass(), filter);
		}
		return asSourceClass(metadata.getClassName(), filter);
	}

	/**
	 * // 20201214 从{@link Class}获取{@link SourceClass}的工厂方法
	 * Factory method to obtain a {@link SourceClass} from a {@link Class}.
	 */
	// 20201214 对注解类型进行包装: 使带注释的源类可以以统一的方式处理，而不管它们如何加载
	SourceClass asSourceClass(@Nullable Class<?> classType, Predicate<String> filter) throws IOException {
		// 20201214 过滤"java.lang.annotation." 以及 "org.springframework.stereotype."类
		if (classType == null || filter.test(classType.getName())) {
			// 20201214 如果过滤到则直接返回, 因为他们是基础的注解类型, 不用再包装
			return this.objectSourceClass;
		}
		try {
			// 20201214 健全性测试，我们可以反射性地读取批注，包括Class属性； 如果不是->退回到ASM
			// Sanity test that we can reflectively read annotations,
			// including Class attributes; if not -> fall back to ASM
			// 20201214 遍历检查该类下所有注解声明的属性
			for (Annotation ann : classType.getDeclaredAnnotations()) {
				AnnotationUtils.validateAnnotation(ann);
			}

			// 20201214 对注解类型进行包装: 使带注释的源类可以以统一的方式处理，而不管它们如何加载
			return new SourceClass(classType);
		}
		catch (Throwable ex) {
			// Enforce ASM via class name resolution // 20201214 通过类名解析实施ASM
			return asSourceClass(classType.getName(), filter);
		}
	}

	/**
	 * Factory method to obtain a {@link SourceClass} collection from class names.
	 */
	private Collection<SourceClass> asSourceClasses(String[] classNames, Predicate<String> filter) throws IOException {
		List<SourceClass> annotatedClasses = new ArrayList<>(classNames.length);
		for (String className : classNames) {
			annotatedClasses.add(asSourceClass(className, filter));
		}
		return annotatedClasses;
	}

	/**
	 * Factory method to obtain a {@link SourceClass} from a class name.
	 */
	SourceClass asSourceClass(@Nullable String className, Predicate<String> filter) throws IOException {
		if (className == null || filter.test(className)) {
			return this.objectSourceClass;
		}
		if (className.startsWith("java")) {
			// Never use ASM for core java types
			try {
				return new SourceClass(ClassUtils.forName(className, this.resourceLoader.getClassLoader()));
			}
			catch (ClassNotFoundException ex) {
				throw new NestedIOException("Failed to load class [" + className + "]", ex);
			}
		}
		return new SourceClass(this.metadataReaderFactory.getMetadataReader(className));
	}


	@SuppressWarnings("serial")
	private static class ImportStack extends ArrayDeque<ConfigurationClass> implements ImportRegistry {

		// 20201215 已经Import进来的注解元数据集合
		private final MultiValueMap<String, AnnotationMetadata> imports = new LinkedMultiValueMap<>();

		public void registerImport(AnnotationMetadata importingClass, String importedClass) {
			this.imports.add(importedClass, importingClass);
		}

		// 20201215 根据注解类型, 获取已经Import进来的注解元数据集合中元数据
		@Override
		@Nullable
		public AnnotationMetadata getImportingClassFor(String importedClass) {
			// 20201215 根据注解类型, 获取已经Import进来的注解元数据集合中元数据
			return CollectionUtils.lastElement(this.imports.get(importedClass));
		}

		@Override
		public void removeImportingClass(String importingClass) {
			for (List<AnnotationMetadata> list : this.imports.values()) {
				for (Iterator<AnnotationMetadata> iterator = list.iterator(); iterator.hasNext();) {
					if (iterator.next().getClassName().equals(importingClass)) {
						iterator.remove();
						break;
					}
				}
			}
		}

		/**
		 * Given a stack containing (in order)
		 * <ul>
		 * <li>com.acme.Foo</li>
		 * <li>com.acme.Bar</li>
		 * <li>com.acme.Baz</li>
		 * </ul>
		 * return "[Foo->Bar->Baz]".
		 */
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner("->", "[", "]");
			for (ConfigurationClass configurationClass : this) {
				joiner.add(configurationClass.getSimpleName());
			}
			return joiner.toString();
		}
	}

    // 20201215 DeferredImportSelector处理器
	private class DeferredImportSelectorHandler {

        // 20201215 DeferredImportSelector列表
		@Nullable
		private List<DeferredImportSelectorHolder> deferredImportSelectors = new ArrayList<>();

        /**
         * 20201215
         * 处理指定的{@link DeferredImportSelector}。 如果要收集延迟的导入选择器，则会将此实例注册到列表中。 如果正在处理它们，则
         * {@link DeferredImportSelector}也会根据其{@link DeferredImportSelector.Group}立即进行处理。
         */
		/**
		 * Handle the specified {@link DeferredImportSelector}. If deferred import
		 * selectors are being collected, this registers this instance to the list. If
		 * they are being processed, the {@link DeferredImportSelector} is also processed
		 * immediately according to its {@link DeferredImportSelector.Group}.
		 * @param configClass the source configuration class
		 * @param importSelector the selector to handle
		 */
		// 20201215 【自动装配重点】处理指定的{@link DeferredImportSelector} -> 添加到DeferredImportSelector列表中
		public void handle(ConfigurationClass configClass, DeferredImportSelector importSelector) {
		    // 20201215 构建DeferredImportSelector包装类
			DeferredImportSelectorHolder holder = new DeferredImportSelectorHolder(configClass, importSelector);

			// 20201215 如果DeferredImportSelector列表未初始化
			if (this.deferredImportSelectors == null) {
                // 20201215 构建DeferredImportSelector批量处理器, 然后注册该列表
				DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler();

                // 20201215 设置注解配置类集合 -> 添加配置注解元数据 以及 注解类型
				handler.register(holder);

				// 20201215 【自动装配重点】处理导入的元数据Group -> 获取过滤排序后的配置类全限定类名列表
				handler.processGroupImports();
			}
			else {
			    // 20201215 如果DeferredImportSelector列表已初始化, 则添加到DeferredImportSelector列表中
				this.deferredImportSelectors.add(holder);
			}
		}

		// 20201215 【自动装配重点】DeferredImportSelector处理器执行处理操作
		public void process() {
		    // 20201215 获取DeferredImportSelector列表
			List<DeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;

            // 20201215 清空DeferredImportSelector列表
			this.deferredImportSelectors = null;
			try {
			    // 20201215 如果deferredImports列表不为空
				if (deferredImports != null) {
                    // 20201215 构建DeferredImportSelector批量处理器
					DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler();

                    // 20201215 使用{@code AnnotationAwareOrderComparator}的共享默认实例进行排序
					deferredImports.sort(DEFERRED_IMPORT_COMPARATOR);

					// 20201215 注解配置类集合添加注解元数据 以及 注解类型
					deferredImports.forEach(handler::register);

					// 20201215 处理导入的元数据Group -> 获取过滤排序后的配置类全限定类名列表
					handler.processGroupImports();
				}
			}
			finally {
                // 20201215 重新初始化DeferredImportSelector列表
				this.deferredImportSelectors = new ArrayList<>();
			}
		}
	}

    // 20201215 构建DeferredImportSelector批量处理器
	private class DeferredImportSelectorGroupingHandler {
        // 20201215 DeferredImportSelector组实例列表
		private final Map<Object, DeferredImportSelectorGrouping> groupings = new LinkedHashMap<>();

		// 20201215 注解配置类集合
		private final Map<AnnotationMetadata, ConfigurationClass> configurationClasses = new HashMap<>();

		// 20201215 设置注解配置类集合 -> 添加配置注解元数据 以及 注解类型
		public void register(DeferredImportSelectorHolder deferredImport) {
            // 20201215 通过DeferredImportSelector包装器获取DeferredImportSelector自动导入选择器, 然后获取自动配置导入组Class
			Class<? extends Group> group = deferredImport.getImportSelector().getImportGroup();

            // 20201215 DeferredImportSelector组实例列表
			DeferredImportSelectorGrouping grouping = this.groupings.computeIfAbsent(
					(group != null ? group : deferredImport),

                    // 20201215 通过用于对来自不同导入选择器的结果进行分组的接口Group实例构建DeferredImportSelector组实例
					key -> new DeferredImportSelectorGrouping(
                            // 20201215 构建Group实例
					        createGroup(group)
                    )
            );

            // 20201215 Group实例列表添加DeferredImportSelectorHolder
			grouping.add(deferredImport);

            // 20201215 注解配置类集合添加注解元数据 以及 注解类型
			this.configurationClasses.put(deferredImport.getConfigurationClass().getMetadata(),
					deferredImport.getConfigurationClass());
		}

		// 20201215 处理导入的元数据Group -> 获取过滤排序后的配置类全限定类名列表
		public void processGroupImports() {
		    // 20201215 遍历/DeferredImportSelector组实例列表
			for (DeferredImportSelectorGrouping grouping : this.groupings.values()) {
                // 20201215 获取组件过滤操作 -> 返回取了并集的过滤器
				Predicate<String> exclusionFilter = grouping.getCandidateFilter();

				// 20201215 【自动装配重点】遍历该组已经排除且排序后的配置类的全限定类名列表
				grouping.getImports().forEach(entry -> {
					// 20201215 根据当前配置类的元数据取注解配置类集合中的配置类Class
					ConfigurationClass configurationClass = this.configurationClasses.get(entry.getMetadata());
					try {
						// 20201215【自动装配重点】处理@Import注解 -> 添加AutoConfigurationImportSelector到DeferredImportSelector列表中
						processImports(configurationClass, asSourceClass(configurationClass, exclusionFilter),
								Collections.singleton(asSourceClass(entry.getImportClassName(), exclusionFilter)),
								exclusionFilter, false);
					}
					catch (BeanDefinitionStoreException ex) {
						throw ex;
					}
					catch (Throwable ex) {
						throw new BeanDefinitionStoreException(
								"Failed to process import candidates for configuration class [" +
										configurationClass.getMetadata().getClassName() + "]", ex);
					}
				});
			}
		}

		// 20201215 构建Group实例
		private Group createGroup(@Nullable Class<? extends Group> type) {
			Class<? extends Group> effectiveType = (type != null ? type : DefaultDeferredImportSelectorGroup.class);
			return ParserStrategyUtils.instantiateClass(effectiveType, Group.class,
					ConfigurationClassParser.this.environment,
					ConfigurationClassParser.this.resourceLoader,
					ConfigurationClassParser.this.registry);
		}
	}

    // 20201215 DeferredImportSelector处理器
	private static class DeferredImportSelectorHolder {

	    // 20201215 @Configuration类
		private final ConfigurationClass configurationClass;

        // 20201215 DeferredImportSelector自动导入选择器
		private final DeferredImportSelector importSelector;

		// 20201215 构建DeferredImportSelector包装类
		public DeferredImportSelectorHolder(ConfigurationClass configClass, DeferredImportSelector selector) {
			this.configurationClass = configClass;
			this.importSelector = selector;
		}

		public ConfigurationClass getConfigurationClass() {
			return this.configurationClass;
		}

		// 20201215 通过DeferredImportSelector包装器获取DeferredImportSelector自动导入选择器
		public DeferredImportSelector getImportSelector() {
            // 20201215 DeferredImportSelector自动导入选择器
			return this.importSelector;
		}
	}

    // 20201215 DeferredImportSelector组实例
	private static class DeferredImportSelectorGrouping {

		private final DeferredImportSelector.Group group;

		// 20201215 DeferredImportSelector包装类列表
		private final List<DeferredImportSelectorHolder> deferredImports = new ArrayList<>();

		// 20201215 通过用于对来自不同导入选择器的结果进行分组的接口实例构建DeferredImportSelector组实例
		DeferredImportSelectorGrouping(Group group) {
			this.group = group;
		}

		public void add(DeferredImportSelectorHolder deferredImport) {
			this.deferredImports.add(deferredImport);
		}

		/**
		 * Return the imports defined by the group.
		 * @return each import with its associated configuration class
		 */
		// 20201215 【自动装配重点】返回由组定义的导入。
		public Iterable<Group.Entry> getImports() {
            // 20201215 遍历DeferredImportSelector包装类列表
			for (DeferredImportSelectorHolder deferredImport : this.deferredImports) {
				// 20201215 【自动装配重点】使用指定的{@link DeferredImportSelector}处理导入的{@link Configuration}类的{@link AnnotationMetadata}
				this.group.process(deferredImport.getConfigurationClass().getMetadata(), deferredImport.getImportSelector());
			}

			// 20201215 返回应为此组导入哪个类的{@link Entry条目} -> 对剩余的自动配置类的全限定类名列表进行排序, 并返回排序后的结果列表
			return this.group.selectImports();
		}

		// 20201215 获取组件过滤操作 -> 返回取了并集的过滤器
		public Predicate<String> getCandidateFilter() {
            // 20201215 默认过滤"java.lang.annotation." 以及 "org.springframework.stereotype."类
			Predicate<String> mergedFilter = DEFAULT_EXCLUSION_FILTER;

            // 20201215 遍历Group实例中的DeferredImportSelector包装类列表
			for (DeferredImportSelectorHolder deferredImport : this.deferredImports) {
                // 20201215 通过DeferredImportSelector包装器获取DeferredImportSelector自动导入选择器, 获取注解排除过滤操作
				Predicate<String> selectorFilter = deferredImport.getImportSelector().getExclusionFilter();

				// 20201215 如果过滤操作存在
				if (selectorFilter != null) {
				    // 20201215 则合并这些过滤器
					mergedFilter = mergedFilter.or(selectorFilter);
				}
			}

			// 20201215 返回取了并集的过滤器
			return mergedFilter;
		}
	}


	private static class DefaultDeferredImportSelectorGroup implements Group {

		private final List<Entry> imports = new ArrayList<>();

		@Override
		public void process(AnnotationMetadata metadata, DeferredImportSelector selector) {
			for (String importClassName : selector.selectImports(metadata)) {
				this.imports.add(new Entry(metadata, importClassName));
			}
		}

		@Override
		public Iterable<Entry> selectImports() {
			return this.imports;
		}
	}


	/**
	 * Simple wrapper that allows annotated source classes to be dealt with
	 * in a uniform manner, regardless of how they are loaded.
	 */
	// 20201214 一个简单的包装程序，使带注释的源类可以以统一的方式处理，而不管它们如何加载。
	private class SourceClass implements Ordered {

		// 20201214 类或MetadataReader
		private final Object source;  // Class or MetadataReader

		private final AnnotationMetadata metadata;

		public SourceClass(Object source) {
			this.source = source;
			if (source instanceof Class) {
				this.metadata = AnnotationMetadata.introspect((Class<?>) source);
			}
			else {
				this.metadata = ((MetadataReader) source).getAnnotationMetadata();
			}
		}

		public final AnnotationMetadata getMetadata() {
			return this.metadata;
		}

		@Override
		public int getOrder() {
			Integer order = ConfigurationClassUtils.getOrder(this.metadata);
			return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
		}

		public Class<?> loadClass() throws ClassNotFoundException {
			if (this.source instanceof Class) {
				return (Class<?>) this.source;
			}
			String className = ((MetadataReader) this.source).getClassMetadata().getClassName();
			return ClassUtils.forName(className, resourceLoader.getClassLoader());
		}

		public boolean isAssignable(Class<?> clazz) throws IOException {
			if (this.source instanceof Class) {
				return clazz.isAssignableFrom((Class<?>) this.source);
			}
			return new AssignableTypeFilter(clazz).match((MetadataReader) this.source, metadataReaderFactory);
		}

		public ConfigurationClass asConfigClass(ConfigurationClass importedBy) {
			if (this.source instanceof Class) {
				return new ConfigurationClass((Class<?>) this.source, importedBy);
			}
			return new ConfigurationClass((MetadataReader) this.source, importedBy);
		}

		// 20201214 获取嵌套注解的Class -> 主类时返回空的Class成员列表
		public Collection<SourceClass> getMemberClasses() throws IOException {
			// 20201214 获取类或MetadataReader
			Object sourceToProcess = this.source;

			// 20201214 如果为Class类型
			if (sourceToProcess instanceof Class) {
				Class<?> sourceClass = (Class<?>) sourceToProcess;
				try {
					// 20201214 则获取表示的类的成员的所有类和接口的Class数组, 明显主类没有内部类
					Class<?>[] declaredClasses = sourceClass.getDeclaredClasses();
					List<SourceClass> members = new ArrayList<>(declaredClasses.length);
					for (Class<?> declaredClass : declaredClasses) {
						members.add(asSourceClass(declaredClass, DEFAULT_EXCLUSION_FILTER));
					}

					// 20201214 则返回空的Class成员列表
					return members;
				}
				catch (NoClassDefFoundError err) {
					// getDeclaredClasses() failed because of non-resolvable dependencies
					// -> fall back to ASM below
					sourceToProcess = metadataReaderFactory.getMetadataReader(sourceClass.getName());
				}
			}

			// ASM-based resolution - safe for non-resolvable classes as well
			MetadataReader sourceReader = (MetadataReader) sourceToProcess;
			String[] memberClassNames = sourceReader.getClassMetadata().getMemberClassNames();
			List<SourceClass> members = new ArrayList<>(memberClassNames.length);
			for (String memberClassName : memberClassNames) {
				try {
					members.add(asSourceClass(memberClassName, DEFAULT_EXCLUSION_FILTER));
				}
				catch (IOException ex) {
					// Let's skip it if it's not resolvable - we're just looking for candidates
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to resolve member class [" + memberClassName +
								"] - not considering it as a configuration class candidate");
					}
				}
			}
			return members;
		}

		public SourceClass getSuperClass() throws IOException {
			if (this.source instanceof Class) {
				return asSourceClass(((Class<?>) this.source).getSuperclass(), DEFAULT_EXCLUSION_FILTER);
			}
			return asSourceClass(
					((MetadataReader) this.source).getClassMetadata().getSuperClassName(), DEFAULT_EXCLUSION_FILTER);
		}

		public Set<SourceClass> getInterfaces() throws IOException {
			Set<SourceClass> result = new LinkedHashSet<>();
			if (this.source instanceof Class) {
				Class<?> sourceClass = (Class<?>) this.source;
				for (Class<?> ifcClass : sourceClass.getInterfaces()) {
					result.add(asSourceClass(ifcClass, DEFAULT_EXCLUSION_FILTER));
				}
			}
			else {
				for (String className : this.metadata.getInterfaceNames()) {
					result.add(asSourceClass(className, DEFAULT_EXCLUSION_FILTER));
				}
			}
			return result;
		}

		// 20201215 source.getAnnotations(): 获取当前源Class的所有注解
		public Set<SourceClass> getAnnotations() {
			// 20201215 初始化收集结果集
			Set<SourceClass> result = new LinkedHashSet<>();

			// 20201215 如果当前源属于Class类型
			if (this.source instanceof Class) {
				Class<?> sourceClass = (Class<?>) this.source;

				// 20201215 则获取遍历源Class当前类的注解
				for (Annotation ann : sourceClass.getDeclaredAnnotations()) {
					// 20201215 获取每个注解的注解类型
					Class<?> annType = ann.annotationType();

					// 20201215 如果注解名称不是以java开头
					if (!annType.getName().startsWith("java")) {
						try {
							// 20201215 则对注解类型进行包装, 然后添加到结果集中
							result.add(asSourceClass(annType, DEFAULT_EXCLUSION_FILTER));
						}
						catch (Throwable ex) {
							// 20201215 JVM的类加载将忽略在类路径上不存在的注释->在此也忽略
							// An annotation not present on the classpath is being ignored
							// by the JVM's class loading -> ignore here as well.
						}
					}
				}
			}
			else {
				for (String className : this.metadata.getAnnotationTypes()) {
					if (!className.startsWith("java")) {
						try {
							result.add(getRelated(className));
						}
						catch (Throwable ex) {
							// An annotation not present on the classpath is being ignored
							// by the JVM's class loading -> ignore here as well.
						}
					}
				}
			}
			return result;
		}

		// 20201215 source.getAnnotationAttributes(): 获取指定注解类型的指定注解属性值 -> SourceClass列表
		public Collection<SourceClass> getAnnotationAttributes(String annType, String attribute) throws IOException {
			// 20201209 检索给定类型的注解的属性（如果有的话）（即，如果在基础元素上定义为直接注解或元注解），也要考虑对组合注解的属性覆盖。
			Map<String, Object> annotationAttributes = this.metadata.getAnnotationAttributes(annType, true);

			// 20201215 如果属性列表中没有指定的属性, 则返回空
			if (annotationAttributes == null || !annotationAttributes.containsKey(attribute)) {
				return Collections.emptySet();
			}

			// 20201215 否则获取指定属性
			String[] classNames = (String[]) annotationAttributes.get(attribute);
			Set<SourceClass> result = new LinkedHashSet<>();

			// 20201215 遍历该属性的所有值
			for (String className : classNames) {
				// 20201215 包装ClassName成SourceClass
				result.add(getRelated(className));
			}

			// 20201215 返回封装好的SourceClass列表
			return result;
		}

		// 20201215 包装ClassName成SourceClass
		private SourceClass getRelated(String className) throws IOException {
			if (this.source instanceof Class) {
				try {
					Class<?> clazz = ClassUtils.forName(className, ((Class<?>) this.source).getClassLoader());
					return asSourceClass(clazz, DEFAULT_EXCLUSION_FILTER);
				}
				catch (ClassNotFoundException ex) {
					// Ignore -> fall back to ASM next, except for core java types.
					if (className.startsWith("java")) {
						throw new NestedIOException("Failed to load class [" + className + "]", ex);
					}
					return new SourceClass(metadataReaderFactory.getMetadataReader(className));
				}
			}
			return asSourceClass(className, DEFAULT_EXCLUSION_FILTER);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof SourceClass &&
					this.metadata.getClassName().equals(((SourceClass) other).metadata.getClassName())));
		}

		@Override
		public int hashCode() {
			return this.metadata.getClassName().hashCode();
		}

		@Override
		public String toString() {
			return this.metadata.getClassName();
		}
	}


	/**
	 * {@link Problem} registered upon detection of a circular {@link Import}.
	 */
	// 202012115 在检测到通告{@link Import}时注册了{@link问题}。
	private static class CircularImportProblem extends Problem {

		public CircularImportProblem(ConfigurationClass attemptedImport, Deque<ConfigurationClass> importStack) {
			super(String.format("A circular @Import has been detected: " +
					"Illegal attempt by @Configuration class '%s' to import class '%s' as '%s' is " +
					"already present in the current import stack %s", importStack.element().getSimpleName(),
					attemptedImport.getSimpleName(), attemptedImport.getSimpleName(), importStack),
					new Location(importStack.element().getResource(), attemptedImport.getMetadata()));
		}
	}

}
