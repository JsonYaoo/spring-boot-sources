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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;

/**
 * 20201222
 * A. 工作流接口，允许自定义处理程序执行链。 应用程序可以为某些处理程序组注册任意数量的现有或自定义拦截器，以添加常见的预处理行为，而无需修改每个处理程序实现。
 * B. 在适当的HandlerAdapter触发处理程序本身的执行之前，将调用HandlerInterceptor。 该机制可以用于预处理方面的大领域，例如用于授权检查或常见的处理程序行为，
 *    例如语言环境或主题更改。 其主要目的是允许排除重复的处理程序代码。
 * C. 在异步处理方案中，处理程序可以在主线程退出时在单独的线程中执行，而无需呈现或调用{@code postHandle}和{@code afterCompletion}回调。 当并发处理程序执行完成时，
 *    将回发该请求以继续呈现模型，并再次调用该协定的所有方法。 有关更多选项和详细信息，请参见{@code org.springframework.web.servlet.AsyncHandlerInterceptor}
 * D. 通常，每个HandlerMapping bean定义一个拦截器链，共享其粒度。 为了能够将某个拦截器链应用于一组处理程序，需要通过一个HandlerMapping bean映射所需的处理程序。
 *    拦截器本身在应用程序上下文中定义为bean，由映射bean定义通过其“interceptors”属性（在XML中：a <list> of <ref>）进行引用。
 * E. HandlerInterceptor基本上类似于Servlet过滤器，但与后者相反，它仅允许自定义预处理以及禁止执行处理程序本身和自定义后处理的选项。 过滤器功能更强大，
 *    例如，它们允许交换传递到链中的请求和响应对象。 请注意，在应用程序上下文中的HandlerInterceptor web.xml中配置了过滤器。
 * F. 作为基本准则，与处理程序相关的细粒度预处理任务是HandlerInterceptor实现的候选对象，尤其是分解出的公共处理程序代码和授权检查。 另一方面，过滤器非常适合请求内容和
 *    视图内容处理，例如多部分表单和GZIP压缩。 这通常显示何时需要将过滤器映射到某些内容类型（例如图像）或所有请求。
 */
/**
 * A.
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 *
 * B.
 * <p>A HandlerInterceptor gets called before the appropriate HandlerAdapter
 * triggers the execution of the handler itself. This mechanism can be used
 * for a large field of preprocessing aspects, e.g. for authorization checks,
 * or common handler behavior like locale or theme changes. Its main purpose
 * is to allow for factoring out repetitive handler code.
 *
 * C.
 * <p>In an asynchronous processing scenario, the handler may be executed in a
 * separate thread while the main thread exits without rendering or invoking the
 * {@code postHandle} and {@code afterCompletion} callbacks. When concurrent
 * handler execution completes, the request is dispatched back in order to
 * proceed with rendering the model and all methods of this contract are invoked
 * again. For further options and details see
 * {@code org.springframework.web.servlet.AsyncHandlerInterceptor}
 *
 * D.
 * <p>Typically an interceptor chain is defined per HandlerMapping bean,
 * sharing its granularity. To be able to apply a certain interceptor chain
 * to a group of handlers, one needs to map the desired handlers via one
 * HandlerMapping bean. The interceptors themselves are defined as beans
 * in the application context, referenced by the mapping bean definition
 * via its "interceptors" property (in XML: a &lt;list&gt; of &lt;ref&gt;).
 *
 * E.
 * <p>HandlerInterceptor is basically similar to a Servlet Filter, but in
 * contrast to the latter it just allows custom pre-processing with the option
 * of prohibiting the execution of the handler itself, and custom post-processing.
 * Filters are more powerful, for example they allow for exchanging the request
 * and response objects that are handed down the chain. Note that a filter
 * gets configured in web.xml, a HandlerInterceptor in the application context.
 *
 * F.
 * <p>As a basic guideline, fine-grained handler-related preprocessing tasks are
 * candidates for HandlerInterceptor implementations, especially factored-out
 * common handler code and authorization checks. On the other hand, a Filter
 * is well-suited for request content and view content handling, like multipart
 * forms and GZIP compression. This typically shows when one needs to map the
 * filter to certain content types (e.g. images), or to all requests.
 *
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerExecutionChain#getInterceptors
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor
 * @see org.springframework.web.servlet.i18n.LocaleChangeInterceptor
 * @see org.springframework.web.servlet.theme.ThemeChangeInterceptor
 * @see javax.servlet.Filter
 */
