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

package org.springframework.context.annotation;

import java.beans.Introspector;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 20201208
 * A. 带有{@link org.springframework.stereotype.Component @Component}注解或本身带有{@code @Component}注解的另一个注解的bean的{@link BeanNameGenerator}实现。
 *    例如，Spring的构造型注解（例如{@link org.springframework.stereotype.Repository @Repository}）本身都带有{@code @Component}注解。
 * B. 如果可用，还支持Java EE 6的{@link javax.annotation.ManagedBean}和JSR-330的{@link javax.inject.Named}注解。 请注意，Spring组件注解始终会覆盖此类标准注解。
 * C. 如果注解的值不表示Bean名称，则将基于类的短名称（首字母小写）构建一个适当的名称。 例如：
 * 			com.xyz.FooServiceImpl -> fooServiceImpl
 */
/**
 * A.
 * {@link BeanNameGenerator} implementation for bean classes annotated with the
 * {@link org.springframework.stereotype.Component @Component} annotation or
 * with another annotation that is itself annotated with {@code @Component} as a
 * meta-annotation. For example, Spring's stereotype annotations (such as
 * {@link org.springframework.stereotype.Repository @Repository}) are
 * themselves annotated with {@code @Component}.
 *
 * B.
 * <p>Also supports Java EE 6's {@link javax.annotation.ManagedBean} and
 * JSR-330's {@link javax.inject.Named} annotations, if available. Note that
 * Spring component annotations always override such standard annotations.
 *
 * C.
 * <p>If the annotation's value doesn't indicate a bean name, an appropriate
 * name will be built based on the short name of the class (with the first
 * letter lower-cased). For example:
 *
 * <pre class="code">com.xyz.FooServiceImpl -&gt; fooServiceImpl</pre>
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see org.springframework.stereotype.Component#value()
 * @see org.springframework.stereotype.Repository#value()
 * @see org.springframework.stereotype.Service#value()
 * @see org.springframework.stereotype.Controller#value()
 * @see javax.inject.Named#value()
 * @see FullyQualifiedAnnotationBeanNameGenerator
 */
// 20201208 @Component注解的实现
public class AnnotationBeanNameGenerator implements BeanNameGenerator {

	/**
	 * A convenient constant for a default {@code AnnotationBeanNameGenerator} instance,
	 * as used for component scanning purposes.
	 * @since 5.2
	 */
	// 20201208 用于默认{@code AnnotationBeanNameGenerator}实例的便捷常量，用于组件扫描。
	public static final AnnotationBeanNameGenerator INSTANCE = new AnnotationBeanNameGenerator();

	private static final String COMPONENT_ANNOTATION_CLASSNAME = "org.springframework.stereotype.Component";

	// 20201209 注解全限定类名-注解全部全限定类名集合
	private final Map<String, Set<String>> metaAnnotationTypesCache = new ConcurrentHashMap<>();

