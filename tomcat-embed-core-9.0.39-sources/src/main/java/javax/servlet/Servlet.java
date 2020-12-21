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

import java.io.IOException;

/**
 * 20201220
 * A. 定义所有servlet必须实现的方法。
 * B. Servlet是在Web服务器中运行的小型Java程序。 Servlet通常通过HTTP（超文本传输协议）接收和响应来自Web客户端的请求。
 * C. 要实现此接口，您可以编写扩展javax.servlet.GenericServlet的通用servlet或扩展javax.servlet.http.HttpServlet的HTTP servlet。
 * D. 该接口定义了初始化Servlet，处理请求以及从服务器中删除Servlet的方法。 这些称为生命周期方法，按以下顺序调用：
 *      a. 构造Servlet，然后使用init方法初始化。
 *      b. 客户端对service方法的所有调用都会得到处理。
 *      c. 将该Servlet退出服务，然后使用destroy方法将其破坏，然后将垃圾回收并完成。
 * E. 除了生命周期方法外，此接口还提供了Servlet可用于获取任何启动信息的getServletConfig方法，以及允许Servlet返回有关自身的基本信息（如作者，版本和版本、版权）。
 */
/**
 * A.
 * Defines methods that all servlets must implement.
 *
 * B.
 * <p>
 * A servlet is a small Java program that runs within a Web server. Servlets
 * receive and respond to requests from Web clients, usually across HTTP, the
 * HyperText Transfer Protocol.
 *
 * C.
 * <p>
 * To implement this interface, you can write a generic servlet that extends
 * <code>javax.servlet.GenericServlet</code> or an HTTP servlet that extends
 * <code>javax.servlet.http.HttpServlet</code>.
 *
 * D.
 * <p>
 * This interface defines methods to initialize a servlet, to service requests,
 * and to remove a servlet from the server. These are known as life-cycle
 * methods and are called in the following sequence:
 * <ol>
 * a.
 * <li>The servlet is constructed, then initialized with the <code>init</code>
 * method.
 *
 * b.
 * <li>Any calls from clients to the <code>service</code> method are handled.
 *
 * c.
 * <li>The servlet is taken out of service, then destroyed with the
 * <code>destroy</code> method, then garbage collected and finalized.
 * </ol>
 *
 * E.
 * <p>
 * In addition to the life-cycle methods, this interface provides the
 * <code>getServletConfig</code> method, which the servlet can use to get any
 * startup information, and the <code>getServletInfo</code> method, which allows
 * the servlet to return basic information about itself, such as author,
 * version, and copyright.
 *
 * @see GenericServlet
 * @see javax.servlet.http.HttpServlet
 */
// 20201220 定义所有servlet必须实现的方法: Servlet是在Web服务器中运行的小型Java程序。 Servlet通常通过HTTP（超文本传输协议）接收和响应来自Web客户端的请求
public interface Servlet {

    /**
     * Called by the servlet container to indicate to a servlet that the servlet
     * is being placed into service.
     *
     * <p>
     * The servlet container calls the <code>init</code> method exactly once
     * after instantiating the servlet. The <code>init</code> method must
     * complete successfully before the servlet can receive any requests.
     *
     * <p>
     * The servlet container cannot place the servlet into service if the
     * <code>init</code> method
     * <ol>
     * <li>Throws a <code>ServletException</code>
     * <li>Does not return within a time period defined by the Web server
     * </ol>
     *
     *
     * @param config
     *            a <code>ServletConfig</code> object containing the servlet's
     *            configuration and initialization parameters
     *
     * @exception ServletException
     *                if an exception has occurred that interferes with the
     *                servlet's normal operation
     *
     * @see UnavailableException
     * @see #getServletConfig
     */
    public void init(ServletConfig config) throws ServletException;

    /**
     *
     * Returns a {@link ServletConfig} object, which contains initialization and
     * startup parameters for this servlet. The <code>ServletConfig</code>
     * object returned is the one passed to the <code>init</code> method.
     *
     * <p>
     * Implementations of this interface are responsible for storing the
     * <code>ServletConfig</code> object so that this method can return it. The
     * {@link GenericServlet} class, which implements this interface, already
     * does this.
     *
     * @return the <code>ServletConfig</code> object that initializes this
     *         servlet
     *
     * @see #init
     */
    public ServletConfig getServletConfig();

    /**
     * 20201221
     * A. 由Servlet容器调用，以允许Servlet响应请求。
     * B. 仅在servlet的init方法成功完成后才调用此方法。
     * C. 始终应为引发或发送错误的servlet设置响应的状态代码。
     * D. Servlet通常在多线程Servlet容器中运行，该容器可以同时处理多个请求。 开发人员必须注意同步访问任何共享资源，例如文件，网络连接，以及Servlet的类和实例变量。
     *    <a href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">有关多线程编程的Java教程</a>中提供了有关Java多线程编程的更多信息。
     */
    /**
     * A.
     * Called by the servlet container to allow the servlet to respond to a
     * request.
     *
     * B.
     * <p>
     * This method is only called after the servlet's <code>init()</code> method
     * has completed successfully.
     *
     * C.
     * <p>
     * The status code of the response always should be set for a servlet that
     * throws or sends an error.
     *
     * D.
     * <p>
     * Servlets typically run inside multithreaded servlet containers that can
     * handle multiple requests concurrently. Developers must be aware to
     * synchronize access to any shared resources such as files, network
     * connections, and as well as the servlet's class and instance variables.
     * More information on multithreaded programming in Java is available in <a
     * href
     * ="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">
     * the Java tutorial on multi-threaded programming</a>.
     *
     * // 20201221 包含客户端请求的ServletRequest对象
     * @param req
     *            the <code>ServletRequest</code> object that contains the
     *            client's request
     *
     * // 20201221 包含Servlet响应的ServletResponse对象
     * @param res
     *            the <code>ServletResponse</code> object that contains the
     *            servlet's response
     *
     * @exception ServletException
     *                if an exception occurs that interferes with the servlet's
     *                normal operation
     *
     * @exception IOException
     *                if an input or output exception occurs
     */
    // 20201221 由Servlet容器调用，以允许Servlet响应请求, 仅在servlet的init方法成功完成后才调用此方法
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

    /**
     * Returns information about the servlet, such as author, version, and
     * copyright.
     *
     * <p>
     * The string that this method returns should be plain text and not markup
     * of any kind (such as HTML, XML, etc.).
     *
     * @return a <code>String</code> containing servlet information
     */
    public String getServletInfo();

    /**
     * Called by the servlet container to indicate to a servlet that the servlet
     * is being taken out of service. This method is only called once all
     * threads within the servlet's <code>service</code> method have exited or
     * after a timeout period has passed. After the servlet container calls this
     * method, it will not call the <code>service</code> method again on this
     * servlet.
     *
     * <p>
     * This method gives the servlet an opportunity to clean up any resources
     * that are being held (for example, memory, file handles, threads) and make
     * sure that any persistent state is synchronized with the servlet's current
     * state in memory.
     */
    public void destroy();
}
