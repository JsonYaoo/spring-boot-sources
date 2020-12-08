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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Scanner to search for relevant annotations in the annotation hierarchy of an
 * {@link AnnotatedElement}.
 *
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 5.2
 * @see AnnotationsProcessor
 */
// 20201208 注解扫描程序, 可在{@link AnnotatedElement}的注释层次结构中搜索相关的注解。
abstract class AnnotationsScanner {
	// 20201208 空的注解列表
	private static final Annotation[] NO_ANNOTATIONS = {};

	private static final Method[] NO_METHODS = {};

	// 20201208 每个源本地的所有注解缓存列表
	private static final Map<AnnotatedElement, Annotation[]> declaredAnnotationCache = new ConcurrentReferenceHashMap<>(256);

	private static final Map<Class<?>, Method[]> baseTypeMethodsCache =
			new ConcurrentReferenceHashMap<>(256);


	private AnnotationsScanner() {
	}


	/**
	 * Scan the hierarchy of the specified element for relevant annotations and
	 * call the processor as required.
	 * @param context an optional context object that will be passed back to the
	 * processor
	 * @param source the source element to scan
	 * @param searchStrategy the search strategy to use
	 * @param processor the processor that receives the annotations
	 * @return the result of {@link AnnotationsProcessor#finish(Object)}
	 */
	@Nullable
	static <C, R> R scan(C context, AnnotatedElement source, SearchStrategy searchStrategy,
			AnnotationsProcessor<C, R> processor) {

		R result = process(context, source, searchStrategy, processor);
		return processor.finish(result);
	}

	@Nullable
	private static <C, R> R process(C context, AnnotatedElement source,
			SearchStrategy searchStrategy, AnnotationsProcessor<C, R> processor) {

		if (source instanceof Class) {
			return processClass(context, (Class<?>) source, searchStrategy, processor);
		}
		if (source instanceof Method) {
			return processMethod(context, (Method) source, searchStrategy, processor);
		}
		return processElement(context, source, processor);
	}

	@Nullable
	private static <C, R> R processClass(C context, Class<?> source,
			SearchStrategy searchStrategy, AnnotationsProcessor<C, R> processor) {

		switch (searchStrategy) {
			case DIRECT:
				return processElement(context, source, processor);
			case INHERITED_ANNOTATIONS:
				return processClassInheritedAnnotations(context, source, searchStrategy, processor);
			case SUPERCLASS:
				return processClassHierarchy(context, source, processor, false, false);
			case TYPE_HIERARCHY:
				return processClassHierarchy(context, source, processor, true, false);
			case TYPE_HIERARCHY_AND_ENCLOSING_CLASSES:
				return processClassHierarchy(context, source, processor, true, true);
		}
		throw new IllegalStateException("Unsupported search strategy " + searchStrategy);
	}

	@Nullable
	private static <C, R> R processClassInheritedAnnotations(C context, Class<?> source,
			SearchStrategy searchStrategy, AnnotationsProcessor<C, R> processor) {

		try {
			if (isWithoutHierarchy(source, searchStrategy)) {
				return processElement(context, source, processor);
			}
			Annotation[] relevant = null;
			int remaining = Integer.MAX_VALUE;
			int aggregateIndex = 0;
			Class<?> root = source;
			while (source != null && source != Object.class && remaining > 0 &&
					!hasPlainJavaAnnotationsOnly(source)) {
				R result = processor.doWithAggregate(context, aggregateIndex);
				if (result != null) {
					return result;
				}
				Annotation[] declaredAnnotations = getDeclaredAnnotations(source, true);
				if (relevant == null && declaredAnnotations.length > 0) {
					relevant = root.getAnnotations();
					remaining = relevant.length;
				}
				for (int i = 0; i < declaredAnnotations.length; i++) {
					if (declaredAnnotations[i] != null) {
						boolean isRelevant = false;
						for (int relevantIndex = 0; relevantIndex < relevant.length; relevantIndex++) {
							if (relevant[relevantIndex] != null &&
									declaredAnnotations[i].annotationType() == relevant[relevantIndex].annotationType()) {
								isRelevant = true;
								relevant[relevantIndex] = null;
								remaining--;
								break;
							}
						}
						if (!isRelevant) {
							declaredAnnotations[i] = null;
						}
					}
				}
				result = processor.doWithAnnotations(context, aggregateIndex, source, declaredAnnotations);
				if (result != null) {
					return result;
				}
				source = source.getSuperclass();
				aggregateIndex++;
			}
		}
		catch (Throwable ex) {
			AnnotationUtils.handleIntrospectionFailure(source, ex);
		}
		return null;
	}

