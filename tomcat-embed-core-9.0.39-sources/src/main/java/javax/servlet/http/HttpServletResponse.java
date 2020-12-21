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
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.ServletResponse;

/**
 * 20201221
 * A. 扩展{@link ServletResponse}接口，以在发送响应时提供特定于HTTP的功能。 例如，它具有访问HTTP标头和cookie的方法。
 * B. Servlet容器创建一个HttpServletResponse对象，并将其作为参数传递给Servlet的服务方法（doGet，doPost等）。
 */
/**
 * A.
 * Extends the {@link ServletResponse} interface to provide HTTP-specific
 * functionality in sending a response. For example, it has methods to access
 * HTTP headers and cookies.
 *
 * B.
 * <p>
 * The servlet container creates an <code>HttpServletResponse</code> object and
 * passes it as an argument to the servlet's service methods (<code>doGet</code>, <code>doPost</code>, etc).
 *
 * @see javax.servlet.ServletResponse
 */
// 20201221 ServletResponse接口扩展: 在发送响应时提供特定于HTTP的功能(例如，它具有访问HTTP标头和cookie的方法), 作为参数传递给Servlet的服务方法（doGet，doPost等）
public interface HttpServletResponse extends ServletResponse {

    /**
     * Adds the specified cookie to the response. This method can be called
     * multiple times to set more than one cookie.
     *
     * @param cookie
     *            the Cookie to return to the client
     */
    public void addCookie(Cookie cookie);

    /**
     * Returns a boolean indicating whether the named response header has
     * already been set.
     *
     * @param name
     *            the header name
     * @return <code>true</code> if the named response header has already been
     *         set; <code>false</code> otherwise
     */
    public boolean containsHeader(String name);

    /**
     * Encodes the specified URL by including the session ID in it, or, if
     * encoding is not needed, returns the URL unchanged. The implementation of
     * this method includes the logic to determine whether the session ID needs
     * to be encoded in the URL. For example, if the browser supports cookies,
     * or session tracking is turned off, URL encoding is unnecessary.
     * <p>
     * For robust session tracking, all URLs emitted by a servlet should be run
     * through this method. Otherwise, URL rewriting cannot be used with
     * browsers which do not support cookies.
     *
     * @param url
     *            the url to be encoded.
     * @return the encoded URL if encoding is needed; the unchanged URL
     *         otherwise.
     */
    public String encodeURL(String url);

    /**
     * Encodes the specified URL for use in the <code>sendRedirect</code> method
     * or, if encoding is not needed, returns the URL unchanged. The
     * implementation of this method includes the logic to determine whether the
     * session ID needs to be encoded in the URL. Because the rules for making
     * this determination can differ from those used to decide whether to encode
     * a normal link, this method is separated from the <code>encodeURL</code>
     * method.
     * <p>
     * All URLs sent to the <code>HttpServletResponse.sendRedirect</code> method
     * should be run through this method. Otherwise, URL rewriting cannot be
     * used with browsers which do not support cookies.
     *
     * @param url
     *            the url to be encoded.
     * @return the encoded URL if encoding is needed; the unchanged URL
     *         otherwise.
     * @see #sendRedirect
     * @see #encodeUrl
     */
    public String encodeRedirectURL(String url);

    /**
     * @param url
     *            the url to be encoded.
     * @return the encoded URL if encoding is needed; the unchanged URL
     *         otherwise.
     * @deprecated As of version 2.1, use encodeURL(String url) instead
     */
    @Deprecated
    public String encodeUrl(String url);

    /**
     * @param url
     *            the url to be encoded.
     * @return the encoded URL if encoding is needed; the unchanged URL
     *         otherwise.
     * @deprecated As of version 2.1, use encodeRedirectURL(String url) instead
     */
    @Deprecated
    public String encodeRedirectUrl(String url);

    /**
     * Sends an error response to the client using the specified status code and
     * clears the output buffer. The server defaults to creating the response to
     * look like an HTML-formatted server error page containing the specified
     * message, setting the content type to "text/html", leaving cookies and
     * other headers unmodified. If an error-page declaration has been made for
     * the web application corresponding to the status code passed in, it will
     * be served back in preference to the suggested msg parameter.
     * <p>
     * If the response has already been committed, this method throws an
     * IllegalStateException. After using this method, the response should be
     * considered to be committed and should not be written to.
     *
     * @param sc
     *            the error status code
     * @param msg
     *            the descriptive message
     * @exception IOException
     *                If an input or output exception occurs
     * @exception IllegalStateException
     *                If the response was committed
     */
    public void sendError(int sc, String msg) throws IOException;

