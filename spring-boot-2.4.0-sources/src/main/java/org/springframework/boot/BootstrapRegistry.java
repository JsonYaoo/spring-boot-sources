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

import java.util.function.Supplier;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

/**
 * 20201201
 * A. 一个简单的对象注册表，在启动和{@link Environment}后处理过程中可用，直到{@link ApplicationContext}准备就绪为止。
 * B. 可用于注册创建成本较高或需要在{@link ApplicationContext}可用之前共享的实例。
 * C. 注册表使用{@link Class}作为键，这意味着只能存储给定类型的单个实例。
 * D. {@link #addCloseListener（ApplicationListener）}方法可用于添加一个侦听器，该侦听器可以在{@link BootstrapContext}已关闭且
 *    {@link ApplicationContext}完全准备好时执行操作。例如，实例可以选择将自己注册为常规的springbean，以便应用程序可以使用它。
 */
/**
 * A.
 * A simple object registry that is available during startup and {@link Environment}
 * post-processing up to the point that the {@link ApplicationContext} is prepared.
 *
 * B.
 * <p>
 * Can be used to register instances that may be expensive to create, or need to be shared
 * before the {@link ApplicationContext} is available.
 *
 * C.
 * <p>
 * The registry uses {@link Class} as a key, meaning that only a single instance of a
 * given type can be stored.
 *
 * D.
 * <p>
 * The {@link #addCloseListener(ApplicationListener)} method can be used to add a listener
 * that can perform actions when {@link BootstrapContext} has been closed and the
 * {@link ApplicationContext} is fully prepared. For example, an instance may choose to
 * register itself as a regular Spring bean so that it is available for the application to
 * use.
 *
 * @author Phillip Webb
 * @since 2.4.0
 * @see BootstrapContext
 * @see ConfigurableBootstrapContext
 */
// 20201201 Springboot最初启动注册表接口 -> addCloseListener（ApplicationListener）添加监听器后, 可以让实例在该注册表关闭 且 ApplicationContext初始化完成后执行一定操作
public interface BootstrapRegistry {

	/**
	 * Register a specific type with the registry. If the specified type has already been
	 * registered, but not get obtained, it will be replaced.
	 * @param <T> the instance type
	 * @param type the instance type
	 * @param instanceSupplier the instance supplier
	 */
	<T> void register(Class<T> type, InstanceSupplier<T> instanceSupplier);

	/**
	 * Register a specific type with the registry if one is not already present.
	 * @param <T> the instance type
	 * @param type the instance type
	 * @param instanceSupplier the instance supplier
	 */
	<T> void registerIfAbsent(Class<T> type, InstanceSupplier<T> instanceSupplier);

	/**
	 * Return if a registration exists for the given type.
	 * @param <T> the instance type
	 * @param type the instance type
	 * @return {@code true} if the type has already been registered
	 */
	<T> boolean isRegistered(Class<T> type);

	/**
	 * Return any existing {@link InstanceSupplier} for the given type.
	 * @param <T> the instance type
	 * @param type the instance type
	 * @return the registered {@link InstanceSupplier} or {@code null}
	 */
	<T> InstanceSupplier<T> getRegisteredInstanceSupplier(Class<T> type);

	/**
	 * Add an {@link ApplicationListener} that will be called with a
	 * {@link BootstrapContextClosedEvent} when the {@link BootstrapContext} is closed and
	 * the {@link ApplicationContext} has been prepared.
	 * @param listener the listener to add
	 */
	void addCloseListener(ApplicationListener<BootstrapContextClosedEvent> listener);

	/**
	 * Supplier used to provide the actual instance the first time it is accessed.
	 *
	 * @param <T> the instance type
	 */
	public interface InstanceSupplier<T> {

		/**
		 * Factory method used to create the instance when needed.
		 * @param context the {@link BootstrapContext} which may be used to obtain other
		 * bootstrap instances.
		 * @return the instance
		 */
		T get(BootstrapContext context);

		/**
		 * Factory method that can be used to create a {@link InstanceSupplier} for a
		 * given instance.
		 * @param <T> the instance type
		 * @param instance the instance
		 * @return a new {@link InstanceSupplier}
		 */
		static <T> InstanceSupplier<T> of(T instance) {
			return (registry) -> instance;
		}

		/**
		 * Factory method that can be used to create a {@link InstanceSupplier} from a
		 * {@link Supplier}.
		 * @param <T> the instance type
		 * @param supplier the supplier that will provide the instance
		 * @return a new {@link InstanceSupplier}
		 */
		static <T> InstanceSupplier<T> from(Supplier<T> supplier) {
			return (registry) -> (supplier != null) ? supplier.get() : null;
		}

	}

}
