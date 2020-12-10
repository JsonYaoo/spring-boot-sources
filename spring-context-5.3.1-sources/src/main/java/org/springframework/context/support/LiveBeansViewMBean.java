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

package org.springframework.context.support;

/**
 * MBean operation interface for the {@link LiveBeansView} feature.
 *
 * @author Juergen Hoeller
 * @since 3.2
 * @deprecated as of 5.3, in favor of using Spring Boot actuators for such needs
 */
// 20201210 {@link LiveBeansView}功能的MBean操作界面。
@Deprecated
public interface LiveBeansViewMBean {

	/**
	 * Generate a JSON snapshot of current beans and their dependencies.
	 */
	String getSnapshotAsJson();

}