// 20201222 工作流接口，允许自定义处理程序执行链: 添加常见的预处理行为，而无需修改每个处理程序实现, 在适当的HandlerAdapter触发处理程序本身的执行之前，将调用HandlerInterceptor
public interface HandlerInterceptor {

	/**
	 * 20201222
	 * A. 拦截处理程序的执行。 在HandlerMapping确定适当的处理程序对象之后但在HandlerAdapter调用处理程序之前调用。
	 * B. DispatcherServlet处理执行链中的处理程序，该处理程序由任意数量的拦截器组成，处理程序本身位于末尾。 使用此方法，每个拦截器都可以决定中止执行链，
	 *    通常是发送HTTP错误或编写自定义响应。
	 * C. 注意：特殊注意事项适用于异步请求处理。 有关更多详细信息，请参见{@link org.springframework.web.servlet.AsyncHandlerInterceptor}。
	 * D. 默认实现返回{@code true}。
	 */
	/**
	 * A.
	 * Intercept the execution of a handler. Called after HandlerMapping determined
	 * an appropriate handler object, but before HandlerAdapter invokes the handler.
	 *
	 * B.
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can decide to abort the execution chain,
	 * typically sending an HTTP error or writing a custom response.
	 *
	 * C.
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 *
	 * D.
	 * <p>The default implementation returns {@code true}.
	 *
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 *
	 * // 20201222 {@code true}，如果执行链应该继续下一个拦截器或处理程序本身。 否则，DispatcherServlet假定此拦截器已经处理了响应本身。
	 * @return {@code true} if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherServlet assumes
	 * that this interceptor has already dealt with the response itself.
	 * @throws Exception in case of errors
	 */
	// 20201222 拦截处理程序的执行: 在HandlerMapping确定适当的处理程序对象之后但在HandlerAdapter调用处理程序之前调用, 默认实现返回{@code true}
	default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return true;
	}

	/**
	 * 20201224
	 * A. 拦截处理程序的执行。 在HandlerAdapter实际调用处理程序之后但在DispatcherServlet呈现视图之前调用。 可以通过给定的ModelAndView将其他模型对象暴露给视图。
	 * B. DispatcherServlet处理执行链中的处理程序，该处理程序由任意数量的拦截器组成，处理程序本身位于末尾。 使用此方法，每个拦截器都可以对执行进行后处理，
	 *    并以与执行链相反的顺序进行应用。
	 * C. 注意：特殊注意事项适用于异步请求处理。 有关更多详细信息，请参见{@link org.springframework.web.servlet.AsyncHandlerInterceptor}。
	 * D. 默认实现为空。
	 */
	/**
	 * A.
	 * Intercept the execution of a handler. Called after HandlerAdapter actually
	 * invoked the handler, but before the DispatcherServlet renders the view.
	 * Can expose additional model objects to the view via the given ModelAndView.
	 *
	 * B.
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can post-process an execution,
	 * getting applied in inverse order of the execution chain.
	 *
	 * C.
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 *
	 * D.
	 * <p>The default implementation is empty.
	 *
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param modelAndView the {@code ModelAndView} that the handler returned
	 * (can also be {@code null})
	 * @throws Exception in case of errors
	 */
	// 202012224 拦截处理程序的执行。 在HandlerAdapter实际调用处理程序之后但在DispatcherServlet呈现视图之前调用。
	default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
	}

	/**
	 * 20201224
	 * A. 完成请求处理后（即渲染视图之后）的回调。 处理程序执行的任何结果都将被调用，从而允许适当的资源清理
	 * B. 注意：仅当此拦截器的{@code preHandle}方法成功完成并返回{@code true}时才会调用！
	 * C. 与{@code postHandle}方法一样，该方法将在链中的每个拦截器上以相反的顺序被调用，因此第一个拦截器将是最后一个被调用。
	 * D. 注意：特殊注意事项适用于异步请求处理。 有关更多详细信息，请参见{@link org.springframework.web.servlet.AsyncHandlerInterceptor}。
	 * E. 默认实现为空。
	 */
	/**
	 * A.
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 *
	 * B.
	 * <p>Note: Will only be called if this interceptor's {@code preHandle}
	 * method has successfully completed and returned {@code true}!
	 *
	 * C.
	 * <p>As with the {@code postHandle} method, the method will be invoked on each
	 * interceptor in the chain in reverse order, so the first interceptor will be
	 * the last to be invoked.
	 *
	 * D.
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 *
	 * E.
	 * <p>The default implementation is empty.
	 *
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param ex any exception thrown on handler execution, if any; this does not
	 * include exceptions that have been handled through an exception resolver
	 * @throws Exception in case of errors
	 */
	default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
	}

}
