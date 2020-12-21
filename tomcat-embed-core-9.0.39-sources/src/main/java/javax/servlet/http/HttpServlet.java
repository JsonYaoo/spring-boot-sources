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
package javax.servlet.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.servlet.DispatcherType;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 20201220
 * A. 提供要被子类化的抽象类，以创建适合于网站的HTTP servlet。 HttpServlet的子类必须重写至少一个方法，通常是以下方法之一：
 *      a. doGet，如果Servlet支持HTTP GET请求
 *      b. doPost，用于HTTP POST请求
 *      c. doPut，用于HTTP PUT请求
 *      d. doDelete，用于HTTP DELETE请求
 *      e. init and destroy, 以管理在Servlet生命周期内保留的资源
 *      f. getServletInfo，servlet用于提供有关其自身的信息
 * B. 几乎没有理由覆盖服务方法。 服务通过将标准HTTP请求分派到每种HTTP请求类型的处理程序方法（上面列出的do Method方法）来处理它们。
 * C. 同样，几乎没有理由覆盖doOptions和doTrace方法。
 * D. Servlet通常在多线程服务器上运行，因此请注意，Servlet必须处理并发请求，并注意同步对共享资源的访问。 共享资源包括内存中的数据（例如实例或类变量）和外部对象
 *   （例如文件，数据库连接和网络连接）。 有关在Java程序中处理多个线程的更多信息，请参见
 *    <a href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">有关多线程编程的Java教程</a>。
 */
/**
 * A.
 * Provides an abstract class to be subclassed to create
 * an HTTP servlet suitable for a Web site. A subclass of
 * <code>HttpServlet</code> must override at least
 * one method, usually one of these:
 *
 * <ul>
 * a.
 * <li> <code>doGet</code>, if the servlet supports HTTP GET requests
 *
 * b.
 * <li> <code>doPost</code>, for HTTP POST requests
 *
 * c.
 * <li> <code>doPut</code>, for HTTP PUT requests
 *
 * d.
 * <li> <code>doDelete</code>, for HTTP DELETE requests
 *
 * e.
 * <li> <code>init</code> and <code>destroy</code>,
 * to manage resources that are held for the life of the servlet
 *
 * f.
 * <li> <code>getServletInfo</code>, which the servlet uses to
 * provide information about itself
 * </ul>
 *
 * B.
 * <p>There's almost no reason to override the <code>service</code>
 * method. <code>service</code> handles standard HTTP
 * requests by dispatching them to the handler methods
 * for each HTTP request type (the <code>do</code><i>Method</i>
 * methods listed above).
 *
 * C.
 * <p>Likewise, there's almost no reason to override the
 * <code>doOptions</code> and <code>doTrace</code> methods.
 *
 * D.
 * <p>Servlets typically run on multithreaded servers,
 * so be aware that a servlet must handle concurrent
 * requests and be careful to synchronize access to shared resources.
 * Shared resources include in-memory data such as
 * instance or class variables and external objects
 * such as files, database connections, and network
 * connections.
 * See the
 * <a href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">
 * Java Tutorial on Multithreaded Programming</a> for more
 * information on handling multiple threads in a Java program.
 */
// 20201220 提供要被子类化的抽象类，以创建适合于网站的HTTP servlet, 子类需重写doGet | doPost | doPut | doDelete | init and destroy | getServletInfo
public abstract class HttpServlet extends GenericServlet {

    private static final long serialVersionUID = 1L;

    // 20201221 DELETE方法
    private static final String METHOD_DELETE = "DELETE";

    // 20201221 HEAD方法
    private static final String METHOD_HEAD = "HEAD";

    // 20201221 GET方法
    private static final String METHOD_GET = "GET";

    // 20201221 OPTIONS方法
    private static final String METHOD_OPTIONS = "OPTIONS";

    // 20201221 POST方法
    private static final String METHOD_POST = "POST";

    // 20201221 PUT方法
    private static final String METHOD_PUT = "PUT";

    // 20201221 TRACE方法
    private static final String METHOD_TRACE = "TRACE";

    private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";

    private static final String LSTRING_FILE =
        "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings =
        ResourceBundle.getBundle(LSTRING_FILE);


    /**
     * Does nothing, because this is an abstract class.
     */
    public HttpServlet() {
        // NOOP
    }

