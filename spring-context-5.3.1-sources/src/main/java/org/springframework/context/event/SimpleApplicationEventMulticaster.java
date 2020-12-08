/*
 * Copyright 2002-2020 the original author or authors.
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

import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;
import org.springframework.util.ErrorHandler;

/**
 * 20201207
 * A. {@link ApplicationEventMulticaster}接口的简单实现。
 * B. 将所有事件多播到所有注册的侦听器，让侦听器忽略他们不感兴趣的事件。侦听器通常将对传入的事件对象执行相应的{@code instanceof}检查。
 * C. 默认情况下，所有侦听器在调用线程中被调用。 这带来了恶意侦听器阻塞整个应用程序的危险，但增加了最小的开销。 指定备用任务执行程序，以使侦听器在不同的线程中执行，例如从线程池中执行。
 */
/**
 * A.
 * Simple implementation of the {@link ApplicationEventMulticaster} interface.
 *
 * B.
 * <p>Multicasts all events to all registered listeners, leaving it up to
 * the listeners to ignore events that they are not interested in.
 * Listeners will usually perform corresponding {@code instanceof}
 * checks on the passed-in event object.
 *
 * C.
 * <p>By default, all listeners are invoked in the calling thread.
 * This allows the danger of a rogue listener blocking the entire application,
 * but adds minimal overhead. Specify an alternative task executor to have
 * listeners executed in different threads, for example from a thread pool.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Brian Clozel
 * @see #setTaskExecutor
 */
