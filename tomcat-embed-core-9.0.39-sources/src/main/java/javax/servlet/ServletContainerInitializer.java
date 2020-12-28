/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.servlet;

import java.util.Set;

/**
 * 20201228
 * A. ServletContainerInitializer（SCI）通过文件META-INF / services / javax.servlet.ServletContainerInitializer中的条目注册，该条目必须实现SCI接口。
 * B. 不管元数据完成的设置如何，都执行SCI处理。 可以通过片段顺序对每个JAR文件控制SCI处理。 如果定义了绝对排序，则仅对SCI处理排序中包含的JAR。 为了完全禁用SCI处理，
 *    可以定义一个空的绝对顺序。
 * C. SCI通过添加到类中的{@link javax.servlet.annotation.HandlesTypes}注释来注册对注释（类，方法或字段）和/或类型的兴趣。
 */
/**
 * A.
 * ServletContainerInitializers (SCIs) are registered via an entry in the
 * file META-INF/services/javax.servlet.ServletContainerInitializer that must be
 * included in the JAR file that contains the SCI implementation.
 *
 * B.
 * <p>
 * SCI processing is performed regardless of the setting of metadata-complete.
 * SCI processing can be controlled per JAR file via fragment ordering. If
 * absolute ordering is defined, then only the JARs included in the ordering
 * will be processed for SCIs. To disable SCI processing completely, an empty
 * absolute ordering may be defined.
 *
 * C.
 * <p>
 * SCIs register an interest in annotations (class, method or field) and/or
 * types via the {@link javax.servlet.annotation.HandlesTypes} annotation which
 * is added to the class.
 *
 * @since Servlet 3.0
 */
// 20201228 ServletContainerInitializer（SCI）通过文件META-INF / services / javax.servlet.ServletContainerInitializer中的条目注册: 该条目必须实现SCI接口
public interface ServletContainerInitializer {

    /**
     * 20201228
     * 在Web应用程序启动期间接收与通过{@link javax.servlet.annotation.HandlesTypes}注释定义的条件相匹配的Web应用程序中的类的通知。
     */
    /**
     * Receives notification during startup of a web application of the classes
     * within the web application that matched the criteria defined via the
     * {@link javax.servlet.annotation.HandlesTypes} annotation.
     *
     * @param c     The (possibly null) set of classes that met the specified
     *              criteria
     * @param ctx   The ServletContext of the web application in which the
     *              classes were discovered
     *
     * @throws ServletException If an error occurs
     */
    // 20201228 在Web应用程序启动期间接收与通过{@link javax.servlet.annotation.HandlesTypes}注释定义的条件相匹配的Web应用程序中的类的通知。
    void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException;
}
