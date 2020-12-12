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

package org.springframework.context;

import org.springframework.beans.factory.Aware;
import org.springframework.core.io.ResourceLoader;

/**
 * 20201204
 * A. 希望通过其运行所在的ResourceLoader（通常是ApplicationContext）得到通知的任何对象所要实现的接口。这是通过org.springframework.context.ApplicationContextAware接口
 *    替代完全ApplicationContext依赖项的替代方法。
 * B. 请注意，{@link org.springframework.core.io.Resource}依赖项也可以公开为{@code Resource}类型的bean属性，由bean工厂通过自动转换类型的字符串填充。
 *    这消除了仅出于访问特定文件资源的目的而实现任何回调接口的需要。
 * C. 当您的应用程序对象必须访问其名称经过计算的各种文件资源时，通常需要一个{@link ResourceLoader}。 一个好的策略是使对象使用
 *    {@link org.springframework.core.io.DefaultResourceLoader}，但仍实现{@code ResourceLoaderAware}以允许在{@code ApplicationContext}中运行时进行覆盖。
 *    有关示例，请参见{@link org.springframework.context.support.ReloadableResourceBundleMessageSource}。
 * D. 还可以检查传入的{@code ResourceLoader}中的{@link org.springframework.core.io.support.ResourcePatternResolver}接口，并进行相应的投射，以便将资源模式解析为
 *    {@code Resource}对象的数组 。 在ApplicationContext中运行时，这将始终有效（因为上下文接口扩展了ResourcePatternResolver接口）。 默认使用
 *    {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}； 另请参见{@code ResourcePatternUtils.getResourcePatternResolver}方法。
 * E. 作为{@code ResourcePatternResolver}依赖项的替代方法，请考虑公开{@code Resource}数组类型的bean属性，该属性通过模式字符串填充，并在绑定时由bean工厂进行自动类型转换。
 */
/**
 * A.
 * Interface to be implemented by any object that wishes to be notified of the
 * {@link ResourceLoader} (typically the ApplicationContext) that it runs in.
 * This is an alternative to a full {@link ApplicationContext} dependency via
 * the {@link org.springframework.context.ApplicationContextAware} interface.
 *
 * B.
 * <p>Note that {@link org.springframework.core.io.Resource} dependencies can also
 * be exposed as bean properties of type {@code Resource}, populated via Strings
 * with automatic type conversion by the bean factory. This removes the need for
 * implementing any callback interface just for the purpose of accessing a
 * specific file resource.
 *
 * C.
 * <p>You typically need a {@link ResourceLoader} when your application object has to
 * access a variety of file resources whose names are calculated. A good strategy is
 * to make the object use a {@link org.springframework.core.io.DefaultResourceLoader}
 * but still implement {@code ResourceLoaderAware} to allow for overriding when
 * running in an {@code ApplicationContext}. See
 * {@link org.springframework.context.support.ReloadableResourceBundleMessageSource}
 * for an example.
 *
 * D.
 * <p>A passed-in {@code ResourceLoader} can also be checked for the
 * {@link org.springframework.core.io.support.ResourcePatternResolver} interface
 * and cast accordingly, in order to resolve resource patterns into arrays of
 * {@code Resource} objects. This will always work when running in an ApplicationContext
 * (since the context interface extends the ResourcePatternResolver interface). Use a
 * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver} as
 * default; see also the {@code ResourcePatternUtils.getResourcePatternResolver} method.
 *
 * E.
 * <p>As an alternative to a {@code ResourcePatternResolver} dependency, consider
 * exposing bean properties of type {@code Resource} array, populated via pattern
 * Strings with automatic type conversion by the bean factory at binding time.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 10.03.2004
 * @see ApplicationContextAware
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.core.io.support.ResourcePatternResolver
 */
// 20201204 资源加载器自觉接口: 替代完全ApplicationContext依赖项 -> 通过ApplicationContext得到通知的任何对象所要实现的接口
public interface ResourceLoaderAware extends Aware {

	/**
	 * Set the ResourceLoader that this object runs in.
	 * <p>This might be a ResourcePatternResolver, which can be checked
	 * through {@code instanceof ResourcePatternResolver}. See also the
	 * {@code ResourcePatternUtils.getResourcePatternResolver} method.
	 * <p>Invoked after population of normal bean properties but before an init callback
	 * like InitializingBean's {@code afterPropertiesSet} or a custom init-method.
	 * Invoked before ApplicationContextAware's {@code setApplicationContext}.
	 * @param resourceLoader the ResourceLoader object to be used by this object
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.ResourcePatternUtils#getResourcePatternResolver
	 */
	void setResourceLoader(ResourceLoader resourceLoader);

}
