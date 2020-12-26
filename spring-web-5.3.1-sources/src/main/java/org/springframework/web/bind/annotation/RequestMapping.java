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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 20201225
 * A. 用于将Web请求映射到具有灵活方法签名的请求处理类中的方法的注释。
 * B. Spring MVC和Spring WebFlux都通过各自模块和包结构中的{@code RequestMappingHandlerMapping}和{@code RequestMappingHandlerAdapter}支持此注释。
 *    有关每个中支持的处理程序方法参数和返回类型的确切列表，请使用下面的参考文档链接：
 *    	a. Spring MVC: <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-arguments">方法参数</a>
 *    	   和<a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-return-types">返回值</a>
 *    	b. Spring WebFlux: <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-ann-arguments">方法参数</a>
 *    	   和<a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-ann-return-types">返回值</a>
 * C. 注意：此注释可以在类和方法级别上使用。 在大多数情况下，在方法级别，应用程序倾向于使用特定于HTTP方法的变体之一{@link GetMapping @GetMapping}，
 *    {@link PostMapping @PostMapping}，{@link PutMapping @PutMapping}，{@link DeleteMapping @DeleteMapping }或{@link PatchMapping @PatchMapping}。
 * D. 注意：使用控制器接口时（例如，用于AOP代理），请确保将所有映射注释（例如{@code @RequestMapping}和{@code @SessionAttributes}）一致地放置在控制器接口上，而不是在实现类上。
 */
/**
 * A.
 * Annotation for mapping web requests onto methods in request-handling classes
 * with flexible method signatures.
 *
 * B.
 * <p>Both Spring MVC and Spring WebFlux support this annotation through a
 * {@code RequestMappingHandlerMapping} and {@code RequestMappingHandlerAdapter}
 * in their respective modules and package structure. For the exact list of
 * supported handler method arguments and return types in each, please use the
 * reference documentation links below:
 * <ul>
 * a.
 * <li>Spring MVC
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-arguments">Method Arguments</a>
 * and
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-return-types">Return Values</a>
 * </li>
 *
 * b.
 * <li>Spring WebFlux
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-ann-arguments">Method Arguments</a>
 * and
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-ann-return-types">Return Values</a>
 * </li>
 * </ul>
 *
 * C.
 * <p><strong>Note:</strong> This annotation can be used both at the class and
 * at the method level. In most cases, at the method level applications will
 * prefer to use one of the HTTP method specific variants
 * {@link GetMapping @GetMapping}, {@link PostMapping @PostMapping},
 * {@link PutMapping @PutMapping}, {@link DeleteMapping @DeleteMapping}, or
 * {@link PatchMapping @PatchMapping}.</p>
 *
 * D.
 * <p><b>NOTE:</b> When using controller interfaces (e.g. for AOP proxying),
 * make sure to consistently put <i>all</i> your mapping annotations - such as
 * {@code @RequestMapping} and {@code @SessionAttributes} - on
 * the controller <i>interface</i> rather than on the implementation class.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @author Sam Brannen
 * @since 2.5
 * @see GetMapping
 * @see PostMapping
 * @see PutMapping
 * @see DeleteMapping
 * @see PatchMapping
 */
