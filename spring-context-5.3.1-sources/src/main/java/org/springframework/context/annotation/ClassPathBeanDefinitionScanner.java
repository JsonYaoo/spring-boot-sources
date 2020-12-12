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

package org.springframework.context.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

/**
 * 20201204
 * A. 一个bean定义扫描器，它检测类路径上的bean候选者，并使用给定的注册表（{@code BeanFactory}或{@code ApplicationContext}）注册相应的bean定义。
 * B. 通过可配置的类型过滤器检测候选类。 默认过滤器包括用Spring的{@link org.springframework.stereotype.Component @Component}，
 *    {@ link org.springframework.stereotype.Repository @Repository}，{@ link org.springframework.stereotype.Service @Service} 或
 *    {@link org.springframework.stereotype.Controller @Controller}注释的类。
 * C. 还支持Java EE 6的{@link javax.annotation.ManagedBean}和SR-330的{@link javax.inject.Named}注释（如果有）。
 */
/**
 * A.
 * A bean definition scanner that detects bean candidates on the classpath,
 * registering corresponding bean definitions with a given registry ({@code BeanFactory}
 * or {@code ApplicationContext}).
 *
 * B.
 * <p>Candidate classes are detected through configurable type filters. The
 * default filters include classes that are annotated with Spring's
 * {@link org.springframework.stereotype.Component @Component},
 * {@link org.springframework.stereotype.Repository @Repository},
 * {@link org.springframework.stereotype.Service @Service}, or
 * {@link org.springframework.stereotype.Controller @Controller} stereotype.
 *
 * C.
 * <p>Also supports Java EE 6's {@link javax.annotation.ManagedBean} and
 * JSR-330's {@link javax.inject.Named} annotations, if available.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see AnnotationConfigApplicationContext#scan
 * @see org.springframework.stereotype.Component
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.stereotype.Service
 * @see org.springframework.stereotype.Controller
 */