    /**
     * Sends an error response to the client using the specified status code and
     * clears the buffer. This is equivalent to calling {@link #sendError(int,
     * String)} with the same status code and <code>null</code> for the message.
     *
     * @param sc
     *            the error status code
     * @exception IOException
     *                If an input or output exception occurs
     * @exception IllegalStateException
     *                If the response was committed before this method call
     */
    public void sendError(int sc) throws IOException;

    /**
     * Sends a temporary redirect response to the client using the specified
     * redirect location URL. This method can accept relative URLs; the servlet
     * container must convert the relative URL to an absolute URL before sending
     * the response to the client. If the location is relative without a leading
     * '/' the container interprets it as relative to the current request URI.
     * If the location is relative with a leading '/' the container interprets
     * it as relative to the servlet container root.
     * <p>
     * If the response has already been committed, this method throws an
     * IllegalStateException. After using this method, the response should be
     * considered to be committed and should not be written to.
     *
     * @param location
     *            the redirect location URL
     * @exception IOException
     *                If an input or output exception occurs
     * @exception IllegalStateException
     *                If the response was committed or if a partial URL is given
     *                and cannot be converted into a valid URL
     */
    public void sendRedirect(String location) throws IOException;

    /**
     * Sets a response header with the given name and date-value. The date is
     * specified in terms of milliseconds since the epoch. If the header had
     * already been set, the new value overwrites the previous one. The
     * <code>containsHeader</code> method can be used to test for the presence
     * of a header before setting its value.
     *
     * @param name
     *            the name of the header to set
     * @param date
     *            the assigned date value
     * @see #containsHeader
     * @see #addDateHeader
     */
    public void setDateHeader(String name, long date);

    /**
     * Adds a response header with the given name and date-value. The date is
     * specified in terms of milliseconds since the epoch. This method allows
     * response headers to have multiple values.
     *
     * @param name
     *            the name of the header to set
     * @param date
     *            the additional date value
     * @see #setDateHeader
     */
    public void addDateHeader(String name, long date);

    /**
     * Sets a response header with the given name and value. If the header had
     * already been set, the new value overwrites the previous one. The
     * <code>containsHeader</code> method can be used to test for the presence
     * of a header before setting its value.
     *
     * @param name
     *            the name of the header
     * @param value
     *            the header value If it contains octet string, it should be
     *            encoded according to RFC 2047
     *            (http://www.ietf.org/rfc/rfc2047.txt)
     * @see #containsHeader
     * @see #addHeader
     */
    public void setHeader(String name, String value);

    /**
     * Adds a response header with the given name and value. This method allows
     * response headers to have multiple values.
     *
     * @param name
     *            the name of the header
     * @param value
     *            the additional header value If it contains octet string, it
     *            should be encoded according to RFC 2047
     *            (http://www.ietf.org/rfc/rfc2047.txt)
     * @see #setHeader
     */
    public void addHeader(String name, String value);

    /**
     * Sets a response header with the given name and integer value. If the
     * header had already been set, the new value overwrites the previous one.
     * The <code>containsHeader</code> method can be used to test for the
     * presence of a header before setting its value.
     *
     * @param name
     *            the name of the header
     * @param value
     *            the assigned integer value
     * @see #containsHeader
     * @see #addIntHeader
     */
    public void setIntHeader(String name, int value);

    /**
     * Adds a response header with the given name and integer value. This method
     * allows response headers to have multiple values.
     *
     * @param name
     *            the name of the header
     * @param value
     *            the assigned integer value
     * @see #setIntHeader
     */
    public void addIntHeader(String name, int value);

    /**
     * Sets the status code for this response. This method is used to set the
     * return status code when there is no error (for example, for the status
     * codes SC_OK or SC_MOVED_TEMPORARILY). If there is an error, and the
     * caller wishes to invoke an error-page defined in the web application, the
     * <code>sendError</code> method should be used instead.
     * <p>
     * The container clears the buffer and sets the Location header, preserving
     * cookies and other headers.
     *
     * @param sc
     *            the status code
     * @see #sendError
     */
    public void setStatus(int sc);

    /**
     * Sets the status code and message for this response.
     *
     * @param sc
     *            the status code
     * @param sm
     *            the status message
     * @deprecated As of version 2.1, due to ambiguous meaning of the message
     *             parameter. To set a status code use
     *             <code>setStatus(int)</code>, to send an error with a
     *             description use <code>sendError(int, String)</code>.
     */
    @Deprecated
    public void setStatus(int sc, String sm);

