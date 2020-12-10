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

/**
 * 20201130
 * A. 封装事件发布功能的接口。
 * B. 用作{@link ApplicationContext}的超级接口。
 */
/**
 * A.
 * Interface that encapsulates event publication functionality.
 *
 * B.
 * <p>Serves as a super-interface for {@link ApplicationContext}.
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 1.1.1
 * @see ApplicationContext
 * @see ApplicationEventPublisherAware
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.event.EventPublicationInterceptor
 */
// 20201130 封装事件发布功能的接口
@FunctionalInterface
public interface ApplicationEventPublisher {

	/**
	 * 20201210
	 * A. 通知所有与此应用程序注册的匹配侦听器一个应用程序事件。 事件可以是框架事件（例如ContextRefreshedEvent）或特定于应用程序的事件。
	 * B. 这样的事件发布步骤实际上是到多播器的切换，并且根本不意味着同步/异步执行或什至立即执行。 鼓励事件侦听器尽可能地高效，并单独使用异步执行来运行更长的时间并可能阻塞操作。
	 */
	/**
	 * A.
	 * Notify all <strong>matching</strong> listeners registered with this
	 * application of an application event. Events may be framework events
	 * (such as ContextRefreshedEvent) or application-specific events.
	 *
	 * B.
	 * <p>Such an event publication step is effectively a hand-off to the
	 * multicaster and does not imply synchronous/asynchronous execution
	 * or even immediate execution at all. Event listeners are encouraged
	 * to be as efficient as possible, individually using asynchronous
	 * execution for longer-running and potentially blocking operations.
	 *
	 * @param event the event to publish
	 * @see #publishEvent(Object)
	 * @see org.springframework.context.event.ContextRefreshedEvent
	 * @see org.springframework.context.event.ContextClosedEvent
	 */
	// 20201210 通知所有与此应用程序注册的匹配侦听器一个应用程序事件
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}

	/**
	 * 20201210
	 * A. 通知所有与此应用程序注册的匹配侦听器事件。
	 * B. 如果指定的{@code event}不是{@link ApplicationEvent}，则将其包装在{@link Payload ApplicationEvent}中。
	 * C. 这样的事件发布步骤实际上是到多播器的切换，并且根本不意味着同步/异步执行或什至立即执行。 鼓励事件侦听器尽可能地高效，并单独使用异步执行来运行更长的时间并可能阻塞操作。
	 */
	/**
	 * A.
	 * Notify all <strong>matching</strong> listeners registered with this
	 * application of an event.
	 *
	 * B.
	 * <p>If the specified {@code event} is not an {@link ApplicationEvent},
	 * it is wrapped in a {@link PayloadApplicationEvent}.
	 *
	 * C.
	 * <p>Such an event publication step is effectively a hand-off to the
	 * multicaster and does not imply synchronous/asynchronous execution
	 * or even immediate execution at all. Event listeners are encouraged
	 * to be as efficient as possible, individually using asynchronous
	 * execution for longer-running and potentially blocking operations.
	 *
	 * @param event the event to publish
	 * @since 4.2
	 * @see #publishEvent(ApplicationEvent)
	 * @see PayloadApplicationEvent
	 */
	// 20201210 通知所有与此应用程序注册的匹配侦听器事件
	void publishEvent(Object event);

}
