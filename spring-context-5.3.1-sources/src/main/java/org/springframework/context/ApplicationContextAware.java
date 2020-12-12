/*
 * Copyright 2002-2012 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;

/**
 * 20201210
 * A. 希望由其运行在其中的{@link ApplicationContext}通知的任何对象所实现的接口。
 * B. 例如，当对象需要访问一组协作bean时，实现此接口就很有意义。 请注意，仅出于bean查找目的，通过bean引用进行配置比实现此接口更可取。
 * C. 如果对象需要访问文件资源（例如，想要调用{@code getResource}，想要发布应用程序事件或需要访问MessageSource），则也可以实现此接口。 但是，在这种特定情况下，
 *    最好实现更特定的{@link ResourceLoaderAware}，{@link ApplicationEventPublisherAware}或{@link MessageSourceAware}接口。
 * D. 请注意，文件资源依赖关系也可以公开为{@link org.springframework.core.io.Resource}类型的bean属性，该属性通过字符串进行填充，并由bean工厂进行自动类型转换。
 *    这样就无需为了访问特定文件资源而实现任何回调接口。
 * E. {@link org.springframework.context.support.ApplicationObjectSupport}是应用程序对象的便捷基类，实现了此接口。
 * F. 有关所有bean生命周期方法的列表，请参见{@link org.springframework.beans.factory.BeanFactory BeanFactory javadocs}。
 */
/**
 * A.
 * Interface to be implemented by any object that wishes to be notified
 * of the {@link ApplicationContext} that it runs in.
 *
 * B.
 * <p>Implementing this interface makes sense for example when an object
 * requires access to a set of collaborating beans. Note that configuration
 * via bean references is preferable to implementing this interface just
 * for bean lookup purposes.
 *
 * C.
 * <p>This interface can also be implemented if an object needs access to file
 * resources, i.e. wants to call {@code getResource}, wants to publish
 * an application event, or requires access to the MessageSource. However,
 * it is preferable to implement the more specific {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} or {@link MessageSourceAware} interface
 * in such a specific scenario.
 *
 * D.
 * <p>Note that file resource dependencies can also be exposed as bean properties
 * of type {@link org.springframework.core.io.Resource}, populated via Strings
 * with automatic type conversion by the bean factory. This removes the need
 * for implementing any callback interface just for the purpose of accessing
 * a specific file resource.
 *
 * E.
 * <p>{@link org.springframework.context.support.ApplicationObjectSupport} is a
 * convenience base class for application objects, implementing this interface.
 *
 * F.
 * <p>For a list of all bean lifecycle methods, see the
 * {@link org.springframework.beans.factory.BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see ResourceLoaderAware
 * @see ApplicationEventPublisherAware
 * @see MessageSourceAware
 * @see org.springframework.context.support.ApplicationObjectSupport
 * @see org.springframework.beans.factory.BeanFactoryAware
 */
// 20201210 ApplicationContext自觉接口: 希望由其运行在其中的{@link ApplicationContext}通知的任何对象所实现的接口 -> 当对象需要访问一组协作bean时，实现此接口就很有意义
public interface ApplicationContextAware extends Aware {

	/**
	 * Set the ApplicationContext that this object runs in.
	 * Normally this call will be used to initialize the object.
	 * <p>Invoked after population of normal bean properties but before an init callback such
	 * as {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()}
	 * or a custom init-method. Invoked after {@link ResourceLoaderAware#setResourceLoader},
	 * {@link ApplicationEventPublisherAware#setApplicationEventPublisher} and
	 * {@link MessageSourceAware}, if applicable.
	 * @param applicationContext the ApplicationContext object to be used by this object
	 * @throws ApplicationContextException in case of context initialization errors
	 * @throws BeansException if thrown by application context methods
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

}