    /**
     * Get the HTTP status code for this Response.
     *
     * @return The HTTP status code for this Response
     *
     * @since Servlet 3.0
     */
    public int getStatus();

    /**
     * Return the value for the specified header, or <code>null</code> if this
     * header has not been set.  If more than one value was added for this
     * name, only the first is returned; use {@link #getHeaders(String)} to
     * retrieve all of them.
     *
     * @param name Header name to look up
     *
     * @return The first value for the specified header. This is the raw value
     *         so if multiple values are specified in the first header then they
     *         will be returned as a single header value .
     *
     * @since Servlet 3.0
     */
    public String getHeader(String name);

    /**
     * Return a Collection of all the header values associated with the
     * specified header name.
     *
     * @param name Header name to look up
     *
     * @return The values for the specified header. These are the raw values so
     *         if multiple values are specified in a single header that will be
     *         returned as a single header value.
     *
     * @since Servlet 3.0
     */
    public Collection<String> getHeaders(String name);

    /**
     * Get the header names set for this HTTP response.
     *
     * @return The header names set for this HTTP response.
     *
     * @since Servlet 3.0
     */
    public Collection<String> getHeaderNames();

    /**
     * Configure the supplier of the trailer headers. The supplier will be
     * called in the scope of the thread that completes the response.
     * <br>
     * Trailers that don't meet the requirements of RFC 7230, section 4.1.2 will
     * be ignored.
     * <br>
     * The default implementation is a NO-OP.
     *
     * @param supplier The supplier for the trailer headers
     *
     * @throws IllegalStateException if this method is called when the
     *         underlying protocol does not support trailer headers or if using
     *         HTTP/1.1 and the response has already been committed
     *
     * @since Servlet 4.0
     */
    public default void setTrailerFields(Supplier<Map<String, String>> supplier) {
        // NO-OP
    }

    /**
     * Obtain the supplier of the trailer headers.
     * <br>
     * The default implementation returns null.
     *
     * @return The supplier for the trailer headers
     *
     * @since Servlet 4.0
     */
    public default Supplier<Map<String, String>> getTrailerFields() {
        return null;
    }

    /**
     * 20201221
     * 服务器状态码； 参见RFC 2068。
     */
    /*
     * Server status codes; see RFC 2068.
     */

    /**
     * Status code (100) indicating the client can continue.
     */
    // 20201221 指示客户端可以继续的状态代码（100）。
    public static final int SC_CONTINUE = 100;

    /**
     * Status code (101) indicating the server is switching protocols according
     * to Upgrade header.
     */
    // 20201221 指示服务器正在根据升级标头切换协议的状态码（101）。
    public static final int SC_SWITCHING_PROTOCOLS = 101;

    /**
     * Status code (200) indicating the request succeeded normally.
     */
    // 20201221 指示请求成功的状态码（200）。
    public static final int SC_OK = 200;

    /**
     * Status code (201) indicating the request succeeded and created a new
     * resource on the server.
     */
    // 20201221 指示请求成功的状态代码（201），并在服务器上创建了新资源。
    public static final int SC_CREATED = 201;

    /**
     * Status code (202) indicating that a request was accepted for processing,
     * but was not completed.
     */
    // 20201221 状态码（202），表示请求已接受处理，但未完成。
    public static final int SC_ACCEPTED = 202;

    /**
     * Status code (203) indicating that the meta information presented by the
     * client did not originate from the server.
     */
    // 20201221 状态码（203），指示客户端提供的元信息不是来自服务器。
    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;

    /**
     * Status code (204) indicating that the request succeeded but that there
     * was no new information to return.
     */
    // 20201221 状态码（204），指示请求成功，但没有新信息要返回。
    public static final int SC_NO_CONTENT = 204;

    /**
     * Status code (205) indicating that the agent <em>SHOULD</em> reset the
     * document view which caused the request to be sent.
     */
    // 20201221 状态码（205），指示代理SHOULD重置导致发送请求的文档视图。
    public static final int SC_RESET_CONTENT = 205;

    /**
     * Status code (206) indicating that the server has fulfilled the partial
     * GET request for the resource.
     */
    // 20201221 状态代码（206），指示服务器已满足对资源的部分GET请求。
    public static final int SC_PARTIAL_CONTENT = 206;

    /**
     * Status code (300) indicating that the requested resource corresponds to
     * any one of a set of representations, each with its own specific location.
     */
    // 20201221 状态码（300），指示所请求的资源对应于一组表示中的任何一个，每个都有自己的特定位置。
    public static final int SC_MULTIPLE_CHOICES = 300;

