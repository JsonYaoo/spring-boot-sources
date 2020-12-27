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

/**
 * 20201227
 * A. 确保Servlet一次仅处理一个请求。 该接口没有方法。
 * B. 如果servlet实现此接口，则可以确保在servlet的service方法中不会同时执行两个线程。 servlet容器可以通过同步对servlet单个实例的访问，或维护servlet实例池并将每个新请求
 *    分派给空闲的servlet来保证。
 * C. 请注意，SingleThreadModel不能解决所有线程安全问题。 例如，即使使用SingleThreadModel servlet，会话属性和静态变量仍然可以同时由多个线程上的多个请求访问。
 *     建议开发人员采取其他方法解决这些问题，而不要实现此接口，例如，避免使用实例变量或同步访问这些资源的代码块。 Servlet API版本2.4中不推荐使用此接口。
 */
/**
 * A.
 * Ensures that servlets handle only one request at a time. This interface has
 * no methods.
 *
 * B.
 * <p>
 * If a servlet implements this interface, you are <i>guaranteed</i> that no two
 * threads will execute concurrently in the servlet's <code>service</code>
 * method. The servlet container can make this guarantee by synchronizing access
 * to a single instance of the servlet, or by maintaining a pool of servlet
 * instances and dispatching each new request to a free servlet.
 *
 * C.
 * <p>
 * Note that SingleThreadModel does not solve all thread safety issues. For
 * example, session attributes and static variables can still be accessed by
 * multiple requests on multiple threads at the same time, even when
 * SingleThreadModel servlets are used. It is recommended that a developer take
 * other means to resolve those issues instead of implementing this interface,
 * such as avoiding the usage of an instance variable or synchronizing the block
 * of the code accessing those resources. This interface is deprecated in
 * Servlet API version 2.4.
 *
 * @deprecated As of Java Servlet API 2.4, with no direct replacement.
 */
// 20201227 确保Servlet一次仅处理一个请求, 如果servlet实现此接口，则可以确保在servlet的service方法中不会同时执行两个线程: servlet容器可以通过同步对servlet单个实例的访问，或维护servlet实例池并将每个新请求分派给空闲的servlet来保证。Servlet API版本2.4中不推荐使用此接口
@Deprecated
public interface SingleThreadModel {
    // No methods
}
