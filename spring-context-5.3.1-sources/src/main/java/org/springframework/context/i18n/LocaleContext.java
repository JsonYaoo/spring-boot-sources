/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.context.i18n;

import java.util.Locale;

import org.springframework.lang.Nullable;

/**
 * 20201221
 * A. 用于确定当前语言环境的策略界面。
 * B. 可以通过LocaleContextHolder类将LocaleContext实例与线程关联。
 */
/**
 * A.
 * Strategy interface for determining the current Locale.
 *
 * B.
 * <p>A LocaleContext instance can be associated with a thread
 * via the LocaleContextHolder class.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see LocaleContextHolder#getLocale()
 * @see TimeZoneAwareLocaleContext
 */
// 20201221 用于确定当前语言环境的策略界面
public interface LocaleContext {

	/**
	 * Return the current Locale, which can be fixed or determined dynamically,
	 * depending on the implementation strategy.
	 * @return the current Locale, or {@code null} if no specific Locale associated
	 */
	@Nullable
	Locale getLocale();

}
