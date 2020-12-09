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
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201208
 * A. 提供对合并注解的集合的访问，这些注解通常是从{@link Class}或{@link Method}之类的来源获得的。
 * B. 每个合并的注解表示一个视图，在该视图中可以从不同的源值“合并”属性值，通常是：
 * 		a. 注解中一个或多个属性的显式和隐式{@link AliasFor @AliasFor}声明
 * 		b. 用于元注解的显式{@link AliasFor @AliasFor}声明
 * 		c. 元注解的基于约定的属性别名
 * 		d. 来自元注解声明
 * C. 例如，{@ code @PostMapping}注解可能定义如下：
 * 			@Retention(RetentionPolicy.RUNTIME)
 * 			@RequestMapping(method = RequestMethod.POST)
 * 			public @interface PostMapping {
 *
 *     			@AliasFor(attribute = "path")
 *     			String[] value() default {};
 *
 *     			@AliasFor(attribute = "value")
 *     			String[] path() default {};
 * 			}
 * D. 如果使用{@code @PostMapping（“/home”）}注解方法，则它将包含{@code @PostMapping}和元注解
 *    {@code @RequestMapping}的合并注解。 {@code @RequestMapping}批注的合并视图将包含以下属性：
 *			Name					Value						Source
 *			value					"/home"						Declared in {@code @PostMapping}
 *			path					"/home"						Explicit {@code @AliasFor}
 *			method					RequestMethod.POST			Declared in meta-annotation
 * E. 可以从任何Java {@link AnnotatedElement}的{@linkplain #from（AnnotatedElement）}获取
 *    {@link MergedAnnotations}。 它们也可用于不使用反射的源（例如直接解析字节码的源）。
 * F. 可以使用不同的{@linkplain SearchStrategy搜索策略}来查找包含要聚合的注解的相关源元素。
 *    例如，{@link SearchStrategy＃TYPE_HIERARCHY}将同时搜索超类和已实现的接口。
 * G. 从{@link MergedAnnotations}实例中，您可以{@linkplain #get（String）获取}单个注解，也可以
 *    {@linkplain #stream（）流所有注解}或仅匹配{@linkplain #stream（String） 特定类型}。
 *    您还可以快速判断是否存在注解{@linkplain #isPresent（String）}。
 * H. 以下是一些典型示例：
 * 			// is an annotation present or meta-present?
 * 			// 20201208 注解存在还是元存在？
 * 			mergedAnnotations.isPresent(ExampleAnnotation.class);
 *
 * 			// get the merged "value" attribute of ExampleAnnotation (either directly or
 * 			// meta-present)
 * 		    // 20201208 获取ExampleAnnotation的合并的“值”属性（直接或元存在）
 * 			mergedAnnotations.get(ExampleAnnotation.class).getString("value");
 *
 * 			// get all meta-annotations but no directly present annotations
 * 			// 20201208 获取所有元注解，但不直接显示注解
 * 			mergedAnnotations.stream().filter(MergedAnnotation::isMetaPresent);
 *
 * 			// get all ExampleAnnotation declarations (including any meta-annotations) and
 * 			// print the merged "value" attributes
 * 			// 20201208 获取所有ExampleAnnotation声明（包括任何元注解）并打印合并的“ value”属性
 * 			mergedAnnotations.stream(ExampleAnnotation.class)
 *     			.map(mergedAnnotation -&gt; mergedAnnotation.getString("value"))
 *     			.forEach(System.out::println);
 * I. 注意：{@code MergedAnnotations} API及其底层模型是为Spring通用组件模型中的可组合注解设计的，
 *    着重于属性别名和元注解关系。不支持使用此API检索纯Java注解； 请使用标准Java反射或Spring的
 *    {@link AnnotationUtils}进行简单的注解检索。
 */
/**
 * A.
 * Provides access to a collection of merged annotations, usually obtained
 * from a source such as a {@link Class} or {@link Method}.
 *
 * B.
 * <p>Each merged annotation represents a view where the attribute values may be
 * "merged" from different source values, typically:
 *
 * <ul>
 * a.
 * <li>Explicit and Implicit {@link AliasFor @AliasFor} declarations on one or
 * more attributes within the annotation</li>
 *
 * b.
 * <li>Explicit {@link AliasFor @AliasFor} declarations for a meta-annotation</li>
 *
 * c.
 * <li>Convention based attribute aliases for a meta-annotation</li>
 *
 * d.
 * <li>From a meta-annotation declaration</li>
 * </ul>
 *
 * C.
 * <p>For example, a {@code @PostMapping} annotation might be defined as follows:
 *
 * <pre class="code">
 * &#064;Retention(RetentionPolicy.RUNTIME)
 * &#064;RequestMapping(method = RequestMethod.POST)
 * public &#064;interface PostMapping {
 *
 *     &#064;AliasFor(attribute = "path")
 *     String[] value() default {};
 *
 *     &#064;AliasFor(attribute = "value")
 *     String[] path() default {};
 * }
 * </pre>
 *
 * D.
 * <p>If a method is annotated with {@code @PostMapping("/home")} it will contain
 * merged annotations for both {@code @PostMapping} and the meta-annotation
 * {@code @RequestMapping}. The merged view of the {@code @RequestMapping}
 * annotation will contain the following attributes:
 *
 * <p><table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Value</th>
 * <th>Source</th>
 * </tr>
 * <tr>
 * <td>value</td>
 * <td>"/home"</td>
 * <td>Declared in {@code @PostMapping}</td>
 * </tr>
 * <tr>
 * <td>path</td>
 * <td>"/home"</td>
 * <td>Explicit {@code @AliasFor}</td>
 * </tr>
 * <tr>
 * <td>method</td>
 * <td>RequestMethod.POST</td>
 * <td>Declared in meta-annotation</td>
 * </tr>
 * </table>
 *
 * E.
 * <p>{@link MergedAnnotations} can be obtained {@linkplain #from(AnnotatedElement)
 * from} any Java {@link AnnotatedElement}. They may also be used for sources that
 * don't use reflection (such as those that directly parse bytecode).
 *
 * F.
 * <p>Different {@linkplain SearchStrategy search strategies} can be used to locate
 * related source elements that contain the annotations to be aggregated. For
 * example, {@link SearchStrategy#TYPE_HIERARCHY} will search both superclasses and
 * implemented interfaces.
 *
 * G.
 * <p>From a {@link MergedAnnotations} instance you can either
 * {@linkplain #get(String) get} a single annotation, or {@linkplain #stream()
 * stream all annotations} or just those that match {@linkplain #stream(String)
 * a specific type}. You can also quickly tell if an annotation
 * {@linkplain #isPresent(String) is present}.
 *
 * H.
 * <p>Here are some typical examples:
 *
 * <pre class="code">
 * // is an annotation present or meta-present?
 * mergedAnnotations.isPresent(ExampleAnnotation.class);
 *
 * // get the merged "value" attribute of ExampleAnnotation (either directly or
 * // meta-present)
 * mergedAnnotations.get(ExampleAnnotation.class).getString("value");
 *
 * // get all meta-annotations but no directly present annotations
 * mergedAnnotations.stream().filter(MergedAnnotation::isMetaPresent);
 *
 * // get all ExampleAnnotation declarations (including any meta-annotations) and
 * // print the merged "value" attributes
 * mergedAnnotations.stream(ExampleAnnotation.class)
 *     .map(mergedAnnotation -&gt; mergedAnnotation.getString("value"))
 *     .forEach(System.out::println);
 * </pre>
 *
 * I.
 * <p><b>NOTE: The {@code MergedAnnotations} API and its underlying model have
 * been designed for composable annotations in Spring's common component model,
 * with a focus on attribute aliasing and meta-annotation relationships.</b>
 * There is no support for retrieving plain Java annotations with this API;
 * please use standard Java reflection or Spring's {@link AnnotationUtils}
 * for simple annotation retrieval purposes.
 *
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 5.2
 * @see MergedAnnotation
 * @see MergedAnnotationCollectors
 * @see MergedAnnotationPredicates
 * @see MergedAnnotationSelectors
 */
// 20201208 提供对合并注解的集合的访问，这些注解通常是从{@link Class}或{@link Method}之类的来源获得的
public interface MergedAnnotations extends Iterable<MergedAnnotation<Annotation>> {

	/**
	 * Determine if the specified annotation is either directly present or
	 * meta-present.
	 * <p>Equivalent to calling {@code get(annotationType).isPresent()}.
	 * @param annotationType the annotation type to check
	 * @return {@code true} if the annotation is present
	 */
	<A extends Annotation> boolean isPresent(Class<A> annotationType);

	/**
	 * 20201208
	 * A. 返回注解详细信息确定指定的注解是直接存在还是元存在。 在基础元素的直接注解上。
	 * B. 等效于调用{@code get（annotationType）.isPresent（）}。
	 */
	/**
	 * A.
	 * Determine if the specified annotation is either directly present or
	 * meta-present.
	 *
	 * B.
	 * <p>Equivalent to calling {@code get(annotationType).isPresent()}.
	 * 
	 * // 20201208 注解类型检查的注解类型的全限定类名
	 * @param annotationType the fully qualified class name of the annotation type
	 * to check
	 * @return {@code true} if the annotation is present	// 20201208 {@code true}（如果存在注解）
	 */
	boolean isPresent(String annotationType);

	/**
	 * Determine if the specified annotation is directly present.
	 * <p>Equivalent to calling {@code get(annotationType).isDirectlyPresent()}.
	 * @param annotationType the annotation type to check
	 * @return {@code true} if the annotation is directly present
	 */
	<A extends Annotation> boolean isDirectlyPresent(Class<A> annotationType);

	/**
	 * Determine if the specified annotation is directly present.
	 * <p>Equivalent to calling {@code get(annotationType).isDirectlyPresent()}.
	 * @param annotationType the fully qualified class name of the annotation type
	 * to check
	 * @return {@code true} if the annotation is directly present
	 */
	boolean isDirectlyPresent(String annotationType);

	/**
	 * Get the {@linkplain MergedAnnotationSelectors#nearest() nearest} matching
	 * annotation or meta-annotation of the specified type, or
	 * {@link MergedAnnotation#missing()} if none is present.
	 * @param annotationType the annotation type to get
	 * @return a {@link MergedAnnotation} instance
	 */
	<A extends Annotation> MergedAnnotation<A> get(Class<A> annotationType);

	/**
	 * Get the {@linkplain MergedAnnotationSelectors#nearest() nearest} matching
	 * annotation or meta-annotation of the specified type, or
	 * {@link MergedAnnotation#missing()} if none is present.
	 * @param annotationType the annotation type to get
	 * @param predicate a predicate that must match, or {@code null} if only
	 * type matching is required
	 * @return a {@link MergedAnnotation} instance
	 * @see MergedAnnotationPredicates
	 */
	<A extends Annotation> MergedAnnotation<A> get(Class<A> annotationType,
                                                   @Nullable Predicate<? super MergedAnnotation<A>> predicate);

	/**
	 * Get a matching annotation or meta-annotation of the specified type, or
	 * {@link MergedAnnotation#missing()} if none is present.
	 * @param annotationType the annotation type to get
	 * @param predicate a predicate that must match, or {@code null} if only
	 * type matching is required
	 * @param selector a selector used to choose the most appropriate annotation
	 * within an aggregate, or {@code null} to select the
	 * {@linkplain MergedAnnotationSelectors#nearest() nearest}
	 * @return a {@link MergedAnnotation} instance
	 * @see MergedAnnotationPredicates
	 * @see MergedAnnotationSelectors
	 */
	<A extends Annotation> MergedAnnotation<A> get(Class<A> annotationType,
                                                   @Nullable Predicate<? super MergedAnnotation<A>> predicate,
                                                   @Nullable MergedAnnotationSelector<A> selector);

	/**
	 * Get the {@linkplain MergedAnnotationSelectors#nearest() nearest} matching
	 * annotation or meta-annotation of the specified type, or
	 * {@link MergedAnnotation#missing()} if none is present.
	 * @param annotationType the fully qualified class name of the annotation type
	 * to get
	 * @return a {@link MergedAnnotation} instance
	 */
	<A extends Annotation> MergedAnnotation<A> get(String annotationType);

	/**
	 * Get the {@linkplain MergedAnnotationSelectors#nearest() nearest} matching
	 * annotation or meta-annotation of the specified type, or
	 * {@link MergedAnnotation#missing()} if none is present.
	 * @param annotationType the fully qualified class name of the annotation type
	 * to get
	 * @param predicate a predicate that must match, or {@code null} if only
	 * type matching is required
	 * @return a {@link MergedAnnotation} instance
	 * @see MergedAnnotationPredicates
	 */
	<A extends Annotation> MergedAnnotation<A> get(String annotationType,
                                                   @Nullable Predicate<? super MergedAnnotation<A>> predicate);

	/**
	 * Get a matching annotation or meta-annotation of the specified type, or
	 * {@link MergedAnnotation#missing()} if none is present.
	 * @param annotationType the fully qualified class name of the annotation type
	 * to get
	 * @param predicate a predicate that must match, or {@code null} if only
	 * type matching is required
	 * @param selector a selector used to choose the most appropriate annotation
	 * within an aggregate, or {@code null} to select the
	 * {@linkplain MergedAnnotationSelectors#nearest() nearest}
	 * @return a {@link MergedAnnotation} instance
	 * @see MergedAnnotationPredicates
	 * @see MergedAnnotationSelectors
	 */
	// 20201209 获取指定类型的匹配注解或元注解；如果不存在，则获取{@link MergedAnnotation＃missing（）}。
	<A extends Annotation> MergedAnnotation<A> get(String annotationType,
                                                   @Nullable Predicate<? super MergedAnnotation<A>> predicate,
                                                   @Nullable MergedAnnotationSelector<A> selector);

	/**
	 * Stream all annotations and meta-annotations that match the specified
	 * type. The resulting stream follows the same ordering rules as
	 * {@link #stream()}.
	 * @param annotationType the annotation type to match
	 * @return a stream of matching annotations
	 */
	<A extends Annotation> Stream<MergedAnnotation<A>> stream(Class<A> annotationType);

	/**
	 * Stream all annotations and meta-annotations that match the specified
	 * type. The resulting stream follows the same ordering rules as
	 * {@link #stream()}.
	 * @param annotationType the fully qualified class name of the annotation type
	 * to match
	 * @return a stream of matching annotations
	 */
	<A extends Annotation> Stream<MergedAnnotation<A>> stream(String annotationType);

	/**
	 * Stream all annotations and meta-annotations contained in this collection.
	 * The resulting stream is ordered first by the
	 * {@linkplain MergedAnnotation#getAggregateIndex() aggregate index} and then
	 * by the annotation distance (with the closest annotations first). This ordering
	 * means that, for most use-cases, the most suitable annotations appear
	 * earliest in the stream.
	 * @return a stream of annotations
	 */
	Stream<MergedAnnotation<Annotation>> stream();


	/**
	 * Create a new {@link MergedAnnotations} instance containing all
	 * annotations and meta-annotations from the specified element. The
	 * resulting instance will not include any inherited annotations. If you
	 * want to include those as well you should use
	 * {@link #from(AnnotatedElement, SearchStrategy)} with an appropriate
	 * {@link SearchStrategy}.
	 * @param element the source element
	 * @return a {@link MergedAnnotations} instance containing the element's
	 * annotations
	 */
	static MergedAnnotations from(AnnotatedElement element) {
		return from(element, SearchStrategy.DIRECT);
	}

	/**
	 * Create a new {@link MergedAnnotations} instance containing all
	 * annotations and meta-annotations from the specified element and,
	 * depending on the {@link SearchStrategy}, related inherited elements.
	 * @param element the source element
	 * @param searchStrategy the search strategy to use
	 * @return a {@link MergedAnnotations} instance containing the merged
	 * element annotations
	 */
	static MergedAnnotations from(AnnotatedElement element, SearchStrategy searchStrategy) {
		return from(element, searchStrategy, RepeatableContainers.standardRepeatables());
	}

	/**
	 * Create a new {@link MergedAnnotations} instance containing all
	 * annotations and meta-annotations from the specified element and,
	 * depending on the {@link SearchStrategy}, related inherited elements.
	 * @param element the source element	// 20201208 源元素
	 * @param searchStrategy the search strategy to use // 20201208	使用的搜索策略
	 * @param repeatableContainers the repeatable containers that may be used by // 20201208 元素注解或元注解可以使用的可重复容器
	 * the element annotations or the meta-annotations
	 * @return a {@link MergedAnnotations} instance containing the merged	// 20201208 包含合并后的{@link MergedAnnotations}实例
	 * element annotations
	 */
	// 20201208 创建一个新的{@link MergedAnnotations}实例，该实例包含指定元素中的所有注解和元注解，并取决于{@link SearchStrategy}，相关的继承元素。
	static MergedAnnotations from(AnnotatedElement element, SearchStrategy searchStrategy, RepeatableContainers repeatableContainers) {
		// 20201208 获取合并注解 -> 这里补充了含"java.lang", "org.springframework.lang"包路径注解过滤器
		return from(element, searchStrategy, repeatableContainers, AnnotationFilter.PLAIN);
	}

	/**
	 * Create a new {@link MergedAnnotations} instance containing all
	 * annotations and meta-annotations from the specified element and,
	 * depending on the {@link SearchStrategy}, related inherited elements.
	 *
	 * @param element the source element	// 20201208 源元素
	 * @param searchStrategy the search strategy to use		// 20201208 使用的搜索策略
	 * @param repeatableContainers the repeatable containers that may be used by
	 * the element annotations or the meta-annotations	//	20201208 元素注解或元注解可以使用的可重复容器
	 * @param annotationFilter an annotation filter used to restrict the
	 * annotations considered	//	20201208 注解过滤器，用于限制所考虑的注解
	 * @return a {@link MergedAnnotations} instance containing the merged
	 * annotations for the supplied element
	 */
	// 20201208 创建一个新的{@link MergedAnnotations}实例，该实例包含指定元素中的所有注解和元注解，并取决于{@link SearchStrategy}，相关的继承元素。
	static MergedAnnotations from(AnnotatedElement element,	// 20201208 源元素
								  SearchStrategy searchStrategy,	// 20201208 使用的搜索策略
                                  RepeatableContainers repeatableContainers, 	//	20201208 元素注解或元注解可以使用的可重复容器
								  AnnotationFilter annotationFilter	//	20201208 注解过滤器，用于限制所考虑的注解
	) {
		// 20201208 元素注解或元注解可以使用的可重复容器不能为空
		Assert.notNull(repeatableContainers, "RepeatableContainers must not be null");

		// 20201208 注解过滤器不能为空
		Assert.notNull(annotationFilter, "AnnotationFilter must not be null");

		// 20201208 创建一个新的{@link MergedAnnotations}实例，该实例包含指定元素中的所有注解和元注解，并取决于{@link SearchStrategy}，相关的继承元素。
		return TypeMappedAnnotations.from(element, searchStrategy, repeatableContainers, annotationFilter);
	}

	/**
	 * Create a new {@link MergedAnnotations} instance from the specified
	 * annotations.
	 * @param annotations the annotations to include
	 * @return a {@link MergedAnnotations} instance containing the annotations
	 * @see #from(Object, Annotation...)
	 */
	static MergedAnnotations from(Annotation... annotations) {
		return from(annotations, annotations);
	}

	/**
	 * Create a new {@link MergedAnnotations} instance from the specified
	 * annotations.
	 * @param source the source for the annotations. This source is used only
	 * for information and logging. It does not need to <em>actually</em>
	 * contain the specified annotations, and it will not be searched.
	 * @param annotations the annotations to include
	 * @return a {@link MergedAnnotations} instance containing the annotations
	 * @see #from(Annotation...)
	 * @see #from(AnnotatedElement)
	 */
	static MergedAnnotations from(Object source, Annotation... annotations) {
		return from(source, annotations, RepeatableContainers.standardRepeatables());
	}

	/**
	 * Create a new {@link MergedAnnotations} instance from the specified
	 * annotations.
	 * @param source the source for the annotations. This source is used only
	 * for information and logging. It does not need to <em>actually</em>
	 * contain the specified annotations, and it will not be searched.
	 * @param annotations the annotations to include
	 * @param repeatableContainers the repeatable containers that may be used by
	 * meta-annotations
	 * @return a {@link MergedAnnotations} instance containing the annotations
	 */
	static MergedAnnotations from(Object source, Annotation[] annotations, RepeatableContainers repeatableContainers) {
		return from(source, annotations, repeatableContainers, AnnotationFilter.PLAIN);
	}

	/**
	 * Create a new {@link MergedAnnotations} instance from the specified
	 * annotations.
	 * @param source the source for the annotations. This source is used only
	 * for information and logging. It does not need to <em>actually</em>
	 * contain the specified annotations, and it will not be searched.
	 * @param annotations the annotations to include
	 * @param repeatableContainers the repeatable containers that may be used by
	 * meta-annotations
	 * @param annotationFilter an annotation filter used to restrict the
	 * annotations considered
	 * @return a {@link MergedAnnotations} instance containing the annotations
	 */
	static MergedAnnotations from(Object source, Annotation[] annotations,
                                  RepeatableContainers repeatableContainers, AnnotationFilter annotationFilter) {

		Assert.notNull(repeatableContainers, "RepeatableContainers must not be null");
		Assert.notNull(annotationFilter, "AnnotationFilter must not be null");
		return TypeMappedAnnotations.from(source, annotations, repeatableContainers, annotationFilter);
	}

	/**
	 * Create a new {@link MergedAnnotations} instance from the specified
	 * collection of directly present annotations. This method allows a
	 * {@link MergedAnnotations} instance to be created from annotations that
	 * are not necessarily loaded using reflection. The provided annotations
	 * must all be {@link MergedAnnotation#isDirectlyPresent() directly present}
	 * and must have an {@link MergedAnnotation#getAggregateIndex() aggregate
	 * index} of {@code 0}.
	 * <p>The resulting {@link MergedAnnotations} instance will contain both the
	 * specified annotations, and any meta-annotations that can be read using
	 * reflection.
	 * @param annotations the annotations to include
	 * @return a {@link MergedAnnotations} instance containing the annotations
	 * @see MergedAnnotation#of(ClassLoader, Object, Class, java.util.Map)
	 */
	static MergedAnnotations of(Collection<MergedAnnotation<?>> annotations) {
		return MergedAnnotationsCollection.of(annotations);
	}

	/**
	 * 20201208
	 * A. {@link MergedAnnotations＃from（AnnotatedElement，SearchStrategy）}支持的搜索策略。
	 * B. 每种策略都会创建一组不同的集合，这些集合将组合在一起以创建最终的{@link MergedAnnotations}。
	 */
	/**
	 * A.
	 * Search strategies supported by
	 * {@link MergedAnnotations#from(AnnotatedElement, SearchStrategy)}.
	 *
	 * B.
	 * <p>Each strategy creates a different set of aggregates that will be
	 * combined to create the final {@link MergedAnnotations}.
	 */
	// 20201208 MergedAnnotations支持的搜索策略枚举类
	enum SearchStrategy {

		/**
		 * Find only directly declared annotations, without considering
		 * {@link Inherited @Inherited} annotations and without searching
		 * superclasses or implemented interfaces.
		 */
		// 20201208 仅查找直接声明的注解，而不考虑{@link Inherited @Inherited}注解，也无需搜索超类或已实现的接口。
		DIRECT,// 20201208 直接查找

		/**
		 * 20201208
		 * 查找所有直接声明的注解以及任何{@link Inherited @Inherited}超类注解。 该策略仅在与{@link Class}类型一起使用时才真正有用，因为所有其他{@linkplain AnnotatedElement带注解元素}
		 * 都将忽略{@link Inherited @Inherited}注解。 此策略不搜索已实现的接口。
		 */
		/**
		 * Find all directly declared annotations as well as any
		 * {@link Inherited @Inherited} superclass annotations. This strategy
		 * is only really useful when used with {@link Class} types since the
		 * {@link Inherited @Inherited} annotation is ignored for all other
		 * {@linkplain AnnotatedElement annotated elements}. This strategy does
		 * not search implemented interfaces.
		 */
		INHERITED_ANNOTATIONS,// 20201208 继承的注解查找

		/**
		 * Find all directly declared and superclass annotations. This strategy
		 * is similar to {@link #INHERITED_ANNOTATIONS} except the annotations
		 * do not need to be meta-annotated with {@link Inherited @Inherited}.
		 * This strategy does not search implemented interfaces.
		 */
		// 20201208 查找所有直接声明和超类的注解。 该策略与{@link #INHERITED_ANNOTATIONS}相似，不同之处在于注解不需要使用{@link Inherited @Inherited}进行元注解。 此策略不搜索已实现的接口。
		SUPERCLASS,// 20201208 超类查找

		/**
		 * Perform a full search of the entire type hierarchy, including
		 * superclasses and implemented interfaces. Superclass annotations do
		 * not need to be meta-annotated with {@link Inherited @Inherited}.
		 */
		// 20201208 对整个类型层次结构进行完整搜索，包括超类和已实现的接口。 超类注解不需要使用{@link Inherited @Inherited}进行元注解。
		TYPE_HIERARCHY,// 20201208 类型层次查找

		/**
		 * 20201208
		 * 对源和所有封闭的类执行整个类型层次结构的完整搜索。 该策略与{@link #TYPE_HIERARCHY}相似，不同之处在于，还搜索了{@linkplain Class＃getEnclosingClass（）封闭类}。 超类注解不需要使用{@link Inherited @Inherited}进行元注解。 搜索{@link方法}源时，此策略与{@link #TYPE_HIERARCHY}相同。
		 */
		/**
		 * Perform a full search of the entire type hierarchy on the source
		 * <em>and</em> any enclosing classes. This strategy is similar to
		 * {@link #TYPE_HIERARCHY} except that {@linkplain Class#getEnclosingClass()
		 * enclosing classes} are also searched. Superclass annotations do not
		 * need to be meta-annotated with {@link Inherited @Inherited}. When
		 * searching a {@link Method} source, this strategy is identical to
		 * {@link #TYPE_HIERARCHY}.
		 */
		TYPE_HIERARCHY_AND_ENCLOSING_CLASSES// 20201208 类型层次与闭包查找
	}

}
