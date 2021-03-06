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

package org.springframework.beans.factory;

/**
 * 20201202
 * A. 一个标记超接口，指示bean有资格通过回调样式的方法由Spring容器通知特定的框架对象。实际的方法签名由各个子接口确定，但通常只包含一个接受单个参数的void返回方法。
 * B. 注意，仅仅实现{@link Aware}没有提供默认功能。相反，必须显式地进行处理，例如在{@link org.springframework.beans.factory.config.BeanPostProcessor}.
 *    参考{@link org.springframework.context.support.applicationContextawreProcessor}例如，处理特定的{@code Aware}接口回调。
 */
/**
 * A.
 * A marker superinterface indicating that a bean is eligible to be notified by the
 * Spring container of a particular framework object through a callback-style method.
 * The actual method signature is determined by individual subinterfaces but should
 * typically consist of just one void-returning method that accepts a single argument.
 *
 * B.
 * <p>Note that merely implementing {@link Aware} provides no default functionality.
 * Rather, processing must be done explicitly, for example in a
 * {@link org.springframework.beans.factory.config.BeanPostProcessor}.
 * Refer to {@link org.springframework.context.support.ApplicationContextAwareProcessor}
 * for an example of processing specific {@code *Aware} interface callbacks.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
// 20201204 自觉接口: 一个标记超接口，指示bean有资格通过回调样式的方法由Spring容器通知特定的框架对象。
public interface Aware {

}
