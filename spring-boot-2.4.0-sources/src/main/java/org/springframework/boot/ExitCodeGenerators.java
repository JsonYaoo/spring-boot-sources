/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Maintains a collection of {@link ExitCodeGenerator} instances and allows the final exit
 * code to be calculated.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @see #getExitCode()
 * @see ExitCodeGenerator
 */
// 20201210 维护{@link ExitCodeGenerator}实例的集合，并允许计算最终退出代码。
class ExitCodeGenerators implements Iterable<ExitCodeGenerator> {

	private List<ExitCodeGenerator> generators = new ArrayList<>();

	// 20201210 添加所有策略接口(可用于提供异常和退出代码之间的映射)
	void addAll(Throwable exception, ExitCodeExceptionMapper... mappers) {
		Assert.notNull(exception, "Exception must not be null");
		Assert.notNull(mappers, "Mappers must not be null");

		// 20201210 添加所有策略接口(可用于提供异常和退出代码之间的映射)
		addAll(exception, Arrays.asList(mappers));
	}

	// 20201210 添加所有策略接口(可用于提供异常和退出代码之间的映射)
	void addAll(Throwable exception, Iterable<? extends ExitCodeExceptionMapper> mappers) {
		Assert.notNull(exception, "Exception must not be null");
		Assert.notNull(mappers, "Mappers must not be null");

		// 20201210 添加MappedExitCodeGenerator, 使{@link ExitCodeExceptionMapper}适应{@link ExitCodeGenerator}。
		for (ExitCodeExceptionMapper mapper : mappers) {
			add(exception, mapper);
		}
	}

	// 20201210 添加MappedExitCodeGenerator, 使{@link ExitCodeExceptionMapper}适应{@link ExitCodeGenerator}。
	void add(Throwable exception, ExitCodeExceptionMapper mapper) {
		Assert.notNull(exception, "Exception must not be null");
		Assert.notNull(mapper, "Mapper must not be null");

		// 20201210 添加MappedExitCodeGenerator, 使{@link ExitCodeExceptionMapper}适应{@link ExitCodeGenerator}。
		add(new MappedExitCodeGenerator(exception, mapper));
	}

	void addAll(ExitCodeGenerator... generators) {
		Assert.notNull(generators, "Generators must not be null");
		addAll(Arrays.asList(generators));
	}

	void addAll(Iterable<? extends ExitCodeGenerator> generators) {
		Assert.notNull(generators, "Generators must not be null");
		for (ExitCodeGenerator generator : generators) {
			add(generator);
		}
	}

	void add(ExitCodeGenerator generator) {
		Assert.notNull(generator, "Generator must not be null");
		this.generators.add(generator);
	}

	@Override
	public Iterator<ExitCodeGenerator> iterator() {
		return this.generators.iterator();
	}

	/**
	 * Get the final exit code that should be returned based on all contained generators.
	 * @return the final exit code.
	 */
	// 20201210 获取应基于所有包含的生成器返回的最终退出代码。
	int getExitCode() {
		int exitCode = 0;
		for (ExitCodeGenerator generator : this.generators) {
			try {
				int value = generator.getExitCode();
				if (value > 0 && value > exitCode || value < 0 && value < exitCode) {
					exitCode = value;
				}
			}
			catch (Exception ex) {
				exitCode = (exitCode != 0) ? exitCode : 1;
				ex.printStackTrace();
			}
		}
		return exitCode;
	}

	/**
	 * Adapts an {@link ExitCodeExceptionMapper} to an {@link ExitCodeGenerator}.
	 */
	// 20201210 使{@link ExitCodeExceptionMapper}适应{@link ExitCodeGenerator}。
	private static class MappedExitCodeGenerator implements ExitCodeGenerator {

		private final Throwable exception;

		private final ExitCodeExceptionMapper mapper;

		// 20201210 构造MappedExitCodeGenerator, 使{@link ExitCodeExceptionMapper}适应{@link ExitCodeGenerator}。
		MappedExitCodeGenerator(Throwable exception, ExitCodeExceptionMapper mapper) {
			this.exception = exception;
			this.mapper = mapper;
		}

		@Override
		public int getExitCode() {
			return this.mapper.getExitCode(this.exception);
		}

	}

}
