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

package org.springframework.context.annotation;

import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

/**
 * {@link Condition} that matches based on the value of a {@link Profile @Profile}
 * annotation.
 *
 * @author Chris Beams
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 4.0
 */
// 20201209 根据{@link Profile @Profile}注解的值进行匹配的{@link Condition}。
class ProfileCondition implements Condition {

	// 20201209 匹配上线文和注解元数据
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// 20201209 检索Profile类型的所有注释的所有属性（如果有）（即，如果在基础元素上定义为直接注释或元注释）。 请注意，此变体不考虑属性替代。
		MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());

		// 20201209 如果获取到的属性不为空
		if (attrs != null) {
			// 20201209 则遍历属性值
			for (Object value : attrs.get("value")) {
				// 20201209 返回{@linkplain #getActiveProfiles（）活动配置文件}是否与给定的{@link Profiles}谓词匹配。
				if (context.getEnvironment().acceptsProfiles(Profiles.of((String[]) value))) {
					// 20201209 如果匹配则返回true
					return true;
				}
			}

			// 20201209 否则返回false
			return false;
		}

		// 20201209 如果获取到的属性为空则返回true, 默认匹配
		return true;
	}

}
