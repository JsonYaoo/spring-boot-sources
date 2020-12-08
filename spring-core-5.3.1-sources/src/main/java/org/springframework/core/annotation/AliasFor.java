/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 20201208
 * A. {@code @AliasFor}是一个注解，用于为注解属性声明别名。
 * B. 使用场景:
 * 		a. 注解中的显式别名：在单个注解中，可以在一对属性上声明{@code @AliasFor}，以表示它们是彼此可互换的别名。
 * 		b. 元注解中属性的显式别名：如果{@code @AliasFor}的{@link #annotation}属性设置为与声明它的注解不同的注解，则{@link #attribute}被解释为别名用于元注解中的属性
 * 		（即，显式的元注解属性覆盖）。 这样就可以精确地控制在注解层次结构中哪些属性被覆盖。 实际上，使用{@code @AliasFor}，甚至可以为元注解的{@code value}属性声明别名。
 * 		c. 注解中的隐式别名：如果将注解中的一个或多个属性声明为同一元注解属性的属性替代（直接或传递），则这些属性将被视为彼此的一组隐式别名，从而导致行为类似于注解中显式别名的行为。
 * C. 使用要求: 与Java中的任何注解一样，{@code @AliasFor}本身的存在不会强制实施别名语义。 为了强制实施别名语义，必须通过{@link MergedAnnotations}加载注解。
 * D. 实施要求:
 * 		a. 注解中的显式别名:
 * 			a.1. 组成别名对的每个属性都应使用{@code @AliasFor}进行注解，并且{@link #attribute}或{@link #value}必须引用该对中的另一个属性。 从Spring Framework 5.2.1开始，
 * 		     	 从技术上讲，可以仅对别名对中的一个属性进行注解； 但是，建议使用别名对对这两个属性进行注解，以获取更好的文档并与Spring Framework的早期版本兼容。
 * 		    a.2. 别名属性必须声明相同的返回类型。
 * 		    a.3. 别名属性必须声明一个默认值。
 * 		    a.4. 别名属性必须声明相同的默认值。
 * 		    a.5. {@link #annotation}不应声明。
 * 		b. 元注解中属性的显式别名:
 * 			b.1. 作为元注解中的属性别名的属性必须使用{@code @AliasFor}进行注解，并且{@link #attribute}必须在元注解中引用该属性。
 * 			b.2. 别名属性必须声明相同的返回类型。
 * 			b.3. {@link #annotation}必须引用元注解。
 * 			b.4. 引用的元注解必须在声明{@code @AliasFor}的注解类中是元存在的。
 * 		c. 注解中的隐式别名:
 * 			c.1. 属于一组隐式别名的每个属性都必须使用{@code @AliasFor}进行注解，并且{@link #attribute}必须在同一元注解中引用相同的属性（直接或通过其他显式元注解传递）
 * 		     	 注解层次结构中的属性覆盖）。
 * 		    c.2. 别名属性必须声明相同的返回类型。
 * 		    c.3. 别名属性必须声明一个默认值。
 * 		    c.4. 别名属性必须声明相同的默认值。
 * 		    c.5. {@link #annotation}必须引用适当的元注解。
 * 		    c.6. 引用的元注解必须在声明{@code @AliasFor}的注解类中是元存在的。
 * E. 示例：注解中的显式别名: 在{@code @ContextConfiguration}中，{@code value}和{@code locations}是彼此的显式别名:
 * 			public @interface ContextConfiguration {
 *
 *    			@AliasFor("locations")
 *    			String[] value() default {};
 *
 *    			@AliasFor("value")
 *    			String[] locations() default {};
 *
 *    			// ...
 * 			}
 * F. 示例：元注解中的属性的显式别名: 在{@code @XmlTestConfig}中，{@code xmlFiles}是{@code @ContextConfiguration}中
 *    {@code location}的显式别名。 换句话说，{@code xmlFiles}会覆盖{@code @ContextConfiguration}中的{@code location}属性:
 * 			@ContextConfiguration
 * 			public @interface XmlTestConfig {
 *
 *    			@AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    			String[] xmlFiles();
 * 			}
 * G. 示例：注解中的隐式别名: 在{@code @MyTestConfig}中，{@code value}，{@code groovyScripts}和{@code xmlFiles}都是
 *    {@code @ContextConfiguration}中{@code location}属性的显式元注解属性替代。 因此，这三个属性也是彼此的隐式别名:
 * 			@ContextConfiguration
 * 			public @interface MyTestConfig {
 *
 *    			@AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    			String[] value() default {};
 *
 *    			@AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    			String[] groovyScripts() default {};
 *
 *    			@AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    			String[] xmlFiles() default {};
 * 			}
 * H. 示例：注解中的传递性隐式别名: 在{@code @GroovyOrXmlTestConfig}中，{@code groovy}是{@code @MyTestConfig}中
 *    {@code groovyScripts}属性的显式替代。 而{@code xml}是{@code @ContextConfiguration}中{@code location}属性的显式替代。
 *    此外，{@code groovy}和{@code xml}彼此是传递的隐式别名，因为它们都有效地覆盖了{@code @ContextConfiguration}中的
 *    {@code location}属性:
 * 			@MyTestConfig
 * 			public @interface GroovyOrXmlTestConfig {
 *
 *    			@AliasFor(annotation = MyTestConfig.class, attribute = "groovyScripts")
 *    			String[] groovy() default {};
 *
 *    			@AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    			String[] xml() default {};
 * 			}
 * I. Spring注解支持属性别名: 从Spring Framework 4.2开始，核心Spring中的几个注解已更新为使用{@code @AliasFor}
 *    来配置其内部属性别名。 有关单个注解，请查阅Javadoc；有关详细信息，请参考参考手册。
 */
/**
 * A.
 * {@code @AliasFor} is an annotation that is used to declare aliases for
 * annotation attributes.
 *
 * B.
 * <h3>Usage Scenarios</h3>
 * <ul>
 * a.
 * <li><strong>Explicit aliases within an annotation</strong>: within a single
 * annotation, {@code @AliasFor} can be declared on a pair of attributes to
 * signal that they are interchangeable aliases for each other.</li>
 *
 * b.
 * <li><strong>Explicit alias for attribute in meta-annotation</strong>: if the
 * {@link #annotation} attribute of {@code @AliasFor} is set to a different
 * annotation than the one that declares it, the {@link #attribute} is
 * interpreted as an alias for an attribute in a meta-annotation (i.e., an
 * explicit meta-annotation attribute override). This enables fine-grained
 * control over exactly which attributes are overridden within an annotation
 * hierarchy. In fact, with {@code @AliasFor} it is even possible to declare
 * an alias for the {@code value} attribute of a meta-annotation.</li>
 *
 * c.
 * <li><strong>Implicit aliases within an annotation</strong>: if one or
 * more attributes within an annotation are declared as attribute overrides
 * for the same meta-annotation attribute (either directly or transitively),
 * those attributes will be treated as a set of <em>implicit</em> aliases
 * for each other, resulting in behavior analogous to that for explicit
 * aliases within an annotation.</li>
 * </ul>
 *
 * C.
 * <h3>Usage Requirements</h3>
 * <p>Like with any annotation in Java, the mere presence of {@code @AliasFor}
 * on its own will not enforce alias semantics. For alias semantics to be
 * enforced, annotations must be <em>loaded</em> via {@link MergedAnnotations}.
 *
 * D.
 * <h3>Implementation Requirements</h3>
 * <ul>
 * a.
 * <li><strong>Explicit aliases within an annotation</strong>:
 * <ol>
 * a.1.
 * <li>Each attribute that makes up an aliased pair should be annotated with
 * {@code @AliasFor}, and either {@link #attribute} or {@link #value} must
 * reference the <em>other</em> attribute in the pair. Since Spring Framework
 * 5.2.1 it is technically possible to annotate only one of the attributes in an
 * aliased pair; however, it is recommended to annotate both attributes in an
 * aliased pair for better documentation as well as compatibility with previous
 * versions of the Spring Framework.</li>
 *
 * a.2.
 * <li>Aliased attributes must declare the same return type.</li>
 *
 * a.3.
 * <li>Aliased attributes must declare a default value.</li>
 *
 * a.4.
 * <li>Aliased attributes must declare the same default value.</li>
 *
 * a.5.
 * <li>{@link #annotation} should not be declared.</li>
 * </ol>
 * </li>
 *
 * b.
 * <li><strong>Explicit alias for attribute in meta-annotation</strong>:
 * <ol>
 * b.1.
 * <li>The attribute that is an alias for an attribute in a meta-annotation
 * must be annotated with {@code @AliasFor}, and {@link #attribute} must
 * reference the attribute in the meta-annotation.</li>
 *
 * b.2.
 * <li>Aliased attributes must declare the same return type.</li>
 *
 * b.3.
 * <li>{@link #annotation} must reference the meta-annotation.</li>
 *
 * b.4.
 * <li>The referenced meta-annotation must be <em>meta-present</em> on the
 * annotation class that declares {@code @AliasFor}.</li>
 * </ol>
 * </li>
 *
 * c.
 * <li><strong>Implicit aliases within an annotation</strong>:
 * <ol>
 *
 * c.1.
 * <li>Each attribute that belongs to a set of implicit aliases must be
 * annotated with {@code @AliasFor}, and {@link #attribute} must reference
 * the same attribute in the same meta-annotation (either directly or
 * transitively via other explicit meta-annotation attribute overrides
 * within the annotation hierarchy).</li>
 *
 * c.2.
 * <li>Aliased attributes must declare the same return type.</li>
 *
 * c.3.
 * <li>Aliased attributes must declare a default value.</li>
 *
 * c.4.
 * <li>Aliased attributes must declare the same default value.</li>
 *
 * c.5.
 * <li>{@link #annotation} must reference an appropriate meta-annotation.</li>
 *
 * c.6.
 * <li>The referenced meta-annotation must be <em>meta-present</em> on the
 * annotation class that declares {@code @AliasFor}.</li>
 * </ol>
 * </li>
 * </ul>
 *
 * E.
 * <h3>Example: Explicit Aliases within an Annotation</h3>
 * <p>In {@code @ContextConfiguration}, {@code value} and {@code locations}
 * are explicit aliases for each other.
 *
 * <pre class="code">
 * public &#064;interface ContextConfiguration {
 *
 *    &#064;AliasFor("locations")
 *    String[] value() default {};
 *
 *    &#064;AliasFor("value")
 *    String[] locations() default {};
 *
 *    // ...
 * }</pre>
 *
 * F.
 * <h3>Example: Explicit Alias for Attribute in Meta-annotation</h3>
 * <p>In {@code @XmlTestConfig}, {@code xmlFiles} is an explicit alias for
 * {@code locations} in {@code @ContextConfiguration}. In other words,
 * {@code xmlFiles} overrides the {@code locations} attribute in
 * {@code @ContextConfiguration}.
 *
 * <pre class="code">
 * &#064;ContextConfiguration
 * public &#064;interface XmlTestConfig {
 *
 *    &#064;AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    String[] xmlFiles();
 * }</pre>
 *
 * G.
 * <h3>Example: Implicit Aliases within an Annotation</h3>
 * <p>In {@code @MyTestConfig}, {@code value}, {@code groovyScripts}, and
 * {@code xmlFiles} are all explicit meta-annotation attribute overrides for
 * the {@code locations} attribute in {@code @ContextConfiguration}. These
 * three attributes are therefore also implicit aliases for each other.
 *
 * <pre class="code">
 * &#064;ContextConfiguration
 * public &#064;interface MyTestConfig {
 *
 *    &#064;AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    String[] value() default {};
 *
 *    &#064;AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    String[] groovyScripts() default {};
 *
 *    &#064;AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    String[] xmlFiles() default {};
 * }</pre>
 *
 * H.
 * <h3>Example: Transitive Implicit Aliases within an Annotation</h3>
 * <p>In {@code @GroovyOrXmlTestConfig}, {@code groovy} is an explicit
 * override for the {@code groovyScripts} attribute in {@code @MyTestConfig};
 * whereas, {@code xml} is an explicit override for the {@code locations}
 * attribute in {@code @ContextConfiguration}. Furthermore, {@code groovy}
 * and {@code xml} are transitive implicit aliases for each other, since they
 * both effectively override the {@code locations} attribute in
 * {@code @ContextConfiguration}.
 *
 * <pre class="code">
 * &#064;MyTestConfig
 * public &#064;interface GroovyOrXmlTestConfig {
 *
 *    &#064;AliasFor(annotation = MyTestConfig.class, attribute = "groovyScripts")
 *    String[] groovy() default {};
 *
 *    &#064;AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
 *    String[] xml() default {};
 * }</pre>
 *
 * I.
 * <h3>Spring Annotations Supporting Attribute Aliases</h3>
 * <p>As of Spring Framework 4.2, several annotations within core Spring
 * have been updated to use {@code @AliasFor} to configure their internal
 * attribute aliases. Consult the Javadoc for individual annotations as well
 * as the reference manual for details.
 *
 * @author Sam Brannen
 * @since 4.2
 * @see MergedAnnotations
 * @see SynthesizedAnnotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AliasFor {

	/**
	 * Alias for {@link #attribute}.
	 * <p>Intended to be used instead of {@link #attribute} when {@link #annotation}
	 * is not declared &mdash; for example: {@code @AliasFor("value")} instead of
	 * {@code @AliasFor(attribute = "value")}.
	 */
	@AliasFor("attribute")
	String value() default "";

	/**
	 * The name of the attribute that <em>this</em> attribute is an alias for.
	 * @see #value
	 */
	@AliasFor("value")
	String attribute() default "";

	/**
	 * The type of annotation in which the aliased {@link #attribute} is declared.
	 * <p>Defaults to {@link Annotation}, implying that the aliased attribute is
	 * declared in the same annotation as <em>this</em> attribute.
	 */
	Class<? extends Annotation> annotation() default Annotation.class;

}
