/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot.web.servlet.context;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 20201204
 * A. {@link ServletWebServerApplicationContext}从XML文档获取其配置，{@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}可以理解。
 * B. 注意：如果有多个配置位置，则较新的Bean定义将覆盖较早加载的文件中定义的定义。 可以利用它来通过一个额外的XML文件有意覆盖某些bean定义。
 */
/**
 * A.
 * {@link ServletWebServerApplicationContext} which takes its configuration from XML
 * documents, understood by an
 * {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * B.
 * <p>
 * Note: In case of multiple config locations, later bean definitions will override ones
 * defined in earlier loaded files. This can be leveraged to deliberately override certain
 * bean definitions via an extra XML file.
 *
 * @author Phillip Webb
 * @since 1.0.0
 * @see #setNamespace
 * @see #setConfigLocations
 * @see ServletWebServerApplicationContext
 * @see XmlWebApplicationContext
 */
// 20201204 Servelt XML配置应用上下文: 从XML文档获取其配置、允许对其应用任何bean定义读取器、支持Servlet上下文、配置上下文、Web生命周期获取、将资源路径解释为servlet上下文资源
public class XmlServletWebServerApplicationContext extends ServletWebServerApplicationContext {

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

	/**
	 * Create a new {@link XmlServletWebServerApplicationContext} that needs to be
	 * {@linkplain #load loaded} and then manually {@link #refresh refreshed}.
	 */
	public XmlServletWebServerApplicationContext() {
		this.reader.setEnvironment(getEnvironment());
	}

	/**
	 * Create a new {@link XmlServletWebServerApplicationContext}, loading bean
	 * definitions from the given resources and automatically refreshing the context.
	 * @param resources the resources to load from
	 */
	public XmlServletWebServerApplicationContext(Resource... resources) {
		load(resources);
		refresh();
	}

	/**
	 * Create a new {@link XmlServletWebServerApplicationContext}, loading bean
	 * definitions from the given resource locations and automatically refreshing the
	 * context.
	 * @param resourceLocations the resources to load from
	 */
	public XmlServletWebServerApplicationContext(String... resourceLocations) {
		load(resourceLocations);
		refresh();
	}

	/**
	 * Create a new {@link XmlServletWebServerApplicationContext}, loading bean
	 * definitions from the given resource locations and automatically refreshing the
	 * context.
	 * @param relativeClass class whose package will be used as a prefix when loading each
	 * specified resource name
	 * @param resourceNames relatively-qualified names of resources to load
	 */
	public XmlServletWebServerApplicationContext(Class<?> relativeClass, String... resourceNames) {
		load(relativeClass, resourceNames);
		refresh();
	}

	/**
	 * Set whether to use XML validation. Default is {@code true}.
	 * @param validating if validating the XML
	 */
	public void setValidating(boolean validating) {
		this.reader.setValidating(validating);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates the given environment to underlying {@link XmlBeanDefinitionReader}.
	 * Should be called before any call to {@link #load}.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(getEnvironment());
	}

	/**
	 * Load bean definitions from the given XML resources.
	 * @param resources one or more resources to load from
	 */
	public final void load(Resource... resources) {
		this.reader.loadBeanDefinitions(resources);
	}

	/**
	 * Load bean definitions from the given XML resources.
	 * @param resourceLocations one or more resource locations to load from
	 */
	public final void load(String... resourceLocations) {
		this.reader.loadBeanDefinitions(resourceLocations);
	}

	/**
	 * Load bean definitions from the given XML resources.
	 * @param relativeClass class whose package will be used as a prefix when loading each
	 * specified resource name
	 * @param resourceNames relatively-qualified names of resources to load
	 */
	public final void load(Class<?> relativeClass, String... resourceNames) {
		Resource[] resources = new Resource[resourceNames.length];
		for (int i = 0; i < resourceNames.length; i++) {
			resources[i] = new ClassPathResource(resourceNames[i], relativeClass);
		}
		this.reader.loadBeanDefinitions(resources);
	}

}
