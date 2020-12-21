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
import java.util.Enumeration;

/**
 * 20201220
 * A. 定义一个与协议无关的通用servlet。 要编写用于Web的HTTP Servlet，请扩展{@link javax.servlet.http.HttpServlet}。
 * B. GenericServlet实现Servlet和ServletConfig接口。 可以直接由servlet扩展GenericServlet，尽管扩展特定于协议的子类（如HttpServlet）更为常见。
 * C. GenericServlet使编写servlet更容易。 它提供了生命周期方法init和destroy以及ServletConfig接口中的方法的简单版本。
 *    GenericServlet还实现了在ServletContext接口中声明的log方法。
 * D. 要编写通用servlet，您只需要重写抽象服务方法即可。
 */
/**
 * A.
 * Defines a generic, protocol-independent servlet. To write an HTTP servlet for
 * use on the Web, extend {@link javax.servlet.http.HttpServlet} instead.
 *
 * B.
 * <p>
 * <code>GenericServlet</code> implements the <code>Servlet</code> and
 * <code>ServletConfig</code> interfaces. <code>GenericServlet</code> may be
 * directly extended by a servlet, although it's more common to extend a
 * protocol-specific subclass such as <code>HttpServlet</code>.
 * <p>
 *
 * C.
 * <code>GenericServlet</code> makes writing servlets easier. It provides simple
 * versions of the lifecycle methods <code>init</code> and <code>destroy</code>
 * and of the methods in the <code>ServletConfig</code> interface.
 * <code>GenericServlet</code> also implements the <code>log</code> method,
 * declared in the <code>ServletContext</code> interface.
 *
 * D.
 * <p>
 * To write a generic servlet, you need only override the abstract
 * <code>service</code> method.
 */