// 20201207 简单的应用程序事件多播器实现: 将所有事件多播到所有注册的侦听器，并在调用线程中调用它们。
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

	// 20201207 此多播程序的当前任务执行程序
	@Nullable
	private Executor taskExecutor;

	// 20201207 此多播程序的当前错误处理程序。
	@Nullable
	private ErrorHandler errorHandler;

	// 20201208 应用程序启动收集器
	@Nullable
	private ApplicationStartup applicationStartup;

	/**
	 * Create a new SimpleApplicationEventMulticaster.
	 */
	// 20201208 创建一个新的SimpleApplicationEventMulticaster。
	public SimpleApplicationEventMulticaster() {
	}

	/**
	 * Create a new SimpleApplicationEventMulticaster for the given BeanFactory.
	 */
	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}


	/**
	 * Set a custom executor (typically a {@link org.springframework.core.task.TaskExecutor})
	 * to invoke each listener with.
	 * <p>Default is equivalent to {@link org.springframework.core.task.SyncTaskExecutor},
	 * executing all listeners synchronously in the calling thread.
	 * <p>Consider specifying an asynchronous task executor here to not block the
	 * caller until all listeners have been executed. However, note that asynchronous
	 * execution will not participate in the caller's thread context (class loader,
	 * transaction association) unless the TaskExecutor explicitly supports this.
	 * @see org.springframework.core.task.SyncTaskExecutor
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor
	 */
	public void setTaskExecutor(@Nullable Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Return the current task executor for this multicaster.
	 */
	// 20201207 返回此多播程序的当前任务执行程序。
	@Nullable
	protected Executor getTaskExecutor() {
		return this.taskExecutor;
	}

	/**
	 * Set the {@link ErrorHandler} to invoke in case an exception is thrown
	 * from a listener.
	 * <p>Default is none, with a listener exception stopping the current
	 * multicast and getting propagated to the publisher of the current event.
	 * If a {@linkplain #setTaskExecutor task executor} is specified, each
	 * individual listener exception will get propagated to the executor but
	 * won't necessarily stop execution of other listeners.
	 * <p>Consider setting an {@link ErrorHandler} implementation that catches
	 * and logs exceptions (a la
	 * {@link org.springframework.scheduling.support.TaskUtils#LOG_AND_SUPPRESS_ERROR_HANDLER})
	 * or an implementation that logs exceptions while nevertheless propagating them
	 * (e.g. {@link org.springframework.scheduling.support.TaskUtils#LOG_AND_PROPAGATE_ERROR_HANDLER}).
	 * @since 4.1
	 */
	public void setErrorHandler(@Nullable ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * Return the current error handler for this multicaster.
	 * @since 4.1
	 */
	// 20201207 返回此多播程序的当前错误处理程序。
	@Nullable
	protected ErrorHandler getErrorHandler() {
		return this.errorHandler;
	}

	/**
	 * Set the {@link ApplicationStartup} to track event listener invocations during startup.
	 * @since 5.3
	 */
	public void setApplicationStartup(@Nullable ApplicationStartup applicationStartup) {
		this.applicationStartup = applicationStartup;
	}

	/**
	 * Return the current application startup for this multicaster.
	 */
	@Nullable
	public ApplicationStartup getApplicationStartup() {
		return this.applicationStartup;
	}

	// 20201208 将给定的应用程序事件多播到适当的侦听器
	@Override
	public void multicastEvent(ApplicationEvent event) {
		// 20201207 将给定的应用程序事件多播到适当的侦听器
		multicastEvent(
				event,
				// 20201207 返回指定实例的{@link ResolvableType}
				resolveDefaultEventType(event)
		);
	}

	// 20201207 将给定的应用程序事件多播到适当的侦听器
	@Override
	public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
		// 20201207 获取指定实例的{@link ResolvableType}
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));

		// 20201207 获取此多播程序的当前任务执行程序
		Executor executor = getTaskExecutor();

		// 20201207 返回与给定事件类型匹配的ApplicationListeners的集合。 不匹配的会尽早被排除在外 -> 如果该事件没有注册, 则进行注册了再返回
		// 20201207 遍历每个匹配的监听器
		for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
			// 20201207 如果此多播程序的当前任务执行程序存在
			if (executor != null) {
				// 20201208 用给定的事件调用给定的侦听器
				executor.execute(() -> invokeListener(listener, event));
			}

			// 20201208 此多播程序的当前任务执行程序不存在, 如果应用程序启动收集器已注册
			else if (this.applicationStartup != null) {
				// 20201208 创建新步骤并标记其开始, 步骤名称描述当前操作或阶段
				StartupStep invocationStep = this.applicationStartup.start("spring.event.invoke-listener");

				// 20201208 用给定的事件调用给定的侦听器
				invokeListener(listener, event);

				// 20201208 设置event的标签, 并打印
				invocationStep.tag("event", event::toString);

				// 20201208 如果事件类型不为空, 则设置事件类型的标签, 并打印
				if (eventType != null) {
					invocationStep.tag("eventType", eventType::toString);
				}

				// 20201208 设置监听器标签, 并打印
				invocationStep.tag("listener", listener::toString);

				// 20201208 设置步骤结束
				invocationStep.end();
			}

			// 20201208 如果应用程序启动收集器还没注册
			else {
				// 20201208 则直接用给定的事件调用给定的侦听器
				invokeListener(listener, event);
			}
		}
	}

	// 20201207 返回指定实例的{@link ResolvableType}
	private ResolvableType resolveDefaultEventType(ApplicationEvent event) {
		return ResolvableType.forInstance(event);
	}

	/**
	 * Invoke the given listener with the given event.
	 * @param listener the ApplicationListener to invoke
	 * @param event the current event to propagate
	 * @since 4.1
	 */
	// 20201207 用给定的事件调用给定的侦听器。
	protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
		// 20201207 获取此多播程序的当前错误处理程序
		ErrorHandler errorHandler = getErrorHandler();

		// 20201207 如果此多播程序的当前错误处理程序存在
		if (errorHandler != null) {
			try {
				// 20201208 监听器执行监听事件操作
				doInvokeListener(listener, event);
			}
			catch (Throwable err) {
				// 20201208 处理给定的错误，有可能将其作为致命异常重新抛出。
				errorHandler.handleError(err);
			}
		}
		else {
			// 20201208 如果此多播程序的当前错误处理程序不存在, 则监听器直接执行监听事件操作
			doInvokeListener(listener, event);
		}
	}

	// 20201208 监听器执行监听事件操作
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
		try {
			// 20201208 处理应用程序事件 -> 不同的监听器实现会执行不同的监听操作
			listener.onApplicationEvent(event);
		}
		catch (ClassCastException ex) {
			// 20201208 如果报类转换异常, 且异常类出在事件Class上, 说明是lambda定义的侦听器, 则打印异常日志
			String msg = ex.getMessage();
			if (msg == null || matchesClassCastMessage(msg, event.getClass())) {
				// Possibly a lambda-defined listener which we could not resolve the generic event type for
				// -> let's suppress the exception and just log a debug message.
				// 20201208 可能是lambda定义的侦听器，我们无法解析通用事件类型，因此我们无法抑制该异常并仅记录调试消息。
				Log logger = LogFactory.getLog(getClass());
				if (logger.isTraceEnabled()) {
					logger.trace("Non-matching event type for listener: " + listener, ex);
				}
			}
			else {
				// 20201208 如果不是lambda定义的侦听器, 则抛出异常
				throw ex;
			}
		}
	}

	private boolean matchesClassCastMessage(String classCastMessage, Class<?> eventClass) {
		// On Java 8, the message starts with the class name: "java.lang.String cannot be cast..."
		if (classCastMessage.startsWith(eventClass.getName())) {
			return true;
		}
		// On Java 11, the message starts with "class ..." a.k.a. Class.toString()
		if (classCastMessage.startsWith(eventClass.toString())) {
			return true;
		}
		// On Java 9, the message used to contain the module name: "java.base/java.lang.String cannot be cast..."
		int moduleSeparatorIndex = classCastMessage.indexOf('/');
		if (moduleSeparatorIndex != -1 && classCastMessage.startsWith(eventClass.getName(), moduleSeparatorIndex + 1)) {
			return true;
		}
		// Assuming an unrelated class cast failure...
		return false;
	}

}
