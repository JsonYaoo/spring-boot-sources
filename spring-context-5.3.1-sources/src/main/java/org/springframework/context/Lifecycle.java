/*
 * Copyright 2002-2018 the original author or authors.
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

/**
 * 20201204
 * A. 定义启动/停止生命周期控制方法的通用接口。 典型的用例是控制异步处理。 注意：此接口并不意味着特定的自动启动语义。 考虑为此目的实施{@link SmartLifecycle}。
 * B. 可以由组件（通常是在Spring上下文中定义的Spring bean）和容器（通常是Spring {@link ApplicationContext}本身）实现。 容器会将开始/停止信号传播到每个容器中应用的所有组件，
 *    例如 在运行时停止/重新启动的情况。
 * C. 可用于直接调用或通过JMX进行管理操作。 在后一种情况下，通常使用{@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler}定义
 *    {@link org.springframework.jmx.export.MBeanExporter}，从而将活动控制的组件的可见性限制为 生命周期接口。
 * D. 请注意，当前的{@code Lifecycle}接口仅在顶级单例bean上受支持。 在任何其他组件上，{@code Lifecycle}接口将保持未被检测到并因此被忽略。 另外，请注意，扩展的
 *    {@link SmartLifecycle}接口提供了与应用程序上下文的启动和关闭阶段的复杂集成。
 */
/**
 * A.
 * A common interface defining methods for start/stop lifecycle control.
 * The typical use case for this is to control asynchronous processing.
 * <b>NOTE: This interface does not imply specific auto-startup semantics.
 * Consider implementing {@link SmartLifecycle} for that purpose.</b>
 *
 * B.
 * <p>Can be implemented by both components (typically a Spring bean defined in a
 * Spring context) and containers  (typically a Spring {@link ApplicationContext}
 * itself). Containers will propagate start/stop signals to all components that
 * apply within each container, e.g. for a stop/restart scenario at runtime.
 *
 * C.
 * <p>Can be used for direct invocations or for management operations via JMX.
 * In the latter case, the {@link org.springframework.jmx.export.MBeanExporter}
 * will typically be defined with an
 * {@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler},
 * restricting the visibility of activity-controlled components to the Lifecycle
 * interface.
 *
 * D.
 * <p>Note that the present {@code Lifecycle} interface is only supported on
 * <b>top-level singleton beans</b>. On any other component, the {@code Lifecycle}
 * interface will remain undetected and hence ignored. Also, note that the extended
 * {@link SmartLifecycle} interface provides sophisticated integration with the
 * application context's startup and shutdown phases.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SmartLifecycle
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 */
// 20201204 定义启动/停止生命周期控制方法的通用接口。 典型的用例是控制异步处理。 -> Spring bean | ApplicationContext容器会将开始/停止信号传播到每个容器中应用的所有组件
public interface Lifecycle {

	/**
	 * Start this component.
	 * <p>Should not throw an exception if the component is already running.
	 * <p>In the case of a container, this will propagate the start signal to all
	 * components that apply.
	 * @see SmartLifecycle#isAutoStartup()
	 */
	void start();

	/**
	 * Stop this component, typically in a synchronous fashion, such that the component is
	 * fully stopped upon return of this method. Consider implementing {@link SmartLifecycle}
	 * and its {@code stop(Runnable)} variant when asynchronous stop behavior is necessary.
	 * <p>Note that this stop notification is not guaranteed to come before destruction:
	 * On regular shutdown, {@code Lifecycle} beans will first receive a stop notification
	 * before the general destruction callbacks are being propagated; however, on hot
	 * refresh during a context's lifetime or on aborted refresh attempts, a given bean's
	 * destroy method will be called without any consideration of stop signals upfront.
	 * <p>Should not throw an exception if the component is not running (not started yet).
	 * <p>In the case of a container, this will propagate the stop signal to all components
	 * that apply.
	 * @see SmartLifecycle#stop(Runnable)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	void stop();

	/**
	 * Check whether this component is currently running.
	 * <p>In the case of a container, this will return {@code true} only if <i>all</i>
	 * components that apply are currently running.
	 * @return whether the component is currently running
	 */
	boolean isRunning();

}
