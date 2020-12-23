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

package org.springframework.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 20201223
 * 基于{@link HttpServletResponse}的{@link ServerHttpResponse}实现。
 */
/**
 * {@link ServerHttpResponse} implementation that is based on a {@link HttpServletResponse}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.0
 */
// 20201223 基于{@link HttpServletResponse}的{@link ServerHttpResponse}实现。
public class ServletServerHttpResponse implements ServerHttpResponse {

	private final HttpServletResponse servletResponse;

	private final HttpHeaders headers;

	private boolean headersWritten = false;

	private boolean bodyUsed = false;

	@Nullable
	private HttpHeaders readOnlyHeaders;

	/**
	 * Construct a new instance of the ServletServerHttpResponse based on the given {@link HttpServletResponse}.
	 * @param servletResponse the servlet response
	 */
	// 20201223 根据给定的{@link HttpServletResponse}构造一个ServletServerHttpResponse的新实例。
	public ServletServerHttpResponse(HttpServletResponse servletResponse) {
		Assert.notNull(servletResponse, "HttpServletResponse must not be null");
		// 20201223 eg: ResponseFacade@xxxx: Response@xxxx
		this.servletResponse = servletResponse;

		// 20201223 eg: ServletServerHttpResponse$ServletResponseHttpHeaders@xxxx: []
		this.headers = new ServletResponseHttpHeaders();
	}

	/**
	 * Return the {@code HttpServletResponse} this object is based on.
	 */
	public HttpServletResponse getServletResponse() {
		return this.servletResponse;
	}

	@Override
	public void setStatusCode(HttpStatus status) {
		Assert.notNull(status, "HttpStatus must not be null");
		this.servletResponse.setStatus(status.value());
	}

	@Override
	public HttpHeaders getHeaders() {
		if (this.readOnlyHeaders != null) {
			return this.readOnlyHeaders;
		}
		else if (this.headersWritten) {
			this.readOnlyHeaders = HttpHeaders.readOnlyHttpHeaders(this.headers);
			return this.readOnlyHeaders;
		}
		else {
			return this.headers;
		}
	}

	// 20201223 返回消息的主体作为输出流。
	@Override
	public OutputStream getBody() throws IOException {
		this.bodyUsed = true;
		writeHeaders();

		// 20201223 eg: CoyoteOutputStream@xxxx
		return this.servletResponse.getOutputStream();
	}

	@Override
	public void flush() throws IOException {
		writeHeaders();
		if (this.bodyUsed) {
			this.servletResponse.flushBuffer();
		}
	}

	@Override
	public void close() {
		writeHeaders();
	}

	private void writeHeaders() {
		if (!this.headersWritten) {
			getHeaders().forEach((headerName, headerValues) -> {
				for (String headerValue : headerValues) {
					this.servletResponse.addHeader(headerName, headerValue);
				}
			});
			// HttpServletResponse exposes some headers as properties: we should include those if not already present
			if (this.servletResponse.getContentType() == null && this.headers.getContentType() != null) {
				this.servletResponse.setContentType(this.headers.getContentType().toString());
			}
			if (this.servletResponse.getCharacterEncoding() == null && this.headers.getContentType() != null &&
					this.headers.getContentType().getCharset() != null) {
				this.servletResponse.setCharacterEncoding(this.headers.getContentType().getCharset().name());
			}
			this.headersWritten = true;
		}
	}

	/**
	 * 20201223
	 * A. 扩展HttpHeaders的功能，以查找基础HttpServletResponse中已经存在的标头。
	 * B. 目的只是公开通过HttpServletResponse提供的功能，即按名称查找特定标头值的功能。 所有其他与Map相关的操作（例如，迭代，删除等）仅适用于
	 *    直接通过HttpHeaders方法添加的值。
	 */
	/**
	 * A.
	 * Extends HttpHeaders with the ability to look up headers already present in
	 * the underlying HttpServletResponse.
	 *
	 * B.
	 * <p>The intent is merely to expose what is available through the HttpServletResponse
	 * i.e. the ability to look up specific header values by name. All other
	 * map-related operations (e.g. iteration, removal, etc) apply only to values
	 * added directly through HttpHeaders methods.
	 *
	 * @since 4.0.3
	 */
	// 20201223 扩展HttpHeaders的功能，以查找基础HttpServletResponse中已经存在的标头
	private class ServletResponseHttpHeaders extends HttpHeaders {

		private static final long serialVersionUID = 3410708522401046302L;

		@Override
		public boolean containsKey(Object key) {
			return (super.containsKey(key) || (get(key) != null));
		}

		@Override
		@Nullable
		public String getFirst(String headerName) {
			String value = servletResponse.getHeader(headerName);
			if (value != null) {
				return value;
			}
			else {
				return super.getFirst(headerName);
			}
		}

		@Override
		public List<String> get(Object key) {
			Assert.isInstanceOf(String.class, key, "Key must be a String-based header name");

			Collection<String> values1 = servletResponse.getHeaders((String) key);
			if (headersWritten) {
				return new ArrayList<>(values1);
			}
			boolean isEmpty1 = CollectionUtils.isEmpty(values1);

			List<String> values2 = super.get(key);
			boolean isEmpty2 = CollectionUtils.isEmpty(values2);

			if (isEmpty1 && isEmpty2) {
				return null;
			}

			List<String> values = new ArrayList<>();
			if (!isEmpty1) {
				values.addAll(values1);
			}
			if (!isEmpty2) {
				values.addAll(values2);
			}
			return values;
		}
	}

}
