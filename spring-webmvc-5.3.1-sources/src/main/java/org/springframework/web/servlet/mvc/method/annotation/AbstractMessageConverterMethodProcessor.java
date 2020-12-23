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

package org.springframework.web.servlet.mvc.method.annotation;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * 20201223
 * 通过使用{@link HttpMessageConverter HttpMessageConverters}写入响应，扩展了{@link AbstractMessageConverterMethodArgumentResolver}处理方法返回值的能力。
 */
/**
 * Extends {@link AbstractMessageConverterMethodArgumentResolver} with the ability to handle method
 * return values by writing to the response with {@link HttpMessageConverter HttpMessageConverters}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @author Juergen Hoeller
 * @since 3.1
 */
// 20201223 通过使用{@link HttpMessageConverter HttpMessageConverters}写入响应，扩展了{@link AbstractMessageConverterMethodArgumentResolver}处理方法返回值的能力。
public abstract class AbstractMessageConverterMethodProcessor extends AbstractMessageConverterMethodArgumentResolver implements HandlerMethodReturnValueHandler {

	/* Extensions associated with the built-in message converters */
	private static final Set<String> SAFE_EXTENSIONS = new HashSet<>(Arrays.asList(
			"txt", "text", "yml", "properties", "csv",
			"json", "xml", "atom", "rss",
			"png", "jpe", "jpeg", "jpg", "gif", "wbmp", "bmp"));

	private static final Set<String> SAFE_MEDIA_BASE_TYPES = new HashSet<>(
			Arrays.asList("audio", "image", "video"));

	private static final List<MediaType> ALL_APPLICATION_MEDIA_TYPES =
			Arrays.asList(MediaType.ALL, new MediaType("application"));

	private static final Type RESOURCE_REGION_LIST_TYPE =
			new ParameterizedTypeReference<List<ResourceRegion>>() { }.getType();

	// 20201223 中央类: 用于确定请求的请求{@linkplain MediaType媒体类型}, 还提供了用于查找媒体类型的文件扩展名的方法
	private final ContentNegotiationManager contentNegotiationManager;

	private final Set<String> safeExtensions = new HashSet<>();


