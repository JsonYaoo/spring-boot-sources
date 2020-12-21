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

package org.springframework.web.servlet.mvc.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPattern;

/**
 * 20201221
 * A. 逻辑分离（'||'）请求条件，该条件将请求与一组URL路径模式进行匹配。
 * B. 与使用已解析的{@link PathPattern}的{@link PathPatternsRequestCondition}相比，此条件通过{@link org.springframework.util.AntPathMatcher AntPathMatcher}进行字符串模式匹配
 */
/**
 * A.
 * A logical disjunction (' || ') request condition that matches a request
 * against a set of URL path patterns.
 *
 * B.
 * <p>In contrast to {@link PathPatternsRequestCondition} which uses parsed
 * {@link PathPattern}s, this condition does String pattern matching via
 * {@link org.springframework.util.AntPathMatcher AntPathMatcher}.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
// 20201221 逻辑分离（'||'）请求条件，该条件将请求与一组URL路径模式进行匹配: 通过AntPathMatcher进行字符串模式匹配
public class PatternsRequestCondition extends AbstractRequestCondition<PatternsRequestCondition> {

	private final static Set<String> EMPTY_PATH_PATTERN = Collections.singleton("");

	// 20201221 匹配到的所有模式列表 => eg:"/testController/testRequestMapping"
	private final Set<String> patterns;

	// 20201221 基于{@code String}的路径匹配的策略接口
	private final PathMatcher pathMatcher;

	// 20201221 是否使用后缀模式匹配
	private final boolean useSuffixPatternMatch;

	// 20201221 是否使用尾部斜杠匹配
	private final boolean useTrailingSlashMatch;

	// 20201221 文件扩展列表
	private final List<String> fileExtensions = new ArrayList<>();

	/**
	 * Constructor with URL patterns which are prepended with "/" if necessary.
	 * @param patterns 0 or more URL patterns; no patterns results in an empty
	 * path {@code ""} mapping which matches all requests.
	 */
	public PatternsRequestCondition(String... patterns) {
		this(patterns, true, null);
	}

	/**
	 * Variant of {@link #PatternsRequestCondition(String...)} with a
	 * {@link PathMatcher} and flag for matching trailing slashes.
	 * @since 5.3
	 */
	public PatternsRequestCondition(String[] patterns,  boolean useTrailingSlashMatch,
			@Nullable PathMatcher pathMatcher) {

		this(patterns, null, pathMatcher, useTrailingSlashMatch);
	}

	/**
	 * Variant of {@link #PatternsRequestCondition(String...)} with a
	 * {@link UrlPathHelper} and a {@link PathMatcher}, and whether to match
	 * trailing slashes.
	 * <p>As of 5.3 the the path is obtained through the static method
	 * {@link UrlPathHelper#getResolvedLookupPath} and a {@code UrlPathHelper}
	 * does not need to be passed in.
	 * @since 5.2.4
	 * @deprecated as of 5.3 in favor of
	 * {@link #PatternsRequestCondition(String[], boolean, PathMatcher)}.
	 */
	@Deprecated
	public PatternsRequestCondition(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
			@Nullable PathMatcher pathMatcher, boolean useTrailingSlashMatch) {

		this(patterns, urlPathHelper, pathMatcher, false, useTrailingSlashMatch);
	}

	/**
	 * Variant of {@link #PatternsRequestCondition(String...)} with a
	 * {@link UrlPathHelper} and a {@link PathMatcher}, and flags for matching
	 * with suffixes and trailing slashes.
	 * <p>As of 5.3 the the path is obtained through the static method
	 * {@link UrlPathHelper#getResolvedLookupPath} and a {@code UrlPathHelper}
	 * does not need to be passed in.
	 * @deprecated as of 5.2.4. See class-level note in
	 * {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}
	 * on the deprecation of path extension config options.
	 */
	@Deprecated
	public PatternsRequestCondition(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
			@Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch, boolean useTrailingSlashMatch) {

		this(patterns, urlPathHelper, pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, null);
	}

	/**
	 * Variant of {@link #PatternsRequestCondition(String...)} with a
	 * {@link UrlPathHelper} and a {@link PathMatcher}, and flags for matching
	 * with suffixes and trailing slashes, along with specific extensions.
	 * <p>As of 5.3 the the path is obtained through the static method
	 * {@link UrlPathHelper#getResolvedLookupPath} and a {@code UrlPathHelper}
	 * does not need to be passed in.
	 * @deprecated as of 5.2.4. See class-level note in
	 * {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}
	 * on the deprecation of path extension config options.
	 */
	@Deprecated
	public PatternsRequestCondition(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
			@Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch,
			boolean useTrailingSlashMatch, @Nullable List<String> fileExtensions) {

		this.patterns = initPatterns(patterns);
		this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
		this.useSuffixPatternMatch = useSuffixPatternMatch;
		this.useTrailingSlashMatch = useTrailingSlashMatch;

		if (fileExtensions != null) {
			for (String fileExtension : fileExtensions) {
				if (fileExtension.charAt(0) != '.') {
					fileExtension = "." + fileExtension;
				}
				this.fileExtensions.add(fileExtension);
			}
		}
	}

	private static Set<String> initPatterns(String[] patterns) {
		if (!hasPattern(patterns)) {
			return EMPTY_PATH_PATTERN;
		}
		Set<String> result = new LinkedHashSet<>(patterns.length);
		for (String pattern : patterns) {
			if (StringUtils.hasLength(pattern) && !pattern.startsWith("/")) {
				pattern = "/" + pattern;
			}
			result.add(pattern);
		}
		return result;
	}

	private static boolean hasPattern(String[] patterns) {
		if (!ObjectUtils.isEmpty(patterns)) {
			for (String pattern : patterns) {
				if (StringUtils.hasText(pattern)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Private constructor for use when combining and matching.
	 */
	// 20201221 组合和匹配时使用的私有构造函数。
	private PatternsRequestCondition(Set<String> patterns, PatternsRequestCondition other) {
		// 20201221 匹配到的所有模式列表 => eg = "/testController/testRequestMapping"
		this.patterns = patterns;

		// 20201221 基于{@code String}的路径匹配的策略接口 => eg = 当前的路径匹配器AntPathMatcher
		this.pathMatcher = other.pathMatcher;

		// 20201221 是否使用后缀模式匹配 => eg = false
		this.useSuffixPatternMatch = other.useSuffixPatternMatch;

		// 20201221 是否使用尾部斜杠匹配 => eg = true
		this.useTrailingSlashMatch = other.useTrailingSlashMatch;

		// 20201221 文件扩展列表 => eg = []
		this.fileExtensions.addAll(other.fileExtensions);
	}


	public Set<String> getPatterns() {
		return this.patterns;
	}

	@Override
	protected Collection<String> getContent() {
		return this.patterns;
	}

	@Override
	protected String getToStringInfix() {
		return " || ";
	}

	/**
	 * Whether the condition is the "" (empty path) mapping.
	 */
	// 20201221 条件是否为“”（空路径）映射。
	public boolean isEmptyPathMapping() {
		return this.patterns == EMPTY_PATH_PATTERN;
	}

	/**
	 * Return the mapping paths that are not patterns.
	 * @since 5.3
	 */
	public Set<String> getDirectPaths() {
		if (isEmptyPathMapping()) {
			return EMPTY_PATH_PATTERN;
		}
		Set<String> result = Collections.emptySet();
		for (String pattern : this.patterns) {
			if (!this.pathMatcher.isPattern(pattern)) {
				result = (result.isEmpty() ? new HashSet<>(1) : result);
				result.add(pattern);
			}
		}
		return result;
	}

	/**
	 * Returns a new instance with URL patterns from the current instance ("this") and
	 * the "other" instance as follows:
	 * <ul>
	 * <li>If there are patterns in both instances, combine the patterns in "this" with
	 * the patterns in "other" using {@link PathMatcher#combine(String, String)}.
	 * <li>If only one instance has patterns, use them.
	 * <li>If neither instance has patterns, use an empty String (i.e. "").
	 * </ul>
	 */
	@Override
	public PatternsRequestCondition combine(PatternsRequestCondition other) {
		if (isEmptyPathMapping() && other.isEmptyPathMapping()) {
			return this;
		}
		else if (other.isEmptyPathMapping()) {
			return this;
		}
		else if (isEmptyPathMapping()) {
			return other;
		}
		Set<String> result = new LinkedHashSet<>();
		if (!this.patterns.isEmpty() && !other.patterns.isEmpty()) {
			for (String pattern1 : this.patterns) {
				for (String pattern2 : other.patterns) {
					result.add(this.pathMatcher.combine(pattern1, pattern2));
				}
			}
		}
		return new PatternsRequestCondition(result, this);
	}

	/**
	 * 20201221
	 * A. 检查是否有任何模式与给定请求匹配，并返回一个保证包含匹配模式的实例，该实例通过{@link PathMatcher＃getPatternComparator（String）}进行排序。
	 * B. 通过按以下顺序进行检查可获得匹配的模式：
	 * 		a. 直接比对
	 * 		b. 如果模式不包含“.”，则模式匹配后会附加“.*”。
	 * 		c. 模式匹配
	 * 		d. 如果模式尚未以“ /”结尾，则模式匹配后附加“ /”
	 */
	/**
	 * A.
	 * Checks if any of the patterns match the given request and returns an instance
	 * that is guaranteed to contain matching patterns, sorted via
	 * {@link PathMatcher#getPatternComparator(String)}.
	 *
	 * B.
	 * <p>A matching pattern is obtained by making checks in the following order:
	 * <ul>
	 * a.
	 * <li>Direct match
	 *
	 * b.
	 * <li>Pattern match with ".*" appended if the pattern doesn't already contain a "."
	 *
	 * c.
	 * <li>Pattern match
	 *
	 * d.
	 * <li>Pattern match with "/" appended if the pattern doesn't already end in "/"
	 * </ul>
	 * @param request the current request
	 * @return the same instance if the condition contains no patterns;
	 * or a new condition with sorted matching patterns;
	 * or {@code null} if no patterns match.
	 */
	// 20201221 检查是否有任何模式与给定请求匹配，并返回一个保证包含匹配模式的实例
	@Override
	@Nullable
	public PatternsRequestCondition getMatchingCondition(HttpServletRequest request) {
		// 20201221 返回以前的{@link #getLookupPathForRequest} 已解决的lookupPath => eg: "/testController/testRequestMapping"
		String lookupPath = UrlPathHelper.getResolvedLookupPath(request);

		// 20201221 查找与给定查找路径匹配的模式 => eg:"/testController/testRequestMapping"
		List<String> matches = getMatchingPatterns(lookupPath);

		// 20201221 eg: 返回新的PatternsRequestCondition: "/testController/testRequestMapping"
		return !matches.isEmpty() ? new PatternsRequestCondition(new LinkedHashSet<>(matches), this) : null;
	}

	/**
	 * 20201221
	 * 查找与给定查找路径匹配的模式。 调用此方法应产生与调用{@link #getMatchingCondition}相同的结果。 如果没有可用的请求（例如自省，工具等），则可以使用此方法作为替代方法。
	 */
	/**
	 * Find the patterns matching the given lookup path. Invoking this method should
	 * yield results equivalent to those of calling {@link #getMatchingCondition}.
	 * This method is provided as an alternative to be used if no request is available
	 * (e.g. introspection, tooling, etc).
	 * @param lookupPath the lookup path to match to existing patterns
	 * @return a collection of matching patterns sorted with the closest match at the top	// 20201221 匹配模式的集合，排序方式最接近的匹配项位于顶部
	 */
	// 20201221 查找与给定查找路径匹配的模式
	public List<String> getMatchingPatterns(String lookupPath) {
		List<String> matches = null;

		// 20201221 模式列表 => eg:"/testController/testRequestMapping"
		for (String pattern : this.patterns) {
			// 20201221 根据匹配到的RequestMapping模式匹配指定解析后的路径 => eg:"/testController/testRequestMapping"
			String match = getMatchingPattern(pattern, lookupPath);
			if (match != null) {
				matches = (matches != null ? matches : new ArrayList<>());

				// 20201221 封装所有匹配成功的路径列表 => eg:"/testController/testRequestMapping"
				matches.add(match);
			}
		}
		if (matches == null) {
			return Collections.emptyList();
		}
		if (matches.size() > 1) {
			matches.sort(this.pathMatcher.getPatternComparator(lookupPath));
		}

		// 20201221 封装所有匹配成功的路径列表 => eg:"/testController/testRequestMapping"
		return matches;
	}

	// 20201221 根据匹配到的RequestMapping模式匹配指定解析后的路径
	@Nullable
	private String getMatchingPattern(String pattern, String lookupPath) {
		if (pattern.equals(lookupPath)) {
			return pattern;
		}
		if (this.useSuffixPatternMatch) {
			if (!this.fileExtensions.isEmpty() && lookupPath.indexOf('.') != -1) {
				for (String extension : this.fileExtensions) {
					if (this.pathMatcher.match(pattern + extension, lookupPath)) {
						return pattern + extension;
					}
				}
			}
			else {
				boolean hasSuffix = pattern.indexOf('.') != -1;
				if (!hasSuffix && this.pathMatcher.match(pattern + ".*", lookupPath)) {
					return pattern + ".*";
				}
			}
		}
		if (this.pathMatcher.match(pattern, lookupPath)) {
			return pattern;
		}
		if (this.useTrailingSlashMatch) {
			if (!pattern.endsWith("/") && this.pathMatcher.match(pattern + "/", lookupPath)) {
				return pattern + "/";
			}
		}
		return null;
	}

	/**
	 * Compare the two conditions based on the URL patterns they contain.
	 * Patterns are compared one at a time, from top to bottom via
	 * {@link PathMatcher#getPatternComparator(String)}. If all compared
	 * patterns match equally, but one instance has more patterns, it is
	 * considered a closer match.
	 * <p>It is assumed that both instances have been obtained via
	 * {@link #getMatchingCondition(HttpServletRequest)} to ensure they
	 * contain only patterns that match the request and are sorted with
	 * the best matches on top.
	 */
	@Override
	public int compareTo(PatternsRequestCondition other, HttpServletRequest request) {
		String lookupPath = UrlPathHelper.getResolvedLookupPath(request);
		Comparator<String> patternComparator = this.pathMatcher.getPatternComparator(lookupPath);
		Iterator<String> iterator = this.patterns.iterator();
		Iterator<String> iteratorOther = other.patterns.iterator();
		while (iterator.hasNext() && iteratorOther.hasNext()) {
			int result = patternComparator.compare(iterator.next(), iteratorOther.next());
			if (result != 0) {
				return result;
			}
		}
		if (iterator.hasNext()) {
			return -1;
		}
		else if (iteratorOther.hasNext()) {
			return 1;
		}
		else {
			return 0;
		}
	}

}
