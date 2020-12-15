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

import java.util.function.Predicate;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

/**
 * 20201215
 * A. 由类型决定的接口，这些类型根据给定的选择标准（通常是一个或多个注释属性）来确定应导入哪个{@link Configuration}类。
 * B. {@link ImportSelector}可以实现以下任何{@link org.springframework.beans.factory.Aware Aware}接口，并且它们各自的方法将在{@link #selectImports}之前调用：
 * 		a. {@link org.springframework.context.EnvironmentAware EnvironmentAware}
 * 		b. {@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}
 * 		c. {@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}
 * 		d. {@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}
 * C. 或者，该类可以为单个构造函数提供以下一种或多种受支持的参数类型：
 * 		a. {@link org.springframework.core.env.Environment Environment}
 * 		b. {@link org.springframework.beans.factory.BeanFactory BeanFactory}
 * 		c. {@link java.lang.ClassLoader ClassLoader}
 * 		d. {@link org.springframework.core.io.ResourceLoader ResourceLoader}
 * D. {@code ImportSelector}实现通常以与常规{@code @Import}批注相同的方式处理，但是，也可以将导入的选择推迟到所有{@code @Configuration}类都已处理完之后
 *    （请参阅{@link DeferredImportSelector}有关详细信息}。
 */
/**
 * A.
 * Interface to be implemented by types that determine which @{@link Configuration}
 * class(es) should be imported based on a given selection criteria, usually one or
 * more annotation attributes.
 *
 * B.
 * <p>An {@link ImportSelector} may implement any of the following
 * {@link org.springframework.beans.factory.Aware Aware} interfaces,
 * and their respective methods will be called prior to {@link #selectImports}:
 * <ul>
 * a.
 * <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware}</li>
 *
 * b.
 * <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}</li>
 *
 * c.
 * <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}</li>
 *
 * d.
 * <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}</li>
 * </ul>
 *
 * C.
 * <p>Alternatively, the class may provide a single constructor with one or more of
 * the following supported parameter types:
 * <ul>
 * a.
 * <li>{@link org.springframework.core.env.Environment Environment}</li>
 *
 * b.
 * <li>{@link org.springframework.beans.factory.BeanFactory BeanFactory}</li>
 *
 * c.
 * <li>{@link java.lang.ClassLoader ClassLoader}</li>
 *
 * d.
 * <li>{@link org.springframework.core.io.ResourceLoader ResourceLoader}</li>
 * </ul>
 *
 * D.
 * <p>{@code ImportSelector} implementations are usually processed in the same way
 * as regular {@code @Import} annotations, however, it is also possible to defer
 * selection of imports until all {@code @Configuration} classes have been processed
 * (see {@link DeferredImportSelector} for details).
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see DeferredImportSelector
 * @see Import
 * @see ImportBeanDefinitionRegistrar
 * @see Configuration
 */
// 20201215 自动导入接口: 根据给定的选择标准（通常是一个或多个注释属性）来确定应导入哪个{@link Configuration}类
public interface ImportSelector {

	/**
	 * Select and return the names of which class(es) should be imported based on
	 * the {@link AnnotationMetadata} of the importing @{@link Configuration} class.
	 * @return the class names, or an empty array if none
	 */
	String[] selectImports(AnnotationMetadata importingClassMetadata);

	/**
	 * 20201215
	 * A. 返回一个谓词，从导入候选中排除类，该谓词将可传递地应用于通过此选择器的导入找到的所有类。
	 * B. 如果此谓词为给定的完全限定的类名返回{@code true}，则该类将不被视为导入的配置类，从而绕过了类文件的加载以及元数据的内省。
	 */
	/**
	 * A.
	 * Return a predicate for excluding classes from the import candidates, to be
	 * transitively applied to all classes found through this selector's imports.
	 *
	 * B.
	 * <p>If this predicate returns {@code true} for a given fully-qualified
	 * class name, said class will not be considered as an imported configuration
	 * class, bypassing class file loading as well as metadata introspection.
	 *
	 * @return the filter predicate for fully-qualified candidate class names
	 * of transitively imported configuration classes, or {@code null} if none
	 * @since 5.2.4
	 */
	// 202012125 获取注解排除过滤操作 -> 空实现
	@Nullable
	default Predicate<String> getExclusionFilter() {
		return null;
	}

}
