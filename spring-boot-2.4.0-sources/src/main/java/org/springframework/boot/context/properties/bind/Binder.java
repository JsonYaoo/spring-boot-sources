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

package org.springframework.boot.context.properties.bind;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.ConfigurationPropertyState;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.Assert;

/**
 * A container object which Binds objects from one or more
 * {@link ConfigurationPropertySource ConfigurationPropertySources}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 */
// 20201202 绑定一个或多个{@link ConfigurationPropertySource ConfigurationPropertySources}中的对象的容器对象。
public class Binder {

	private static final Set<Class<?>> NON_BEAN_CLASSES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(Object.class, Class.class)));

	private final Iterable<ConfigurationPropertySource> sources;

	private final PlaceholdersResolver placeholdersResolver;

	// 20201202 用于类型转换的服务接口
	private final ConversionService conversionService;

	// 20201202 属性注册的中心接口
	private final Consumer<PropertyEditorRegistry> propertyEditorInitializer;

	// 20201202 默认绑定后的回调接口
	private final BindHandler defaultBindHandler;

	// 20201202 data绑定实例集合
	private final List<DataObjectBinder> dataObjectBinders;

	/**
	 * Create a new {@link Binder} instance for the specified sources. A
	 * {@link DefaultFormattingConversionService} will be used for all conversion.
	 * @param sources the sources used for binding
	 */
	public Binder(ConfigurationPropertySource... sources) {
		this(Arrays.asList(sources), null, null, null);
	}

	/**
	 * Create a new {@link Binder} instance for the specified sources. A
	 * {@link DefaultFormattingConversionService} will be used for all conversion.
	 * @param sources the sources used for binding
	 */
	public Binder(Iterable<ConfigurationPropertySource> sources) {
		this(sources, null, null, null);
	}

	/**
	 * Create a new {@link Binder} instance for the specified sources.
	 * @param sources the sources used for binding
	 * @param placeholdersResolver strategy to resolve any property placeholders
	 */
	public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver) {
		this(sources, placeholdersResolver, null, null);
	}

	/**
	 * Create a new {@link Binder} instance for the specified sources.
	 * @param sources the sources used for binding
	 * @param placeholdersResolver strategy to resolve any property placeholders
	 * @param conversionService the conversion service to convert values (or {@code null}
	 * to use {@link ApplicationConversionService})
	 */
	public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver,
			ConversionService conversionService) {
		this(sources, placeholdersResolver, conversionService, null);
	}

	/**
	 * Create a new {@link Binder} instance for the specified sources.
	 * @param sources the sources used for binding
	 * @param placeholdersResolver strategy to resolve any property placeholders
	 * @param conversionService the conversion service to convert values (or {@code null}
	 * to use {@link ApplicationConversionService})
	 * @param propertyEditorInitializer initializer used to configure the property editors
	 * that can convert values (or {@code null} if no initialization is required). Often
	 * used to call {@link ConfigurableListableBeanFactory#copyRegisteredEditorsTo}.
	 */
	public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver,
			ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
		this(sources, placeholdersResolver, conversionService, propertyEditorInitializer, null);
	}

	/**
	 * Create a new {@link Binder} instance for the specified sources.
	 * @param sources the sources used for binding	// 20201202 用于绑定的源
	 * @param placeholdersResolver strategy to resolve any property placeholders // 20201202 解析任何属性占位符的策略
	 * @param conversionService the conversion service to convert values (or {@code null}
	 * to use {@link ApplicationConversionService}) // 20201202 转换值的转换服务（或{@code null}使用{@link ApplicationConversionService}）
	 *
	 * // 20201202 初始化器用于配置可以转换值的属性编辑器（如果不需要初始化，则使用{@code null}）。通常用于调用{@link ConfigurableListableBeanFactory#copyRegisteredEditorsTo}。
	 * @param propertyEditorInitializer initializer used to configure the property editors
	 * that can convert values (or {@code null} if no initialization is required). Often
	 * used to call {@link ConfigurableListableBeanFactory#copyRegisteredEditorsTo}.
	 * @param defaultBindHandler the default bind handler to use if none is specified when
	 * binding // 20201202 绑定时未指定时要使用的默认绑定处理程序
	 * @since 2.2.0
	 */
	// 20201202 为指定的源创建一个新的{@link binder}实例。
	public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver,
			ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer,
			BindHandler defaultBindHandler) {
		this(sources, placeholdersResolver, conversionService, propertyEditorInitializer, defaultBindHandler, null);
	}

	/**
	 * Create a new {@link Binder} instance for the specified sources.
	 * @param sources the sources used for binding
	 * @param placeholdersResolver strategy to resolve any property placeholders
	 * @param conversionService the conversion service to convert values (or {@code null}
	 * to use {@link ApplicationConversionService})
	 * @param propertyEditorInitializer initializer used to configure the property editors
	 * that can convert values (or {@code null} if no initialization is required). Often
	 * used to call {@link ConfigurableListableBeanFactory#copyRegisteredEditorsTo}.
	 * @param defaultBindHandler the default bind handler to use if none is specified when
	 * binding // 20201220 绑定时未指定时要使用的默认绑定处理程序
	 * @param constructorProvider the constructor provider which provides the bind
	 * constructor to use when binding // 20201202 提供绑定时要使用的绑定构造函数的构造函数提供程序
	 * @since 2.2.1
	 */
	// 20201202 为指定的源创建一个新的{@linkbinder}实例。
	public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver,
			ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer,
			BindHandler defaultBindHandler, BindConstructorProvider constructorProvider) {
		// 202021202 属性源不能为空
		Assert.notNull(sources, "Sources must not be null");

		// 20201202 注册属性源
		this.sources = sources;

		// 20201202 注册${}占位符解析器
		this.placeholdersResolver = (placeholdersResolver != null) ? placeholdersResolver : PlaceholdersResolver.NONE;

		// 20201202 注册默认应用程序共享实例
		this.conversionService = (conversionService != null) ? conversionService
				: ApplicationConversionService.getSharedInstance();

		// 20201202 注册属性注册中心
		this.propertyEditorInitializer = propertyEditorInitializer;

		// 20201202 注册绑定后的回调接口
		this.defaultBindHandler = (defaultBindHandler != null) ? defaultBindHandler : BindHandler.DEFAULT;

		// 20201202 注册绑定策略接口实现
		if (constructorProvider == null) {
			constructorProvider = BindConstructorProvider.DEFAULT;
		}

		// 20201202 构建值绑定对象
		ValueObjectBinder valueObjectBinder = new ValueObjectBinder(constructorProvider);

		// 20201202 默认data绑定实例
		JavaBeanBinder javaBeanBinder = JavaBeanBinder.INSTANCE;

		// 20201202 初始化data绑定实例集合 -> 其中unmodifiableList表示: 不可修改的集合
		this.dataObjectBinders = Collections.unmodifiableList(Arrays.asList(valueObjectBinder, javaBeanBinder));
	}

	/**
	 * // 20201215 使用此联编程序的{@link ConfigurationPropertySource}属性源绑定指定的目标{@link Class}。
	 * Bind the specified target {@link Class} using this binder's
	 * {@link ConfigurationPropertySource property sources}.
	 * @param name the configuration property name to bind
	 * @param target the target class
	 * @param <T> the bound type
	 * @return the binding result (never {@code null})
	 * @see #bind(ConfigurationPropertyName, Bindable, BindHandler)
	 */
	// 20201215 使用此联编程序的{@link ConfigurationPropertySource}属性源绑定指定的目标{@link Class}。
	public <T> BindResult<T> bind(String name, Class<T> target) {
		return bind(name, Bindable.of(target));
	}

	/**
	 * Bind the specified target {@link Bindable} using this binder's
	 * {@link ConfigurationPropertySource property sources}.
	 * @param name the configuration property name to bind
	 * @param target the target bindable
	 * @param <T> the bound type
	 * @return the binding result (never {@code null})
	 * @see #bind(ConfigurationPropertyName, Bindable, BindHandler)
	 */
	// 20201202 使用此绑定器的{@link ConfigurationPropertySource property sources}绑定指定的目标{@link Bindable}。
	public <T> BindResult<T> bind(String name, Bindable<T> target) {
		// 20201202 String name => CharSequence name
		return bind(
				// 20201202 构造ConfigurationPropertyName对象, 持有属性解析后的对象
				ConfigurationPropertyName.of(name),

				// 20201202 Bindable - Binder实例
				target,

				// 不适用助手类
				null);
	}

	/**
	 * Bind the specified target {@link Bindable} using this binder's
	 * {@link ConfigurationPropertySource property sources}.
	 * @param name the configuration property name to bind
	 * @param target the target bindable
	 * @param <T> the bound type
	 * @return the binding result (never {@code null})
	 * @see #bind(ConfigurationPropertyName, Bindable, BindHandler)
	 */
	public <T> BindResult<T> bind(ConfigurationPropertyName name, Bindable<T> target) {
		return bind(name, target, null);
	}

	/**
	 * Bind the specified target {@link Bindable} using this binder's
	 * {@link ConfigurationPropertySource property sources}.
	 * @param name the configuration property name to bind
	 * @param target the target bindable
	 * @param handler the bind handler (may be {@code null})
	 * @param <T> the bound type
	 * @return the binding result (never {@code null})
	 */
	public <T> BindResult<T> bind(String name, Bindable<T> target, BindHandler handler) {
		return bind(ConfigurationPropertyName.of(name), target, handler);
	}

	/**
	 * Bind the specified target {@link Bindable} using this binder's
	 * {@link ConfigurationPropertySource property sources}.
	 * @param name the configuration property name to bind	// 20201202 要绑定的配置属性名称
	 * @param target the target bindable // 20201202 目标绑定
	 * @param handler the bind handler (may be {@code null}) // 20201202 绑定处理程序（可以是{@code null}）
	 * @param <T> the bound type // 20201202 绑定类型
	 * @return the binding result (never {@code null}) // 20201202 绑定结果（never{@code null}）
	 */
	// 20201202 使用此绑定器绑定指定的目标{@link Bindable}
	public <T> BindResult<T> bind(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler) {
		// 20201202 bindable实例绑定配置属性名称
		T bound = bind(name, target, handler, false);
		return BindResult.of(bound);
	}

	/**
	 * Bind the specified target {@link Class} using this binder's
	 * {@link ConfigurationPropertySource property sources} or create a new instance using
	 * the type of the {@link Bindable} if the result of the binding is {@code null}.
	 * @param name the configuration property name to bind
	 * @param target the target class
	 * @param <T> the bound type
	 * @return the bound or created object
	 * @since 2.2.0
	 * @see #bind(ConfigurationPropertyName, Bindable, BindHandler)
	 */
	public <T> T bindOrCreate(String name, Class<T> target) {
		return bindOrCreate(name, Bindable.of(target));
	}

	/**
	 * Bind the specified target {@link Bindable} using this binder's
	 * {@link ConfigurationPropertySource property sources} or create a new instance using
	 * the type of the {@link Bindable} if the result of the binding is {@code null}.
	 * @param name the configuration property name to bind
	 * @param target the target bindable
	 * @param <T> the bound type
	 * @return the bound or created object
	 * @since 2.2.0
	 * @see #bindOrCreate(ConfigurationPropertyName, Bindable, BindHandler)
	 */
	public <T> T bindOrCreate(String name, Bindable<T> target) {
		return bindOrCreate(ConfigurationPropertyName.of(name), target, null);
	}

	/**
	 * Bind the specified target {@link Bindable} using this binder's
	 * {@link ConfigurationPropertySource property sources} or create a new instance using
	 * the type of the {@link Bindable} if the result of the binding is {@code null}.
	 * @param name the configuration property name to bind
	 * @param target the target bindable
	 * @param handler the bind handler
	 * @param <T> the bound type
	 * @return the bound or created object
	 * @since 2.2.0
	 * @see #bindOrCreate(ConfigurationPropertyName, Bindable, BindHandler)
	 */
	public <T> T bindOrCreate(String name, Bindable<T> target, BindHandler handler) {
		return bindOrCreate(ConfigurationPropertyName.of(name), target, handler);
	}

	/**
	 * Bind the specified target {@link Bindable} using this binder's
	 * {@link ConfigurationPropertySource property sources} or create a new instance using
	 * the type of the {@link Bindable} if the result of the binding is {@code null}.
	 * @param name the configuration property name to bind
	 * @param target the target bindable
	 * @param handler the bind handler (may be {@code null})
	 * @param <T> the bound or created type
	 * @return the bound or created object
	 * @since 2.2.0
	 */
	public <T> T bindOrCreate(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler) {
		return bind(name, target, handler, true);
	}

	// 20201202 绑定操作的实现逻辑
	private <T> T bind(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler, boolean create) {
		// 20201202 配置属性名称不能为空
		Assert.notNull(name, "Name must not be null");

		// 20201202 绑定实例不能为空
		Assert.notNull(target, "Target must not be null");

		// 20201202 如果助手处理程序为空, 则使用默认的处理程序BindHandler
		handler = (handler != null) ? handler : this.defaultBindHandler;

		// 20201202 构建BindContext
		Context context = new Context();

		// 20201202 执行绑定操作的实现逻辑
		return bind(name, target, handler, context, false, create);
	}

	// 20201202 执行绑定操作的实现逻辑
	private <T> T bind(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler, Context context,
			boolean allowRecursiveBinding, boolean create) {
		try {
			// 20201202 绑定开始时进行回调
			Bindable<T> replacementTarget = handler.onStart(name, target, context);
			if (replacementTarget == null) {
				// 20201202 如果回调结果为空, 则抛出绑定异常
				return handleBindResult(name, target, handler, context, null, create);
			}

			// 20201202 否则设置绑定回调结果
			target = replacementTarget;

			// 20201202 获取绑定实例结果
			Object bound = bindObject(name, target, handler, context, allowRecursiveBinding);

			// 20201202 如果绑定结果为空, 将会抛出绑定异常
			return handleBindResult(name, target, handler, context, bound, create);
		}
		catch (Exception ex) {
			return handleBindError(name, target, handler, context, ex);
		}
	}

	private <T> T handleBindResult(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler,
			Context context, Object result, boolean create) throws Exception {
		if (result != null) {
			result = handler.onSuccess(name, target, context, result);
			result = context.getConverter().convert(result, target);
		}
		if (result == null && create) {
			result = create(target, context);
			result = handler.onCreate(name, target, context, result);
			result = context.getConverter().convert(result, target);
			Assert.state(result != null, () -> "Unable to create instance for " + target.getType());
		}
		handler.onFinish(name, target, context, result);
		return context.getConverter().convert(result, target);
	}

	private Object create(Bindable<?> target, Context context) {
		for (DataObjectBinder dataObjectBinder : this.dataObjectBinders) {
			Object instance = dataObjectBinder.create(target, context);
			if (instance != null) {
				return instance;
			}
		}
		return null;
	}

	private <T> T handleBindError(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler,
			Context context, Exception error) {
		try {
			Object result = handler.onFailure(name, target, context, error);
			return context.getConverter().convert(result, target);
		}
		catch (Exception ex) {
			if (ex instanceof BindException) {
				throw (BindException) ex;
			}
			throw new BindException(name, target, context.getConfigurationProperty(), ex);
		}
	}

	private <T> Object bindObject(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler,
			Context context, boolean allowRecursiveBinding) {
		ConfigurationProperty property = findProperty(name, context);
		if (property == null && context.depth != 0 && containsNoDescendantOf(context.getSources(), name)) {
			return null;
		}
		AggregateBinder<?> aggregateBinder = getAggregateBinder(target, context);
		if (aggregateBinder != null) {
			return bindAggregate(name, target, handler, context, aggregateBinder);
		}
		if (property != null) {
			try {
				return bindProperty(target, context, property);
			}
			catch (ConverterNotFoundException ex) {
				// We might still be able to bind it using the recursive binders
				Object instance = bindDataObject(name, target, handler, context, allowRecursiveBinding);
				if (instance != null) {
					return instance;
				}
				throw ex;
			}
		}
		return bindDataObject(name, target, handler, context, allowRecursiveBinding);
	}

	private AggregateBinder<?> getAggregateBinder(Bindable<?> target, Context context) {
		Class<?> resolvedType = target.getType().resolve(Object.class);
		if (Map.class.isAssignableFrom(resolvedType)) {
			return new MapBinder(context);
		}
		if (Collection.class.isAssignableFrom(resolvedType)) {
			return new CollectionBinder(context);
		}
		if (target.getType().isArray()) {
			return new ArrayBinder(context);
		}
		return null;
	}

	private <T> Object bindAggregate(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler,
			Context context, AggregateBinder<?> aggregateBinder) {
		AggregateElementBinder elementBinder = (itemName, itemTarget, source) -> {
			boolean allowRecursiveBinding = aggregateBinder.isAllowRecursiveBinding(source);
			Supplier<?> supplier = () -> bind(itemName, itemTarget, handler, context, allowRecursiveBinding, false);
			return context.withSource(source, supplier);
		};
		return context.withIncreasedDepth(() -> aggregateBinder.bind(name, target, elementBinder));
	}

	private ConfigurationProperty findProperty(ConfigurationPropertyName name, Context context) {
		if (name.isEmpty()) {
			return null;
		}
		for (ConfigurationPropertySource source : context.getSources()) {
			ConfigurationProperty property = source.getConfigurationProperty(name);
			if (property != null) {
				return property;
			}
		}
		return null;
	}

	private <T> Object bindProperty(Bindable<T> target, Context context, ConfigurationProperty property) {
		context.setConfigurationProperty(property);
		Object result = property.getValue();
		result = this.placeholdersResolver.resolvePlaceholders(result);
		result = context.getConverter().convert(result, target);
		return result;
	}

	private Object bindDataObject(ConfigurationPropertyName name, Bindable<?> target, BindHandler handler,
			Context context, boolean allowRecursiveBinding) {
		if (isUnbindableBean(name, target, context)) {
			return null;
		}
		Class<?> type = target.getType().resolve(Object.class);
		if (!allowRecursiveBinding && context.isBindingDataObject(type)) {
			return null;
		}
		DataObjectPropertyBinder propertyBinder = (propertyName, propertyTarget) -> bind(name.append(propertyName),
				propertyTarget, handler, context, false, false);
		return context.withDataObject(type, () -> {
			for (DataObjectBinder dataObjectBinder : this.dataObjectBinders) {
				Object instance = dataObjectBinder.bind(name, target, context, propertyBinder);
				if (instance != null) {
					return instance;
				}
			}
			return null;
		});
	}

	private boolean isUnbindableBean(ConfigurationPropertyName name, Bindable<?> target, Context context) {
		for (ConfigurationPropertySource source : context.getSources()) {
			if (source.containsDescendantOf(name) == ConfigurationPropertyState.PRESENT) {
				// We know there are properties to bind so we can't bypass anything
				return false;
			}
		}
		Class<?> resolved = target.getType().resolve(Object.class);
		if (resolved.isPrimitive() || NON_BEAN_CLASSES.contains(resolved)) {
			return true;
		}
		return resolved.getName().startsWith("java.");
	}

	private boolean containsNoDescendantOf(Iterable<ConfigurationPropertySource> sources,
			ConfigurationPropertyName name) {
		for (ConfigurationPropertySource source : sources) {
			if (source.containsDescendantOf(name) != ConfigurationPropertyState.ABSENT) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create a new {@link Binder} instance from the specified environment.
	 * @param environment the environment source (must have attached
	 * {@link ConfigurationPropertySources})
	 * @return a {@link Binder} instance
	 */
	// 20201202 从指定的环境创建一个新的{@linkbinder}实例。
	public static Binder get(Environment environment) {
		// 20201202 不适用助手类
		return get(environment, null);
	}

	/**
	 * Create a new {@link Binder} instance from the specified environment.
	 * @param environment the environment source (must have attached
	 * {@link ConfigurationPropertySources})
	 * @param defaultBindHandler the default bind handler to use if none is specified when // 20201202 当没有指定时要使用的默认绑定处理程序
	 * binding
	 * @return a {@link Binder} instance
	 * @since 2.2.0
	 */
	// 20201202 从指定的环境创建一个新的{@linkbinder}实例。
	public static Binder get(Environment environment, BindHandler defaultBindHandler) {
		// 20201202 根据环境获取Spring属性源(展开的所有属性源)
		Iterable<ConfigurationPropertySource> sources = ConfigurationPropertySources.get(environment);

		// 20201202 使用默认分隔符解析器${}
		PropertySourcesPlaceholdersResolver placeholdersResolver = new PropertySourcesPlaceholdersResolver(environment);

		// 20201202 为指定的源创建一个新的{@link binder}实例, 使用${}占位符解析器
		return new Binder(sources, placeholdersResolver, null, null, defaultBindHandler);
	}

	/**
	 * Context used when binding and the {@link BindContext} implementation.
	 */
	// 20201202 绑定和{@link BindContext}实现时使用的上下文。
	final class Context implements BindContext {
		// 20201202 用于处理绑定期间所需的任何转换的实用程序
		private final BindConverter converter;

		private int depth;

		// 20201202 属性源列表
		private final List<ConfigurationPropertySource> source = Arrays.asList((ConfigurationPropertySource) null);

		private int sourcePushCount;

		private final Deque<Class<?>> dataObjectBindings = new ArrayDeque<>();

		private final Deque<Class<?>> constructorBindings = new ArrayDeque<>();

		private ConfigurationProperty configurationProperty;

		// 20201202 构建BindContext
		Context() {
			// 20201202 注册用于处理绑定期间所需的任何转换的实用程序 => 根据用于类型转换的服务接口 & 属性注册的中心接口获取
			this.converter = BindConverter.get(Binder.this.conversionService, Binder.this.propertyEditorInitializer);
		}

		private void increaseDepth() {
			this.depth++;
		}

		private void decreaseDepth() {
			this.depth--;
		}

		private <T> T withSource(ConfigurationPropertySource source, Supplier<T> supplier) {
			if (source == null) {
				return supplier.get();
			}
			this.source.set(0, source);
			this.sourcePushCount++;
			try {
				return supplier.get();
			}
			finally {
				this.sourcePushCount--;
			}
		}

		private <T> T withDataObject(Class<?> type, Supplier<T> supplier) {
			this.dataObjectBindings.push(type);
			try {
				return withIncreasedDepth(supplier);
			}
			finally {
				this.dataObjectBindings.pop();
			}
		}

		private boolean isBindingDataObject(Class<?> type) {
			return this.dataObjectBindings.contains(type);
		}

		private <T> T withIncreasedDepth(Supplier<T> supplier) {
			increaseDepth();
			try {
				return supplier.get();
			}
			finally {
				decreaseDepth();
			}
		}

		void setConfigurationProperty(ConfigurationProperty configurationProperty) {
			this.configurationProperty = configurationProperty;
		}

		void clearConfigurationProperty() {
			this.configurationProperty = null;
		}

		void pushConstructorBoundTypes(Class<?> value) {
			this.constructorBindings.push(value);
		}

		boolean isNestedConstructorBinding() {
			return !this.constructorBindings.isEmpty();
		}

		void popConstructorBoundTypes() {
			this.constructorBindings.pop();
		}

		PlaceholdersResolver getPlaceholdersResolver() {
			return Binder.this.placeholdersResolver;
		}

		BindConverter getConverter() {
			return this.converter;
		}

		@Override
		public Binder getBinder() {
			return Binder.this;
		}

		@Override
		public int getDepth() {
			return this.depth;
		}

		@Override
		public Iterable<ConfigurationPropertySource> getSources() {
			if (this.sourcePushCount > 0) {
				return this.source;
			}
			return Binder.this.sources;
		}

		@Override
		public ConfigurationProperty getConfigurationProperty() {
			return this.configurationProperty;
		}

	}

}
