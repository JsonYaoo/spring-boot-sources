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

package org.springframework.web.util.pattern;

import org.springframework.http.server.PathContainer;

/**
 * 20201221
 * A. URI路径模式的解析器，生成{@link PathPattern}实例，然后可以将其与请求进行匹配。
 * B. {@link PathPatternParser}和{@link PathPattern}专为与Web应用程序中的HTTP URL路径一起使用而设计，在Web应用程序中，大量URI路径模式不断与传入请求匹配，
 *    从而激发了有效匹配的需求。
 * C. 有关路径模式语法的详细信息，请参见{@link PathPattern}。
 */
/**
 * A.
 * Parser for URI path patterns producing {@link PathPattern} instances that can
 * then be matched to requests.
 *
 * B.
 * <p>The {@link PathPatternParser} and {@link PathPattern} are specifically
 * designed for use with HTTP URL paths in web applications where a large number
 * of URI path patterns, continuously matched against incoming requests,
 * motivates the need for efficient matching.
 *
 * C.
 * <p>For details of the path pattern syntax see {@link PathPattern}.
 *
 * @author Andy Clement
 * @since 5.0
 */
// 20201221 URI路径模式的解析器，生成{@link PathPattern}实例，然后可以将其与请求进行匹配
public class PathPatternParser {

	private boolean matchOptionalTrailingSeparator = true;

	private boolean caseSensitive = true;

	private PathContainer.Options pathOptions = PathContainer.Options.HTTP_PATH;


	/**
	 * Whether a {@link PathPattern} produced by this parser should
	 * automatically match request paths with a trailing slash.
	 *
	 * <p>If set to {@code true} a {@code PathPattern} without a trailing slash
	 * will also match request paths with a trailing slash. If set to
	 * {@code false} a {@code PathPattern} will only match request paths with
	 * a trailing slash.
	 *
	 * <p>The default is {@code true}.
	 */
	public void setMatchOptionalTrailingSeparator(boolean matchOptionalTrailingSeparator) {
		this.matchOptionalTrailingSeparator = matchOptionalTrailingSeparator;
	}

	/**
	 * Whether optional trailing slashing match is enabled.
	 */
	public boolean isMatchOptionalTrailingSeparator() {
		return this.matchOptionalTrailingSeparator;
	}

	/**
	 * Whether path pattern matching should be case-sensitive.
	 * <p>The default is {@code true}.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Whether case-sensitive pattern matching is enabled.
	 */
	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}

	/**
	 * Set options for parsing patterns. These should be the same as the
	 * options used to parse input paths.
	 * <p>{@link PathContainer.Options#HTTP_PATH}
	 * is used by default.
	 * @since 5.2
	 */
	public void setPathOptions(PathContainer.Options pathOptions) {
		this.pathOptions = pathOptions;
	}

	/**
	 * Return the {@link #setPathOptions configured} pattern parsing options.
	 * @since 5.2
	 */
	public PathContainer.Options getPathOptions() {
		return this.pathOptions;
	}


	/**
	 * Process the path pattern content, a character at a time, breaking it into
	 * path elements around separator boundaries and verifying the structure at each
	 * stage. Produces a PathPattern object that can be used for fast matching
	 * against paths. Each invocation of this method delegates to a new instance of
	 * the {@link InternalPathPatternParser} because that class is not thread-safe.
	 * @param pathPattern the input path pattern, e.g. /project/{name}
	 * @return a PathPattern for quickly matching paths against request paths
	 * @throws PatternParseException in case of parse errors
	 */
	public PathPattern parse(String pathPattern) throws PatternParseException {
		return new InternalPathPatternParser(this).parse(pathPattern);
	}


	/**
	 * Shared, read-only instance of {@code PathPatternParser}. Uses default settings:
	 * <ul>
	 * <li>{@code matchOptionalTrailingSeparator=true}
	 * <li>{@code caseSensitivetrue}
	 * <li>{@code pathOptions=PathContainer.Options.HTTP_PATH}
	 * </ul>
	 */
	public final static PathPatternParser defaultInstance = new PathPatternParser() {

		@Override
		public void setMatchOptionalTrailingSeparator(boolean matchOptionalTrailingSeparator) {
			raiseError();
		}

		@Override
		public void setCaseSensitive(boolean caseSensitive) {
			raiseError();
		}

		@Override
		public void setPathOptions(PathContainer.Options pathOptions) {
			raiseError();
		}

		private void raiseError() {
			throw new UnsupportedOperationException(
					"This is a read-only, shared instance that cannot be modified");
		}
	};
}
