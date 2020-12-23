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

package org.springframework.http.converter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

/**
 * 20201223
 * A. {@link HttpMessageConverter}的实现，可以读取和写入字符串。
 * B. 默认情况下，此转换器支持所有媒体类型（*），并使用{@code text / plain}的{@code Content-Type}进行写入。 可以通过设置
 *    {@link #setSupportedMediaTypessupportedMediaTypes}属性来覆盖此属性。
 */
/**
 * A.
 * Implementation of {@link HttpMessageConverter} that can read and write strings.
 *
 * B.
 * <p>By default, this converter supports all media types (<code>&#42;/&#42;</code>),
 * and writes with a {@code Content-Type} of {@code text/plain}. This can be overridden
 * by setting the {@link #setSupportedMediaTypes supportedMediaTypes} property.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
// 20201223 {@link HttpMessageConverter}的实现，可以读取和写入字符串: 默认情况下，此转换器支持所有媒体类型（*），并使用{@code text / plain}的{@code Content-Type}进行写入
public class StringHttpMessageConverter extends AbstractHttpMessageConverter<String> {

	private static final MediaType APPLICATION_PLUS_JSON = new MediaType("application", "*+json");

	/**
	 * The default charset used by the converter.
	 */
	public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;


	@Nullable
	private volatile List<Charset> availableCharsets;

	// 20201223 是否接入接受的字符集, 默认为false
	private boolean writeAcceptCharset = false;

	/**
	 * A default constructor that uses {@code "ISO-8859-1"} as the default charset.
	 * @see #StringHttpMessageConverter(Charset)
	 */
	public StringHttpMessageConverter() {
		this(DEFAULT_CHARSET);
	}

	/**
	 * A constructor accepting a default charset to use if the requested content
	 * type does not specify one.
	 */
	public StringHttpMessageConverter(Charset defaultCharset) {
		super(defaultCharset, MediaType.TEXT_PLAIN, MediaType.ALL);
	}


	/**
	 * Whether the {@code Accept-Charset} header should be written to any outgoing
	 * request sourced from the value of {@link Charset#availableCharsets()}.
	 * The behavior is suppressed if the header has already been set.
	 * <p>As of 5.2, by default is set to {@code false}.
	 */
	public void setWriteAcceptCharset(boolean writeAcceptCharset) {
		this.writeAcceptCharset = writeAcceptCharset;
	}


	@Override
	public boolean supports(Class<?> clazz) {
		return String.class == clazz;
	}

	@Override
	protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException {
		Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
		return StreamUtils.copyToString(inputMessage.getBody(), charset);
	}

	// 20201223 返回给定类型的内容长度
	@Override
	protected Long getContentLength(String str, @Nullable MediaType contentType) {
		// 20201223 UTF_8@xxxx: "UTF-8"
		Charset charset = getContentTypeCharset(contentType);
		return (long) str.getBytes(charset).length;
	}

	// 20201223 将默认标题添加到输出消息
	@Override
	protected void addDefaultHeaders(HttpHeaders headers, String s, @Nullable MediaType type) throws IOException {
		// 20201223 eg: null
		if (headers.getContentType() == null ) {
			// 20201223 type: MediaType@xxx: "text/html" => eg: true && true && false || false
			if (type != null && type.isConcrete() &&
					// 20201223 eg: "application/json"
					(type.isCompatibleWith(MediaType.APPLICATION_JSON) ||

					// 20201223 eg: "application/*+json"
					type.isCompatibleWith(APPLICATION_PLUS_JSON))) {
				// Prevent charset parameter for JSON..
				headers.setContentType(type);
			}
		}
		super.addDefaultHeaders(headers, s, type);
	}

	// 20201223 编写实际正文的抽象模板方法。 从{@link #write}调用 => eg: 页面输出"Test RestController~~~"
	@Override
	protected void writeInternal(String str, HttpOutputMessage outputMessage) throws IOException {
		// 20201223 eg: ServletServerHttpResponse$ServletReponseHttpHeaders@xxxx: "Content-Type": "text/html;charset=UTF-8"、"Content-Length": "22"
		HttpHeaders headers = outputMessage.getHeaders();

		// 20201223 eg: false && true(null == null) => false
		if (this.writeAcceptCharset && headers.get(HttpHeaders.ACCEPT_CHARSET) == null) {
			headers.setAcceptCharset(getAcceptedCharsets());
		}

		// 20201223 获取ContentType的字符集 => eg: "UTF-8"
		Charset charset = getContentTypeCharset(headers.getContentType());

		// 20201223 将给定String的内容复制到给定OutputStream, 完成后，使流保持打开状态 => eg: 页面输出"Test RestController~~~"
		StreamUtils.copy(
				// 20201223 eg: "Test RestController~~~"
				str,

				// 20201223 eg: "UTF-8"
				charset,

				// 20201223 eg: CoyoteOutputStream@xxxx
				outputMessage.getBody()
		);
	}


	/**
	 * Return the list of supported {@link Charset Charsets}.
	 * <p>By default, returns {@link Charset#availableCharsets()}.
	 * Can be overridden in subclasses.
	 * @return the list of accepted charsets
	 */
	protected List<Charset> getAcceptedCharsets() {
		List<Charset> charsets = this.availableCharsets;
		if (charsets == null) {
			charsets = new ArrayList<>(Charset.availableCharsets().values());
			this.availableCharsets = charsets;
		}
		return charsets;
	}

	// 20201223 获取ContentType的字符集
	private Charset getContentTypeCharset(@Nullable MediaType contentType) {
		if (contentType != null) {
			Charset charset = contentType.getCharset();
			if (charset != null) {
				return charset;
			}
			else if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON) ||
					contentType.isCompatibleWith(APPLICATION_PLUS_JSON)) {
				// Matching to AbstractJackson2HttpMessageConverter#DEFAULT_CHARSET
				return StandardCharsets.UTF_8;
			}
		}
		Charset charset = getDefaultCharset();
		Assert.state(charset != null, "No default charset");
		return charset;
	}

}
