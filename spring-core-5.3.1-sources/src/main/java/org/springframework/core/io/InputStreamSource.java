/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 20201203
 * A. 作为{@link InputStream}源的对象的简单接口。
 * B. 这是Spring更广泛的{@link Resource}接口的基本接口。
 * C. 对于一次性流，{@link InputStreamResource}可以用于任何给定的{@code InputStream}。Spring的{@link bytearrayresource}或任何基于文件的{@code Resource}实现
 *    都可以作为一个具体实例使用，允许用户多次读取底层内容流。例如，这使得该接口作为邮件附件的抽象内容源非常有用。
 */
/**
 * A.
 * Simple interface for objects that are sources for an {@link InputStream}.
 *
 * B.
 * <p>This is the base interface for Spring's more extensive {@link Resource} interface.
 *
 * C.
 * <p>For single-use streams, {@link InputStreamResource} can be used for any
 * given {@code InputStream}. Spring's {@link ByteArrayResource} or any
 * file-based {@code Resource} implementation can be used as a concrete
 * instance, allowing one to read the underlying content stream multiple times.
 * This makes this interface useful as an abstract content source for mail
 * attachments, for example.
 *
 * @author Juergen Hoeller
 * @since 20.01.2004
 * @see InputStream
 * @see Resource
 * @see InputStreamResource
 * @see ByteArrayResource
 */
// 20201203 作为{@link InputStream}源的对象的简单接口。任何基于文件的{@code Resource}实现都可以作为一个具体实例使用，允许用户多次读取底层内容流。
public interface InputStreamSource {

	/**
	 * Return an {@link InputStream} for the content of an underlying resource.
	 * <p>It is expected that each call creates a <i>fresh</i> stream.
	 * <p>This requirement is particularly important when you consider an API such
	 * as JavaMail, which needs to be able to read the stream multiple times when
	 * creating mail attachments. For such a use case, it is <i>required</i>
	 * that each {@code getInputStream()} call returns a fresh stream.
	 * @return the input stream for the underlying resource (must not be {@code null})
	 * @throws java.io.FileNotFoundException if the underlying resource doesn't exist
	 * @throws IOException if the content stream could not be opened
	 */
	InputStream getInputStream() throws IOException;

}