	// 20201209 为给定的bean定义生成一个bean名称。
	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		// 20201029 如果bean定义器为注解Bean定义器AnnotatedBeanDefinition
		if (definition instanceof AnnotatedBeanDefinition) {
			// 20201209 从类的注解之一中获取bean名称 -> 注解树的名称都一样
			String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
			if (StringUtils.hasText(beanName)) {
				// Explicit bean name found.
				return beanName;
			}
		}
		// Fallback: generate a unique default bean name. // 20201209 后备：生成唯一的默认bean名称 => “mypackage.MyJdbcDao”->“ myJdbcDao”
		return buildDefaultBeanName(definition, registry);
	}

	/**
	 * Derive a bean name from one of the annotations on the class.
	 * @param annotatedDef the annotation-aware bean definition
	 * @return the bean name, or {@code null} if none is found
	 */
	// 20201209 从类的注解之一中获取bean名称 -> 注解树的名称都一样
	@Nullable
	protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
		// 20201209 获取此bean定义的bean类的注解元数据（以及基本类元数据）。
		AnnotationMetadata amd = annotatedDef.getMetadata();

		// 20201209 获取基础类上存在的所有注解类型的全限定类名。
		Set<String> types = amd.getAnnotationTypes();

		// 20201209 初始化bean实例名称
		String beanName = null;

		// 20201209 遍历这些全限定类型名
		for (String type : types) {
			// 20201209 根据指定类型的注解以及元数据获取注解属性键值对
			AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(amd, type);

			// 20201209 如果存在键值对
			if (attributes != null) {
				// 20201209 根据注解全限定类型名获取注解全部全限定类名集合
				Set<String> metaTypes = this.metaAnnotationTypesCache.computeIfAbsent(type, key -> {
					Set<String> result = amd.getMetaAnnotationTypes(key);
					return (result.isEmpty() ? Collections.emptySet() : result);
				});

				// 20201209 如果type类型的注解允许使用@value更改组件名
				if (isStereotypeWithNameValue(type, metaTypes, attributes)) {
					// 20201209 则先获取属性值
					Object value = attributes.get("value");
					// 20201209 如果属性值为String类型
					if (value instanceof String) {
						// 20201209 则替换掉当前循环里的bean名称, 保证该注解树的组件名称都一致
						String strVal = (String) value;
						if (StringUtils.hasLength(strVal)) {
							if (beanName != null && !strVal.equals(beanName)) {
								throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
										"component names: '" + beanName + "' versus '" + strVal + "'");
							}
							beanName = strVal;
						}
					}
				}
			}
		}

		// 20201209 返回该组件名称
		return beanName;
	}

	/**
	 * Check whether the given annotation is a stereotype that is allowed
	 * to suggest a component name through its annotation {@code value()}.
	 * @param annotationType the name of the annotation class to check
	 * @param metaAnnotationTypes the names of meta-annotations on the given annotation
	 * @param attributes the map of attributes for the given annotation
	 * @return whether the annotation qualifies as a stereotype with component name
	 */
	// 20201209 检查给定的注解是否为允许通过其注解{@code value（）}来建议组件名称的构造型。
	protected boolean isStereotypeWithNameValue(String annotationType,
			Set<String> metaAnnotationTypes, @Nullable Map<String, Object> attributes) {

		boolean isStereotype = annotationType.equals(COMPONENT_ANNOTATION_CLASSNAME) ||
				metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME) ||
				annotationType.equals("javax.annotation.ManagedBean") ||
				annotationType.equals("javax.inject.Named");

		return (isStereotype && attributes != null && attributes.containsKey("value"));
	}

	/**
	 * 20201209
	 * A. 从给定的bean定义中派生默认的bean名称。
	 * B. 默认实现委托给{@link #buildDefaultBeanName（BeanDefinition）}。
	 */
	/**
	 * A.
	 * Derive a default bean name from the given bean definition.
	 *
	 * B.
	 * <p>The default implementation delegates to {@link #buildDefaultBeanName(BeanDefinition)}.
	 * @param definition the bean definition to build a bean name for
	 * @param registry the registry that the given bean definition is being registered with
	 * @return the default bean name (never {@code null})
	 */
	// 20201209 从给定的bean定义中派生默认的bean名称
	protected String buildDefaultBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		// 20201209 从给定的bean定义中派生默认的bean名称 => “mypackage.MyJdbcDao”->“ myJdbcDao”
		return buildDefaultBeanName(definition);
	}

	/**
	 * 20201209
	 * A. 从给定的bean定义中派生默认的bean名称。
	 * B. 默认实现只是构建简短的类名的大写形式：例如 “mypackage.MyJdbcDao”->“ myJdbcDao”。
	 * C. 请注意，内部类将因此具有“ outerClassName.InnerClassName”形式的名称，如果按名称自动装配，则由于名称中的句点可能会出现问题。
	 */
	/**
	 * A.
	 * Derive a default bean name from the given bean definition.
	 *
	 * B.
	 * <p>The default implementation simply builds a decapitalized version
	 * of the short class name: e.g. "mypackage.MyJdbcDao" -> "myJdbcDao".
	 *
	 * C.
	 * <p>Note that inner classes will thus have names of the form
	 * "outerClassName.InnerClassName", which because of the period in the
	 * name may be an issue if you are autowiring by name.
	 *
	 * @param definition the bean definition to build a bean name for
	 * @return the default bean name (never {@code null})
	 */
	// 20201209 从给定的bean定义中派生默认的bean名称 => “mypackage.MyJdbcDao”->“ myJdbcDao”
	protected String buildDefaultBeanName(BeanDefinition definition) {
		String beanClassName = definition.getBeanClassName();
		Assert.state(beanClassName != null, "No bean class name set");
		String shortClassName = ClassUtils.getShortName(beanClassName);
		return Introspector.decapitalize(shortClassName);
	}

}
