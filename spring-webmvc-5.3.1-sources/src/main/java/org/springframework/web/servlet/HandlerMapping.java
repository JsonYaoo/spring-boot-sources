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

package org.springframework.web.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;

/**
 * 20201221
 * A. 由定义请求和处理程序对象之间的映射关系的对象实现的接口。
 * B. 此类可以由应用程序开发人员实施，尽管不是必需的，例如{@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}和
 *    {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping }包含在框架中。 如果没有在应用程序上下文中注册
 *    HandlerMapping bean，则前者是默认值。
 * C. HandlerMapping实现可以支持映射的拦截器，但不是必须的。 处理程序将始终包装在{@link HandlerExecutionChain}实例中，并可选地伴随一些{@link HandlerInterceptor}实例。 DispatcherServlet将首先以给定的顺序调用每个HandlerInterceptor的{@code preHandle}方法，如果所有{@code preHandle}方法都返回了{@code true}，则最终调用处理程序本身。
 * D. 参数化此映射的功能是此MVC框架的强大而独特的功能。 例如，可以根据会话状态，cookie状态或许多其他变量编写自定义映射。 没有其他MVC框架似乎同样灵活。
 * E. 注意：实现可以实现{@link org.springframework.core.Ordered}接口，以便能够指定排序顺序，从而指定DispatcherServlet应用的优先级。 非排序实例被视为最低优先级。
 */
/**
 * A.
 * Interface to be implemented by objects that define a mapping between
 * requests and handler objects.
 *
 * B.
 * <p>This class can be implemented by application developers, although this is not
 * necessary, as {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}
 * and {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}
 * are included in the framework. The former is the default if no
 * HandlerMapping bean is registered in the application context.
 *
 * C.
 * <p>HandlerMapping implementations can support mapped interceptors but do not
 * have to. A handler will always be wrapped in a {@link HandlerExecutionChain}
 * instance, optionally accompanied by some {@link HandlerInterceptor} instances.
 * The DispatcherServlet will first call each HandlerInterceptor's
 * {@code preHandle} method in the given order, finally invoking the handler
 * itself if all {@code preHandle} methods have returned {@code true}.
 *
 * D.
 * <p>The ability to parameterize this mapping is a powerful and unusual
 * capability of this MVC framework. For example, it is possible to write
 * a custom mapping based on session state, cookie state or many other
 * variables. No other MVC framework seems to be equally flexible.
 *
 * E.
 * <p>Note: Implementations can implement the {@link org.springframework.core.Ordered}
 * interface to be able to specify a sorting order and thus a priority for getting
 * applied by DispatcherServlet. Non-Ordered instances get treated as lowest priority.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.core.Ordered
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
 */
// 20201221 由定义请求和处理程序对象之间的映射关系的对象实现的接口: BeanNameUrlHandlerMapping & RequestMappingHandlerMapping
public interface HandlerMapping {

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the mapped
	 * handler for the best matching pattern.
	 * @since 4.3.21
	 */
	String BEST_MATCHING_HANDLER_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingHandler";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the path
	 * used to look up the matching handler, which depending on the configured
	 * {@link org.springframework.web.util.UrlPathHelper} could be the full path
	 * or without the context path, decoded or not, etc.
	 * @since 5.2
	 * @deprecated as of 5.3 in favor of
	 * {@link org.springframework.web.util.UrlPathHelper#PATH_ATTRIBUTE} and
	 * {@link org.springframework.web.util.ServletRequestPathUtils#PATH_ATTRIBUTE}.
	 * To access the cached path used for request mapping, use
	 * {@link org.springframework.web.util.ServletRequestPathUtils#getCachedPathValue(ServletRequest)}.
	 */
	@Deprecated
	String LOOKUP_PATH = HandlerMapping.class.getName() + ".lookupPath";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the path
	 * within the handler mapping, in case of a pattern match, or the full
	 * relevant URI (typically within the DispatcherServlet's mapping) else.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the
	 * best matching pattern within the handler mapping.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";

	/**
	 * Name of the boolean {@link HttpServletRequest} attribute that indicates
	 * whether type-level mappings should be inspected.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations.
	 */
	String INTROSPECT_TYPE_LEVEL_MAPPING = HandlerMapping.class.getName() + ".introspectTypeLevelMapping";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the URI
	 * templates map, mapping variable names to values.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains a map with
	 * URI variable names and a corresponding MultiValueMap of URI matrix
	 * variables for each.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations and may also not be present depending on
	 * whether the HandlerMapping is configured to keep matrix variable content
	 */
	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the set of
	 * producible MediaTypes applicable to the mapped handler.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. Handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";


	/**
	 * Whether this {@code HandlerMapping} instance has been enabled to use parsed
	 * {@link org.springframework.web.util.pattern.PathPattern}s in which case
	 * the {@link DispatcherServlet} automatically
	 * {@link org.springframework.web.util.ServletRequestPathUtils#parseAndCache parses}
	 * the {@code RequestPath} to make it available for
	 * {@link org.springframework.web.util.ServletRequestPathUtils#getParsedRequestPath
	 * access} in {@code HandlerMapping}s, {@code HandlerInterceptor}s, and
	 * other components.
	 * @since 5.3
	 */
	default boolean usesPathPatterns() {
		return false;
	}

	/**
	 * 20201221
	 * A. 返回此请求的处理程序和所有拦截器。 该选择可以根据请求URL，会话状态或实现类选择的任何因素进行。
	 * B. 返回的HandlerExecutionChain包含一个处理程序对象，甚至没有一个标签接口，因此不会以任何方式限制处理程序。 例如，可以编写HandlerAdapter来允许使用另一个框架的处理程序对象。
	 * C. 如果找不到匹配项，则返回{@code null}。 这不是错误。 DispatcherServlet将查询所有已注册的HandlerMapping Bean以查找匹配项，并且仅在没有人可以找到处理程序的情况下才确定存在错误。
	 */
	/**
	 * A.
	 * Return a handler and any interceptors for this request. The choice may be made
	 * on request URL, session state, or any factor the implementing class chooses.
	 *
	 * B.
	 * <p>The returned HandlerExecutionChain contains a handler Object, rather than
	 * even a tag interface, so that handlers are not constrained in any way.
	 * For example, a HandlerAdapter could be written to allow another framework's
	 * handler objects to be used.
	 *
	 * C.
	 * <p>Returns {@code null} if no match was found. This is not an error.
	 * The DispatcherServlet will query all registered HandlerMapping beans to find
	 * a match, and only decide there is an error if none can find a handler.
	 *
	 * @param request current HTTP request
	 * @return a HandlerExecutionChain instance containing handler object and
	 * any interceptors, or {@code null} if no mapping found
	 * @throws Exception if there is an internal error
	 */
	// 20201221 返回此请求的处理程序和所有拦截器。 该选择可以根据请求URL，会话状态或实现类选择的任何因素进行
	@Nullable
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
