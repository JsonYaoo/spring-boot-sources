/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.beans.factory;

/**
 * 20201213
 * A. 在{@link BeanFactory}引导期间的单例预实例化阶段结束时触发的回调接口。 该接口可以由单例bean实现，以便在常规的单例实例化算法之后执行一些初始化，
 *    避免意外的早期初始化带来的副作用（例如，来自{@link ListableBeanFactory＃getBeansOfType}调用）。 从这个意义上说，它是{@link InitializingBean}的替代方法，
 *    后者在bean的本地构造阶段结束时立即触发。
 * B. 此回调变体有点类似于{@link org.springframework.context.event.ContextRefreshedEvent}，但不需要实现{@link org.springframework.context.ApplicationListener}，
 *    无需在整个上下文中过滤上下文引用层次结构等。这还意味着对{@code beans}包的依赖性最小，并且独立{@link ListableBeanFactory}实现受到尊重，而不仅仅是在
 *    {@link org.springframework.context.ApplicationContext}环境中。
 * C. 注意：如果要启动/管理异步任务，最好实现{@link org.springframework.context.Lifecycle}，它为运行时管理提供了更丰富的模型，并允许分阶段启动/关闭。
 */
/**
 * A.
 * Callback interface triggered at the end of the singleton pre-instantiation phase
 * during {@link BeanFactory} bootstrap. This interface can be implemented by
 * singleton beans in order to perform some initialization after the regular
 * singleton instantiation algorithm, avoiding side effects with accidental early
 * initialization (e.g. from {@link ListableBeanFactory#getBeansOfType} calls).
 * In that sense, it is an alternative to {@link InitializingBean} which gets
 * triggered right at the end of a bean's local construction phase.
 *
 * B.
 * <p>This callback variant is somewhat similar to
 * {@link org.springframework.context.event.ContextRefreshedEvent} but doesn't
 * require an implementation of {@link org.springframework.context.ApplicationListener},
 * with no need to filter context references across a context hierarchy etc.
 * It also implies a more minimal dependency on just the {@code beans} package
 * and is being honored by standalone {@link ListableBeanFactory} implementations,
 * not just in an {@link org.springframework.context.ApplicationContext} environment.
 *
 * C.
 * <p><b>NOTE:</b> If you intend to start/manage asynchronous tasks, preferably
 * implement {@link org.springframework.context.Lifecycle} instead which offers
 * a richer model for runtime management and allows for phased startup/shutdown.
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
 */
// 20201213 在{@link BeanFactory}引导期间的单例预实例化阶段结束时触发的回调接口
public interface SmartInitializingSingleton {

	/**
	 * 20201213
	 * A. 在单例预实例化阶段结束时立即调用，以确保已经创建了所有常规单例bean。 此方法内的{@link ListableBeanFactory＃getBeansOfType}调用在引导过程中不会触发意外的副作用。
	 * B. 注意：对于{@link BeanFactory}引导后按需延迟初始化的单例bean，也不会触发任何其他bean作用域的回调。 仅对具有预期的引导程序语义的bean小心使用它。
	 */
	/**
	 * A.
	 * Invoked right at the end of the singleton pre-instantiation phase,
	 * with a guarantee that all regular singleton beans have been created
	 * already. {@link ListableBeanFactory#getBeansOfType} calls within
	 * this method won't trigger accidental side effects during bootstrap.
	 *
	 * B.
	 * <p><b>NOTE:</b> This callback won't be triggered for singleton beans
	 * lazily initialized on demand after {@link BeanFactory} bootstrap,
	 * and not for any other bean scope either. Carefully use it for beans
	 * with the intended bootstrap semantics only.
	 */
	// 20201213 在单例预实例化阶段结束时立即调用，以确保已经创建了所有常规单例bean
	void afterSingletonsInstantiated();

}
