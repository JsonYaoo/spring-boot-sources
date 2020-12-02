/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.core.convert.support;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;

/**
 * Configuration interface to be implemented by most if not all {@link ConversionService}
 * types. Consolidates the read-only operations exposed by {@link ConversionService} and
 * the mutating operations of {@link ConverterRegistry} to allow for convenient ad-hoc
 * addition and removal of {@link org.springframework.core.convert.converter.Converter
 * Converters} through. The latter is particularly useful when working against a
 * {@link org.springframework.core.env.ConfigurableEnvironment ConfigurableEnvironment}
 * instance in application context bootstrapping code.
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.core.env.ConfigurablePropertyResolver#getConversionService()
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 */
// 20201202 要由大多数{@link ConversionService}类型实现的配置接口。合并{@link ConversionService}公开的只读操作和{@link ConverterRegistry}的变异操作，
// 20201202 以便方便地通过{@linkorg.springframework.core.convert.converter.Converter}临时添加和删除。后者在处理{@link org.springframework.core.env.ConfigurableEnvironment ConfigurableEnvironment}时特别有用, 应用程序上下文引导代码中的ConfigurableEnvironment实例。
public interface ConfigurableConversionService extends ConversionService, ConverterRegistry {

}