	@Nullable
	private static <C, R> R processClassHierarchy(C context, Class<?> source,
			AnnotationsProcessor<C, R> processor, boolean includeInterfaces, boolean includeEnclosing) {

		return processClassHierarchy(context, new int[] {0}, source, processor,
				includeInterfaces, includeEnclosing);
	}

	@Nullable
	private static <C, R> R processClassHierarchy(C context, int[] aggregateIndex, Class<?> source,
			AnnotationsProcessor<C, R> processor, boolean includeInterfaces, boolean includeEnclosing) {

		try {
			R result = processor.doWithAggregate(context, aggregateIndex[0]);
			if (result != null) {
				return result;
			}
			if (hasPlainJavaAnnotationsOnly(source)) {
				return null;
			}
			Annotation[] annotations = getDeclaredAnnotations(source, false);
			result = processor.doWithAnnotations(context, aggregateIndex[0], source, annotations);
			if (result != null) {
				return result;
			}
			aggregateIndex[0]++;
			if (includeInterfaces) {
				for (Class<?> interfaceType : source.getInterfaces()) {
					R interfacesResult = processClassHierarchy(context, aggregateIndex,
						interfaceType, processor, true, includeEnclosing);
					if (interfacesResult != null) {
						return interfacesResult;
					}
				}
			}
			Class<?> superclass = source.getSuperclass();
			if (superclass != Object.class && superclass != null) {
				R superclassResult = processClassHierarchy(context, aggregateIndex,
					superclass, processor, includeInterfaces, includeEnclosing);
				if (superclassResult != null) {
					return superclassResult;
				}
			}
			if (includeEnclosing) {
				// Since merely attempting to load the enclosing class may result in
				// automatic loading of sibling nested classes that in turn results
				// in an exception such as NoClassDefFoundError, we wrap the following
				// in its own dedicated try-catch block in order not to preemptively
				// halt the annotation scanning process.
				try {
					Class<?> enclosingClass = source.getEnclosingClass();
					if (enclosingClass != null) {
						R enclosingResult = processClassHierarchy(context, aggregateIndex,
							enclosingClass, processor, includeInterfaces, true);
						if (enclosingResult != null) {
							return enclosingResult;
						}
					}
				}
				catch (Throwable ex) {
					AnnotationUtils.handleIntrospectionFailure(source, ex);
				}
			}
		}
		catch (Throwable ex) {
			AnnotationUtils.handleIntrospectionFailure(source, ex);
		}
		return null;
	}

	@Nullable
	private static <C, R> R processMethod(C context, Method source,
			SearchStrategy searchStrategy, AnnotationsProcessor<C, R> processor) {

		switch (searchStrategy) {
			case DIRECT:
			case INHERITED_ANNOTATIONS:
				return processMethodInheritedAnnotations(context, source, processor);
			case SUPERCLASS:
				return processMethodHierarchy(context, new int[] {0}, source.getDeclaringClass(),
						processor, source, false);
			case TYPE_HIERARCHY:
			case TYPE_HIERARCHY_AND_ENCLOSING_CLASSES:
				return processMethodHierarchy(context, new int[] {0}, source.getDeclaringClass(),
						processor, source, true);
		}
		throw new IllegalStateException("Unsupported search strategy " + searchStrategy);
	}

	@Nullable
	private static <C, R> R processMethodInheritedAnnotations(C context, Method source,
			AnnotationsProcessor<C, R> processor) {

		try {
			R result = processor.doWithAggregate(context, 0);
			return (result != null ? result :
				processMethodAnnotations(context, 0, source, processor));
		}
		catch (Throwable ex) {
			AnnotationUtils.handleIntrospectionFailure(source, ex);
		}
		return null;
	}

