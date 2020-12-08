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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

/**
 * 20201207
 * A. 标准{@link ApplicationListener}接口的扩展变体，公开了进一步的元数据，例如受支持的事件和源类型。
 * B. 要全面反思一般事件类型，请考虑改为实现{@link GenericApplicationListener}接口。
 */
/**
 * A.
 * Extended variant of the standard {@link ApplicationListener} interface,
 * exposing further metadata such as the supported event and source type.
 *
 * B.
 * <p>For full introspection of generic event types, consider implementing
 * the {@link GenericApplicationListener} interface instead.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see GenericApplicationListener
 * @see GenericApplicationListenerAdapter
 */
// 20201207 标准{@link ApplicationListener}接口的扩展变体，公开了进一步的元数据，例如受支持的事件和源类型。
public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {

	/**
	 * Determine whether this listener actually supports the given event type.
	 * @param eventType the event type (never {@code null})
	 */
	// 20201207 确定此侦听器是否实际上支持给定的事件类型。
	boolean supportsEventType(Class<? extends ApplicationEvent> eventType);

	/**
	 * 20201207
	 * A. 确定此侦听器是否实际上支持给定的源类型。
	 * B. 默认实现始终返回{@code true}。
	 */
	/**
	 * A.
	 * Determine whether this listener actually supports the given source type.
	 *
	 * B.
	 * <p>The default implementation always returns {@code true}.
	 *
	 * @param sourceType the source type, or {@code null} if no source
	 */
	// 20201207 确定此侦听器是否实际上支持给定的源类型 -> 默认实现始终返回{@code true}
	default boolean supportsSourceType(@Nullable Class<?> sourceType) {
		return true;
	}

	/**
	 * Determine this listener's order in a set of listeners for the same event.
	 * <p>The default implementation returns {@link #LOWEST_PRECEDENCE}.
	 */
	@Override
	default int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
