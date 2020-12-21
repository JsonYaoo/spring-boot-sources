/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.web.servlet.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * 20201221
 * A. {@link org.springframework.web.servlet.HandlerMapping}接口的实现，该接口从URL映射到名称以斜杠（“/”）开头的bean，类似于Struts如何将URL映射至动作名称。
 * B. 这是{@link org.springframework.web.servlet.DispatcherServlet}和
 *    {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}一起使用的默认实现。 另外，{@link SimpleUrlHandlerMapping}
 *    允许声明性地自定义处理程序映射。
 * C. 映射是从URL到bean名称。 因此，传入URL“/foo”将映射到名为“/foo”的处理程序，或者在多个映射到单个处理程序的情况下，将映射到“/foo/foo2”。
 * D. 支持直接匹配（给定为“/test”->已注册的“/test”）和“*”匹配项（给定为“/test”->已注册的“/t*”）。 请注意，默认值在适用的情况下，在当前servlet映射中进行映射；
 *    有关详细信息，请参见{@link #setAlwaysUseFullPath“ alwaysUseFullPath”}属性。 有关模式选项的详细信息，请参见
 *    {@link org.springframework.util.AntPathMatcher} Javadoc。
 */
/**
 * A.
 * Implementation of the {@link org.springframework.web.servlet.HandlerMapping}
 * interface that maps from URLs to beans with names that start with a slash ("/"),
 * similar to how Struts maps URLs to action names.
 *
 * B.
 * <p>This is the default implementation used by the
 * {@link org.springframework.web.servlet.DispatcherServlet}, along with
 * {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}.
 * Alternatively, {@link SimpleUrlHandlerMapping} allows for customizing a
 * handler mapping declaratively.
 *
 * C.
 * <p>The mapping is from URL to bean name. Thus an incoming URL "/foo" would map
 * to a handler named "/foo", or to "/foo /foo2" in case of multiple mappings to
 * a single handler.
 *
 * D.
 * <p>Supports direct matches (given "/test" -&gt; registered "/test") and "*"
 * matches (given "/test" -&gt; registered "/t*"). Note that the default is
 * to map within the current servlet mapping if applicable; see the
 * {@link #setAlwaysUseFullPath "alwaysUseFullPath"} property for details.
 * For details on the pattern options, see the
 * {@link org.springframework.util.AntPathMatcher} javadoc.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleUrlHandlerMapping
 */
// 20201221 HandlerMapping接口的实现: 映射是从URL到bean名称, 传入URL“/foo”将映射到名为“/foo”的处理程序，或者在多个映射到单个处理程序的情况下，将映射到“/foo/foo2”
public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping {

	/**
	 * Checks name and aliases of the given bean for URLs, starting with "/".
	 */
	@Override
	protected String[] determineUrlsForHandler(String beanName) {
		List<String> urls = new ArrayList<>();
		if (beanName.startsWith("/")) {
			urls.add(beanName);
		}
		String[] aliases = obtainApplicationContext().getAliases(beanName);
		for (String alias : aliases) {
			if (alias.startsWith("/")) {
				urls.add(alias);
			}
		}
		return StringUtils.toStringArray(urls);
	}

}
