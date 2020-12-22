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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * 20201222
 * A. MVC框架SPI，允许对核心MVC工作流程进行参数化。
 * B. 必须为每种处理程序类型实现的接口才能处理请求。 此接口用于允许{@link DispatcherServlet}无限地扩展。 {@code DispatcherServlet}通过此接口访问所有已安装的处理程序，
 *    这意味着它不包含特定于任何处理程序类型的代码。
 * C. 请注意，处理程序可以是{@code Object}类型。 这是为了使其他框架中的处理程序无需自定义编码即可与此框架集成，并允许注释驱动的处理程序对象不遵循任何特定的Java接口。
 * D. 此接口不适用于应用程序开发人员。 想要开发自己的Web工作流程的处理程序可以使用它。
 * E. 注意：{@code HandlerAdapter}实现者可以实现{@link org.springframework.core.Ordered}接口，以便能够指定排序顺序（从而确定优先级），以供
 *    {@code DispatcherServlet}应用。 非排序实例被视为最低优先级。
 */
/**
 * A.
 * MVC framework SPI, allowing parameterization of the core MVC workflow.
 *
 * B.
 * <p>Interface that must be implemented for each handler type to handle a request.
 * This interface is used to allow the {@link DispatcherServlet} to be indefinitely
 * extensible. The {@code DispatcherServlet} accesses all installed handlers through
 * this interface, meaning that it does not contain code specific to any handler type.
 *
 * C.
 * <p>Note that a handler can be of type {@code Object}. This is to enable
 * handlers from other frameworks to be integrated with this framework without
 * custom coding, as well as to allow for annotation-driven handler objects that
 * do not obey any specific Java interface.
 *
 * D.
 * <p>This interface is not intended for application developers. It is available
 * to handlers who want to develop their own web workflow.
 *
 * E.
 * <p>Note: {@code HandlerAdapter} implementors may implement the {@link
 * org.springframework.core.Ordered} interface to be able to specify a sorting
 * order (and thus a priority) for getting applied by the {@code DispatcherServlet}.
 * Non-Ordered instances get treated as lowest priority.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 */
// 20201222 通过此接口访问所有已安装的处理程序, 允许对核心MVC工作流程进行参数化
public interface HandlerAdapter {

	/**
	 * 20201222
	 * A. 给定处理程序实例，请返回此{@code HandlerAdapter}是否可以支持它。 典型的HandlerAdapters将根据处理程序类型做出决定。 HandlerAdapters通常通常仅支持一种处理程序类型。
	 * B. 典型的实现：
	 * 		return (handler instanceof MyHandler);
	 */
	/**
	 * A.
	 * Given a handler instance, return whether or not this {@code HandlerAdapter}
	 * can support it. Typical HandlerAdapters will base the decision on the handler
	 * type. HandlerAdapters will usually only support one handler type each.
	 *
	 * B.
	 * <p>A typical implementation:
	 * <p>{@code
	 * return (handler instanceof MyHandler);
	 * }
	 * @param handler the handler object to check
	 * @return whether or not this object can use the given handler
	 */
	boolean supports(Object handler);

	/**
	 * Use the given handler to handle this request.
	 * The workflow that is required may vary widely.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler to use. This object must have previously been passed
	 * to the {@code supports} method of this interface, which must have
	 * returned {@code true}.
	 * @throws Exception in case of errors
	 * @return a ModelAndView object with the name of the view and the required
	 * model data, or {@code null} if the request has been handled directly
	 */
	@Nullable
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	/**
	 * Same contract as for HttpServlet's {@code getLastModified} method.
	 * Can simply return -1 if there's no support in the handler class.
	 * @param request current HTTP request
	 * @param handler the handler to use
	 * @return the lastModified value for the given handler
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
	 */
	// 20201222 与HttpServlet的{@code getLastModified}方法具有相同的约定。 如果处理程序类不支持，则可以简单地返回-1。
	long getLastModified(HttpServletRequest request, Object handler);

}
