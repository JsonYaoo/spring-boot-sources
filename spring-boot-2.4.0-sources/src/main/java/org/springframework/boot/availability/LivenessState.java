/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.availability;

/**
 * 20201210
 * A. 应用程序的“活动”状态。
 * B. 当应用程序以正确的内部状态运行时，它被认为是活动的。 “活动”失败意味着应用程序的内部状态已损坏，我们无法从中恢复。 因此，平台应重新启动应用程序。
 */
/**
 * A.
 * "Liveness" state of the application.
 *
 * B.
 * <p>
 * An application is considered live when it's running with a correct internal state.
 * "Liveness" failure means that the internal state of the application is broken and we
 * cannot recover from it. As a result, the platform should restart the application.
 *
 * @author Brian Clozel
 * @since 2.3.0
 */
// 20201210 应用程序的“活动”状态
public enum LivenessState implements AvailabilityState {

	/**
	 * The application is running and its internal state is correct.
	 */
	// 20201210 该应用程序正在运行，并且其内部状态正确。
	CORRECT,

	/**
	 * The application is running but its internal state is broken.
	 */
	// 20201210 该应用程序正在运行，但其内部状态已损坏。
	BROKEN

}
