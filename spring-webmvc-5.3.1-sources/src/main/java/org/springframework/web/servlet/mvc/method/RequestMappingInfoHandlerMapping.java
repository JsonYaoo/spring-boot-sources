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

package org.springframework.web.servlet.mvc.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.web.util.pattern.PathPattern;

/**
 * 20201221
 * {@link RequestMappingInfo}为其定义请求和处理程序方法之间的映射的类的抽象基类。
 */
/**
 * Abstract base class for classes for which {@link RequestMappingInfo} defines
 * the mapping between a request and a handler method.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
// 20201221 RequestMappingInfo为其定义请求和处理程序方法之间的映射的类的抽象基类
public abstract class RequestMappingInfoHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> {

	private static final Method HTTP_OPTIONS_HANDLE_METHOD;

	static {
		try {
			HTTP_OPTIONS_HANDLE_METHOD = HttpOptionsHandler.class.getMethod("handle");
		}
		catch (NoSuchMethodException ex) {
			// Should never happen
			throw new IllegalStateException("Failed to retrieve internal handler method for HTTP OPTIONS", ex);
		}
	}


	protected RequestMappingInfoHandlerMapping() {
		setHandlerMethodMappingNamingStrategy(new RequestMappingInfoHandlerMethodMappingNamingStrategy());
	}


	/**
	 * Get the URL path patterns associated with the supplied {@link RequestMappingInfo}.
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
		return info.getPatternValues();
	}

	@Override
	protected Set<String> getDirectPaths(RequestMappingInfo info) {
		return info.getDirectPaths();
	}

	/**
	 * Check if the given RequestMappingInfo matches the current request and
	 * return a (potentially new) instance with conditions that match the
	 * current request -- for example with a subset of URL patterns.
	 * @return an info in case of a match; or {@code null} otherwise.
	 */
	// 20201221 检查给定的RequestMappingInfo是否与当前请求匹配，并返回一个具有与当前请求匹配的条件的（可能是新的）实例，例如带有URL模式的子集。
	@Override
	protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, HttpServletRequest request) {
		// 20201221 检查此请求映射信息中的所有条件是否与提供的请求匹配，并返回具有针对当前请求量身定制的条件的潜在新请求映射信息
		// 20201221 eg: { [/testController/testRequestMapping]}, request实例, 构建RequestMappingInfo => eg: [/testController/testRequestMapping]
		return info.getMatchingCondition(request);
	}

	/**
	 * Provide a Comparator to sort RequestMappingInfos matched to a request.
	 */
	@Override
	protected Comparator<RequestMappingInfo> getMappingComparator(final HttpServletRequest request) {
		return (info1, info2) -> info1.compareTo(info2, request);
	}

	// 20201221 查找给定请求的处理程序，如果未找到特定请求，则返回{@code null}。 {@link #getHandler}调用此方法； 如果设置了{@code null}返回值，则会导致默认处理程序。
	@Override
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		// 20201221 从此请求中删除属性 eg: "org.springframework.web.servlet.HandlerMapping.producibleMediaTypes"
		request.removeAttribute(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
		try {
			// 20201221 查找给定请求的处理程序方法。
			return super.getHandlerInternal(request);
		}
		finally {
			// 20201224 从此请求中删除属性 "org.springframework.web.servlet.mvc.condition.ProducesRequestCondition.MEDIA_TYPES"
			ProducesRequestCondition.clearMediaTypesAttribute(request);
		}
	}

	/**
	 * Expose URI template variables, matrix variables, and producible media types in the request.
	 * @see HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
	 * @see HandlerMapping#MATRIX_VARIABLES_ATTRIBUTE
	 * @see HandlerMapping#PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
	 */
	// 20201221 在请求中公开URI模板变量，矩阵变量和可生产的媒体类型 => eg: 提取匹配路径的变量详情 => eg: "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables"-{}
	@Override
	protected void handleMatch(RequestMappingInfo info, String lookupPath, HttpServletRequest request) {
		// 20201221 => eg: "org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping"-"/testController/testRequestMapping"
		super.handleMatch(info, lookupPath, request);

		// 20201221 根据不为null的情况，返回{@link #getPathPatternsCondition（）}或{@link #getPatternsCondition（）} => eg: [/testController/testRequestMapping]
		RequestCondition<?> condition = info.getActivePatternsCondition();
		if (condition instanceof PathPatternsRequestCondition) {
			extractMatchDetails((PathPatternsRequestCondition) condition, lookupPath, request);
		}
		else {
			// 20201221 提取匹配路径的变量详情 => eg: "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables"-{}
			extractMatchDetails((PatternsRequestCondition) condition, lookupPath, request);
		}

		// 20201221 eg: isEmpty() == true
		if (!info.getProducesCondition().getProducibleMediaTypes().isEmpty()) {
			Set<MediaType> mediaTypes = info.getProducesCondition().getProducibleMediaTypes();
			request.setAttribute(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);
		}
	}

	private void extractMatchDetails(
			PathPatternsRequestCondition condition, String lookupPath, HttpServletRequest request) {

		PathPattern bestPattern;
		Map<String, String> uriVariables;
		if (condition.isEmptyPathMapping()) {
			bestPattern = condition.getFirstPattern();
			uriVariables = Collections.emptyMap();
		}
		else {
			PathContainer path = ServletRequestPathUtils.getParsedRequestPath(request).pathWithinApplication();
			bestPattern = condition.getFirstPattern();
			PathPattern.PathMatchInfo result = bestPattern.matchAndExtract(path);
			Assert.notNull(result, () ->
					"Expected bestPattern: " + bestPattern + " to match lookupPath " + path);
			uriVariables = result.getUriVariables();
			request.setAttribute(MATRIX_VARIABLES_ATTRIBUTE, result.getMatrixVariables());
		}
		request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern.getPatternString());
		request.setAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariables);
	}

	// 20201221 提取匹配路径的变量详情 => eg: "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables"-{}
	private void extractMatchDetails(PatternsRequestCondition condition, String lookupPath, HttpServletRequest request) {

		String bestPattern;
		Map<String, String> uriVariables;

		// 20201221 条件是否为“”（空路径）映射。 eg: false
		if (condition.isEmptyPathMapping()) {
			bestPattern = lookupPath;
			uriVariables = Collections.emptyMap();
		}
		else {
			// 20201221 eg: "/testController/testRequestMapping"
			bestPattern = condition.getPatterns().iterator().next();

			// 20201221 返回{@link已配置的#setPathMatcher} {@code PathMatcher} eg: AntPathMatcher, 给定模式和完整路径，请提取URI模板变量 => eg: "/testController/testRequestMapping"、"/testController/testRequestMapping" => eg: {}
			uriVariables = getPathMatcher().extractUriTemplateVariables(bestPattern, lookupPath);

			// 20201221 返回{@link已配置的#setUrlPathHelper} {@code UrlPathHelper} eg: UrlPathHelper, 是否配置为删除“;” （分号）来自请求URI的内容 => eg: true
			if (!getUrlPathHelper().shouldRemoveSemicolonContent()) {
				request.setAttribute(MATRIX_VARIABLES_ATTRIBUTE, extractMatrixVariables(request, uriVariables));
			}

			// 20201221 返回{@link已配置的#setUrlPathHelper} {@code UrlPathHelper} eg: UrlPathHelper, 通过{@link #decodeRequestString}解码给定的URI路径变量 => eg: {}
			uriVariables = getUrlPathHelper().decodePathVariables(request, uriVariables);
		}

		// 20201221 该属性包含处理程序映射中的最佳匹配模式: "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern"-"/testController/testRequestMapping"
		request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern);

		// 20201221 包含URI模板映射的属性的名称, 将变量名称映射为值: "org.springframework.web.servlet.HandlerMapping.uriTemplateVariables"-{}
		request.setAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariables);
	}

	private Map<String, MultiValueMap<String, String>> extractMatrixVariables(
			HttpServletRequest request, Map<String, String> uriVariables) {

		Map<String, MultiValueMap<String, String>> result = new LinkedHashMap<>();
		uriVariables.forEach((uriVarKey, uriVarValue) -> {

			int equalsIndex = uriVarValue.indexOf('=');
			if (equalsIndex == -1) {
				return;
			}

			int semicolonIndex = uriVarValue.indexOf(';');
			if (semicolonIndex != -1 && semicolonIndex != 0) {
				uriVariables.put(uriVarKey, uriVarValue.substring(0, semicolonIndex));
			}

			String matrixVariables;
			if (semicolonIndex == -1 || semicolonIndex == 0 || equalsIndex < semicolonIndex) {
				matrixVariables = uriVarValue;
			}
			else {
				matrixVariables = uriVarValue.substring(semicolonIndex + 1);
			}

			MultiValueMap<String, String> vars = WebUtils.parseMatrixVariables(matrixVariables);
			result.put(uriVarKey, getUrlPathHelper().decodeMatrixVariables(request, vars));
		});
		return result;
	}

	/**
	 * Iterate all RequestMappingInfo's once again, look if any match by URL at
	 * least and raise exceptions according to what doesn't match.
	 * @throws HttpRequestMethodNotSupportedException if there are matches by URL
	 * but not by HTTP method
	 * @throws HttpMediaTypeNotAcceptableException if there are matches by URL
	 * but not by consumable/producible media types
	 */
	@Override
	protected HandlerMethod handleNoMatch(
			Set<RequestMappingInfo> infos, String lookupPath, HttpServletRequest request) throws ServletException {

		PartialMatchHelper helper = new PartialMatchHelper(infos, request);
		if (helper.isEmpty()) {
			return null;
		}

		if (helper.hasMethodsMismatch()) {
			Set<String> methods = helper.getAllowedMethods();
			if (HttpMethod.OPTIONS.matches(request.getMethod())) {
				HttpOptionsHandler handler = new HttpOptionsHandler(methods);
				return new HandlerMethod(handler, HTTP_OPTIONS_HANDLE_METHOD);
			}
			throw new HttpRequestMethodNotSupportedException(request.getMethod(), methods);
		}

		if (helper.hasConsumesMismatch()) {
			Set<MediaType> mediaTypes = helper.getConsumableMediaTypes();
			MediaType contentType = null;
			if (StringUtils.hasLength(request.getContentType())) {
				try {
					contentType = MediaType.parseMediaType(request.getContentType());
				}
				catch (InvalidMediaTypeException ex) {
					throw new HttpMediaTypeNotSupportedException(ex.getMessage());
				}
			}
			throw new HttpMediaTypeNotSupportedException(contentType, new ArrayList<>(mediaTypes));
		}

		if (helper.hasProducesMismatch()) {
			Set<MediaType> mediaTypes = helper.getProducibleMediaTypes();
			throw new HttpMediaTypeNotAcceptableException(new ArrayList<>(mediaTypes));
		}

		if (helper.hasParamsMismatch()) {
			List<String[]> conditions = helper.getParamConditions();
			throw new UnsatisfiedServletRequestParameterException(conditions, request.getParameterMap());
		}

		return null;
	}


	/**
	 * Aggregate all partial matches and expose methods checking across them.
	 */
	private static class PartialMatchHelper {

		private final List<PartialMatch> partialMatches = new ArrayList<>();

		public PartialMatchHelper(Set<RequestMappingInfo> infos, HttpServletRequest request) {
			for (RequestMappingInfo info : infos) {
				if (info.getActivePatternsCondition().getMatchingCondition(request) != null) {
					this.partialMatches.add(new PartialMatch(info, request));
				}
			}
		}

		/**
		 * Whether there any partial matches.
		 */
		public boolean isEmpty() {
			return this.partialMatches.isEmpty();
		}

		/**
		 * Any partial matches for "methods"?
		 */
		public boolean hasMethodsMismatch() {
			for (PartialMatch match : this.partialMatches) {
				if (match.hasMethodsMatch()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Any partial matches for "methods" and "consumes"?
		 */
		public boolean hasConsumesMismatch() {
			for (PartialMatch match : this.partialMatches) {
				if (match.hasConsumesMatch()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Any partial matches for "methods", "consumes", and "produces"?
		 */
		public boolean hasProducesMismatch() {
			for (PartialMatch match : this.partialMatches) {
				if (match.hasProducesMatch()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Any partial matches for "methods", "consumes", "produces", and "params"?
		 */
		public boolean hasParamsMismatch() {
			for (PartialMatch match : this.partialMatches) {
				if (match.hasParamsMatch()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Return declared HTTP methods.
		 */
		public Set<String> getAllowedMethods() {
			Set<String> result = new LinkedHashSet<>();
			for (PartialMatch match : this.partialMatches) {
				for (RequestMethod method : match.getInfo().getMethodsCondition().getMethods()) {
					result.add(method.name());
				}
			}
			return result;
		}

		/**
		 * Return declared "consumable" types but only among those that also
		 * match the "methods" condition.
		 */
		public Set<MediaType> getConsumableMediaTypes() {
			Set<MediaType> result = new LinkedHashSet<>();
			for (PartialMatch match : this.partialMatches) {
				if (match.hasMethodsMatch()) {
					result.addAll(match.getInfo().getConsumesCondition().getConsumableMediaTypes());
				}
			}
			return result;
		}

		/**
		 * Return declared "producible" types but only among those that also
		 * match the "methods" and "consumes" conditions.
		 */
		public Set<MediaType> getProducibleMediaTypes() {
			Set<MediaType> result = new LinkedHashSet<>();
			for (PartialMatch match : this.partialMatches) {
				if (match.hasConsumesMatch()) {
					result.addAll(match.getInfo().getProducesCondition().getProducibleMediaTypes());
				}
			}
			return result;
		}

		/**
		 * Return declared "params" conditions but only among those that also
		 * match the "methods", "consumes", and "params" conditions.
		 */
		public List<String[]> getParamConditions() {
			List<String[]> result = new ArrayList<>();
			for (PartialMatch match : this.partialMatches) {
				if (match.hasProducesMatch()) {
					Set<NameValueExpression<String>> set = match.getInfo().getParamsCondition().getExpressions();
					if (!CollectionUtils.isEmpty(set)) {
						int i = 0;
						String[] array = new String[set.size()];
						for (NameValueExpression<String> expression : set) {
							array[i++] = expression.toString();
						}
						result.add(array);
					}
				}
			}
			return result;
		}


		/**
		 * Container for a RequestMappingInfo that matches the URL path at least.
		 */
		private static class PartialMatch {

			private final RequestMappingInfo info;

			private final boolean methodsMatch;

			private final boolean consumesMatch;

			private final boolean producesMatch;

			private final boolean paramsMatch;

			/**
			 * Create a new {@link PartialMatch} instance.
			 * @param info the RequestMappingInfo that matches the URL path.
			 * @param request the current request
			 */
			public PartialMatch(RequestMappingInfo info, HttpServletRequest request) {
				this.info = info;
				this.methodsMatch = (info.getMethodsCondition().getMatchingCondition(request) != null);
				this.consumesMatch = (info.getConsumesCondition().getMatchingCondition(request) != null);
				this.producesMatch = (info.getProducesCondition().getMatchingCondition(request) != null);
				this.paramsMatch = (info.getParamsCondition().getMatchingCondition(request) != null);
			}

			public RequestMappingInfo getInfo() {
				return this.info;
			}

			public boolean hasMethodsMatch() {
				return this.methodsMatch;
			}

			public boolean hasConsumesMatch() {
				return (hasMethodsMatch() && this.consumesMatch);
			}

			public boolean hasProducesMatch() {
				return (hasConsumesMatch() && this.producesMatch);
			}

			public boolean hasParamsMatch() {
				return (hasProducesMatch() && this.paramsMatch);
			}

			@Override
			public String toString() {
				return this.info.toString();
			}
		}
	}


	/**
	 * Default handler for HTTP OPTIONS.
	 */
	private static class HttpOptionsHandler {

		private final HttpHeaders headers = new HttpHeaders();

		public HttpOptionsHandler(Set<String> declaredMethods) {
			this.headers.setAllow(initAllowedHttpMethods(declaredMethods));
		}

		private static Set<HttpMethod> initAllowedHttpMethods(Set<String> declaredMethods) {
			Set<HttpMethod> result = new LinkedHashSet<>(declaredMethods.size());
			if (declaredMethods.isEmpty()) {
				for (HttpMethod method : HttpMethod.values()) {
					if (method != HttpMethod.TRACE) {
						result.add(method);
					}
				}
			}
			else {
				for (String method : declaredMethods) {
					HttpMethod httpMethod = HttpMethod.valueOf(method);
					result.add(httpMethod);
					if (httpMethod == HttpMethod.GET) {
						result.add(HttpMethod.HEAD);
					}
				}
				result.add(HttpMethod.OPTIONS);
			}
			return result;
		}

		@SuppressWarnings("unused")
		public HttpHeaders handle() {
			return this.headers;
		}
	}

}