    /**
     * 20201221
     * A. 由服务器调用（通过service方法），以允许servlet处理GET请求。
     * B. 覆盖此方法以支持GET请求也将自动支持HTTP HEAD请求。 HEAD请求是一个GET请求，它在响应中不返回任何正文，仅返回请求标头字段。
     * C. 重写此方法时，读取请求数据，写入响应标头，获取响应的writer或输出流对象，最后写入响应数据。 最好包括内容类型和编码。 当使用PrintWriter对象返回响应时，
     *    请在访问PrintWriter对象之前设置内容类型。
     * D. Servlet容器必须在提交响应之前写入标头，因为在HTTP中标头必须在响应主体之前发送。
     * E. 尽可能设置Content-Length标头（使用{@link javax.servlet.ServletResponse＃setContentLength}方法），以允许Servlet容器使用持久连接将其响应返回给客户端，
     *    从而提高性能。 如果整个响应都适合响应缓冲区，则内容长度将自动设置。
     * F. 使用HTTP 1.1分块编码时（这意味着响应具有Transfer-Encoding标头），请勿设置Content-Length标头。
     * G. GET方法应该是安全的，也就是说，不会有任何对用户负责的副作用。 例如，大多数表单查询没有副作用。 如果客户端请求旨在更改存储的数据，则该请求应使用其他HTTP方法。
     * H. GET方法也应该是幂等的，这意味着可以安全地重复它。 有时使方法安全也使其具有幂等性。 例如，重复查询既安全又幂等，但是在线购买产品或修改数据既不安全也不幂等。
     * I. 如果请求的格式错误，则doGet返回HTTP“错误请求”消息。
     */
    /**
     * A.
     * Called by the server (via the <code>service</code> method) to
     * allow a servlet to handle a GET request.
     *
     * B.
     * <p>Overriding this method to support a GET request also
     * automatically supports an HTTP HEAD request. A HEAD
     * request is a GET request that returns no body in the
     * response, only the request header fields.
     *
     * C.
     * <p>When overriding this method, read the request data,
     * write the response headers, get the response's writer or
     * output stream object, and finally, write the response data.
     * It's best to include content type and encoding. When using
     * a <code>PrintWriter</code> object to return the response,
     * set the content type before accessing the
     * <code>PrintWriter</code> object.
     *
     * D.
     * <p>The servlet container must write the headers before
     * committing the response, because in HTTP the headers must be sent
     * before the response body.
     *
     * E.
     * <p>Where possible, set the Content-Length header (with the
     * {@link javax.servlet.ServletResponse#setContentLength} method),
     * to allow the servlet container to use a persistent connection
     * to return its response to the client, improving performance.
     * The content length is automatically set if the entire response fits
     * inside the response buffer.
     *
     * F.
     * <p>When using HTTP 1.1 chunked encoding (which means that the response
     * has a Transfer-Encoding header), do not set the Content-Length header.
     *
     * G.
     * <p>The GET method should be safe, that is, without
     * any side effects for which users are held responsible.
     * For example, most form queries have no side effects.
     * If a client request is intended to change stored data,
     * the request should use some other HTTP method.
     *
     * H.
     * <p>The GET method should also be idempotent, meaning
     * that it can be safely repeated. Sometimes making a
     * method safe also makes it idempotent. For example,
     * repeating queries is both safe and idempotent, but
     * buying a product online or modifying data is neither
     * safe nor idempotent.
     *
     * I.
     * <p>If the request is incorrectly formatted, <code>doGet</code>
     * returns an HTTP "Bad Request" message.
     *
     * @param req   an {@link HttpServletRequest} object that
     *                  contains the request the client has made
     *                  of the servlet
     *
     * @param resp  an {@link HttpServletResponse} object that
     *                  contains the response the servlet sends
     *                  to the client
     *
     * @exception IOException   if an input or output error is
     *                              detected when the servlet handles
     *                              the GET request
     *
     * @exception ServletException  if the request for the GET
     *                                  could not be handled
     *
     * @see javax.servlet.ServletResponse#setContentType
     */
    // 20201221 由服务器调用（通过service方法），以允许servlet处理GET请求: GET方法也应该是幂等的，这意味着可以安全地重复它, 如果客户端请求旨在更改存储的数据，则该请求应使用其他HTTP方法
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String msg = lStrings.getString("http.method_get_not_supported");
        sendMethodNotAllowed(req, resp, msg);
    }

    /**
     * 20201221
     * A. 返回自格林尼治标准时间1970年1月1日午夜起最后一次修改HttpServletRequest对象的时间。 如果时间未知，则此方法返回负数（默认值）。
     * B. 支持HTTP GET请求并可以快速确定其上次修改时间的Servlet应该重写此方法。 这使浏览器和代理缓存更有效地工作，从而减少了服务器和网络资源的负载。
     */
    /**
     * A.
     * Returns the time the <code>HttpServletRequest</code>
     * object was last modified,
     * in milliseconds since midnight January 1, 1970 GMT.
     * If the time is unknown, this method returns a negative
     * number (the default).
     *
     * B.
     * <p>Servlets that support HTTP GET requests and can quickly determine
     * their last modification time should override this method.
     * This makes browser and proxy caches work more effectively,
     * reducing the load on server and network resources.
     *
     * @param req   the <code>HttpServletRequest</code>
     *                  object that is sent to the servlet
     *
     * @return  a <code>long</code> integer specifying
     *              the time the <code>HttpServletRequest</code>
     *              object was last modified, in milliseconds
     *              since midnight, January 1, 1970 GMT, or
     *              -1 if the time is not known
     */
    // 20201221 返回自格林尼治标准时间1970年1月1日午夜起最后一次修改HttpServletRequest对象的时间: 这使浏览器和代理缓存更有效地工作，从而减少了服务器和网络资源的负载。
    protected long getLastModified(HttpServletRequest req) {
        return -1;
    }

    /**
     * 20201221
     * A. 从protected service方法接收HTTP HEAD请求并处理该请求。
     * B. 当客户端只想查看响应的标头（例如Content-Type或Content-Length）时，客户端发送HEAD请求。 HTTP HEAD方法对响应中的输出字节进行计数，以准确设置Content-Length标头。
     * C. 如果覆盖此方法，则可以避免计算响应主体，而只需直接设置响应头即可提高性能。 确保您编写的doHead方法既安全又幂等（即，防止自己被一个HTTP HEAD请求多次调用）。
     * D. 如果HTTP HEAD请求的格式错误，则doHead返回HTTP“错误请求”消息。
     */
    /**
     * A.
     * <p>Receives an HTTP HEAD request from the protected
     * <code>service</code> method and handles the
     * request.
     *
     * B.
     * The client sends a HEAD request when it wants
     * to see only the headers of a response, such as
     * Content-Type or Content-Length. The HTTP HEAD
     * method counts the output bytes in the response
     * to set the Content-Length header accurately.
     *
     * C.
     * <p>If you override this method, you can avoid computing
     * the response body and just set the response headers
     * directly to improve performance. Make sure that the
     * <code>doHead</code> method you write is both safe
     * and idempotent (that is, protects itself from being
     * called multiple times for one HTTP HEAD request).
     *
     * D.
     * <p>If the HTTP HEAD request is incorrectly formatted,
     * <code>doHead</code> returns an HTTP "Bad Request"
     * message.
     *
     * @param req   the request object that is passed to the servlet
     *
     * @param resp  the response object that the servlet
     *                  uses to return the headers to the client
     *
     * @exception IOException   if an input or output error occurs
     *
     * @exception ServletException  if the request for the HEAD
     *                                  could not be handled
     */
    // 20201221 从protected service方法接收HTTP HEAD请求并处理该请求: HTTP HEAD方法对响应中的输出字节进行计数，以准确设置Content-Length标头
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        if (DispatcherType.INCLUDE.equals(req.getDispatcherType())) {
            doGet(req, resp);
        } else {
            NoBodyResponse response = new NoBodyResponse(resp);
            doGet(req, response);
            response.setContentLength();
        }
    }

    /**
     * 20201221
     * A. 由服务器调用（通过service方法），以允许servlet处理POST请求。
     * B. HTTP POST方法允许客户端一次将无限长的数据发送到Web服务器，并且在发布诸如信用卡号之类的信息时非常有用。
     * C. 重写此方法时，读取请求数据，写入响应标头，获取响应的writer或输出流对象，最后写入响应数据。 最好包括内容类型和编码。 当使用PrintWriter对象返回响应时，
     *    请在访问PrintWriter对象之前设置内容类型。
     * D. Servlet容器必须在提交响应之前写入标头，因为在HTTP中标头必须在响应主体之前发送。
     * E. 尽可能设置Content-Length标头（使用{@link javax.servlet.ServletResponse＃setContentLength}方法），以允许Servlet容器使用持久连接将其响应返回给客户端，
     *    从而提高性能。 如果整个响应都适合响应缓冲区，则内容长度将自动设置。
     * F. 使用HTTP 1.1分块编码时（这意味着响应具有Transfer-Encoding标头），请勿设置Content-Length标头。
     * G. 此方法不必是安全的或幂等的。 通过POST请求的操作可能会产生副作用，用户可以对此负责，例如，更新存储的数据或在线购买商品。
     * H. 如果HTTP POST请求的格式错误，则doPost返回HTTP“错误请求”消息。
     */
    /**
     * A.
     * Called by the server (via the <code>service</code> method)
     * to allow a servlet to handle a POST request.
     *
     * B.
     * The HTTP POST method allows the client to send
     * data of unlimited length to the Web server a single time
     * and is useful when posting information such as
     * credit card numbers.
     *
     * C.
     * <p>When overriding this method, read the request data,
     * write the response headers, get the response's writer or output
     * stream object, and finally, write the response data. It's best
     * to include content type and encoding. When using a
     * <code>PrintWriter</code> object to return the response, set the
     * content type before accessing the <code>PrintWriter</code> object.
     *
     * D.
     * <p>The servlet container must write the headers before committing the
     * response, because in HTTP the headers must be sent before the
     * response body.
     *
     * E.
     * <p>Where possible, set the Content-Length header (with the
     * {@link javax.servlet.ServletResponse#setContentLength} method),
     * to allow the servlet container to use a persistent connection
     * to return its response to the client, improving performance.
     * The content length is automatically set if the entire response fits
     * inside the response buffer.
     *
     * F.
     * <p>When using HTTP 1.1 chunked encoding (which means that the response
     * has a Transfer-Encoding header), do not set the Content-Length header.
     *
     * G.
     * <p>This method does not need to be either safe or idempotent.
     * Operations requested through POST can have side effects for
     * which the user can be held accountable, for example,
     * updating stored data or buying items online.
     *
     * H.
     * <p>If the HTTP POST request is incorrectly formatted,
     * <code>doPost</code> returns an HTTP "Bad Request" message.
     *
     * @param req   an {@link HttpServletRequest} object that
     *                  contains the request the client has made
     *                  of the servlet
     *
     * @param resp  an {@link HttpServletResponse} object that
     *                  contains the response the servlet sends
     *                  to the client
     *
     * @exception IOException   if an input or output error is
     *                              detected when the servlet handles
     *                              the request
     *
     * @exception ServletException  if the request for the POST
     *                                  could not be handled
     *
     * @see javax.servlet.ServletOutputStream
     * @see javax.servlet.ServletResponse#setContentType
     */
    // 20201221 由服务器调用（通过service方法），以允许servlet处理POST请求: HTTP POST方法允许客户端一次将无限长的数据发送到Web服务器, 此方法不必是安全的或幂等的
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        String msg = lStrings.getString("http.method_post_not_supported");
        sendMethodNotAllowed(req, resp, msg);
    }

    /**
     * 20201221
     * A. 由服务器调用（通过service方法），以允许Servlet处理PUT请求。
     * B. PUT操作允许客户端在服务器上放置文件，类似于通过FTP发送文件。
     * C. 覆盖此方法时，请保留与请求一起发送的所有内容标头（包括Content-Length，Content-Type，Content-Transfer-Encoding，Content-Encoding，Content-Base，
     *    Content-Language，Content-Location，Content-MD5， 和内容范围）。 如果您的方法无法处理内容标头，则它必须发出错误消息（HTTP 501-未实现）并放弃请求。
     *    有关HTTP 1.1的详细信息，请参阅RFC 2616 <a href="http://www.ietf.org/rfc/rfc2616.txt"> </a>。
     * D. 此方法不必是安全的或幂等的。 doPut执行的操作可能会产生副作用，可能会使用户承担责任。 使用此方法时，将受影响的URL的副本保存在临时存储中可能很有用。
     * E. 如果HTTP PUT请求的格式错误，则doPut将返回HTTP“错误请求”消息。
     */
    /**
     * A.
     * Called by the server (via the <code>service</code> method)
     * to allow a servlet to handle a PUT request.
     *
     * B.
     * The PUT operation allows a client to
     * place a file on the server and is similar to
     * sending a file by FTP.
     *
     * C.
     * <p>When overriding this method, leave intact
     * any content headers sent with the request (including
     * Content-Length, Content-Type, Content-Transfer-Encoding,
     * Content-Encoding, Content-Base, Content-Language, Content-Location,
     * Content-MD5, and Content-Range). If your method cannot
     * handle a content header, it must issue an error message
     * (HTTP 501 - Not Implemented) and discard the request.
     * For more information on HTTP 1.1, see RFC 2616
     * <a href="http://www.ietf.org/rfc/rfc2616.txt"></a>.
     *
     * D.
     * <p>This method does not need to be either safe or idempotent.
     * Operations that <code>doPut</code> performs can have side
     * effects for which the user can be held accountable. When using
     * this method, it may be useful to save a copy of the
     * affected URL in temporary storage.
     *
     * E.
     * <p>If the HTTP PUT request is incorrectly formatted,
     * <code>doPut</code> returns an HTTP "Bad Request" message.
     *
     * @param req   the {@link HttpServletRequest} object that
     *                  contains the request the client made of
     *                  the servlet
     *
     * @param resp  the {@link HttpServletResponse} object that
     *                  contains the response the servlet returns
     *                  to the client
     *
     * @exception IOException   if an input or output error occurs
     *                              while the servlet is handling the
     *                              PUT request
     *
     * @exception ServletException  if the request for the PUT
     *                                  cannot be handled
     */
    // 20201221 由服务器调用（通过service方法），以允许Servlet处理PUT请求: PUT操作允许客户端在服务器上放置文件，类似于通过FTP发送文件，此方法不必是安全的或幂等的
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        String msg = lStrings.getString("http.method_put_not_supported");
        sendMethodNotAllowed(req, resp, msg);
    }

    /**
     * 20201221
     * A. 由服务器调用（通过service方法），以允许servlet处理DELETE请求。
     * B. DELETE操作允许客户端从服务器删除文档或网页。
     * C. 此方法不必是安全的或幂等的。 通过DELETE请求的操作可能会产生副作用，用户应对此负责。 使用此方法时，将受影响的URL的副本保存在临时存储中可能很有用。
     * D. 如果HTTP DELETE请求的格式不正确，则doDelete返回HTTP“ Bad Request”消息。
     */
    /**
     * A.
     * Called by the server (via the <code>service</code> method)
     * to allow a servlet to handle a DELETE request.
     *
     * B.
     * The DELETE operation allows a client to remove a document
     * or Web page from the server.
     *
     * C.
     * <p>This method does not need to be either safe
     * or idempotent. Operations requested through
     * DELETE can have side effects for which users
     * can be held accountable. When using
     * this method, it may be useful to save a copy of the
     * affected URL in temporary storage.
     *
     * D.
     * <p>If the HTTP DELETE request is incorrectly formatted,
     * <code>doDelete</code> returns an HTTP "Bad Request"
     * message.
     *
     * @param req   the {@link HttpServletRequest} object that
     *                  contains the request the client made of
     *                  the servlet
     *
     *
     * @param resp  the {@link HttpServletResponse} object that
     *                  contains the response the servlet returns
     *                  to the client
     *
     * @exception IOException   if an input or output error occurs
     *                              while the servlet is handling the
     *                              DELETE request
     *
     * @exception ServletException  if the request for the
     *                                  DELETE cannot be handled
     */
    // 20201221 由服务器调用（通过service方法），以允许servlet处理DELETE请求: DELETE操作允许客户端从服务器删除文档或网页, 此方法不必是安全的或幂等的
    protected void doDelete(HttpServletRequest req,
                            HttpServletResponse resp)
        throws ServletException, IOException {

        String msg = lStrings.getString("http.method_delete_not_supported");
        sendMethodNotAllowed(req, resp, msg);
    }


    private void sendMethodNotAllowed(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
        String protocol = req.getProtocol();
        // Note: Tomcat reports "" for HTTP/0.9 although some implementations
        //       may report HTTP/0.9
        if (protocol.length() == 0 || protocol.endsWith("0.9") || protocol.endsWith("1.0")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
        }
    }


    private static Method[] getAllDeclaredMethods(Class<?> c) {

        if (c.equals(javax.servlet.http.HttpServlet.class)) {
            return null;
        }

        Method[] parentMethods = getAllDeclaredMethods(c.getSuperclass());
        Method[] thisMethods = c.getDeclaredMethods();

        if ((parentMethods != null) && (parentMethods.length > 0)) {
            Method[] allMethods =
                new Method[parentMethods.length + thisMethods.length];
            System.arraycopy(parentMethods, 0, allMethods, 0,
                             parentMethods.length);
            System.arraycopy(thisMethods, 0, allMethods, parentMethods.length,
                             thisMethods.length);

            thisMethods = allMethods;
        }

        return thisMethods;
    }

    /**
     * 20201221
     * A. 由服务器调用（通过service方法），以允许servlet处理OPTIONS请求。
     * B. OPTIONS请求确定服务器支持的HTTP方法并返回适当的标头。 例如，如果servlet覆盖doGet，则此方法返回以下标头：
     *    允许：GET，HEAD，TRACE，OPTIONS
     * C. 除非Servlet实现新的HTTP方法（而不是HTTP 1.1实现的方法），否则无需重写此方法。
     */
    /**
     * A.
     * Called by the server (via the <code>service</code> method)
     * to allow a servlet to handle an OPTIONS request.
     *
     * B.
     * The OPTIONS request determines which HTTP methods
     * the server supports and
     * returns an appropriate header. For example, if a servlet
     * overrides <code>doGet</code>, this method returns the
     * following header:
     *
     * <p><code>Allow: GET, HEAD, TRACE, OPTIONS</code>
     *
     * C.
     * <p>There's no need to override this method unless the
     * servlet implements new HTTP methods, beyond those
     * implemented by HTTP 1.1.
     *
     * @param req   the {@link HttpServletRequest} object that
     *                  contains the request the client made of
     *                  the servlet
     *
     * @param resp  the {@link HttpServletResponse} object that
     *                  contains the response the servlet returns
     *                  to the client
     *
     * @exception IOException   if an input or output error occurs
     *                              while the servlet is handling the
     *                              OPTIONS request
     *
     * @exception ServletException  if the request for the
     *                                  OPTIONS cannot be handled
     */
    // 20201221 由服务器调用（通过service方法），以允许servlet处理OPTIONS请求: OPTIONS请求确定服务器支持的HTTP方法并返回适当的标头
    protected void doOptions(HttpServletRequest req,
            HttpServletResponse resp)
        throws ServletException, IOException {

        Method[] methods = getAllDeclaredMethods(this.getClass());

        boolean ALLOW_GET = false;
        boolean ALLOW_HEAD = false;
        boolean ALLOW_POST = false;
        boolean ALLOW_PUT = false;
        boolean ALLOW_DELETE = false;
        boolean ALLOW_TRACE = true;
        boolean ALLOW_OPTIONS = true;

        // Tomcat specific hack to see if TRACE is allowed
        Class<?> clazz = null;
        try {
            clazz = Class.forName("org.apache.catalina.connector.RequestFacade");
            Method getAllowTrace = clazz.getMethod("getAllowTrace", (Class<?>[]) null);
            ALLOW_TRACE = ((Boolean) getAllowTrace.invoke(req, (Object[]) null)).booleanValue();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // Ignore. Not running on Tomcat. TRACE is always allowed.
        }
        // End of Tomcat specific hack

        for (int i=0; i<methods.length; i++) {
            Method m = methods[i];

            if (m.getName().equals("doGet")) {
                ALLOW_GET = true;
                ALLOW_HEAD = true;
            }
            if (m.getName().equals("doPost"))
                ALLOW_POST = true;
            if (m.getName().equals("doPut"))
                ALLOW_PUT = true;
            if (m.getName().equals("doDelete"))
                ALLOW_DELETE = true;
        }

        String allow = null;
        if (ALLOW_GET)
            allow=METHOD_GET;
        if (ALLOW_HEAD)
            if (allow==null) allow=METHOD_HEAD;
            else allow += ", " + METHOD_HEAD;
        if (ALLOW_POST)
            if (allow==null) allow=METHOD_POST;
            else allow += ", " + METHOD_POST;
        if (ALLOW_PUT)
            if (allow==null) allow=METHOD_PUT;
            else allow += ", " + METHOD_PUT;
        if (ALLOW_DELETE)
            if (allow==null) allow=METHOD_DELETE;
            else allow += ", " + METHOD_DELETE;
        if (ALLOW_TRACE)
            if (allow==null) allow=METHOD_TRACE;
            else allow += ", " + METHOD_TRACE;
        if (ALLOW_OPTIONS)
            if (allow==null) allow=METHOD_OPTIONS;
            else allow += ", " + METHOD_OPTIONS;

        resp.setHeader("Allow", allow);
    }

    /**
     * 20201221
     * A. 由服务器调用（通过service方法），以允许servlet处理TRACE请求。
     * B. TRACE将与TRACE请求一起发送的标头返回给客户端，以便可以在调试中使用它们。 无需重写此方法。
     */
    /**
     * A.
     * Called by the server (via the <code>service</code> method)
     * to allow a servlet to handle a TRACE request.
     *
     * B.
     * A TRACE returns the headers sent with the TRACE
     * request to the client, so that they can be used in
     * debugging. There's no need to override this method.
     *
     * @param req   the {@link HttpServletRequest} object that
     *                  contains the request the client made of
     *                  the servlet
     *
     * @param resp  the {@link HttpServletResponse} object that
     *                  contains the response the servlet returns
     *                  to the client
     *
     * @exception IOException   if an input or output error occurs
     *                              while the servlet is handling the
     *                              TRACE request
     *
     * @exception ServletException  if the request for the
     *                                  TRACE cannot be handled
     */
    // 20201221 由服务器调用（通过service方法），以允许servlet处理TRACE请求: TRACE将与TRACE请求一起发送的标头返回给客户端，以便可以在调试中使用它们
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {

        int responseLength;

        String CRLF = "\r\n";
        StringBuilder buffer = new StringBuilder("TRACE ").append(req.getRequestURI())
            .append(" ").append(req.getProtocol());

        Enumeration<String> reqHeaderEnum = req.getHeaderNames();

        while( reqHeaderEnum.hasMoreElements() ) {
            String headerName = reqHeaderEnum.nextElement();
            buffer.append(CRLF).append(headerName).append(": ")
                .append(req.getHeader(headerName));
        }

        buffer.append(CRLF);

        responseLength = buffer.length();

        resp.setContentType("message/http");
        resp.setContentLength(responseLength);
        ServletOutputStream out = resp.getOutputStream();
        out.print(buffer.toString());
        out.close();
    }

    /**
     * 20201221
     * 从公共服务方法接收标准的HTTP请求，并将其分派到此类中定义的do Method方法。 此方法是{@link javax.servlet.Servlet＃service}方法的HTTP特定版本。 无需重写此方法。
     */
    /**
     * Receives standard HTTP requests from the public
     * <code>service</code> method and dispatches
     * them to the <code>do</code><i>Method</i> methods defined in
     * this class. This method is an HTTP-specific version of the
     * {@link javax.servlet.Servlet#service} method. There's no
     * need to override this method.
     *
     * @param req   the {@link HttpServletRequest} object that
     *                  contains the request the client made of
     *                  the servlet
     *
     * @param resp  the {@link HttpServletResponse} object that
     *                  contains the response the servlet returns
     *                  to the client
     *
     * @exception IOException   if an input or output error occurs
     *                              while the servlet is handling the
     *                              HTTP request
     *
     * @exception ServletException  if the HTTP request
     *                                  cannot be handled
     *
     * @see javax.servlet.Servlet#service
     */
    // 20201221 从公共服务方法接收标准的HTTP请求，并将其分派到此类中定义的do Method方法, 此方法是service方法的HTTP特定版本。 无需重写此方法。
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        // 20201221 返回发出此请求的HTTP方法的名称，例如GET，POST或PUT。
        String method = req.getMethod();

        // 20201221 GET方法
        if (method.equals(METHOD_GET)) {
            // 20201221 返回自格林尼治标准时间1970年1月1日午夜起最后一次修改HttpServletRequest对象的时间: 这使浏览器和代理缓存更有效地工作，从而减少了服务器和网络资源的负载。
            long lastModified = getLastModified(req);
            if (lastModified == -1) {
                // 20201221 servlet不支持if-modified, 因为没有理由通过更昂贵的逻辑
                // servlet doesn't support if-modified-since, no reason
                // to go through further expensive logic
                // 20201221 由服务器调用（通过service方法），以允许servlet处理GET请求: GET方法也应该是幂等的，这意味着可以安全地重复它, 如果客户端请求旨在更改存储的数据，则该请求应使用其他HTTP方法
                doGet(req, resp);
            } else {
                long ifModifiedSince;
                try {
                    ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
                } catch (IllegalArgumentException iae) {
                    // Invalid date header - proceed as if none was set
                    ifModifiedSince = -1;
                }
                if (ifModifiedSince < (lastModified / 1000 * 1000)) {
                    // If the servlet mod time is later, call doGet()
                    // Round down to the nearest second for a proper compare
                    // A ifModifiedSince of -1 will always be less
                    maybeSetLastModified(resp, lastModified);
                    // 20201221 由服务器调用（通过service方法），以允许servlet处理GET请求: GET方法也应该是幂等的，这意味着可以安全地重复它, 如果客户端请求旨在更改存储的数据，则该请求应使用其他HTTP方法
                    doGet(req, resp);
                } else {
                    // 20201221 状态码（304），指示有条件的GET操作发现资源可用且未修改。
                    resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            }
        }
        // 20201221 HEAD方法
        else if (method.equals(METHOD_HEAD)) {
            long lastModified = getLastModified(req);
            maybeSetLastModified(resp, lastModified);
            // 20201221 从protected service方法接收HTTP HEAD请求并处理该请求: HTTP HEAD方法对响应中的输出字节进行计数，以准确设置Content-Length标头
            doHead(req, resp);
        }

        // 20201221 POST方法
        else if (method.equals(METHOD_POST)) {
            // 20201221 由服务器调用（通过service方法），以允许servlet处理POST请求: HTTP POST方法允许客户端一次将无限长的数据发送到Web服务器, 此方法不必是安全的或幂等的
            doPost(req, resp);
        }

        // 20201221 PUT方法
        else if (method.equals(METHOD_PUT)) {
            // 20201221 由服务器调用（通过service方法），以允许Servlet处理PUT请求: PUT操作允许客户端在服务器上放置文件，类似于通过FTP发送文件，此方法不必是安全的或幂等的
            doPut(req, resp);
        }

        // 20201221 DELETE方法
        else if (method.equals(METHOD_DELETE)) {
            // 20201221 由服务器调用（通过service方法），以允许servlet处理DELETE请求: DELETE操作允许客户端从服务器删除文档或网页, 此方法不必是安全的或幂等的
            doDelete(req, resp);
        }

        // 20201221 OPTIONS方法
        else if (method.equals(METHOD_OPTIONS)) {
            // 20201221 由服务器调用（通过service方法），以允许servlet处理OPTIONS请求: OPTIONS请求确定服务器支持的HTTP方法并返回适当的标头
            doOptions(req,resp);
        }

        // 20201221 TRACE方法
        else if (method.equals(METHOD_TRACE)) {
            // 20201221 由服务器调用（通过service方法），以允许servlet处理TRACE请求: TRACE将与TRACE请求一起发送的标头返回给客户端，以便可以在调试中使用它们
            doTrace(req,resp);
        }

        // 20201221 否则抛出501
        else {
            // 20201221 请注意，这意味着该服务器上任何位置的servlet都不支持所请求的任何方法。
            //
            // Note that this means NO servlet supports whatever
            // method was requested, anywhere on this server.
            //
            String errMsg = lStrings.getString("http.method_not_implemented");
            Object[] errArgs = new Object[1];
            errArgs[0] = method;
            errMsg = MessageFormat.format(errMsg, errArgs);
            // 20201221 指示HTTP服务器的状态代码（501）不支持满足请求所需的功能。
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
        }
    }

    /*
     * Sets the Last-Modified entity header field, if it has not
     * already been set and if the value is meaningful.  Called before
     * doGet, to ensure that headers are set before response data is
     * written.  A subclass might have set this header already, so we
     * check.
     */
    private void maybeSetLastModified(HttpServletResponse resp,
                                      long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD))
            return;
        if (lastModified >= 0)
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
    }

    /**
     * 20201221
     * 将客户端请求分派到受保护的服务方法。 无需重写此方法。
     */
    /**
     * A.
     * Dispatches client requests to the protected
     * <code>service</code> method. There's no need to
     * override this method.
     *
     * @param req   the {@link HttpServletRequest} object that
     *                  contains the request the client made of
     *                  the servlet
     *
     * @param res   the {@link HttpServletResponse} object that
     *                  contains the response the servlet returns
     *                  to the client
     *
     * @exception IOException   if an input or output error occurs
     *                              while the servlet is handling the
     *                              HTTP request
     *
     * @exception ServletException  if the HTTP request cannot
     *                                  be handled
     *
     * @see javax.servlet.Servlet#service
     */
    // 20201221 将客户端请求分派到受保护的服务方法。 无需重写此方法。
    @Override
    public void service(ServletRequest req, ServletResponse res)
        throws ServletException, IOException {

        HttpServletRequest  request;
        HttpServletResponse response;

        try {
            // 20201221 ServletRequest扩展接口: 提供HTTP Servlet的请求信息
            request = (HttpServletRequest) req;

            // 20201221 ServletResponse接口扩展: 在发送响应时提供特定于HTTP的功能(例如，它具有访问HTTP标头和cookie的方法)
            response = (HttpServletResponse) res;
        } catch (ClassCastException e) {
            throw new ServletException(lStrings.getString("http.non_http"));
        }

        // 20201221 从公共服务方法接收标准的HTTP请求，并将其分派到此类中定义的do Method方法, 此方法是service方法的HTTP特定版本。 无需重写此方法。
        service(request, response);
    }
}