	@Nullable
	private static <C, R> R processMethodHierarchy(C context, int[] aggregateIndex,
			Class<?> sourceClass, AnnotationsProcessor<C, R> processor, Method rootMethod,
			boolean includeInterfaces) {

		try {
			R result = processor.doWithAggregate(context, aggregateIndex[0]);
			if (result != null) {
				return result;
			}
			if (hasPlainJavaAnnotationsOnly(sourceClass)) {
				return null;
			}
			boolean calledProcessor = false;
			if (sourceClass == rootMethod.getDeclaringClass()) {
				result = processMethodAnnotations(context, aggregateIndex[0],
					rootMethod, processor);
				calledProcessor = true;
				if (result != null) {
					return result;
				}
			}
			else {
				for (Method candidateMethod : getBaseTypeMethods(context, sourceClass)) {
					if (candidateMethod != null && isOverride(rootMethod, candidateMethod)) {
						result = processMethodAnnotations(context, aggregateIndex[0],
							candidateMethod, processor);
						calledProcessor = true;
						if (result != null) {
							return result;
						}
					}
				}
			}
			if (Modifier.isPrivate(rootMethod.getModifiers())) {
				return null;
			}
			if (calledProcessor) {
				aggregateIndex[0]++;
			}
			if (includeInterfaces) {
				for (Class<?> interfaceType : sourceClass.getInterfaces()) {
					R interfacesResult = processMethodHierarchy(context, aggregateIndex,
						interfaceType, processor, rootMethod, true);
					if (interfacesResult != null) {
						return interfacesResult;
					}
				}
			}
			Class<?> superclass = sourceClass.getSuperclass();
			if (superclass != Object.class && superclass != null) {
				R superclassResult = processMethodHierarchy(context, aggregateIndex,
					superclass, processor, rootMethod, includeInterfaces);
				if (superclassResult != null) {
					return superclassResult;
				}
			}
		}
		catch (Throwable ex) {
			AnnotationUtils.handleIntrospectionFailure(rootMethod, ex);
		}
		return null;
	}

	private static <C> Method[] getBaseTypeMethods(C context, Class<?> baseType) {
		if (baseType == Object.class || hasPlainJavaAnnotationsOnly(baseType)) {
			return NO_METHODS;
		}

		Method[] methods = baseTypeMethodsCache.get(baseType);
		if (methods == null) {
			boolean isInterface = baseType.isInterface();
			methods = isInterface ? baseType.getMethods() : ReflectionUtils.getDeclaredMethods(baseType);
			int cleared = 0;
			for (int i = 0; i < methods.length; i++) {
				if ((!isInterface && Modifier.isPrivate(methods[i].getModifiers())) ||
						hasPlainJavaAnnotationsOnly(methods[i]) ||
						getDeclaredAnnotations(methods[i], false).length == 0) {
					methods[i] = null;
					cleared++;
				}
			}
			if (cleared == methods.length) {
				methods = NO_METHODS;
			}
			baseTypeMethodsCache.put(baseType, methods);
		}
		return methods;
	}

	private static boolean isOverride(Method rootMethod, Method candidateMethod) {
		return (!Modifier.isPrivate(candidateMethod.getModifiers()) &&
				candidateMethod.getName().equals(rootMethod.getName()) &&
				hasSameParameterTypes(rootMethod, candidateMethod));
	}

	private static boolean hasSameParameterTypes(Method rootMethod, Method candidateMethod) {
		if (candidateMethod.getParameterCount() != rootMethod.getParameterCount()) {
			return false;
		}
		Class<?>[] rootParameterTypes = rootMethod.getParameterTypes();
		Class<?>[] candidateParameterTypes = candidateMethod.getParameterTypes();
		if (Arrays.equals(candidateParameterTypes, rootParameterTypes)) {
			return true;
		}
		return hasSameGenericTypeParameters(rootMethod, candidateMethod,
				rootParameterTypes);
	}

	private static boolean hasSameGenericTypeParameters(
			Method rootMethod, Method candidateMethod, Class<?>[] rootParameterTypes) {

		Class<?> sourceDeclaringClass = rootMethod.getDeclaringClass();
		Class<?> candidateDeclaringClass = candidateMethod.getDeclaringClass();
		if (!candidateDeclaringClass.isAssignableFrom(sourceDeclaringClass)) {
			return false;
		}
		for (int i = 0; i < rootParameterTypes.length; i++) {
			Class<?> resolvedParameterType = ResolvableType.forMethodParameter(
					candidateMethod, i, sourceDeclaringClass).resolve();
			if (rootParameterTypes[i] != resolvedParameterType) {
				return false;
			}
		}
		return true;
	}

