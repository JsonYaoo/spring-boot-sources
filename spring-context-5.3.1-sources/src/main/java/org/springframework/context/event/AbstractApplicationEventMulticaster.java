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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 20201207
 * A. {@link ApplicationEventMulticaster}接口的抽象实现，提供了基本的侦听器注册功能。
 * B. 默认情况下，不允许同一侦听器的多个实例，因为它将侦听器保留在链接的Set中。 可以通过“collectionClass” bean属性覆盖用于保存ApplicationListener对象的集合类。
 * C. 实现ApplicationEventMulticaster的实际{@link #multicastEvent}方法留给子类。 {@link SimpleApplicationEventMulticaster}只是将所有事件多播到所有注册的侦听器，
 *    并在调用线程中调用它们。 在这些方面，替代实现可能会更复杂。
 */
/**
 * A.
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility.
 *
 * B.
 * <p>Doesn't permit multiple instances of the same listener by default,
 * as it keeps listeners in a linked Set. The collection class used to hold
 * ApplicationListener objects can be overridden through the "collectionClass"
 * bean property.
 *
 * C.
 * <p>Implementing ApplicationEventMulticaster's actual {@link #multicastEvent} method
 * is left to subclasses. {@link SimpleApplicationEventMulticaster} simply multicasts
 * all events to all registered listeners, invoking them in the calling thread.
 * Alternative implementations could be more sophisticated in those respects.
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 1.2.3
 * @see #getApplicationListeners(ApplicationEvent, ResolvableType)
 * @see SimpleApplicationEventMulticaster
 */
// 20201207 应用程序事件多播器接口的抽象实现: 提供了基本的侦听器注册功能
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {
	// 20201207 一组常规目标侦听器的Helper类
	private final DefaultListenerRetriever defaultRetriever = new DefaultListenerRetriever();

	// 20201207 基于事件类型和源类型的ListenerRetrievers缓存-监听器过滤缓存器
	final Map<ListenerCacheKey, CachedListenerRetriever> retrieverCache = new ConcurrentHashMap<>(64);

	// 20201207 bean类加载器
	@Nullable
	private ClassLoader beanClassLoader;

	// 20201207 bean工厂配置实例
	@Nullable
	private ConfigurableBeanFactory beanFactory;

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableBeanFactory)) {
			throw new IllegalStateException("Not running in a ConfigurableBeanFactory: " + beanFactory);
		}
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
		if (this.beanClassLoader == null) {
			this.beanClassLoader = this.beanFactory.getBeanClassLoader();
		}
	}

	// 20201207 返回已注册的bean工厂配置实例
	private ConfigurableBeanFactory getBeanFactory() {
		// 20201207 bean工厂配置实例还没在该应用程序事件多播器注册, 在报错
		if (this.beanFactory == null) {
			throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans " +
					"because it is not associated with a BeanFactory");
		}

		// 20201207 返回已注册的bean工厂配置实例
		return this.beanFactory;
	}


	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.defaultRetriever) {
			// Explicitly remove target for a proxy, if registered already,
			// in order to avoid double invocations of the same listener.
			Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
			if (singletonTarget instanceof ApplicationListener) {
				this.defaultRetriever.applicationListeners.remove(singletonTarget);
			}
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void addApplicationListenerBean(String listenerBeanName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.remove(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListenerBean(String listenerBeanName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeAllListeners() {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.clear();
			this.defaultRetriever.applicationListenerBeans.clear();
			this.retrieverCache.clear();
		}
	}


	/**
	 * Return a Collection containing all ApplicationListeners.
	 * @return a Collection of ApplicationListeners
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection<ApplicationListener<?>> getApplicationListeners() {
		synchronized (this.defaultRetriever) {
			return this.defaultRetriever.getApplicationListeners();
		}
	}

	/**
	 * Return a Collection of ApplicationListeners matching the given
	 * event type. Non-matching listeners get excluded early.
	 *
	 * @param event the event to be propagated. Allows for excluding
	 * non-matching listeners early, based on cached matching information. // 20201207 要传播的事件。 允许根据缓存的匹配信息尽早排除不匹配的侦听器。
	 * @param eventType the event type // 20201207 事件类型
	 * @return a Collection of ApplicationListeners // 20201207 ApplicationListeners的集合
	 * @see org.springframework.context.ApplicationListener
	 */
	// 20201207 返回与给定事件类型匹配的ApplicationListeners的集合。 不匹配的会尽早被排除在外 -> 如果该事件没有注册, 则进行注册了再返回
	protected Collection<ApplicationListener<?>> getApplicationListeners(ApplicationEvent event, ResolvableType eventType) {
		// 20201207 获取最初发生事件
		Object source = event.getSource();

		// 20201207 获取事件的类型
		Class<?> sourceType = (source != null ? source.getClass() : null);

		// 20201207 构造事件类型和源类型的ListenerRetrievers缓存
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

		// Potential new retriever to populate
		// 20201207 可能要填充的新检索器-监听器过滤缓存器
		CachedListenerRetriever newRetriever = null;

		// Quick check for existing entry on ConcurrentHashMap
		// 20201207 快速检查ConcurrentHashMap上的现有条目 -> 获取对应事件缓存的监听器过滤缓存器
		CachedListenerRetriever existingRetriever = this.retrieverCache.get(cacheKey);

		// 如果对应事件缓存的监听器过滤缓存器不存在
		if (existingRetriever == null) {
			// Caching a new ListenerRetriever if possible
			// 20201207 如果可能，缓存一个新的ListenerRetriever
			if (this.beanClassLoader == null ||
					(ClassUtils.isCacheSafe(event.getClass(), this.beanClassLoader) &&
							(sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
				// 20201207 如果两类型都是缓存安全的, 即当前bean加载器都是两类型类加载器的子类加载器, 则构建监听器过滤缓存器
				newRetriever = new CachedListenerRetriever();

				// 20201207 如果基于事件类型和源类型的ListenerRetrievers缓存-监听器过滤缓存器不存在该键值对, 则添加监听器过滤缓存器
				existingRetriever = this.retrieverCache.putIfAbsent(cacheKey, newRetriever);

				// 20201207 如果监听器过滤缓存器已经存在, 则清空设置的监听器过滤缓存器
				if (existingRetriever != null) {
					newRetriever = null;  // no need to populate it in retrieveApplicationListeners // 20201207 无需在retrieveApplicationListeners中填充它
				}
			}
		}

		// 如果对应事件缓存的监听器过滤缓存器已经存在
		if (existingRetriever != null) {
			// 20201207 获取所有监听器
			Collection<ApplicationListener<?>> result = existingRetriever.getApplicationListeners();

			// 20201207 如果不为空则返回
			if (result != null) {
				return result;
			}
			// 20201207 如果result为null，则另一个线程尚未完全填充现有的检索器。
			// 20201207 对于当前的本地尝试，无法进行缓存。
			// If result is null, the existing retriever is not fully populated yet by another thread.
			// Proceed like caching wasn't possible for this current local attempt.
		}

		// 20201207 实际检索给定事件和源类型的应用程序侦听器 -> 如果该事件没有注册, 则进行注册了再返回
		return retrieveApplicationListeners(eventType, sourceType, newRetriever);
	}

	/**
	 * Actually retrieve the application listeners for the given event and source type.
	 * @param eventType the event type
	 * @param sourceType the event source type
	 * @param retriever the ListenerRetriever, if supposed to populate one (for caching purposes)	// 20201207 ListenerRetriever，如果应该填充一个（出于缓存目的）
	 * @return the pre-filtered list of application listeners for the given event and source type	// 20201207 给定事件和源类型的应用程序侦听器的预过滤列表
	 */
	// 20201207 实际检索给定事件和源类型的应用程序侦听器 -> 如果该事件没有注册, 则进行注册了再返回
	private Collection<ApplicationListener<?>> retrieveApplicationListeners(
			ResolvableType eventType, @Nullable Class<?> sourceType, @Nullable CachedListenerRetriever retriever) {

		// 20201207 所有监听器列表
		List<ApplicationListener<?>> allListeners = new ArrayList<>();

		// 20201207 监听器缓存列表
		Set<ApplicationListener<?>> filteredListeners = (retriever != null ? new LinkedHashSet<>() : null);

		// 20201207 监听器缓存bean名称列表
		Set<String> filteredListenerBeans = (retriever != null ? new LinkedHashSet<>() : null);

		// 20201207 应用程序监听器列表
		Set<ApplicationListener<?>> listeners;

		// 20201207 应用程序监听器bean名称列表
		Set<String> listenerBeans;

		// 20201207 一组常规目标侦听器的Helper类上锁
		synchronized (this.defaultRetriever) {
			// 20201207 获取Helper类的监听器集合
			listeners = new LinkedHashSet<>(this.defaultRetriever.applicationListeners);

			// 20201207 获取Helper类的监听器Bean名称集合
			listenerBeans = new LinkedHashSet<>(this.defaultRetriever.applicationListenerBeans);
		}

		// Add programmatically registered listeners, including ones coming
		// from ApplicationListenerDetector (singleton beans and inner beans).
		// 20201207 添加以编程方式注册的侦听器，包括来自ApplicationListenerDetector的侦听器（单个Bean和内部Bean）。
		for (ApplicationListener<?> listener : listeners) {
			// 20201207 确定给定的侦听器是否支持给定的事件 -> 判断监听器或Class代理后的ResolvableType是否为该事件类型分配的
			if (supportsEvent(listener, eventType, sourceType)) {
				// 20201207 是, 如果缓存没为空, 则监听器缓存列表添加该监听器
				if (retriever != null) {
					filteredListeners.add(listener);
				}

				// 20201207 所有监听器列表也添加该监听器
				allListeners.add(listener);
			}
		}

		// Add listeners by bean name, potentially overlapping with programmatically
		// registered listeners above - but here potentially with additional metadata.
		// 20201207 按bean名称添加侦听器，可能与上面以编程方式注册的侦听器重叠-但此处可能与其他元数据重叠。
		// 20201207 如果应用程序监听器bean名称列表没为空
		if (!listenerBeans.isEmpty()) {
			// 20201207 获取已注册的bean工厂配置实例
			ConfigurableBeanFactory beanFactory = getBeanFactory();

			// 20201207 遍历应用程序监听器bean名称列表
			for (String listenerBeanName : listenerBeans) {
				try {
					// 20201207 判断给定的侦听器是否应包含在给定事件类型的候选项中 -> 如果由监听器类型获取到的ResolvableType为空, 或者确实是为该事件类型分配的, 则为true
					if (supportsEvent(beanFactory, listenerBeanName, eventType)) {
						// 20201207 如果确实在给定事件类型的候选项中, 则根据bean工厂返回一个实例，该实例名称为listenerBeanName, 类型为ApplicationListener
						ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);

						// 20201207 确定该侦听器实例支持给定的事件, 且不在中所有监听器列表
						if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
							// 20201207 如果缓存不为空
							if (retriever != null) {
								// 20201207 如果该bean是单例的
								if (beanFactory.isSingleton(listenerBeanName)) {
									// 20201207 则添加到监听器缓存列表中
									filteredListeners.add(listener);
								}
								else {
									// 20201207 如果不是单例的, 则添加到监听器缓存bean名称列表中
									filteredListenerBeans.add(listenerBeanName);
								}
							}

							// 20201207 最后添加到所有监听器列表中
							allListeners.add(listener);
						}
					}
					else {
						// Remove non-matching listeners that originally came from
						// ApplicationListenerDetector, possibly ruled out by additional
						// BeanDefinition metadata (e.g. factory method generics) above.
						// 20201207 删除最初来自ApplicationListenerDetector的不匹配的侦听器，这些侦听器可能被上述其他BeanDefinition元数据（例如，工厂方法泛型）排除。
						Object listener = beanFactory.getSingleton(listenerBeanName);
						if (retriever != null) {
							filteredListeners.remove(listener);
						}
						allListeners.remove(listener);
					}
				}
				catch (NoSuchBeanDefinitionException ex) {
					// 20201207 单例侦听器实例（没有支持bean的定义）消失了-可能在销毁阶段的中间
					// Singleton listener instance (without backing bean definition) disappeared -
					// probably in the middle of the destruction phase
				}
			}
		}

		// 20201207 根据注解对所有监听器列表进行排序
		AnnotationAwareOrderComparator.sort(allListeners);

		// 20201207 如果缓存不为空
		if (retriever != null) {
			// 20201207 如果监听器缓存bean名称列表不为空
			if (filteredListenerBeans.isEmpty()) {
				// 20201207 则更新注册应用程序事件监听器集合为所有监听器列表
				retriever.applicationListeners = new LinkedHashSet<>(allListeners);

				// 20201207 更新监听器缓存bean名称列表
				retriever.applicationListenerBeans = filteredListenerBeans;
			}
			else {
				// 20201207 否则更新注册应用程序事件监听器集合为监听器缓存列表
				retriever.applicationListeners = filteredListeners;

				// 20201207 更新监听器缓存bean名称列表
				retriever.applicationListenerBeans = filteredListenerBeans;
			}
		}

		// 20201207 返回所有监听器列表
		return allListeners;
	}

	/**
	 * 20201207
	 * A. 在尝试实例化bean定义的侦听器之前，请先检查其一般声明的事件类型，以对其进行早期过滤。
	 * B. 如果此方法作为给定的侦听器返回的第一遍返回{@code true}，则此侦听器实例将在之后通过
	 *    {@link #supportsEvent（ApplicationListener，ResolvableType，Class）}调用进行检索并得到完全评估。
	 */
	/**
	 * A.
	 * Filter a bean-defined listener early through checking its generically declared
	 * event type before trying to instantiate it.
	 *
	 * B.
	 * <p>If this method returns {@code true} for a given listener as a first pass,
	 * the listener instance will get retrieved and fully evaluated through a
	 * {@link #supportsEvent(ApplicationListener, ResolvableType, Class)} call afterwards.
	 *
	 * @param beanFactory the BeanFactory that contains the listener beans	// 20201207 包含侦听器bean的BeanFactory
	 * @param listenerBeanName the name of the bean in the BeanFactory // 20201207 BeanFactory中的bean的名称
	 * @param eventType the event type to check // 20201207 要检查的事件类型
	 * @return whether the given listener should be included in the candidates
	 * for the given event type // 20201207 给定的侦听器是否应包含在给定事件类型的候选项中
	 * @see #supportsEvent(Class, ResolvableType)
	 * @see #supportsEvent(ApplicationListener, ResolvableType, Class)
	 */
	// 20201207 判断给定的侦听器是否应包含在给定事件类型的候选项中 -> 如果由监听器类型获取到的ResolvableType为空, 或者确实是为该事件类型分配的, 则为true
	private boolean supportsEvent(ConfigurableBeanFactory beanFactory, String listenerBeanName, ResolvableType eventType) {
		// 20201207 确定具有给定名称的bean的类型。 更具体地说，确定{@link #getBean}将返回给定名称的对象的类型。
		Class<?> listenerType = beanFactory.getType(listenerBeanName);

		// 20201207 如果bean工厂没有对应的实例类型, 或者GenericApplicationListener | SmartApplicationListener是从该bean指定的
		if (listenerType == null || GenericApplicationListener.class.isAssignableFrom(listenerType) ||
				SmartApplicationListener.class.isAssignableFrom(listenerType)) {
			// 20201207 则返回true, 代表给定的侦听器确实应包含在给定事件类型的候选项中
			return true;
		}

		// 20201207 如果通过listenerType获取到的ResolvableType不为空, 或者不是该事件类型分配的, 则返回false
		if (!supportsEvent(listenerType, eventType)) {
			return false;
		}
		try {
			// 20201207 根据监听器名称获取给定bean名称的合并BeanDefinition
			BeanDefinition bd = beanFactory.getMergedBeanDefinition(listenerBeanName);

			// 20201207 获取实例的可解析的类型, 作为ApplicationListener指定的ResolvableType类型返回, 获取返回的ResolvableType的第一个泛型
			ResolvableType genericEventType = bd.getResolvableType().as(ApplicationListener.class).getGeneric();

			// 20201207 如果该泛型类型为空, 或者确实是来自该事件指定的, 则为true
			return (genericEventType == ResolvableType.NONE || genericEventType.isAssignableFrom(eventType));
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Ignore - no need to check resolvable type for manually registered singleton
			// 20201207 忽略-无需检查手动注册的单例的可解析类型
			return true;
		}
	}

	/**
	 * 20201207
	 * A. 在尝试实例化侦听器之前，通过检查其一般声明的事件类型来尽早筛选侦听器。
	 * B. 如果此方法作为给定的侦听器返回的第一遍返回{@code true}，则此侦听器实例将在之后通过{@link #supportsEvent（ApplicationListener，ResolvableType，Class）}
	 *    调用进行检索并得到完全评估。
	 */
	/**
	 * A.
	 * Filter a listener early through checking its generically declared event
	 * type before trying to instantiate it.
	 *
	 * B.
	 * <p>If this method returns {@code true} for a given listener as a first pass,
	 * the listener instance will get retrieved and fully evaluated through a
	 * {@link #supportsEvent(ApplicationListener, ResolvableType, Class)} call afterwards.
	 *
	 * @param listenerType the listener's type as determined by the BeanFactory
	 * @param eventType the event type to check
	 * @return whether the given listener should be included in the candidates
	 * for the given event type
	 */
	// 20201207 确定给定的侦听器是否应包含在给定事件类型的候选项中
	protected boolean supportsEvent(Class<?> listenerType, ResolvableType eventType) {
		// 20201207 根据监听器Class对象获取对应的ResolvableType
		ResolvableType declaredEventType = GenericApplicationListenerAdapter.resolveDeclaredEventType(listenerType);

		// 20201207 如果获取到的ResolvableType为空, 或者确实是为该事件类型分配的, 则为true
		return (declaredEventType == null || declaredEventType.isAssignableFrom(eventType));
	}

	/**
	 * 20201207
	 * A. 确定给定的侦听器是否支持给定的事件。
	 * B. 默认实现检测{@link SmartApplicationListener}和{@link GenericApplicationListener}接口。 如果使用标准{@link ApplicationListener}，则将使用
	 *    {@link GenericApplicationListenerAdapter}内省目标侦听器的通用声明类型。
	 */
	/**
	 * A.
	 * Determine whether the given listener supports the given event.
	 *
	 * B.
	 * <p>The default implementation detects the {@link SmartApplicationListener}
	 * and {@link GenericApplicationListener} interfaces. In case of a standard
	 * {@link ApplicationListener}, a {@link GenericApplicationListenerAdapter}
	 * will be used to introspect the generically declared type of the target listener.
	 *
	 * @param listener the target listener to check
	 * @param eventType the event type to check against
	 * @param sourceType the source type to check against
	 * @return whether the given listener should be included in the candidates
	 * for the given event type // 20201207 给定的侦听器是否应包含在给定事件类型的候选项中
	 */
	// 20201207 确定给定的侦听器是否支持给定的事件 -> 判断监听器或Class代理后的ResolvableType是否为该事件类型分配的
	protected boolean supportsEvent(ApplicationListener<?> listener, ResolvableType eventType, @Nullable Class<?> sourceType) {
		// 20201207 获取增强后的ApplicationListener -> 如果本身不是则进行适配
		GenericApplicationListener smartListener = (listener instanceof GenericApplicationListener ?
				(GenericApplicationListener) listener : new GenericApplicationListenerAdapter(listener));

		// 20201207 判断增强后的ApplicationListener是否都支持解析后的事件和原生的事件
		return (
				// 20201207 确定此侦听器是否实际上支持给定的事件类型 -> 判断监听器或Class代理后的ResolvableType是否为该事件类型分配的
				smartListener.supportsEventType(eventType) &&

				// 20201207 确定此侦听器是否实际上支持给定的源类型 -> 默认实现始终返回{@code true}
				smartListener.supportsSourceType(sourceType));
	}


	/**
	 * Cache key for ListenerRetrievers, based on event type and source type.
	 */
	// 20201207 基于事件类型和源类型的ListenerRetrievers缓存。
	private static final class ListenerCacheKey implements Comparable<ListenerCacheKey> {

		// 20201207 事件类型
		private final ResolvableType eventType;

		// 20201207 事件源类型
		@Nullable
		private final Class<?> sourceType;

		// 20201207 构造事件类型和源类型的ListenerRetrievers缓存
		public ListenerCacheKey(ResolvableType eventType, @Nullable Class<?> sourceType) {
			// 20201207 事件类型ResolvableType不能为空
			Assert.notNull(eventType, "Event type must not be null");

			// 20201207 注册事件类型
			this.eventType = eventType;

			// 20201207 注册事件源类型
			this.sourceType = sourceType;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ListenerCacheKey)) {
				return false;
			}
			ListenerCacheKey otherKey = (ListenerCacheKey) other;
			return (this.eventType.equals(otherKey.eventType) &&
					ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType));
		}

		@Override
		public int hashCode() {
			return this.eventType.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.sourceType);
		}

		@Override
		public String toString() {
			return "ListenerCacheKey [eventType = " + this.eventType + ", sourceType = " + this.sourceType + "]";
		}

		@Override
		public int compareTo(ListenerCacheKey other) {
			int result = this.eventType.toString().compareTo(other.eventType.toString());
			if (result == 0) {
				if (this.sourceType == null) {
					return (other.sourceType == null ? 0 : -1);
				}
				if (other.sourceType == null) {
					return 1;
				}
				result = this.sourceType.getName().compareTo(other.sourceType.getName());
			}
			return result;
		}
	}

	/**
	 * 20201207
	 * A. 封装了一组特定的目标侦听器的Helper类，从而可以有效地检索预先过滤的侦听器。
	 * B. 每个事件类型和源类型都会缓存此帮助器的实例。
	 */
	/**
	 * A.
	 * Helper class that encapsulates a specific set of target listeners,
	 * allowing for efficient retrieval of pre-filtered listeners.
	 *
	 * B.
	 * <p>An instance of this helper gets cached per event type and source type.
	 */
	// 20201207 监听器过滤缓存器
	private class CachedListenerRetriever {

		// 20201207 应用程序事件监听器集合
		@Nullable
		public volatile Set<ApplicationListener<?>> applicationListeners;

		// 20201207 监听器过滤缓存器
		@Nullable
		public volatile Set<String> applicationListenerBeans;

		// 20201207 获取所有监听器
		@Nullable
		public Collection<ApplicationListener<?>> getApplicationListeners() {
			// 20201207 获取应用程序事件监听器集合
			Set<ApplicationListener<?>> applicationListeners = this.applicationListeners;

			// 20201207 获取监听器过滤缓存器
			Set<String> applicationListenerBeans = this.applicationListenerBeans;

			// 20201207 如果应用程序事件监听器集合 & 监听器过滤缓存器都为空, 则说明尚未完全填充
			if (applicationListeners == null || applicationListenerBeans == null) {
				// Not fully populated yet	// 20201207 尚未完全填充
				return null;
			}

			// 20201207 如果存在, 则汇总两事件监听器
			List<ApplicationListener<?>> allListeners = new ArrayList<>(
					applicationListeners.size() + applicationListenerBeans.size());
			allListeners.addAll(applicationListeners);

			// 20201207 如果监听器过滤缓存器不为空, 则添加监听器过滤缓存器实例到结果集中
			if (!applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : applicationListenerBeans) {
					try {
						allListeners.add(beanFactory.getBean(listenerBeanName, ApplicationListener.class));
					}
					catch (NoSuchBeanDefinitionException ex) {
						// Singleton listener instance (without backing bean definition) disappeared -
						// probably in the middle of the destruction phase
					}
				}
			}

			// 20201207 如果监听器过滤缓存器不为空, 则根据注解对结果集进行排序
			if (!applicationListenerBeans.isEmpty()) {
				AnnotationAwareOrderComparator.sort(allListeners);
			}

			// 20201207 返回所有监听器结果集
			return allListeners;
		}
	}


	/**
	 * Helper class that encapsulates a general set of target listeners.
	 */
	// 20201207 封装了一组常规目标侦听器的Helper类。
	private class DefaultListenerRetriever {
		// 20201207 Helper类的监听器集合
		public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

		// 20201207 Helper类的监听器Bean名称集合
		public final Set<String> applicationListenerBeans = new LinkedHashSet<>();

		public Collection<ApplicationListener<?>> getApplicationListeners() {
			List<ApplicationListener<?>> allListeners = new ArrayList<>(
					this.applicationListeners.size() + this.applicationListenerBeans.size());
			allListeners.addAll(this.applicationListeners);
			if (!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : this.applicationListenerBeans) {
					try {
						ApplicationListener<?> listener =
								beanFactory.getBean(listenerBeanName, ApplicationListener.class);
						if (!allListeners.contains(listener)) {
							allListeners.add(listener);
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						// Singleton listener instance (without backing bean definition) disappeared -
						// probably in the middle of the destruction phase
					}
				}
			}
			AnnotationAwareOrderComparator.sort(allListeners);
			return allListeners;
		}
	}

}