// 20201220 定义一个与协议无关的通用servlet: 使编写servlet更容易, 提供了生命周期方法init和destroy以及ServletConfig接口中的方法的简单版本, 还实现了在ServletContext接口中声明的log方法
public abstract class GenericServlet implements Servlet, ServletConfig, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private transient ServletConfig config;

    /**
     * Does nothing. All of the servlet initialization is done by one of the
     * <code>init</code> methods.
     */
    public GenericServlet() {
        // NOOP
    }

    /**
     * Called by the servlet container to indicate to a servlet that the servlet
     * is being taken out of service. See {@link Servlet#destroy}.
     */
    @Override
    public void destroy() {
        // NOOP by default
    }

    /**
     * Returns a <code>String</code> containing the value of the named
     * initialization parameter, or <code>null</code> if the parameter does not
     * exist. See {@link ServletConfig#getInitParameter}.
     * <p>
     * This method is supplied for convenience. It gets the value of the named
     * parameter from the servlet's <code>ServletConfig</code> object.
     *
     * @param name
     *            a <code>String</code> specifying the name of the
     *            initialization parameter
     * @return String a <code>String</code> containing the value of the
     *         initialization parameter
     */
    @Override
    public String getInitParameter(String name) {
        return getServletConfig().getInitParameter(name);
    }

    /**
     * Returns the names of the servlet's initialization parameters as an
     * <code>Enumeration</code> of <code>String</code> objects, or an empty
     * <code>Enumeration</code> if the servlet has no initialization parameters.
     * See {@link ServletConfig#getInitParameterNames}.
     * <p>
     * This method is supplied for convenience. It gets the parameter names from
     * the servlet's <code>ServletConfig</code> object.
     *
     * @return Enumeration an enumeration of <code>String</code> objects
     *         containing the names of the servlet's initialization parameters
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        return getServletConfig().getInitParameterNames();
    }

    /**
     * Returns this servlet's {@link ServletConfig} object.
     *
     * @return ServletConfig the <code>ServletConfig</code> object that
     *         initialized this servlet
     */
    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    /**
     * Returns a reference to the {@link ServletContext} in which this servlet
     * is running. See {@link ServletConfig#getServletContext}.
     * <p>
     * This method is supplied for convenience. It gets the context from the
     * servlet's <code>ServletConfig</code> object.
     *
     * @return ServletContext the <code>ServletContext</code> object passed to
     *         this servlet by the <code>init</code> method
     */
    @Override
    public ServletContext getServletContext() {
        return getServletConfig().getServletContext();
    }

    /**
     * Returns information about the servlet, such as author, version, and
     * copyright. By default, this method returns an empty string. Override this
     * method to have it return a meaningful value. See
     * {@link Servlet#getServletInfo}.
     *
     * @return String information about this servlet, by default an empty string
     */
    @Override
    public String getServletInfo() {
        return "";
    }

    /**
     * Called by the servlet container to indicate to a servlet that the servlet
     * is being placed into service. See {@link Servlet#init}.
     * <p>
     * This implementation stores the {@link ServletConfig} object it receives
     * from the servlet container for later use. When overriding this form of
     * the method, call <code>super.init(config)</code>.
     *
     * @param config
     *            the <code>ServletConfig</code> object that contains
     *            configuration information for this servlet
     * @exception ServletException
     *                if an exception occurs that interrupts the servlet's
     *                normal operation
     * @see UnavailableException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        this.init();
    }

    /**
     * A convenience method which can be overridden so that there's no need to
     * call <code>super.init(config)</code>.
     * <p>
     * Instead of overriding {@link #init(ServletConfig)}, simply override this
     * method and it will be called by
     * <code>GenericServlet.init(ServletConfig config)</code>. The
     * <code>ServletConfig</code> object can still be retrieved via
     * {@link #getServletConfig}.
     *
     * @exception ServletException
     *                if an exception occurs that interrupts the servlet's
     *                normal operation
     */
    public void init() throws ServletException {
        // NOOP by default
    }

    /**
     * Writes the specified message to a servlet log file, prepended by the
     * servlet's name. See {@link ServletContext#log(String)}.
     *
     * @param message
     *            a <code>String</code> specifying the message to be written to
     *            the log file
     */
    public void log(String message) {
        getServletContext().log(getServletName() + ": " + message);
    }

    /**
     * Writes an explanatory message and a stack trace for a given
     * <code>Throwable</code> exception to the servlet log file, prepended by
     * the servlet's name. See {@link ServletContext#log(String, Throwable)}.
     *
     * @param message
     *            a <code>String</code> that describes the error or exception
     * @param t
     *            the <code>java.lang.Throwable</code> error or exception
     */
    public void log(String message, Throwable t) {
        getServletContext().log(getServletName() + ": " + message, t);
    }

    /**
     * 20201221
     * A. 由Servlet容器调用，以允许Servlet响应请求。 请参阅{@link Servlet＃service}。
     * B. 将此方法声明为抽象方法，因此子类（例如HttpServlet）必须覆盖它。
     */
    /**
     * A.
     * Called by the servlet container to allow the servlet to respond to a
     * request. See {@link Servlet#service}.
     *
     * B.
     * <p>
     * This method is declared abstract so subclasses, such as
     * <code>HttpServlet</code>, must override it.
     *
     * @param req
     *            the <code>ServletRequest</code> object that contains the
     *            client's request
     * @param res
     *            the <code>ServletResponse</code> object that will contain the
     *            servlet's response
     * @exception ServletException
     *                if an exception occurs that interferes with the servlet's
     *                normal operation occurred
     * @exception IOException
     *                if an input or output exception occurs
     */
    // 20201221 由Servlet容器调用，以允许Servlet响应请求: 子类（例如HttpServlet）必须覆盖它
    @Override
    public abstract void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

    /**
     * Returns the name of this servlet instance. See
     * {@link ServletConfig#getServletName}.
     *
     * @return the name of this servlet instance
     */
    @Override
    public String getServletName() {
        return config.getServletName();
    }
}