	/**
	 * Constructor with list of converters only.
	 */
	protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> converters) {
		this(converters, null, null);
	}

	/**
	 * Constructor with list of converters and ContentNegotiationManager.
	 */
	protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> converters,
			@Nullable ContentNegotiationManager contentNegotiationManager) {

		this(converters, contentNegotiationManager, null);
	}

	/**
	 * Constructor with list of converters and ContentNegotiationManager as well
	 * as request/response body advice instances.
	 */
	protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> converters,
			@Nullable ContentNegotiationManager manager, @Nullable List<Object> requestResponseBodyAdvice) {

		super(converters, requestResponseBodyAdvice);

		this.contentNegotiationManager = (manager != null ? manager : new ContentNegotiationManager());
		this.safeExtensions.addAll(this.contentNegotiationManager.getAllFileExtensions());
		this.safeExtensions.addAll(SAFE_EXTENSIONS);
	}


	/**
	 * Creates a new {@link HttpOutputMessage} from the given {@link NativeWebRequest}.
	 * @param webRequest the web request to create an output message from
	 * @return the output message
	 */
	// 20201223 从给定的{@link NativeWebRequest}创建一个新的{@link HttpOutputMessage}。
	protected ServletServerHttpResponse createOutputMessage(NativeWebRequest webRequest) {
		// 20201223 eg: ResponseFacade@xxxx: Response@xxxx
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		Assert.state(response != null, "No HttpServletResponse");

		// 20201223 eg: ServletServerHttpResponse@xxxx: servletResponse(ResponseFacade@xxxx: Response@xxxx)、headers(ServletServerHttpResponse$ServletResponseHttpHeaders@xxxx: [])
		return new ServletServerHttpResponse(response);
	}

	/**
	 * Writes the given return value to the given web request. Delegates to
	 * {@link #writeWithMessageConverters(Object, MethodParameter, ServletServerHttpRequest, ServletServerHttpResponse)}
	 */
	protected <T> void writeWithMessageConverters(T value, MethodParameter returnType, NativeWebRequest webRequest)
			throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

		ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);
		writeWithMessageConverters(value, returnType, inputMessage, outputMessage);
	}

	/**
	 * Writes the given return type to the given output message.
	 * @param value the value to write to the output message
	 * @param returnType the type of the value
	 * @param inputMessage the input messages. Used to inspect the {@code Accept} header.
	 * @param outputMessage the output message to write to
	 * @throws IOException thrown in case of I/O errors
	 * @throws HttpMediaTypeNotAcceptableException thrown when the conditions indicated
	 * by the {@code Accept} header on the request cannot be met by the message converters
	 * @throws HttpMessageNotWritableException thrown if a given message cannot
	 * be written by a converter, or if the content-type chosen by the server
	 * has no compatible converter.
	 */
	// 20201223 将给定的返回类型写入给定的输出消息。=> eg: 页面输出"Test RestController~~~"
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected <T> void writeWithMessageConverters(@Nullable T value, MethodParameter returnType,
			ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
			throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

		Object body;
		Class<?> valueType;
		Type targetType;

		// 20201223 eg: true
		if (value instanceof CharSequence) {
			// 20201223 eg: "Test RestController~~~"
			body = value.toString();


			valueType = String.class;
			targetType = String.class;
		}
		else {
			body = value;
			valueType = getReturnValueType(body, returnType);
			targetType = GenericTypeResolver.resolveType(getGenericType(returnType), returnType.getContainingClass());
		}

		// 20201223 eg: false
		if (isResourceType(value, returnType)) {
			outputMessage.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");
			if (value != null && inputMessage.getHeaders().getFirst(HttpHeaders.RANGE) != null &&
					outputMessage.getServletResponse().getStatus() == 200) {
				Resource resource = (Resource) value;
				try {
					List<HttpRange> httpRanges = inputMessage.getHeaders().getRange();
					outputMessage.getServletResponse().setStatus(HttpStatus.PARTIAL_CONTENT.value());
					body = HttpRange.toResourceRegions(httpRanges, resource);
					valueType = body.getClass();
					targetType = RESOURCE_REGION_LIST_TYPE;
				}
				catch (IllegalArgumentException ex) {
					outputMessage.getHeaders().set(HttpHeaders.CONTENT_RANGE, "bytes */" + resource.contentLength());
					outputMessage.getServletResponse().setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
				}
			}
		}

		MediaType selectedMediaType = null;

		// 20201223 返回正文的{@linkplain MediaType媒体类型}，如{@code Content-Type}标头所指定 => eg: null
		MediaType contentType = outputMessage.getHeaders().getContentType();

		// 20201223 eg: false
		boolean isContentTypePreset = contentType != null && contentType.isConcrete();
		if (isContentTypePreset) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found 'Content-Type:" + contentType + "' in response");
			}
			selectedMediaType = contentType;
		}
		else {
			// 20201223 eg: RequestFacade@xxxx: Request@xxxx
			HttpServletRequest request = inputMessage.getServletRequest();

			// 20201223 获取逗号分割后的"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
			List<MediaType> acceptableTypes = getAcceptableMediaTypes(request);

			// 20201223 返回支持的MediaType列表
			List<MediaType> producibleTypes = getProducibleMediaTypes(request, valueType, targetType);

			// 20201223 eg: false
			if (body != null && producibleTypes.isEmpty()) {
				throw new HttpMessageNotWritableException(
						"No converter found for return value of type: " + valueType);
			}

			// 20201223 排序后添加的兼容的MediaType列表
			List<MediaType> mediaTypesToUse = new ArrayList<>();

			// 20201223 eg: 逗号分割后的"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
			for (MediaType requestedType : acceptableTypes) {
				// 20201223 遍历支持的MediaType列表
				for (MediaType producibleType : producibleTypes) {
					// 20201223 指示此{@code MediaType}是否与给定的媒体类型兼容。
					if (requestedType.isCompatibleWith(producibleType)) {
						// 20201223 排序后添加到兼容的MediaType列表里
						mediaTypesToUse.add(getMostSpecificMediaType(requestedType, producibleType));
					}
				}
			}

			// 20201223 eg: size = 20
			if (mediaTypesToUse.isEmpty()) {
				if (body != null) {
					throw new HttpMediaTypeNotAcceptableException(producibleTypes);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("No match for " + acceptableTypes + ", supported: " + producibleTypes);
				}
				return;
			}

			// 20201223 按特定性将给定的{@code MediaType}对象列表分类为主要标准，次要质量为值。
			MediaType.sortBySpecificityAndQuality(mediaTypesToUse);

			for (MediaType mediaType : mediaTypesToUse) {
				// 20201223 指示此MIME类型是否具体，即该类型或子类型是否都不是通配符*。
				if (mediaType.isConcrete()) {
					selectedMediaType = mediaType;
					break;
				}
				else if (mediaType.isPresentIn(ALL_APPLICATION_MEDIA_TYPES)) {
					selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
					break;
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Using '" + selectedMediaType + "', given " +
						acceptableTypes + " and supported " + producibleTypes);
			}
		}

		// 20201222 eg: MediaType@xxxx: "text/html"
		if (selectedMediaType != null) {
			// 20201223 eg: MediaType@xxxx: "text/html"
			selectedMediaType = selectedMediaType.removeQualityValue();

			// 20201223 ArrayList@xxxx: size = 10
			for (HttpMessageConverter<?> converter : this.messageConverters) {
				// 20201223 eg: null
				GenericHttpMessageConverter genericConverter = (converter instanceof GenericHttpMessageConverter ?
						(GenericHttpMessageConverter<?>) converter : null);

				// 20201223 eg: null
				if (genericConverter != null ?
						((GenericHttpMessageConverter) converter).canWrite(targetType, valueType, selectedMediaType) :
						converter.canWrite(valueType, selectedMediaType)) {
					body = getAdvice().beforeBodyWrite(body, returnType, selectedMediaType,
							(Class<? extends HttpMessageConverter<?>>) converter.getClass(),
							inputMessage, outputMessage);

					// 202012223 eg: "Test RestController~~~"
					if (body != null) {
						Object theBody = body;
						LogFormatUtils.traceDebug(logger, traceOn ->
								"Writing [" + LogFormatUtils.formatValue(theBody, !traceOn) + "]");

						// 20201223 检查路径是否具有文件扩展名，以及该扩展名是否在{@link #SAFE_EXTENSIONS安全扩展名}列表中或是否明确显示在ContentNegotiationManager中已注册
						addContentDispositionHeader(inputMessage, outputMessage);

						// 20201223 eg: null
						if (genericConverter != null) {
							genericConverter.write(body, targetType, selectedMediaType, outputMessage);
						}
						else {
							// 20201223 此实现通过调用{@link #addDefaultHeaders}设置默认标头，然后调用{@link #writeInternal} => eg: 页面输出"Test RestController~~~"
							((HttpMessageConverter) converter).write(body, selectedMediaType, outputMessage);
						}
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Nothing to write: null body");
						}
					}
					return;
				}
			}
		}

		if (body != null) {
			Set<MediaType> producibleMediaTypes =
					(Set<MediaType>) inputMessage.getServletRequest()
							.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

			if (isContentTypePreset || !CollectionUtils.isEmpty(producibleMediaTypes)) {
				throw new HttpMessageNotWritableException(
						"No converter for [" + valueType + "] with preset Content-Type '" + contentType + "'");
			}
			throw new HttpMediaTypeNotAcceptableException(this.allSupportedMediaTypes);
		}
	}

	/**
	 * Return the type of the value to be written to the response. Typically this is
	 * a simple check via getClass on the value but if the value is null, then the
	 * return type needs to be examined possibly including generic type determination
	 * (e.g. {@code ResponseEntity<T>}).
	 */
	protected Class<?> getReturnValueType(@Nullable Object value, MethodParameter returnType) {
		return (value != null ? value.getClass() : returnType.getParameterType());
	}

	/**
	 * Return whether the returned value or the declared return type extends {@link Resource}.
	 */
	// 20201223 返回返回值或声明的返回类型是否扩展{@link Resource}。
	protected boolean isResourceType(@Nullable Object value, MethodParameter returnType) {
		Class<?> clazz = getReturnValueType(value, returnType);
		return clazz != InputStreamResource.class && Resource.class.isAssignableFrom(clazz);
	}

	/**
	 * Return the generic type of the {@code returnType} (or of the nested type
	 * if it is an {@link HttpEntity}).
	 */
	private Type getGenericType(MethodParameter returnType) {
		if (HttpEntity.class.isAssignableFrom(returnType.getParameterType())) {
			return ResolvableType.forType(returnType.getGenericParameterType()).getGeneric().getType();
		}
		else {
			return returnType.getGenericParameterType();
		}
	}

	/**
	 * Returns the media types that can be produced.
	 * @see #getProducibleMediaTypes(HttpServletRequest, Class, Type)
	 */
	@SuppressWarnings("unused")
	protected List<MediaType> getProducibleMediaTypes(HttpServletRequest request, Class<?> valueClass) {
		return getProducibleMediaTypes(request, valueClass, null);
	}

	/**
	 * 20201223
	 * A. 返回可以产生的媒体类型。 产生的媒体类型为：
	 * 		a. 请求映射中指定的可生产媒体类型，或
	 * 		b. 可以写入特定返回值的已配置转换器的媒体类型，或者
	 * 		c. {@link MediaType #ALL} "*//*"
	 */
	/**
	 * A.
	 * Returns the media types that can be produced. The resulting media types are:
	 * <ul>
	 * a.
	 * <li>The producible media types specified in the request mappings, or
	 *
	 * b.
	 * <li>Media types of configured converters that can write the specific return value, or
	 *
	 * c.
	 * <li>{@link MediaType#ALL}
	 * </ul>
	 * @since 4.2
	 */
	// 20201223 返回可以产生的媒体类型
	@SuppressWarnings("unchecked")
	protected List<MediaType> getProducibleMediaTypes(HttpServletRequest request, Class<?> valueClass, @Nullable Type targetType) {
		// 20201223 eg: "org.springframework.web.servlet.HandlerMapping.producibleMediaTypes"-null
		Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
		if (!CollectionUtils.isEmpty(mediaTypes)) {
			return new ArrayList<>(mediaTypes);
		}

		// 20201223 eg: Collections$UnmodifiableRandomAccessList@xxxx: size = 11
		else if (!this.allSupportedMediaTypes.isEmpty()) {
			List<MediaType> result = new ArrayList<>();
			// 20201223 eg: size = 11: StringHttpMessageConverter@xxxx...
			for (HttpMessageConverter<?> converter : this.messageConverters) {
				if (converter instanceof GenericHttpMessageConverter && targetType != null) {
					if (((GenericHttpMessageConverter<?>) converter).canWrite(targetType, valueClass, null)) {
						result.addAll(converter.getSupportedMediaTypes());
					}
				}
				else if (converter.canWrite(valueClass, null)) {
					result.addAll(converter.getSupportedMediaTypes());
				}
			}

			// 20201223 返回支持的MediaType列表
			return result;
		}
		else {
			return Collections.singletonList(MediaType.ALL);
		}
	}

	// 20201223 获取逗号分割后的"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
	private List<MediaType> getAcceptableMediaTypes(HttpServletRequest request) throws HttpMediaTypeNotAcceptableException {
		// 20201223 eg: ContentNegotiationManager@xxxx: strategies: HeaderContentNegotitationStrategy@xxxx, resolvers: MappingMeidaTypeFileExtensionResovler@xxxx
		return this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
	}

	/**
	 * Return the more specific of the acceptable and the producible media types
	 * with the q-value of the former.
	 */
	// 20201223 使用前者的q值返回更具体的可接受和可生产的媒体类型。
	private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
		MediaType produceTypeToUse = produceType.copyQualityValue(acceptType);
		return (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceTypeToUse) <= 0 ? acceptType : produceTypeToUse);
	}

	/**
	 * 20201223
	 * 检查路径是否具有文件扩展名，以及该扩展名是否在{@link #SAFE_EXTENSIONS安全扩展名}列表中，或是否明确显示在
	 * {@link ContentNegotiationManager＃getAllFileExtensions（）}中已注册。 如果不是，并且状态在2xx范围内，则会添加带有安全附件文件名（“ f.txt”）的
	 * “ Content-Disposition”标头，以防止RFD攻击。
	 */
	/**
	 * Check if the path has a file extension and whether the extension is either
	 * on the list of {@link #SAFE_EXTENSIONS safe extensions} or explicitly
	 * {@link ContentNegotiationManager#getAllFileExtensions() registered}.
	 * If not, and the status is in the 2xx range, a 'Content-Disposition'
	 * header with a safe attachment file name ("f.txt") is added to prevent
	 * RFD exploits.
	 */
	// 20201223 检查路径是否具有文件扩展名，以及该扩展名是否在{@link #SAFE_EXTENSIONS安全扩展名}列表中或是否明确显示在ContentNegotiationManager中已注册
	private void addContentDispositionHeader(ServletServerHttpRequest request, ServletServerHttpResponse response) {
		// 20201223 eg: ServletServerHttpResponse$ServletResponseHttpHeaders@xxxx
		HttpHeaders headers = response.getHeaders();

		// 20201223 eg: "Content-Disposition" => false
		if (headers.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
			return;
		}

		try {
			// 20201223 eg: 200
			int status = response.getServletResponse().getStatus();
			if (status < 200 || (status > 299 && status < 400)) {
				return;
			}
		}
		catch (Throwable ex) {
			// ignore
		}

		// 20201223 eg: RequestFacade@xxxx: Request@xxxx
		HttpServletRequest servletRequest = request.getServletRequest();

		// 20201223 "/testController/testRestController"
		String requestUri = UrlPathHelper.rawPathInstance.getOriginatingRequestUri(servletRequest);

		int index = requestUri.lastIndexOf('/') + 1;
		String filename = requestUri.substring(index);
		String pathParams = "";

		index = filename.indexOf(';');
		if (index != -1) {
			pathParams = filename.substring(index);
			filename = filename.substring(0, index);
		}

		// 20201223 eg: "testRestController"
		filename = UrlPathHelper.defaultInstance.decodeRequestString(servletRequest, filename);

		// 20201223 eg: null
		String ext = StringUtils.getFilenameExtension(filename);

		// 20201223 eg: ""
		pathParams = UrlPathHelper.defaultInstance.decodeRequestString(servletRequest, pathParams);

		// 20201223 eg: null
		String extInPathParams = StringUtils.getFilenameExtension(pathParams);

		// 20201223 eg: !true || !true  => false
		if (!safeExtension(servletRequest, ext) || !safeExtension(servletRequest, extInPathParams)) {
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=f.txt");
		}
	}

	@SuppressWarnings("unchecked")
	private boolean safeExtension(HttpServletRequest request, @Nullable String extension) {
		if (!StringUtils.hasText(extension)) {
			return true;
		}
		extension = extension.toLowerCase(Locale.ENGLISH);
		if (this.safeExtensions.contains(extension)) {
			return true;
		}
		String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		if (pattern != null && pattern.endsWith("." + extension)) {
			return true;
		}
		if (extension.equals("html")) {
			String name = HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE;
			Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(name);
			if (!CollectionUtils.isEmpty(mediaTypes) && mediaTypes.contains(MediaType.TEXT_HTML)) {
				return true;
			}
		}
		MediaType mediaType = resolveMediaType(request, extension);
		return (mediaType != null && (safeMediaType(mediaType)));
	}

	@Nullable
	private MediaType resolveMediaType(ServletRequest request, String extension) {
		MediaType result = null;
		String rawMimeType = request.getServletContext().getMimeType("file." + extension);
		if (StringUtils.hasText(rawMimeType)) {
			result = MediaType.parseMediaType(rawMimeType);
		}
		if (result == null || MediaType.APPLICATION_OCTET_STREAM.equals(result)) {
			result = MediaTypeFactory.getMediaType("file." + extension).orElse(null);
		}
		return result;
	}

	private boolean safeMediaType(MediaType mediaType) {
		return (SAFE_MEDIA_BASE_TYPES.contains(mediaType.getType()) ||
				mediaType.getSubtype().endsWith("+xml"));
	}

}
