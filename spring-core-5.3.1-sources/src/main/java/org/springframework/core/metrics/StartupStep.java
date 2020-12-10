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

package org.springframework.core.metrics;

import java.util.function.Supplier;

import org.springframework.lang.Nullable;

/**
 * 20201208
 * A. 分步记录有关在{@link ApplicationStartup}期间发生的特定阶段或操作的指标。
 * B. {@code StartupStep}的生命周期如下：
 * 		a. 该步骤是通过调用{@link ApplicationStartup＃start（String）应用程序启动}创建并开始的，并且分配了唯一的{@link StartupStep＃getId（）id}。
 * 		b. 然后我们可以在处理过程中将信息附加到{@link Tags}
 * 		c. 然后，我们需要标记该步骤的{@link #end（）}
 * C. 实现可以跟踪步骤的“执行时间”或其他指标。
 */
/**
 * A.
 * Step recording metrics about a particular phase or action happening during the {@link ApplicationStartup}.
 *
 * B.
 * <p>The lifecycle of a {@code StartupStep} goes as follows:
 * <ol>
 * <li>the step is created and starts by calling {@link ApplicationStartup#start(String) the application startup}
 * and is assigned a unique {@link StartupStep#getId() id}.
 * <li>we can then attach information with {@link Tags} during processing
 * <li>we then need to mark the {@link #end()} of the step
 * </ol>
 *
 * C.
 * <p>Implementations can track the "execution time" or other metrics for steps.
 *
 * @author Brian Clozel
 * @since 5.3
 */
// 20201208 分步记录有关在{@link ApplicationStartup}期间发生的特定阶段或操作的指标
public interface StartupStep {

	/**
	 * Return the name of the startup step.
	 * <p>A step name describes the current action or phase. This technical
	 * name should be "." namespaced and can be reused to describe other instances of
	 * similar steps during application startup.
	 */
	String getName();

	/**
	 * Return the unique id for this step within the application startup.
	 */
	long getId();

	/**
	 * Return, if available, the id of the parent step.
	 * <p>The parent step is the step that was started the most recently
	 * when the current step was created.
	 */
	@Nullable
	Long getParentId();

	/**
	 * Add a {@link Tag} to the step.
	 * @param key tag key
	 * @param value tag value
	 */
	StartupStep tag(String key, String value);

	/**
	 * Add a {@link Tag} to the step.
	 * @param key tag key	// 20201208 标签键
	 * @param value {@link Supplier} for the tag value	// 20201208 提供的标签值
	 */
	// 20201208 在步骤中添加一个{@link Tag}。
	StartupStep tag(String key, Supplier<String> value);

	/**
	 * Return the {@link Tag} collection for this step.
	 */
	Tags getTags();

	/**
	 * 20201210
	 * A. 记录步骤的状态以及可能的其他度量，例如执行时间。
	 * B. 一旦结束，就不能更改步进状态。
	 */
	/**
	 * A.
	 * Record the state of the step and possibly other metrics like execution time.
	 *
	 * B.
	 * <p>Once ended, changes on the step state are not allowed.
	 */
	// 20201210 记录步骤的状态以及可能的其他度量，例如执行时间。
	void end();

	/**
	 * Immutable collection of {@link Tag}.
	 */
	interface Tags extends Iterable<Tag> {
	}


	/**
	 * Simple key/value association for storing step metadata.
	 */
	interface Tag {

		/**
		 * Return the {@code Tag} name.
		 */
		String getKey();

		/**
		 * Return the {@code Tag} value.
		 */
		String getValue();
	}

}