    /**
     * Status code (301) indicating that the resource has permanently moved to a
     * new location, and that future references should use a new URI with their
     * requests.
     */
    // 20201221 状态码（301），指示资源已永久移动到新位置，并且将来的引用应在其请求中使用新的URI。
    public static final int SC_MOVED_PERMANENTLY = 301;

    /**
     * Status code (302) indicating that the resource has temporarily moved to
     * another location, but that future references should still use the
     * original URI to access the resource. This definition is being retained
     * for backwards compatibility. SC_FOUND is now the preferred definition.
     */
    // 20201221 状态码（302），指示资源已临时移动到另一个位置，但是将来的引用仍应使用原始URI来访问资源。 保留此定义是为了向后兼容。 现在，SC_FOUND是首选的定义。
    public static final int SC_MOVED_TEMPORARILY = 302;

    /**
     * Status code (302) indicating that the resource reside temporarily under a
     * different URI. Since the redirection might be altered on occasion, the
     * client should continue to use the Request-URI for future
     * requests.(HTTP/1.1) To represent the status code (302), it is recommended
     * to use this variable.
     */
    // 20201221 指示资源暂时驻留在不同URI下的状态码（302）。 由于重定向有时可能会更改，因此客户端应继续将Request-URI用于将来的请求。（HTTP / 1.1）为了表示状态码（302），建议使用此变量。
    public static final int SC_FOUND = 302;

    /**
     * Status code (303) indicating that the response to the request can be
     * found under a different URI.
     */
    // 20201221 指示可以在不同的URI下找到对请求的响应的状态码（303）。
    public static final int SC_SEE_OTHER = 303;

    /**
     * Status code (304) indicating that a conditional GET operation found that
     * the resource was available and not modified.
     */
    // 20201221 状态码（304），指示有条件的GET操作发现资源可用且未修改。
    public static final int SC_NOT_MODIFIED = 304;

    /**
     * Status code (305) indicating that the requested resource <em>MUST</em> be
     * accessed through the proxy given by the <code><em>Location</em></code>
     * field.
     */
    // 20201221 状态码（305）指示MUST通过Location字段给出的代理访问所请求的资源。
    public static final int SC_USE_PROXY = 305;

    /**
     * Status code (307) indicating that the requested resource resides
     * temporarily under a different URI. The temporary URI <em>SHOULD</em> be
     * given by the <code><em>Location</em></code> field in the response.
     */
    // 20201221 指示请求的资源临时驻留在其他URI下的状态码（307）。 临时URISHOULD由响应中的Location字段给出。
    public static final int SC_TEMPORARY_REDIRECT = 307;

    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    // 20201221 状态代码（400）指示客户端发送的请求在语法上不正确。
    public static final int SC_BAD_REQUEST = 400;

    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */
    // 20201221 状态码（401），指示该请求需要HTTP身份验证。
    public static final int SC_UNAUTHORIZED = 401;

    /**
     * Status code (402) reserved for future use.
     */
    // 20201221 状态码（402）保留供将来使用。
    public static final int SC_PAYMENT_REQUIRED = 402;

    /**
     * Status code (403) indicating the server understood the request but
     * refused to fulfill it.
     */
    // 20201221 指示服务器理解了请求但拒绝执行的状态码（403）。
    public static final int SC_FORBIDDEN = 403;

    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    // 20201221 指示请求的资源不可用的状态码（404）。
    public static final int SC_NOT_FOUND = 404;

    /**
     * Status code (405) indicating that the method specified in the
     * <code><em>Request-Line</em></code> is not allowed for the resource
     * identified by the <code><em>Request-URI</em></code>.
     */
    // 20201221 状态码（405），指示请求URI所标识的资源不允许使用请求行中指定的方法。
    public static final int SC_METHOD_NOT_ALLOWED = 405;

    /**
     * Status code (406) indicating that the resource identified by the request
     * is only capable of generating response entities which have content
     * characteristics not acceptable according to the accept headers sent in
     * the request.
     */
    // 20201221 状态码（406），指示由请求标识的资源仅能够生成响应实体，该响应实体具有根据请求中发送的接受标头不可接受的内容特征。
    public static final int SC_NOT_ACCEPTABLE = 406;

    /**
     * Status code (407) indicating that the client <em>MUST</em> first
     * authenticate itself with the proxy.
     */
    // 20201221 状态码（407），指示客户务MUST先通过代理进行身份验证。
    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;

