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

package org.springframework.core.annotation;

import java.util.Arrays;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link AnnotationFilter} implementation used for
 * {@link AnnotationFilter#packages(String...)}.
 *
 * @author Phillip Webb
 * @since 5.2
 */
// 20201208 包路径注释过滤, 用于{@link AnnotationFilter＃packages（String ...）}的{@link AnnotationFilter}实现。
final class PackagesAnnotationFilter implements AnnotationFilter {
	// 20201208 包路径数组
	private final String[] prefixes;

	// 20201208 包路径数组的hashCode
	private final int hashCode;

	// 20201208 构造包路径注释过滤器
	PackagesAnnotationFilter(String... packages) {
		// 20201208 包路径不能为空
		Assert.notNull(packages, "Packages array must not be null");

		// 20201208 构建包路径数组 -> 每个包路径多加个.
		this.prefixes = new String[packages.length];
		for (int i = 0; i < packages.length; i++) {
			String pkg = packages[i];
			Assert.hasText(pkg, "Packages array must not have empty elements");
			this.prefixes[i] = pkg + ".";
		}

		// 20201208 对包数组的每个包进行排序
		Arrays.sort(this.prefixes);

		// 20201208 设置包路径数组的hashCode = 31 * 1 + 每个元素的hashCode
		this.hashCode = Arrays.hashCode(this.prefixes);
	}

	@Override
	public boolean matches(String annotationType) {
		for (String prefix : this.prefixes) {
			if (annotationType.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		return Arrays.equals(this.prefixes, ((PackagesAnnotationFilter) other).prefixes);
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public String toString() {
		return "Packages annotation filter: " +
				StringUtils.arrayToCommaDelimitedString(this.prefixes);
	}

}
