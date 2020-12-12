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

package org.springframework.context.index;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 20201205
 * A. 提供对{@code META-INF / spring.components}中定义的候选者的访问。
 * B. 可以在索引上注册（和查询）任意数量的构造型：一个典型的示例是为特定用例标记类的注释的完全限定名称。 以下调用返回{@code com.example}包（及其子包）的所有
 *    {@code @Component}候选类型：
 * 			Set<String> candidates = index.getCandidateTypes("com.example", "org.springframework.stereotype.Component");
 * C. {@code type}通常是类的完全限定名称，尽管这不是规则。 同样，{@code stereotype}通常是目标类型的完全限定名称，但实际上可以是任何标记。
 */
/**
 * A.
 * Provide access to the candidates that are defined in {@code META-INF/spring.components}.
 *
 * B.
 * <p>An arbitrary number of stereotypes can be registered (and queried) on the index: a
 * typical example is the fully qualified name of an annotation that flags the class for
 * a certain use case. The following call returns all the {@code @Component}
 * <b>candidate</b> types for the {@code com.example} package (and its sub-packages):
 * <pre class="code">
 * Set&lt;String&gt; candidates = index.getCandidateTypes(
 *         "com.example", "org.springframework.stereotype.Component");
 * </pre>
 *
 * C.
 * <p>The {@code type} is usually the fully qualified name of a class, though this is
 * not a rule. Similarly, the {@code stereotype} is usually the fully qualified name of
 * a target type but it can be any marker really.
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
// 20201205 提供对{@code META-INF / spring.components}中定义的候选者的访问。
public class CandidateComponentsIndex {

	private static final AntPathMatcher pathMatcher = new AntPathMatcher(".");

	// 2020105 属性键值对Map
	private final MultiValueMap<String, Entry> index;

	// 20201205 构建候选者组件索引实例
	CandidateComponentsIndex(List<Properties> content) {
		this.index = parseIndex(content);
	}

	// 20201205 解析资源属性集列 -> 属性键值对Map
	private static MultiValueMap<String, Entry> parseIndex(List<Properties> content) {
		MultiValueMap<String, Entry> index = new LinkedMultiValueMap<>();
		for (Properties entry : content) {
			entry.forEach((type, values) -> {
				String[] stereotypes = ((String) values).split(",");
				for (String stereotype : stereotypes) {
					index.add(stereotype, new Entry((String) type));
				}
			});
		}
		return index;
	}


	/**
	 * Return the candidate types that are associated with the specified stereotype.
	 * @param basePackage the package to check for candidates
	 * @param stereotype the stereotype to use
	 * @return the candidate types associated with the specified {@code stereotype}
	 * or an empty set if none has been found for the specified {@code basePackage}
	 */
	// 20201212 只返回指定包路径的候选组件Class名称
	public Set<String> getCandidateTypes(String basePackage, String stereotype) {
		// 20201212 根据注解Class名称获取所有索引到的候选组件Class名称
		List<Entry> candidates = this.index.get(stereotype);
		if (candidates != null) {
			// 20201212 只返回指定包路径的候选组件Class名称
			return candidates.parallelStream()
					.filter(t -> t.match(basePackage))
					.map(t -> t.type)
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}


	private static class Entry {

		private final String type;

		private final String packageName;

		Entry(String type) {
			this.type = type;
			this.packageName = ClassUtils.getPackageName(type);
		}

		public boolean match(String basePackage) {
			if (pathMatcher.isPattern(basePackage)) {
				return pathMatcher.match(basePackage, this.packageName);
			}
			else {
				return this.type.startsWith(basePackage);
			}
		}
	}

}