// 20201204 bean定义扫描器: 扫描@Component、@Repository、@Service、@Controller
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	// 20201205 注册表
	private final BeanDefinitionRegistry registry;

	private BeanDefinitionDefaults beanDefinitionDefaults = new BeanDefinitionDefaults();

	@Nullable
	private String[] autowireCandidatePatterns;

	// 20201208 用于为bean定义生成bean名称的策略接口实例
	private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	// 20201209 解决bean定义范围的策略接口 -> 设置枚举各种作用域代理选项为 不要创建作用域代理
	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	// 20201209 是否注册注解配置 -> 默认为true
	private boolean includeAnnotationConfig = true;

	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory.
	 *
	 * // 20201204 注册{@code BeanFactory}以便以{@code BeanDefinitionRegistry}的形式将bean定义加载到其中
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 */
	// 20201204 为给定的bean工厂创建一个新的{@code ClassPathBeanDefinitionScanner}。
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		// 20201204 包含默认过滤器(四大注解)
		this(registry, true);
	}

	/**
	 * 20201205
	 * A. 为给定的bean工厂创建一个新的{@code ClassPathBeanDefinitionScanner}。 如果传入的bean工厂不仅实现了{@code BeanDefinitionRegistry}接口，而且还实现了
	 *    {@code ResourceLoader}接口，则它将也用作默认的{@code ResourceLoader}。 {@link org.springframework.context.ApplicationContext}实现通常是这种情况。
	 * B. 如果给出普通的{@code BeanDefinitionRegistry}，则默认的{@code ResourceLoader}将是{@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}。
	 *    如果传入的bean工厂也实现了{@link EnvironmentCapable}，则此阅读器将使用其环境。 否则，读者将初始化并使用
	 *     {@link org.springframework.core.env.StandardEnvironment}。 所有{@code ApplicationContext}实现都是{@code EnvironmentCapable}，而普通的
	 *     {@code BeanFactory}实现不是。
	 */
	/**
	 * A.
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory.
	 * <p>If the passed-in bean factory does not only implement the
	 * {@code BeanDefinitionRegistry} interface but also the {@code ResourceLoader}
	 * interface, it will be used as default {@code ResourceLoader} as well. This will
	 * usually be the case for {@link org.springframework.context.ApplicationContext}
	 * implementations.
	 *
	 * B.
	 * <p>If given a plain {@code BeanDefinitionRegistry}, the default {@code ResourceLoader}
	 * will be a {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
	 * <p>If the passed-in bean factory also implements {@link EnvironmentCapable} its
	 * environment will be used by this reader.  Otherwise, the reader will initialize and
	 * use a {@link org.springframework.core.env.StandardEnvironment}. All
	 * {@code ApplicationContext} implementations are {@code EnvironmentCapable}, while
	 * normal {@code BeanFactory} implementations are not.
	 *
	 * // 20201205 注册{@code BeanFactory}以{@code BeanDefinitionRegistry}的形式将bean定义加载到其中
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 *
	 * // 20201205 是否包括{@link org.springframework.stereotype.Component @Component}，
	 * {@ link org.springframework.stereotype.Repository @Repository}，{@ link org.springframework.stereotype.Service @Service}的默认过滤器，
	 * 和{@link org.springframework.stereotype.Controller @Controller}构造型注释
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype annotations
	 *
	 * @see #setResourceLoader
	 * @see #setEnvironment
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		// 20201205 通过注册获取环境, 然后用来构造ClassPathBeanDefinitionScanner
		this(registry, useDefaultFilters, getOrCreateEnvironment(registry));
	}

	/**
	 * 20201205
	 * A. 在评估bean定义概要文件元数据时，为给定的bean工厂创建一个新的{@code ClassPathBeanDefinitionScanner}，并使用给定的{@link Environment}。
	 * B. 如果传入的bean工厂不仅实现了{@code BeanDefinitionRegistry}接口，而且还实现了{@link ResourceLoader}接口，那么它将也用作默认的{@code ResourceLoader}。
	 *    {@link org.springframework.context.ApplicationContext}实现通常是这种情况。
	 * C. 如果给出普通的{@code BeanDefinitionRegistry}，则默认的{@code ResourceLoader}将是{@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}。
	 */
	/**
	 * A.
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory and
	 * using the given {@link Environment} when evaluating bean definition profile metadata.
	 *
	 * B.
	 * <p>If the passed-in bean factory does not only implement the {@code
	 * BeanDefinitionRegistry} interface but also the {@link ResourceLoader} interface, it
	 * will be used as default {@code ResourceLoader} as well. This will usually be the
	 * case for {@link org.springframework.context.ApplicationContext} implementations.
	 *
	 * C.
	 * <p>If given a plain {@code BeanDefinitionRegistry}, the default {@code ResourceLoader}
	 * will be a {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
	 *
	 * // 20201205 注册{@code BeanFactory}以{@code BeanDefinitionRegistry}的形式将bean定义加载到其中
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 *
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype annotations
	 *
	 * // 20201205 在评估bean定义配置文件元数据时使用的Spring {@link Environment}
	 * @param environment the Spring {@link Environment} to use when evaluating bean
	 * definition profile metadata
	 * @since 3.1
	 * @see #setResourceLoader
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment) {
		// 20201205 获取资源加载器, 如果为ApplicationContext那么它本身也是资源加载器
		this(registry, useDefaultFilters, environment,
				(registry instanceof ResourceLoader ? (ResourceLoader) registry : null));
	}

	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory and
	 * using the given {@link Environment} when evaluating bean definition profile metadata.
	 *
	 * // 20201205 注册{@code BeanFactory}以便以{@code BeanDefinitionRegistry}的形式将bean定义加载到其中
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 *
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype annotations
	 *
	 * @param environment the Spring {@link Environment} to use when evaluating bean
	 * definition profile metadata
	 *
	 * @param resourceLoader the {@link ResourceLoader} to use
	 * @since 4.3.6
	 */
	// 2002015 在评估bean定义概要文件元数据时，为给定的bean工厂创建一个新的{@code ClassPathBeanDefinitionScanner}，并使用给定的{@link Environment}。
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, @Nullable ResourceLoader resourceLoader) {
		// 20201205 注册表不能为空
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

		// 20201205 设置注册表
		this.registry = registry;

		// 20201205 如果使用默认四大过滤器
		if (useDefaultFilters) {
			// 20201205 则调用父类的registerDefaultFilters(), 注册默认四大过滤器
			registerDefaultFilters();
		}

		// 20201205 则调用父类的setEnvironment(), 注册环境实例
		setEnvironment(environment);

		// 20201205 则调用父类的setResourceLoader(), 设置资源加载器, 以用于资源位置
		setResourceLoader(resourceLoader);
	}


	/**
	 * Return the BeanDefinitionRegistry that this scanner operates on.
	 */
	@Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Set the defaults to use for detected beans.
	 * @see BeanDefinitionDefaults
	 */
	public void setBeanDefinitionDefaults(@Nullable BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults =
				(beanDefinitionDefaults != null ? beanDefinitionDefaults : new BeanDefinitionDefaults());
	}

	/**
	 * Return the defaults to use for detected beans (never {@code null}).
	 * @since 4.1
	 */
	public BeanDefinitionDefaults getBeanDefinitionDefaults() {
		return this.beanDefinitionDefaults;
	}

	/**
	 * Set the name-matching patterns for determining autowire candidates.
	 * @param autowireCandidatePatterns the patterns to match against
	 */
	public void setAutowireCandidatePatterns(@Nullable String... autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}

	/**
	 * 20201208
	 * A. 设置BeanNameGenerator以用于检测到的Bean类。
	 * B. 默认值为{@link AnnotationBeanNameGenerator}。
	 */
	/**
	 * A.
	 * Set the BeanNameGenerator to use for detected bean classes.
	 *
	 * B.
	 * <p>Default is a {@link AnnotationBeanNameGenerator}.
	 */
	// 20201208 设置BeanNameGenerator以用于检测到的Bean类。
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		// 20201208 设置用于为bean定义生成bean名称的策略接口实例
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : AnnotationBeanNameGenerator.INSTANCE);
	}

	/**
	 * Set the ScopeMetadataResolver to use for detected bean classes.
	 * Note that this will override any custom "scopedProxyMode" setting.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * @see #setScopedProxyMode
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}

	/**
	 * Specify the proxy behavior for non-singleton scoped beans.
	 * Note that this will override any custom "scopeMetadataResolver" setting.
	 * <p>The default is {@link ScopedProxyMode#NO}.
	 * @see #setScopeMetadataResolver
	 */
	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
	}

	/**
	 * Specify whether to register annotation config post-processors.
	 * <p>The default is to register the post-processors. Turn this off
	 * to be able to ignore the annotations or to process them differently.
	 */
	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}


	/**
	 * Perform a scan within the specified base packages.
	 * @param basePackages the packages to check for annotated classes
	 * @return number of beans registered	// 20201209 返回已注册的bean数目
	 */
	// 20201212 在指定的基本程序包中执行扫描 -> 返回已注册的BeanDefinitionHolder(beanDefinition包装类)数目
	public int scan(String... basePackages) {
		// 20201209 获取注册前的注册表中定义的bean数。
		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

		// 20201209 在指定的基本程序包中执行扫描，返回已注册的BeanDefinitionHolder(beanDefinition包装类)。
		doScan(basePackages);

		// Register annotation config processors, if necessary.
		// 20201209 如有必要，注册注解配置处理器, 默认为true
		if (this.includeAnnotationConfig) {
			// 20201211 给registry添加后置处理器
			AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
		}

		// 20201209 获取注册前后的bean差值 -> 得到当前注册的bean数目
		return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
	}

	/**
	 * 20201209
	 * A. 在指定的基本程序包中执行扫描，返回已注册的bean定义。
	 * B. 此方法不注册注解配置处理器，而是将其留给调用方。
	 */
	/**
	 * A.
	 * Perform a scan within the specified base packages,
	 * returning the registered bean definitions.
	 *
	 * B.
	 * <p>This method does <i>not</i> register an annotation config processor
	 * but rather leaves this up to the caller.
	 * @param basePackages the packages to check for annotated classes
	 *
	 * // 20201209 为工具注册目的而已注册的一组bean（永远{@code null}）
	 * @return set of beans registered if any for tooling registration purposes (never {@code null})
	 */
	// 20201209 在指定的基本程序包中执行扫描，返回已注册的BeanDefinitionHolder(beanDefinition包装类)。
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		// 20201209 包路径不能为空
		Assert.notEmpty(basePackages, "At least one base package must be specified");

		// 20201209 已注册列表
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();

		// 20201209 遍历包数组
		for (String basePackage : basePackages) {
			// 20201212 设置扫描候选组件(ScannedGenericBeanDefinition)
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);

			// 20201209 遍历查找到的候选组件
			for (BeanDefinition candidate : candidates) {
				// 20201209 解决出适合于提供的bean定义器的bean的范围特征
				ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);

				// 20201209 覆盖此bean的目标作用域，并指定一个新的作用域名称
				candidate.setScope(scopeMetadata.getScopeName());

				// 20201209 用于为bean定义生成bean名称的策略接口实例 -> 为给定的bean定义生成一个bean名称
				String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);

				// 20201209 如果候选组件为AbstractBeanDefinition类型
				if (candidate instanceof AbstractBeanDefinition) {
					// 20201209 BeanDefinition后置处理器: 设置beanDefinition是否自动装配组件
					postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
				}

				// 20201209 如果候选组件为AnnotatedBeanDefinition类型
				if (candidate instanceof AnnotatedBeanDefinition) {
					// 20201209 则处理默认公共注解定义: Lazy, Primary, DependsOn, Role, Description
					AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
				}

				// 20201209 检查给定候选者的Bean名称，以确定是否需要注册相应的Bean定义或与现有定义冲突。
				if (checkCandidate(beanName, candidate)) {
					// 20201209 检查通过, Bean可以原样注册, 创建一个新的BeanDefinitionHolder
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);

					// 20201209 根据作用域代理模式创建实例 -> 不要创建作用域代理、创建一个JDK动态代理、创建一个基于类的代理（使用CGLIB）
					definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);

					// 20201209 已注册列表添加该BeanDefinitionHolder
					beanDefinitions.add(definitionHolder);

					// 20201209 使用给定的注册表注册该BeanDefinitionHolder
					registerBeanDefinition(definitionHolder, this.registry);
				}
			}
		}
		return beanDefinitions;
	}

	/**
	 * Apply further settings to the given bean definition,
	 * beyond the contents retrieved from scanning the component class.
	 * @param beanDefinition the scanned bean definition
	 * @param beanName the generated bean name for the given bean
	 */
	// 20201209 BeanDefinition后置处理器: 设置beanDefinition是否自动装配组件
	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
		beanDefinition.applyDefaults(this.beanDefinitionDefaults);
		if (this.autowireCandidatePatterns != null) {
			// 20201212 设置beanDefinition是否自动装配组件
			beanDefinition.setAutowireCandidate(PatternMatchUtils.simpleMatch(this.autowireCandidatePatterns, beanName));
		}
	}

	/**
	 * 20201209
	 * A. 使用给定的注册表注册指定的bean。
	 * B. 可以在子类中覆盖，例如适应注册过程或为每个扫描的bean注册其他bean定义。
	 */
	/**
	 * A.
	 * Register the specified bean with the given registry.
	 *
	 * B.
	 * <p>Can be overridden in subclasses, e.g. to adapt the registration
	 * process or to register further bean definitions for each scanned bean.
	 * @param definitionHolder the bean definition plus bean name for the bean
	 * @param registry the BeanDefinitionRegistry to register the bean with
	 */
	// 20201209 使用给定的注册表注册指定的bean
	protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
		// 20201209 向给定的bean工厂注册给定的bean定义
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}


	/**
	 * Check the given candidate's bean name, determining whether the corresponding
	 * bean definition needs to be registered or conflicts with an existing definition.
	 * @param beanName the suggested name for the bean
	 * @param beanDefinition the corresponding bean definition
	 *
	 * // 20201209 {@code true}，如果bean可以原样注册； {@code false}如果由于指定名称已经存在兼容的bean定义而应跳过
	 * @return {@code true} if the bean can be registered as-is;
	 * {@code false} if it should be skipped because there is an
	 * existing, compatible bean definition for the specified name
	 *
	 * @throws ConflictingBeanDefinitionException if an existing, incompatible
	 * bean definition has been found for the specified name
	 */
	// 20201209 检查给定候选者的Bean名称，以确定是否需要注册相应的Bean定义或与现有定义冲突。
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return true;
		}
		BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
		BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
		if (originatingDef != null) {
			existingDef = originatingDef;
		}
		if (isCompatible(beanDefinition, existingDef)) {
			return false;
		}
		throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
				"' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
				"non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
	}

	/**
	 * Determine whether the given new bean definition is compatible with
	 * the given existing bean definition.
	 * <p>The default implementation considers them as compatible when the existing
	 * bean definition comes from the same source or from a non-scanning source.
	 * @param newDefinition the new bean definition, originated from scanning
	 * @param existingDefinition the existing bean definition, potentially an
	 * explicitly defined one or a previously generated one from scanning
	 * @return whether the definitions are considered as compatible, with the
	 * new definition to be skipped in favor of the existing definition
	 */
	protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
		return (!(existingDefinition instanceof ScannedGenericBeanDefinition) ||  // explicitly registered overriding bean
				(newDefinition.getSource() != null && newDefinition.getSource().equals(existingDefinition.getSource())) ||  // scanned same file twice
				newDefinition.equals(existingDefinition));  // scanned equivalent class twice
	}


	/**
	 * Get the Environment from the given registry if possible, otherwise return a new
	 * StandardEnvironment.
	 */
	// 20201205 如果可能，从给定的注册表中获取环境，否则返回一个新的StandardEnvironment。
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable) {
			return ((EnvironmentCapable) registry).getEnvironment();
		}
		return new StandardEnvironment();
	}

}