    /**
     * Status code (408) indicating that the client did not produce a request
     * within the time that the server was prepared to wait.
     */
    // 20201221 状态码（408），指示客户端在服务器准备等待的时间内没有发出请求。
    public static final int SC_REQUEST_TIMEOUT = 408;

    /**
     * Status code (409) indicating that the request could not be completed due
     * to a conflict with the current state of the resource.
     */
    // 20201221 状态码（409），指示由于与资源的当前状态冲突而无法完成请求。
    public static final int SC_CONFLICT = 409;

    /**
     * Status code (410) indicating that the resource is no longer available at
     * the server and no forwarding address is known. This condition
     * <em>SHOULD</em> be considered permanent.
     */
    // 20201221 指示资源不再在服务器上可用并且未知转发地址的状态码（410）。 这种情况SHOULD视为永久性。
    public static final int SC_GONE = 410;

    /**
     * Status code (411) indicating that the request cannot be handled without a
     * defined <code><em>Content-Length</em></code>.
     */
    // 20201221 状态码（411），指示没有定义的Content-Length无法处理请求。
    public static final int SC_LENGTH_REQUIRED = 411;

    /**
     * Status code (412) indicating that the precondition given in one or more
     * of the request-header fields evaluated to false when it was tested on the
     * server.
     */
    // 20201221 状态代码（412），指示在服务器上测试时，在一个或多个请求标头字段中给出的前提条件被评估为false。
    public static final int SC_PRECONDITION_FAILED = 412;

    /**
     * Status code (413) indicating that the server is refusing to process the
     * request because the request entity is larger than the server is willing
     * or able to process.
     */
    // 20201221 状态码（413），指示服务器由于请求实体大于服务器愿意或能够处理的大小而拒绝处理该请求。
    public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;

    /**
     * Status code (414) indicating that the server is refusing to service the
     * request because the <code><em>Request-URI</em></code> is longer than the
     * server is willing to interpret.
     */
    // 20201221 状态代码（414），指示服务器拒绝服务请求，因为请求URI比服务器愿意解释的时间长。
    public static final int SC_REQUEST_URI_TOO_LONG = 414;

    /**
     * Status code (415) indicating that the server is refusing to service the
     * request because the entity of the request is in a format not supported by
     * the requested resource for the requested method.
     */
    // 20201221 状态码（415），指示服务器拒绝服务请求，因为请求的实体采用的格式不受所请求方法的所请求资源支持。
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * Status code (416) indicating that the server cannot serve the requested
     * byte range.
     */
    // 20201221 状态代码（416），指示服务器无法满足请求的字节范围。
    public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    /**
     * Status code (417) indicating that the server could not meet the
     * expectation given in the Expect request header.
     */
    // 20201221 状态码（417），指示服务器无法满足Expect请求标头中给出的期望。
    public static final int SC_EXPECTATION_FAILED = 417;

    /**
     * Status code (500) indicating an error inside the HTTP server which
     * prevented it from fulfilling the request.
     */
    // 20201221 状态码（500），指示HTTP服务器内部发生错误，导致该服务器无法满足请求。
    public static final int SC_INTERNAL_SERVER_ERROR = 500;

    /**
     * Status code (501) indicating the HTTP server does not support the
     * functionality needed to fulfill the request.
     */
    // 20201221 指示HTTP服务器的状态代码（501）不支持满足请求所需的功能。
    public static final int SC_NOT_IMPLEMENTED = 501;

    /**
     * Status code (502) indicating that the HTTP server received an invalid
     * response from a server it consulted when acting as a proxy or gateway.
     */
    // 20201221 状态代码（502），指示HTTP服务器在充当代理或网关时从其查询的服务器接收到无效响应。
    public static final int SC_BAD_GATEWAY = 502;

    /**
     * Status code (503) indicating that the HTTP server is temporarily
     * overloaded, and unable to handle the request.
     */
    // 20201221 状态码（503），指示HTTP服务器暂时超载，无法处理该请求。
    public static final int SC_SERVICE_UNAVAILABLE = 503;

    /**
     * Status code (504) indicating that the server did not receive a timely
     * response from the upstream server while acting as a gateway or proxy.
     */
    // 20201221 状态代码（504），指示服务器在充当网关或代理时未及时收到上游服务器的响应。
    public static final int SC_GATEWAY_TIMEOUT = 504;

    /**
     * Status code (505) indicating that the server does not support or refuses
     * to support the HTTP protocol version that was used in the request
     * message.
     */
    // 20201221 状态代码（505），指示服务器不支持或拒绝支持请求消息中使用的HTTP协议版本。
    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
}
