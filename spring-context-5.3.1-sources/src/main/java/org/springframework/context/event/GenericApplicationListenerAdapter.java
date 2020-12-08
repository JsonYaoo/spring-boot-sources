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

package org.springframework.context.event;

import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * {@link GenericApplicationListener} adapter that determines supported event types
 * through introspecting the generically declared type of the target listener.
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.0
 * @see org.springframework.context.ApplicationListener#onApplicationEvent
 */
public class GenericApplicationListenerAdapter implements GenericApplicationListener, SmartApplicationListener {

	// 20201207 监听器Class-监听器类型缓存
	private static final Map<Class<?>, ResolvableType> eventTypeCache = new ConcurrentReferenceHashMap<>();

	// 20201207 委托的应用程序事件监听器
	private final ApplicationListener<ApplicationEvent> delegate;

	// 20201207 监听器或Class代理后的ResolvableType
	@Nullable
	private final ResolvableType declaredEventType;

	/**
	 * Create a new GenericApplicationListener for the given delegate.
	 * @param delegate the delegate listener to be invoked	// 20201207 要调用的委托侦听器
	 */
	// 20201207 为给定的委托创建一个新的GenericApplicationListener。
	@SuppressWarnings("unchecked")
	public GenericApplicationListenerAdapter(ApplicationListener<?> delegate) {
		// 20201207 要调用的委托侦听器不能为空
		Assert.notNull(delegate, "Delegate listener must not be null");

		// 20201207 注册委托的应用程序事件监听器
		this.delegate = (ApplicationListener<ApplicationEvent>) delegate;

		// 20201207 设置监听器或Class代理后的ResolvableType
		this.declaredEventType = resolveDeclaredEventType(this.delegate);
	}


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		this.delegate.onApplicationEvent(event);
	}

	// 20201207 判断监听器或Class代理后的ResolvableType是否为该事件类型分配的
	@Override
	@SuppressWarnings("unchecked")
	public boolean supportsEventType(ResolvableType eventType) {
		// 20201207 如果委托的应用程序事件监听器为SmartApplicationListener类型(旧版)
		if (this.delegate instanceof SmartApplicationListener) {
			// 20201207 则先获取解析后的事件类型
			Class<? extends ApplicationEvent> eventClass = (Class<? extends ApplicationEvent>) eventType.resolve();

			// 20201207 再确定此侦听器是否实际上支持给定的事件类型
			return (eventClass != null && ((SmartApplicationListener) this.delegate).supportsEventType(eventClass));
		}
		else {
			// 20201207 否则如果为GenericApplicationListener, 则判断监听器或Class代理后的ResolvableType是否为该事件类型分配的
			return (this.declaredEventType == null || this.declaredEventType.isAssignableFrom(eventType));
		}
	}

	// 20201207 确定此侦听器是否实际上支持给定的事件类型。
	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return supportsEventType(ResolvableType.forClass(eventType));
	}

	// 20201207 确定此侦听器是否实际上支持给定的源类型 -> 默认实现始终返回{@code true}
	@Override
	public boolean supportsSourceType(@Nullable Class<?> sourceType) {
		return !(this.delegate instanceof SmartApplicationListener) ||
				((SmartApplicationListener) this.delegate).supportsSourceType(sourceType);
	}

	@Override
	public int getOrder() {
		return (this.delegate instanceof Ordered ? ((Ordered) this.delegate).getOrder() : Ordered.LOWEST_PRECEDENCE);
	}

	// 20201207 返回监听器或Class代理后的ResolvableType
	@Nullable
	private static ResolvableType resolveDeclaredEventType(ApplicationListener<ApplicationEvent> listener) {
		// 20201207 根据监听器Class对象获取对应的ResolvableType
		ResolvableType declaredEventType = resolveDeclaredEventType(listener.getClass());

		// 20201207 如果返回的ResolvableType确实是监听器Class分配的
		if (declaredEventType == null || declaredEventType.isAssignableFrom(ApplicationEvent.class)) {
			// 20201207 则获取监听器代理后的Class(可能是原生Class)
			Class<?> targetClass = AopUtils.getTargetClass(listener);

			// 20201207 如果监听器被代理过
			if (targetClass != listener.getClass()) {
				// 20201207 则获取对应的ResolvableType
				declaredEventType = resolveDeclaredEventType(targetClass);
			}
		}

		// 20201207 返回监听器或Class代理后的ResolvableType
		return declaredEventType;
	}

	// 20201207 根据监听器Class对象获取对应的ResolvableType
	@Nullable
	static ResolvableType resolveDeclaredEventType(Class<?> listenerType) {
		// 20201207 根据监听器Class从监听器Class-监听器类型缓存获取监听器类型
		ResolvableType eventType = eventTypeCache.get(listenerType);

		// 20201207 如果获取的监听器类型不存在
		if (eventType == null) {
			// 20201207 则返回并设置参数化类型数组的第一个监听器类型
			eventType = ResolvableType.forClass(listenerType).as(ApplicationListener.class).getGeneric();
			eventTypeCache.put(listenerType, eventType);
		}

		// 20201207 如果还是为空, 则返回null
		return (eventType != ResolvableType.NONE ? eventType : null);
	}

}
