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

import java.lang.reflect.Constructor;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201215
 * 用于处理解析器策略的通用委托代码，例如 {@code TypeFilter}，{@code ImportSelector}，{@code ImportBeanDefinitionRegistrar}
 */
/**
 * Common delegate code for the handling of parser strategies, e.g.
 * {@code TypeFilter}, {@code ImportSelector}, {@code ImportBeanDefinitionRegistrar}
 *
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 4.3.3
 */
// 20201215 用于处理解析器策略的通用委托代码: 比如TypeFilter、ImportSelector、ImportBeanDefinitionRegistrar
abstract class ParserStrategyUtils {

	/**
	 * 20201215
	 * 使用适当的构造函数实例化一个类，并以指定的可分配类型返回新实例。 如果返回的实例是由给定对象实现的，它们将调用{@link BeanClassLoaderAware}，
	 * {@link BeanFactoryAware}，{@link EnvironmentAware}和{@link ResourceLoaderAware}合同。
	 */
	/**
	 * Instantiate a class using an appropriate constructor and return the new
	 * instance as the specified assignable type. The returned instance will
	 * have {@link BeanClassLoaderAware}, {@link BeanFactoryAware},
	 * {@link EnvironmentAware}, and {@link ResourceLoaderAware} contracts
	 * invoked if they are implemented by the given object.
	 * @since 5.2
	 */
	// 20201215 使用适当的构造函数实例化一个类，并以指定的可分配类型返回新实例, 且当前实例具有某些意识功能: BeanClassLoader、BeanFactory、Environment、ResourceLoader
	@SuppressWarnings("unchecked")
	static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo, Environment environment,
			ResourceLoader resourceLoader, BeanDefinitionRegistry registry) {
		// 20201215 源Class不能为空
		Assert.notNull(clazz, "Class must not be null");

		// 20201215 源Class必须为指定的assignableTo类型
		Assert.isAssignable(assignableTo, clazz);

		// 20201215 源Class不能是接口
		if (clazz.isInterface()) {
			throw new BeanInstantiationException(clazz, "Specified class is an interface");
		}

		// 20201215 获取类加载器
		ClassLoader classLoader = (registry instanceof ConfigurableBeanFactory ?
				((ConfigurableBeanFactory) registry).getBeanClassLoader() : resourceLoader.getClassLoader());

		// 20201215 构建实例
		T instance = (T) createInstance(clazz, environment, resourceLoader, registry, classLoader);

		// 202012125 执行意识方法, 使其能具有某意识功能: BeanClassLoader、BeanFactory、Environment、ResourceLoader
		ParserStrategyUtils.invokeAwareMethods(instance, environment, resourceLoader, registry, classLoader);

		// 20201215 返回代理了意识功能的实例
		return instance;
	}

	// 20201215 构建实例
	private static Object createInstance(Class<?> clazz, Environment environment,
			ResourceLoader resourceLoader, BeanDefinitionRegistry registry,
			@Nullable ClassLoader classLoader) {

		// 20201215 获取当前Class的本类构造方法
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();

		// 20201215 如果构造方法个数为1
		if (constructors.length == 1 && constructors[0].getParameterCount() > 0) {
			try {
				// 20201215 获取该构造方法
				Constructor<?> constructor = constructors[0];

				// 20201215 封装构造方法参数
				Object[] args = resolveArgs(constructor.getParameterTypes(),
						environment, resourceLoader, registry, classLoader);

				// 20201201 使用给定构造函数实例化类的便利方法。请注意，如果给定了不可访问（即非公共）构造函数，则此方法尝试将构造函数设置为可访问的，并支持具有可选参数和默认值的Kotlin类。
				return BeanUtils.instantiateClass(constructor, args);
			}
			catch (Exception ex) {
				throw new BeanInstantiationException(clazz, "No suitable constructor found", ex);
			}
		}

		// 20201215 如果构造方法有多个, 则使用其“主要”构造函数（对于Kotlin类，可能声明了默认参数）或其缺省构造函数（对于常规Java类，需要标准无参数设置）实例化一个类
		return BeanUtils.instantiateClass(clazz);
	}

	// 20201215 封装构造方法参数
	private static Object[] resolveArgs(Class<?>[] parameterTypes,
			Environment environment, ResourceLoader resourceLoader,
			BeanDefinitionRegistry registry, @Nullable ClassLoader classLoader) {

			Object[] parameters = new Object[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				parameters[i] = resolveParameter(parameterTypes[i], environment,
						resourceLoader, registry, classLoader);
			}
			return parameters;
	}

	@Nullable
	private static Object resolveParameter(Class<?> parameterType,
			Environment environment, ResourceLoader resourceLoader,
			BeanDefinitionRegistry registry, @Nullable ClassLoader classLoader) {

		if (parameterType == Environment.class) {
			return environment;
		}
		if (parameterType == ResourceLoader.class) {
			return resourceLoader;
		}
		if (parameterType == BeanFactory.class) {
			return (registry instanceof BeanFactory ? registry : null);
		}
		if (parameterType == ClassLoader.class) {
			return classLoader;
		}
		throw new IllegalStateException("Illegal method parameter type: " + parameterType.getName());
	}

	// 202012125 执行意识方法, 使其能具有某意识功能: BeanClassLoader、BeanFactory、Environment、ResourceLoader
	private static void invokeAwareMethods(Object parserStrategyBean, Environment environment,
			ResourceLoader resourceLoader, BeanDefinitionRegistry registry, @Nullable ClassLoader classLoader) {

		if (parserStrategyBean instanceof Aware) {
			// 20201215 执行BeanClassLoaderAware意识方法, 使其能具有BeanClassLoader意识功能
			if (parserStrategyBean instanceof BeanClassLoaderAware && classLoader != null) {
				((BeanClassLoaderAware) parserStrategyBean).setBeanClassLoader(classLoader);
			}

			// 20201215 执行BeanFactoryAware意识方法, 使其能具有BeanFactory意识功能
			if (parserStrategyBean instanceof BeanFactoryAware && registry instanceof BeanFactory) {
				((BeanFactoryAware) parserStrategyBean).setBeanFactory((BeanFactory) registry);
			}

			// 20201215 执行EnvironmentAware意识方法, 使其能具有Environment意识功能
			if (parserStrategyBean instanceof EnvironmentAware) {
				((EnvironmentAware) parserStrategyBean).setEnvironment(environment);
			}

			// 20201215 执行ResourceLoaderAware意识方法, 使其能具有ResourceLoader意识功能
			if (parserStrategyBean instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) parserStrategyBean).setResourceLoader(resourceLoader);
			}
		}
	}

}
