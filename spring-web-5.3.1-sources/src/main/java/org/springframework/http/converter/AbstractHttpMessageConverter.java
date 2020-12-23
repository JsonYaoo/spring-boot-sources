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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpLogging;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 20201223
 * A. 大多数{@link HttpMessageConverter}实现的抽象基类。
 * B. 该基类通过{@link #setSupportedMediaTypes（List）supportedMediaTypes} bean属性添加了对设置受支持的{@code MediaTypes}的支持。 在写入输出消息时，
 *    它还增加了对{@code Content-Type}和{@code Content-Length}的支持。
 */
/**
 * A.
 * Abstract base class for most {@link HttpMessageConverter} implementations.
 *
 * B.
 * <p>This base class adds support for setting supported {@code MediaTypes}, through the
 * {@link #setSupportedMediaTypes(List) supportedMediaTypes} bean property. It also adds
 * support for {@code Content-Type} and {@code Content-Length} when writing to output messages.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 3.0
 * @param <T> the converted object type
 */
// 20201223 大多数{@link HttpMessageConverter}实现的抽象基类: 添加了对设置受支持的{@code MediaTypes}的支持, 还增加了对{@code Content-Type}和{@code Content-Length}的支持。
public abstract class AbstractHttpMessageConverter<T> implements HttpMessageConverter<T> {

	/** Logger available to subclasses. */
	protected final Log logger = HttpLogging.forLogName(getClass());

	private List<MediaType> supportedMediaTypes = Collections.emptyList();

	@Nullable
	private Charset defaultCharset;


	/**
	 * Construct an {@code AbstractHttpMessageConverter} with no supported media types.
	 * @see #setSupportedMediaTypes
	 */
	protected AbstractHttpMessageConverter() {
	}

	/**
	 * Construct an {@code AbstractHttpMessageConverter} with one supported media type.
	 * @param supportedMediaType the supported media type
	 */
	protected AbstractHttpMessageConverter(MediaType supportedMediaType) {
		setSupportedMediaTypes(Collections.singletonList(supportedMediaType));
	}

	/**
	 * Construct an {@code AbstractHttpMessageConverter} with multiple supported media types.
	 * @param supportedMediaTypes the supported media types
	 */
	protected AbstractHttpMessageConverter(MediaType... supportedMediaTypes) {
		setSupportedMediaTypes(Arrays.asList(supportedMediaTypes));
	}

	/**
	 * Construct an {@code AbstractHttpMessageConverter} with a default charset and
	 * multiple supported media types.
	 * @param defaultCharset the default character set
	 * @param supportedMediaTypes the supported media types
	 * @since 4.3
	 */
	protected AbstractHttpMessageConverter(Charset defaultCharset, MediaType... supportedMediaTypes) {
		this.defaultCharset = defaultCharset;
		setSupportedMediaTypes(Arrays.asList(supportedMediaTypes));
	}


	/**
	 * Set the list of {@link MediaType} objects supported by this converter.
	 */
	public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
		Assert.notEmpty(supportedMediaTypes, "MediaType List must not be empty");
		this.supportedMediaTypes = new ArrayList<>(supportedMediaTypes);
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return Collections.unmodifiableList(this.supportedMediaTypes);
	}

	/**
	 * Set the default character set, if any.
	 * @since 4.3
	 */
	public void setDefaultCharset(@Nullable Charset defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	/**
	 * Return the default character set, if any.
	 * @since 4.3
	 */
	@Nullable
	public Charset getDefaultCharset() {
		return this.defaultCharset;
	}


	/**
	 * This implementation checks if the given class is {@linkplain #supports(Class) supported},
	 * and if the {@linkplain #getSupportedMediaTypes() supported media types}
	 * {@linkplain MediaType#includes(MediaType) include} the given media type.
	 */
	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return supports(clazz) && canRead(mediaType);
	}

	/**
	 * Returns {@code true} if any of the {@linkplain #setSupportedMediaTypes(List)
	 * supported} media types {@link MediaType#includes(MediaType) include} the
	 * given media type.
	 * @param mediaType the media type to read, can be {@code null} if not specified.
	 * Typically the value of a {@code Content-Type} header.
	 * @return {@code true} if the supported media types include the media type,
	 * or if the media type is {@code null}
	 */
	protected boolean canRead(@Nullable MediaType mediaType) {
		if (mediaType == null) {
			return true;
		}
		for (MediaType supportedMediaType : getSupportedMediaTypes()) {
			if (supportedMediaType.includes(mediaType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This implementation checks if the given class is
	 * {@linkplain #supports(Class) supported}, and if the
	 * {@linkplain #getSupportedMediaTypes() supported} media types
	 * {@linkplain MediaType#includes(MediaType) include} the given media type.
	 */
	@Override
	public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
		return supports(clazz) && canWrite(mediaType);
	}

	/**
	 * Returns {@code true} if the given media type includes any of the
	 * {@linkplain #setSupportedMediaTypes(List) supported media types}.
	 * @param mediaType the media type to write, can be {@code null} if not specified.
	 * Typically the value of an {@code Accept} header.
	 * @return {@code true} if the supported media types are compatible with the media type,
	 * or if the media type is {@code null}
	 */
	protected boolean canWrite(@Nullable MediaType mediaType) {
		if (mediaType == null || MediaType.ALL.equalsTypeAndSubtype(mediaType)) {
			return true;
		}
		for (MediaType supportedMediaType : getSupportedMediaTypes()) {
			if (supportedMediaType.isCompatibleWith(mediaType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This implementation simple delegates to {@link #readInternal(Class, HttpInputMessage)}.
	 * Future implementations might add some default behavior, however.
	 */
	@Override
	public final T read(Class<? extends T> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {

		return readInternal(clazz, inputMessage);
	}

	/**
	 * This implementation sets the default headers by calling {@link #addDefaultHeaders},
	 * and then calls {@link #writeInternal}.
	 */
	// 20201223 此实现通过调用{@link #addDefaultHeaders}设置默认标头，然后调用{@link #writeInternal} => eg: 页面输出"Test RestController~~~"
	@Override
	public final void write(final T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		// 20201223 ServletServerHttpResponse$ServletResponseHttpHeaders@xxxx: []
		final HttpHeaders headers = outputMessage.getHeaders();

		// 20201223 将默认标题添加到输出消息 => eg: ContentType: "text/html;charset=UTF-8", "Content-Length": 22
		addDefaultHeaders(headers, t, contentType);

		// 20201223 eg: false
		if (outputMessage instanceof StreamingHttpOutputMessage) {
			StreamingHttpOutputMessage streamingOutputMessage = (StreamingHttpOutputMessage) outputMessage;
			streamingOutputMessage.setBody(outputStream -> writeInternal(t, new HttpOutputMessage() {
				@Override
				public OutputStream getBody() {
					return outputStream;
				}
				@Override
				public HttpHeaders getHeaders() {
					return headers;
				}
			}));
		}
		else {
			// 20201223 "Test RestController~~~", ServletServerHttpResponse@xxxx: Response@xxxx, ServletServerHttpResponse$ServletResponseHttpHeaders@xxxx
			// 20201223 编写实际正文的抽象模板方法。 从{@link #write}调用 => eg: 页面输出"Test RestController~~~"
			writeInternal(t, outputMessage);

			// 20201223 将缓冲区发送给客户端。
			outputMessage.getBody().flush();
		}
	}

	/**
	 * 20201223
	 * A. 将默认标题添加到输出消息。
	 * B. 如果未提供内容类型，则此实现委派给{@link #getDefaultContentType（Object）}，并在必要时设置默认字符集，调用{@link #getContentLength}，并设置相应的标头。
	 */
	/**
	 * A.
	 * Add default headers to the output message.
	 *
	 * B.
	 * <p>This implementation delegates to {@link #getDefaultContentType(Object)} if a
	 * content type was not provided, set if necessary the default character set, calls
	 * {@link #getContentLength}, and sets the corresponding headers.
	 * @since 4.2
	 */
	// 20201223 将默认标题添加到输出消息 => eg: ContentType: "text/html;charset=UTF-8", "Content-Length": 22
	protected void addDefaultHeaders(HttpHeaders headers, T t, @Nullable MediaType contentType) throws IOException {
		if (headers.getContentType() == null) {
			MediaType contentTypeToUse = contentType;
			if (contentType == null || !contentType.isConcrete()) {
				contentTypeToUse = getDefaultContentType(t);
			}
			else if (MediaType.APPLICATION_OCTET_STREAM.equals(contentType)) {
				MediaType mediaType = getDefaultContentType(t);
				contentTypeToUse = (mediaType != null ? mediaType : contentTypeToUse);
			}
			if (contentTypeToUse != null) {
				if (contentTypeToUse.getCharset() == null) {
					// 20201223 eg: "UTF-8"
					Charset defaultCharset = getDefaultCharset();
					if (defaultCharset != null) {
						contentTypeToUse = new MediaType(contentTypeToUse, defaultCharset);
					}
				}
				// 20201223 MediaType@xxxx: "text/html;charset=UTF-8"
				headers.setContentType(contentTypeToUse);
			}
		}

		// 20201223 eg: -1, "Transfer-Encoding" => true && !false => true
		if (headers.getContentLength() < 0 && !headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
			// 20201223 eg: 22
			Long contentLength = getContentLength(t, headers.getContentType());
			if (contentLength != null) {
				// 20201223 按照{@code Content-Length}标头指定的格式设置正文长度（以字节为单位）: "Content-Length": 22
				headers.setContentLength(contentLength);
			}
		}
	}

	/**
	 * Returns the default content type for the given type. Called when {@link #write}
	 * is invoked without a specified content type parameter.
	 * <p>By default, this returns the first element of the
	 * {@link #setSupportedMediaTypes(List) supportedMediaTypes} property, if any.
	 * Can be overridden in subclasses.
	 * @param t the type to return the content type for
	 * @return the content type, or {@code null} if not known
	 */
	@Nullable
	protected MediaType getDefaultContentType(T t) throws IOException {
		List<MediaType> mediaTypes = getSupportedMediaTypes();
		return (!mediaTypes.isEmpty() ? mediaTypes.get(0) : null);
	}

	/**
	 * 20201223
	 * A. 返回给定类型的内容长度。
	 * B. 默认情况下，它返回{@code null}，表示内容长度未知。 可以在子类中覆盖。
	 */
	/**
	 * A.
	 * Returns the content length for the given type.
	 *
	 * B.
	 * <p>By default, this returns {@code null}, meaning that the content length is unknown.
	 * Can be overridden in subclasses.
	 *
	 * @param t the type to return the content length for
	 * @return the content length, or {@code null} if not known
	 */
	// 20201223 返回给定类型的内容长度
	@Nullable
	protected Long getContentLength(T t, @Nullable MediaType contentType) throws IOException {
		return null;
	}

	/**
	 * Indicates whether the given class is supported by this converter.
	 * @param clazz the class to test for support
	 * @return {@code true} if supported; {@code false} otherwise
	 */
	protected abstract boolean supports(Class<?> clazz);

	/**
	 * Abstract template method that reads the actual object. Invoked from {@link #read}.
	 * @param clazz the type of object to return
	 * @param inputMessage the HTTP input message to read from
	 * @return the converted object
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotReadableException in case of conversion errors
	 */
	protected abstract T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException;

	/**
	 * Abstract template method that writes the actual body. Invoked from {@link #write}.
	 * @param t the object to write to the output message
	 * @param outputMessage the HTTP output message to write to
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotWritableException in case of conversion errors
	 */
	// 20201223 编写实际正文的抽象模板方法。 从{@link #write}调用。
	protected abstract void writeInternal(T t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException;

}