// 20201225 用于将Web请求映射到具有灵活方法签名的请求处理类中的方法的注释
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface RequestMapping {

	/**
	 * 20201225
	 * A. 为该映射分配名称。
	 * B. 在类型级别和方法级别都受支持！ 当在两个级别上使用时，组合名称是通过以“＃”作为分隔符的串联而派生的。
	 */
	/**
	 * A.
	 * Assign a name to this mapping.
	 *
	 * B.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used on both levels, a combined name is derived by concatenation
	 * with "#" as separator.
	 *
	 * @see org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
	 * @see org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy
	 */
	// 20201225 为该映射分配名称
	String name() default "";

	/**
	 * 20201225
	 * A. 此注释表示的主要映射。
	 * B. 这是{@link #path}的别名。 例如，{@code @RequestMapping（“ / foo”）}等效于{@code @RequestMapping（path =“ / foo”）}。
	 * C. 在类型级别和方法级别都受支持！ 当在类型级别使用时，所有方法级别的映射都继承此主映射，从而将其缩小为特定的处理程序方法。
	 * D. 注意：未显式映射到任何路径的处理程序方法将有效地映射到空路径。
	 */
	/**
	 * A.
	 * The primary mapping expressed by this annotation.
	 *
	 * B.
	 * <p>This is an alias for {@link #path}. For example,
	 * {@code @RequestMapping("/foo")} is equivalent to
	 * {@code @RequestMapping(path="/foo")}.
	 *
	 * C.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit
	 * this primary mapping, narrowing it for a specific handler method.
	 *
	 * D.
	 * <p><strong>NOTE</strong>: A handler method that is not mapped to any path
	 * explicitly is effectively mapped to an empty path.
	 */
	// 20201225 此注释表示的主要映射
	@AliasFor("path")
	String[] value() default {};

	/**
	 * 20201225
	 * A. 路径映射URI（例如{@code“ / profile”}）。
	 * B. 还支持Ant-style的路径模式（例如{@code“ / profile / **”}）。 在方法级别，支持相对路径（例如{@code“ edit”}）在以类型级别表示的主映射中。
	 *    路径映射URI可能包含占位符（例如“ / $ {profile_path}”
	 * C. 在类型级别和方法级别都受支持！ 当在类型级别使用时，所有方法级别的映射都继承此主映射，从而将其缩小为特定的处理程序方法。
	 * D. 注意：未显式映射到任何路径的处理程序方法将有效地映射到空路径。
	 */
	/**
	 * A.
	 * The path mapping URIs (e.g. {@code "/profile"}).
	 *
	 * B.
	 * <p>Ant-style path patterns are also supported (e.g. {@code "/profile/**"}).
	 * At the method level, relative paths (e.g. {@code "edit"}) are supported
	 * within the primary mapping expressed at the type level.
	 * Path mapping URIs may contain placeholders (e.g. <code>"/${profile_path}"</code>).
	 *
	 * C.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit
	 * this primary mapping, narrowing it for a specific handler method.
	 *
	 * D.
	 * <p><strong>NOTE</strong>: A handler method that is not mapped to any path
	 * explicitly is effectively mapped to an empty path.
	 * @since 4.2
	 */
	// 20201225 路径映射URI（例如{@code“ / profile”}）: 支持Ant-style的路径模式（例如{@code“ / profile / **”}）、可能包含占位符（例如“ / $ {profile_path}”
	@AliasFor("value")
	String[] path() default {};

	/**
	 * 20201225
	 * A. 要映射到的HTTP请求方法，缩小了主要映射的范围：GET，POST，HEAD，OPTIONS，PUT，PATCH，DELETE，TRACE。
	 * B. 在类型级别和方法级别都受支持！ 在类型级别使用时，所有方法级别的映射都继承此HTTP方法限制。
	 */
	/**
	 * A.
	 * The HTTP request methods to map to, narrowing the primary mapping:
	 * GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE.
	 *
	 * B.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit this
	 * HTTP method restriction.
	 */
	// 20201225 要映射到的HTTP请求方法，缩小了主要映射的范围：GET，POST，HEAD，OPTIONS，PUT，PATCH，DELETE，TRACE。
	RequestMethod[] method() default {};

	/**
	 * 20201225
	 * A. 映射请求的参数，从而缩小了主映射的范围。
	 * B. 任何环境的格式均相同：“ myParam = myValue”样式表达式的序列，仅当发现每个这样的参数都具有给定值时才映射请求。 可以通过使用“！=”运算符来否定表达式，
	 *    如“ myParam！= myValue”。 还支持“ myParam”样式表达式，此类参数必须存在于请求中（允许具有任何值）。 最后，“！myParam”样式表达式表示请求中不应存在指定的参数。
	 * C. 在类型级别和方法级别都受支持！ 在类型级别使用时，所有方法级别的映射都继承此参数限制。
	 */
	/**
	 * A.
	 * The parameters of the mapped request, narrowing the primary mapping.
	 *
	 * B.
	 * <p>Same format for any environment: a sequence of "myParam=myValue" style
	 * expressions, with a request only mapped if each such parameter is found
	 * to have the given value. Expressions can be negated by using the "!=" operator,
	 * as in "myParam!=myValue". "myParam" style expressions are also supported,
	 * with such parameters having to be present in the request (allowed to have
	 * any value). Finally, "!myParam" style expressions indicate that the
	 * specified parameter is <i>not</i> supposed to be present in the request.
	 *
	 * C.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit this
	 * parameter restriction.
	 */
	// 20201225 映射请求的参数，从而缩小了主映射的范围
	String[] params() default {};

	/**
	 * 20201225
	 * A. 映射请求的标头，缩小了主映射的范围。
	 * B. 任何环境的格式均相同：“ My-Header = myValue”样式表达式的序列，仅当发现每个此类标头具有给定值时，才映射请求。 可以使用“！=”运算符来否定表达式，
	 *    如“ My-Header！= myValue”中所示。 还支持“ My-Header”样式表达式，此类标头必须存在于请求中（允许具有任何值）。
	 *    最后，“！My-Header”样式表达式表示请求中不应存在指定的标头。
	 * C. 对于标头（例如Accept和Content-Type），还支持媒体类型通配符（*）。 例如:
	 *      	@RequestMapping(value = "/something", headers = "content-type=text/*")
	 *    将匹配Content-Type为“ text / html”，“ text / plain”等的请求。
	 * D. 在类型级别和方法级别都受支持！ 在类型级别使用时，所有方法级别的映射都继承此标头限制。
	 *
	 */
	/**
	 * A.
	 * The headers of the mapped request, narrowing the primary mapping.
	 *
	 * B.
	 * <p>Same format for any environment: a sequence of "My-Header=myValue" style
	 * expressions, with a request only mapped if each such header is found
	 * to have the given value. Expressions can be negated by using the "!=" operator,
	 * as in "My-Header!=myValue". "My-Header" style expressions are also supported,
	 * with such headers having to be present in the request (allowed to have
	 * any value). Finally, "!My-Header" style expressions indicate that the
	 * specified header is <i>not</i> supposed to be present in the request.
	 *
	 * C.
	 * <p>Also supports media type wildcards (*), for headers such as Accept
	 * and Content-Type. For instance,
	 * <pre class="code">
	 * &#064;RequestMapping(value = "/something", headers = "content-type=text/*")
	 * </pre>
	 * will match requests with a Content-Type of "text/html", "text/plain", etc.
	 *
	 * D.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * When used at the type level, all method-level mappings inherit this
	 * header restriction.
	 * @see org.springframework.http.MediaType
	 */
	// 20201225 映射请求的标头，缩小了主映射的范围
	String[] headers() default {};

	/**
	 * 20201225
	 * A. 按可以由映射处理程序使用的媒体类型缩小主映射。 由一种或多种媒体类型组成，其中一种必须与请求{@code Content-Type}标头匹配。 例子：
	 * 			consumes = "text/plain"
	 * 			consumes = {"text/plain", "application/*"}
	 * 			consumes = MediaType.TEXT_PLAIN_VALUE
	 * 	  可以使用“！”取反表达式。 运算符，如“！text / plain”中一样，它匹配除“ text / plain”以外的所有带有{@code Content-Type}的请求。
	 * B. 在类型级别和方法级别都受支持！ 如果在两个级别上均指定，则方法级别的消耗条件将覆盖类型级别的条件。
	 */
	/**
	 * A.
	 * Narrows the primary mapping by media types that can be consumed by the
	 * mapped handler. Consists of one or more media types one of which must
	 * match to the request {@code Content-Type} header. Examples:
	 * <pre class="code">
	 * consumes = "text/plain"
	 * consumes = {"text/plain", "application/*"}
	 * consumes = MediaType.TEXT_PLAIN_VALUE
	 * </pre>
	 * Expressions can be negated by using the "!" operator, as in
	 * "!text/plain", which matches all requests with a {@code Content-Type}
	 * other than "text/plain".
	 *
	 * B.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * If specified at both levels, the method level consumes condition overrides
	 * the type level condition.
	 *
	 * @see org.springframework.http.MediaType
	 * @see javax.servlet.http.HttpServletRequest#getContentType()
	 */
	// 20201225 按可以由映射处理程序使用的媒体类型缩小主映射。 由一种或多种媒体类型组成，其中一种必须与请求{@code Content-Type}标头匹配
	String[] consumes() default {};

	/**
	 * 20201225
	 * A. 通过可以由映射处理程序生成的媒体类型来缩小主映射。 由一种或多种媒体类型组成，其中一种必须通过针对请求的“可接受”媒体类型的内容协商来选择。 通常，这些是从
	 *    {@code“ Accept”}标头中提取的，但也可以从查询参数或其他参数中得出。 例子：
	 * 			produces = "text/plain"
	 * 			produces = {"text/plain", "application/*"}
	 * 			produces = MediaType.TEXT_PLAIN_VALUE
	 * 			produces = "text/plain;charset=UTF-8"
	 * B. 如果声明的媒体类型包含参数（例如“ charset = UTF-8”，“ type = feed”，“ type = entry”），并且请求中的兼容媒体类型也具有该参数，则参数值必须匹配 。
	 *    否则，如果请求中的媒体类型不包含参数，则假定客户端接受任何值。
	 * C. 可以使用“！”取反表达式。 运算符，如“！text / plain”中所示，该操作符将所有请求与除“ text / plain”之外的其他所有请求都用{@code Accept}匹配。
	 * D. 在类型级别和方法级别都受支持！ 如果在两个级别上均指定，则方法级别的产生条件将覆盖类型级别的条件。
	 */
	/**
	 * A.
	 * Narrows the primary mapping by media types that can be produced by the
	 * mapped handler. Consists of one or more media types one of which must
	 * be chosen via content negotiation against the "acceptable" media types
	 * of the request. Typically those are extracted from the {@code "Accept"}
	 * header but may be derived from query parameters, or other. Examples:
	 * <pre class="code">
	 * produces = "text/plain"
	 * produces = {"text/plain", "application/*"}
	 * produces = MediaType.TEXT_PLAIN_VALUE
	 * produces = "text/plain;charset=UTF-8"
	 * </pre>
	 *
	 * B.
	 * <p>If a declared media type contains a parameter (e.g. "charset=UTF-8",
	 * "type=feed", "type=entry") and if a compatible media type from the request
	 * has that parameter too, then the parameter values must match. Otherwise
	 * if the media type from the request does not contain the parameter, it is
	 * assumed the client accepts any value.
	 *
	 * C.
	 * <p>Expressions can be negated by using the "!" operator, as in "!text/plain",
	 * which matches all requests with a {@code Accept} other than "text/plain".
	 *
	 * D.
	 * <p><b>Supported at the type level as well as at the method level!</b>
	 * If specified at both levels, the method level produces condition overrides
	 * the type level condition.
	 * @see org.springframework.http.MediaType
	 */
	// 20201225 通过可以由映射处理程序生成的媒体类型来缩小主映射: 这些是从{@code“ Accept”}标头中提取的，但也可以从查询参数或其他参数中得出
	String[] produces() default {};

}