/*
 * A response wrapper for use in (dumb) "HEAD" support.
 * This just swallows that body, counting the bytes in order to set
 * the content length appropriately.  All other methods delegate to the
 * wrapped HTTP Servlet Response object.
 */
// file private
class NoBodyResponse extends HttpServletResponseWrapper {
    private final NoBodyOutputStream noBody;
    private PrintWriter writer;
    private boolean didSetContentLength;

    // file private
    NoBodyResponse(HttpServletResponse r) {
        super(r);
        noBody = new NoBodyOutputStream(this);
    }

    // file private
    void setContentLength() {
        if (!didSetContentLength) {
            if (writer != null) {
                writer.flush();
            }
            super.setContentLength(noBody.getContentLength());
        }
    }


    // SERVLET RESPONSE interface methods

    @Override
    public void setContentLength(int len) {
        super.setContentLength(len);
        didSetContentLength = true;
    }

    @Override
    public void setContentLengthLong(long len) {
        super.setContentLengthLong(len);
        didSetContentLength = true;
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        checkHeader(name);
    }

    private void checkHeader(String name) {
        if ("content-length".equalsIgnoreCase(name)) {
            didSetContentLength = true;
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return noBody;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {

        if (writer == null) {
            OutputStreamWriter w;

            w = new OutputStreamWriter(noBody, getCharacterEncoding());
            writer = new PrintWriter(w);
        }
        return writer;
    }
}


/*
 * Servlet output stream that gobbles up all its data.
 */

// file private
class NoBodyOutputStream extends ServletOutputStream {

    private static final String LSTRING_FILE =
        "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings =
        ResourceBundle.getBundle(LSTRING_FILE);

    private final HttpServletResponse response;
    private boolean flushed = false;
    private int contentLength = 0;

    // file private
    NoBodyOutputStream(HttpServletResponse response) {
        this.response = response;
    }

    // file private
    int getContentLength() {
        return contentLength;
    }

    @Override
    public void write(int b) throws IOException {
        contentLength++;
        checkCommit();
    }

    @Override
    public void write(byte buf[], int offset, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException(
                    lStrings.getString("err.io.nullArray"));
        }

        if (offset < 0 || len < 0 || offset+len > buf.length) {
            String msg = lStrings.getString("err.io.indexOutOfBounds");
            Object[] msgArgs = new Object[3];
            msgArgs[0] = Integer.valueOf(offset);
            msgArgs[1] = Integer.valueOf(len);
            msgArgs[2] = Integer.valueOf(buf.length);
            msg = MessageFormat.format(msg, msgArgs);
            throw new IndexOutOfBoundsException(msg);
        }

        contentLength += len;
        checkCommit();
    }

    @Override
    public boolean isReady() {
        // TODO SERVLET 3.1
        return false;
    }

    @Override
    public void setWriteListener(javax.servlet.WriteListener listener) {
        // TODO SERVLET 3.1
    }

    private void checkCommit() throws IOException {
        if (!flushed && contentLength > response.getBufferSize()) {
            response.flushBuffer();
            flushed = true;
        }
    }
}