	@Nullable
	private static <C, R> R processMethodAnnotations(C context, int aggregateIndex, Method source,
			AnnotationsProcessor<C, R> processor) {

		Annotation[] annotations = getDeclaredAnnotations(source, false);
		R result = processor.doWithAnnotations(context, aggregateIndex, source, annotations);
		if (result != null) {
			return result;
		}
		Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(source);
		if (bridgedMethod != source) {
			Annotation[] bridgedAnnotations = getDeclaredAnnotations(bridgedMethod, true);
			for (int i = 0; i < bridgedAnnotations.length; i++) {
				if (ObjectUtils.containsElement(annotations, bridgedAnnotations[i])) {
					bridgedAnnotations[i] = null;
				}
			}
			return processor.doWithAnnotations(context, aggregateIndex, source, bridgedAnnotations);
		}
		return null;
	}

	@Nullable
	private static <C, R> R processElement(C context, AnnotatedElement source,
			AnnotationsProcessor<C, R> processor) {

		try {
			R result = processor.doWithAggregate(context, 0);
			return (result != null ? result : processor.doWithAnnotations(
				context, 0, source, getDeclaredAnnotations(source, false)));
		}
		catch (Throwable ex) {
			AnnotationUtils.handleIntrospectionFailure(source, ex);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	static <A extends Annotation> A getDeclaredAnnotation(AnnotatedElement source, Class<A> annotationType) {
		Annotation[] annotations = getDeclaredAnnotations(source, false);
		for (Annotation annotation : annotations) {
			if (annotation != null && annotationType == annotation.annotationType()) {
				return (A) annotation;
			}
		}
		return null;
	}

	// 20201208 获取本类声明的所有注解
	static Annotation[] getDeclaredAnnotations(AnnotatedElement source, boolean defensive) {
		// 20201208 初始标记为还没获取缓存
		boolean cached = false;

		// 20201208 根据源Class从每个源本地的所有注解缓存列表获取所有注解
		Annotation[] annotations = declaredAnnotationCache.get(source);

		// 20201208 如果获取到则设置标记为true, 表示已经获取缓存
		if (annotations != null) {
			cached = true;
		}

		// 20201208 否则不存在缓存
		else {
			// 20201208 返回直接在此元素上出现的注解, 此方法将忽略继承的注解
			annotations = source.getDeclaredAnnotations();

			// 20201208 如果获取到的注解数组列表不为空
			if (annotations.length != 0) {
				// 20201208 标记为所有都忽略
				boolean allIgnored = true;

				// 20201208 遍历所有注解
				for (int i = 0; i < annotations.length; i++) {
					Annotation annotation = annotations[i];

					// 20201208 判断是否默认忽略的注解 -> 如果注解类型为"java.lang", "org.springframework.lang"包路径中的注解, 则为true
					if (isIgnorable(annotation.annotationType()) ||
							// 20201208 或者 获取给定注释类型的属性方法, 如果该属性方法能够安全方法该注解, 则为false
							!AttributeMethods.forAnnotationType(annotation.annotationType()).isValid(annotation)) {
						// 20201208 如果该注解视为忽略的注解 或者 不能够安全访问, 则移除该注解
						annotations[i] = null;
					}

					// 20201208 否则该注解标记为不该被忽略
					else {
						allIgnored = false;
					}
				}

				// 20201208 如果所有注解数组中全部都应该被忽略, 则返回空的注解列表
				annotations = (allIgnored ? NO_ANNOTATIONS : annotations);

				// 20201208 如果源Class为Class类型或者Member类型
				if (source instanceof Class || source instanceof Member) {
					// 20201208 则添加该所有注解数组到每个源本地的所有注解缓存列表中
					declaredAnnotationCache.put(source, annotations);

					// 20201208 标记已缓存
					cached = true;
				}
			}
		}

		// 20201208 如果不需要保护, 或者所有注解列表为空, 或者没有缓存时
		if (!defensive || annotations.length == 0 || !cached) {
			// 2020128 则直接返回所有注解列表引用
			return annotations;
		}

		// 20201208 否则需要保护, 则返回深拷贝数组
		return annotations.clone();
	}

	// 20201208 判断是否默认忽略的注解 -> 如果注解类型为"java.lang", "org.springframework.lang"包路径中的注解, 则为true
	private static boolean isIgnorable(Class<?> annotationType) {
		return AnnotationFilter.PLAIN.matches(annotationType);
	}

	// 20201208 判断当前源Class含有的注解是否为空
	static boolean isKnownEmpty(AnnotatedElement source, SearchStrategy searchStrategy) {
		// 20201208 判断注解是否只有一层java注解 -> 如果Class名称带Java开头, 或者为Ordered类型(注解都实现了Order接口?), 则说明为Java注解
		if (hasPlainJavaAnnotationsOnly(source)) {
			return true;
		}

		// 20201208 如果为直接查找 或者 层次查找时没找到层次时
		if (searchStrategy == SearchStrategy.DIRECT
				// 20201208 判断是否没有层次的接口 -> 常见: 如果父类类型就为Object类型 且 没有实现的接口列表, noSuperTypes为true
				|| isWithoutHierarchy(source, searchStrategy)) {
			// 20201208 如果源Class为Method类型, 且为桥接方法时, 则返回false
			if (source instanceof Method && ((Method) source).isBridge()) {
				return false;
			}

			// 20201208 否则获取本类声明的所有注解, 如果所有注解为空时, 则返回true
			return getDeclaredAnnotations(source, false).length == 0;
		}

		// 20201208 否则返回false, 说明该源Class含有的注解不为空
		return false;
	}

	// 20201208 判断注解是否只有一层java直注解 -> 如果Class名称带Java开头, 或者为Ordered类型(注解都实现了Order接口?), 则说明为Java注解
	static boolean hasPlainJavaAnnotationsOnly(@Nullable Object annotatedElement) {
		// 20201208 如果源元素为Class类型
		if (annotatedElement instanceof Class) {
			// 20201208 判断当前bean class是否只有一层java注解 -> 如果Class名称带Java开头, 或者为Ordered类型(注解都实现了Order接口?), 则说明为Java注解
			return hasPlainJavaAnnotationsOnly((Class<?>) annotatedElement);
		}

		// 20201208 如果源元素为Member类型 -> 成员是反映有关单个成员（字段或方法）或构造函数的标识信息的接口。
		else if (annotatedElement instanceof Member) {
			// 20201208 则先根据成员获取本类类型再进行判断
			return hasPlainJavaAnnotationsOnly(((Member) annotatedElement).getDeclaringClass());
		}

		// 20201208 否则如果是其他类型则返回false
		else {
			return false;
		}
	}

	// 20201208 判断当前bean class是否只有一层java注解 -> 如果Class名称带Java开头, 或者为Ordered类型(注解都实现了Order接口?), 则说明为Java注解
	static boolean hasPlainJavaAnnotationsOnly(Class<?> type) {
		// 20201208 如果Class名称带Java开头, 或者为Ordered类型(注解都实现了Order接口?), 则说明为Java注解
		return (type.getName().startsWith("java.") || type == Ordered.class);
	}

	// 20201208 判断是否没有层次的接口 -> 常见: 如果父类类型就为Object类型 且 没有实现的接口列表, noSuperTypes为true
	private static boolean isWithoutHierarchy(AnnotatedElement source, SearchStrategy searchStrategy) {
		// 20201208 如果源Class为Object类型, 则直接返回true, 因为顶级对象没有层次查找可说
		if (source == Object.class) {
			return true;
		}

		// 20201208 如果源Class为Class类型
		if (source instanceof Class) {
			Class<?> sourceClass = (Class<?>) source;
			// 20201208 如果父类类型就为Object类型 且 没有实现的接口列表, noSuperTypes为true
			boolean noSuperTypes = (sourceClass.getSuperclass() == Object.class && sourceClass.getInterfaces().length == 0);
			// 20201208 如果策略为类型层次与闭包查找
			return (searchStrategy == SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES ?
					// 20201208 则如果源Class没有父类和接口 且 不是基础类的直接封闭类, 则返回true
					noSuperTypes && sourceClass.getEnclosingClass() == null :

					// 20201208 如果不是按类型层次与闭包查找, 则判断是否有父类和接口就行了, 没有则为true
					noSuperTypes);
		}

		// 20201208 如果源Class为Method类型
		if (source instanceof Method) {
			Method sourceMethod = (Method) source;
			// 20201208 如果Method是私有的, 或者 递归判断方法的Class没有层次接口, 则为true
			return (Modifier.isPrivate(sourceMethod.getModifiers()) || isWithoutHierarchy(sourceMethod.getDeclaringClass(), searchStrategy));
		}

		// 20201208 否则, 如果是其他类型则返回true
		return true;
	}

	static void clearCache() {
		declaredAnnotationCache.clear();
		baseTypeMethodsCache.clear();
	}

}
