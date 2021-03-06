/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.bind.support;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.WebRequest;

/**
 * 20201222
 * 回调接口，用于初始化{@link WebDataBinder}，以便在特定Web请求的上下文中执行数据绑定。
 */
/**
 * Callback interface for initializing a {@link WebDataBinder} for performing
 * data binding in the context of a specific web request.
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 2.5
 */
// 20201222 回调接口，用于初始化{@link WebDataBinder}，以便在特定Web请求的上下文中执行数据绑定。
public interface WebBindingInitializer {

	/**
	 * Initialize the given DataBinder.
	 * @param binder the DataBinder to initialize
	 * @since 5.0
	 */
	void initBinder(WebDataBinder binder);

	/**
	 * Initialize the given DataBinder for the given (Servlet) request.
	 * @param binder the DataBinder to initialize
	 * @param request the web request that the data binding happens within
	 * @deprecated as of 5.0 in favor of {@link #initBinder(WebDataBinder)}
	 */
	@Deprecated
	default void initBinder(WebDataBinder binder, WebRequest request) {
		initBinder(binder);
	}

}
