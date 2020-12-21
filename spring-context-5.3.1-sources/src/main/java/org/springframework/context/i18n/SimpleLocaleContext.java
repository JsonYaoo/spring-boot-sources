/*
 * Copyright 2002-2017 the original author or authors.
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
 * {@link LocaleContext}接口的简单实现，始终返回指定的{@code Locale}。
 */
/**
 * Simple implementation of the {@link LocaleContext} interface,
 * always returning a specified {@code Locale}.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see LocaleContextHolder#setLocaleContext
 * @see LocaleContextHolder#getLocale()
 * @see SimpleTimeZoneAwareLocaleContext
 */
// 20201221 {@link LocaleContext}接口的简单实现，始终返回指定的{@code Locale}。
public class SimpleLocaleContext implements LocaleContext {

	@Nullable
	private final Locale locale;

	/**
	 * 20201221
	 * 创建一个新的SimpleLocaleContext，以暴露指定的语言环境。 每次{@link #getLocale（）}调用都将返回此语言环境。
	 */
	/**
	 * Create a new SimpleLocaleContext that exposes the specified Locale.
	 * Every {@link #getLocale()} call will return this Locale.
	 * @param locale the Locale to expose, or {@code null} for no specific one
	 */
	// 20201221 创建一个新的SimpleLocaleContext，以暴露指定的语言环境
	public SimpleLocaleContext(@Nullable Locale locale) {
		this.locale = locale;
	}

	@Override
	@Nullable
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public String toString() {
		return (this.locale != null ? this.locale.toString() : "-");
	}

}
