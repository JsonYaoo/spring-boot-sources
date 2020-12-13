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

package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 20201207
 * A. 由可以管理多个{@link ApplicationListener}对象并向其发布事件的对象实现的接口。
 * B. {@link org.springframework.context.ApplicationEventPublisher}（通常是Spring {@link org.springframework.context.ApplicationContext}）可以使用
 *    {@code ApplicationEventMulticaster}作为实际发布事件的委托。
 */
/**
 * A.
 * Interface to be implemented by objects that can manage a number of
 * {@link ApplicationListener} objects and publish events to them.
 *
 * B.
 * <p>An {@link org.springframework.context.ApplicationEventPublisher}, typically
 * a Spring {@link org.springframework.context.ApplicationContext}, can use an
 * {@code ApplicationEventMulticaster} as a delegate for actually publishing events.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @see ApplicationListener
 */
// 20201207 应用程序事件多播器
public interface ApplicationEventMulticaster {

	/**
	 * Add a listener to be notified of all events.
	 * @param listener the listener to add
	 */
	// 20201213 添加一个侦听器以通知所有事件。
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * Add a listener bean to be notified of all events.
	 * @param listenerBeanName the name of the listener bean to add
	 */
	// 20201213 添加一个侦听器bean，以通知所有事件。
	void addApplicationListenerBean(String listenerBeanName);

	/**
	 * Remove a listener from the notification list.
	 * @param listener the listener to remove
	 */
	void removeApplicationListener(ApplicationListener<?> listener);

	/**
	 * Remove a listener bean from the notification list.
	 * @param listenerBeanName the name of the listener bean to remove
	 */
	void removeApplicationListenerBean(String listenerBeanName);

	/**
	 * Remove all listeners registered with this multicaster.
	 * <p>After a remove call, the multicaster will perform no action
	 * on event notification until new listeners are registered.
	 */
	void removeAllListeners();

	/**
	 * 20201207
	 * A. 将给定的应用程序事件多播到适当的侦听器。
	 * B. 如果可能，请考虑使用{@link #multicastEvent（ApplicationEvent，ResolvableType）}，因为它为基于泛型的事件提供了更好的支持。
	 */
	/**
	 * A.
	 * Multicast the given application event to appropriate listeners.
	 *
	 * B.
	 * <p>Consider using {@link #multicastEvent(ApplicationEvent, ResolvableType)}
	 * if possible as it provides better support for generics-based events.
	 *
	 * @param event the event to multicast // 20201207 要多播的事件
	 */
	// 20201207 将给定的应用程序事件多播到适当的侦听器
	void multicastEvent(ApplicationEvent event);

	/**
	 * 20201207
	 * A. 将给定的应用程序事件多播到适当的侦听器。
	 * B. 如果{@code eventType}为{@code null}，则基于{@code event}实例构建默认类型。
	 */
	/**
	 * A.
	 * Multicast the given application event to appropriate listeners.
	 *
	 * B.
	 * <p>If the {@code eventType} is {@code null}, a default type is built
	 * based on the {@code event} instance.
	 *
	 * @param event the event to multicast
	 * @param eventType the type of event (can be {@code null}) // 20201207 事件的类型（可以为{@code null}）
	 * @since 4.2
	 */
	// 20201207 将给定的应用程序事件多播到适当的侦听器
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);

}
