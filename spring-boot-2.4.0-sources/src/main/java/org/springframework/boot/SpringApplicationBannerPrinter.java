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

package org.springframework.boot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Class used by {@link SpringApplication} to print the application banner.
 *
 * @author Phillip Webb
 */
// 20201203 {@link springapplication}用于打印应用程序横幅的类。
class SpringApplicationBannerPrinter {
	// 20201203 Banner定位属性spring.banner.locatio
	static final String BANNER_LOCATION_PROPERTY = "spring.banner.location";

	static final String BANNER_IMAGE_LOCATION_PROPERTY = "spring.banner.image.location";

	// 20201203 默认Banner定位banner.txt
	static final String DEFAULT_BANNER_LOCATION = "banner.txt";

	// 20201203 图片的后缀名数组
	static final String[] IMAGE_EXTENSION = { "gif", "jpg", "png" };

	// 20201203 默认的SpringBootBanner
	private static final Banner DEFAULT_BANNER = new SpringBootBanner();

	// 20201203 资源加载器
	private final ResourceLoader resourceLoader;

	// 20201203 用于以编程方式编写横幅的接口类。
	private final Banner fallbackBanner;

	// 20201203 构建打印横幅实现类
	SpringApplicationBannerPrinter(ResourceLoader resourceLoader, Banner fallbackBanner) {
		this.resourceLoader = resourceLoader;
		this.fallbackBanner = fallbackBanner;
	}

	// 20201203 获取日志Banner, 同时输出spring.banner.charset="UFT-8"
	Banner print(Environment environment, Class<?> sourceClass, Log logger) {
		// 20201203 根据环境构造内部Banner
		Banner banner = getBanner(environment);
		try {
			// 20201203 获取横幅内容且记录到日志中, 为info级别
			logger.info(createStringFromBanner(banner, environment, sourceClass));
		}
		catch (UnsupportedEncodingException ex) {
			logger.warn("Failed to create String for banner", ex);
		}

		// 20201203 构造PrintedBanner
		return new PrintedBanner(banner, sourceClass);
	}

	// 20201203 获取控制台Banner
	Banner print(Environment environment, Class<?> sourceClass, PrintStream out) {
		// 20201203 根据环境获取横幅Banner集合 => 默认的SpringBootBanner
		Banner banner = getBanner(environment);

		// 20201203 使用默认的SpringBootBanner => 将横幅打印到指定的控制台中
		banner.printBanner(environment, sourceClass, out);

		// 20201203 构造PrintedBanner
		return new PrintedBanner(banner, sourceClass);
	}

	// 20201203 根据环境获取横幅Banner集合
	private Banner getBanner(Environment environment) {
		// 20201203 获取内部Banner类实例
		Banners banners = new Banners();

		// 20201203 如果图片的横幅实现实例不为空, 则添加该实例
		banners.addIfNotNull(getImageBanner(environment));

		// 20201203 如果源文本打印横幅实现实例不为空, 则添加该实例
		banners.addIfNotNull(getTextBanner(environment));

		// 20201203 如果图片的横幅实现实例 或者 源文本打印横幅实现实例 不为空
		if (banners.hasAtLeastOneBanner()) {
			// 20201203 则返回这个Banner集合
			return banners;
		}

		// 20201203 如果图片的横幅实现实例 或者 源文本打印横幅实现实例 都为空, 如果用于以编程方式编写横幅的接口类不为空
		if (this.fallbackBanner != null) {
			// 20201203 则返回这个用于以编程方式编写横幅的接口类不为空
			return this.fallbackBanner;
		}

		// 20201203 如果什么都为空, 则返回默认的SpringBootBanner
		return DEFAULT_BANNER;
	}

	// 20201203 获取源文本打印横幅实现
	private Banner getTextBanner(Environment environment) {
		// 20201203 获取Banner的定位属性
		String location = environment.getProperty(BANNER_LOCATION_PROPERTY, DEFAULT_BANNER_LOCATION);

		// 20201203 使用资源加载器加载该属性
		Resource resource = this.resourceLoader.getResource(location);
		try {
			// 20201203 如果资源存在 & 资源路径不包括liquibase-core
			if (resource.exists() && !resource.getURL().toExternalForm().contains("liquibase-core")) {
				// 20201203 则构建源文本打印横幅实现
				return new ResourceBanner(resource);
			}
		}
		catch (IOException ex) {
			// Ignore
		}

		// 20201203 如果资源不存在 或者 资源路径包括liquibase-core, 则返回空
		return null;
	}

	// 20201203 获取图片的横幅实现实例
	private Banner getImageBanner(Environment environment) {
		// 20201203 获取图片定位属性spring.banner.image.location
		String location = environment.getProperty(BANNER_IMAGE_LOCATION_PROPERTY);

		// 20201203 如果图片定位属性不为空
		if (StringUtils.hasLength(location)) {
			// 20201203 则使用资源加载器获取该定位的资源
			Resource resource = this.resourceLoader.getResource(location);

			// 20201203 如果该资源物理存在, 则返回图片的横幅实现实例
			return resource.exists() ? new ImageBanner(resource) : null;
		}

		// 20201203 如果定位属性为空, 则遍历图片的后缀名数组"gif", "jpg", "png"
		for (String ext : IMAGE_EXTENSION) {
			// 20201203 查找banner.jpg是否存在
			Resource resource = this.resourceLoader.getResource("banner." + ext);

			// 20201203 如果资源存在, 则返回图片的横幅实现实例
			if (resource.exists()) {
				return new ImageBanner(resource);
			}
		}

		// 20201203 如果没有实际的图片资源, 则返回null
		return null;
	}

	// 20201203 使用Banner创建String实例, 同时输出spring.banner.charset="UFT-8"
	private String createStringFromBanner(Banner banner, Environment environment, Class<?> mainApplicationClass)
			throws UnsupportedEncodingException {
		// 20201203 实例字节输出流
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// 20201203 将横幅打印到指定的打印流。
		banner.printBanner(environment, mainApplicationClass, new PrintStream(baos));

		// 20201203 环境设置UTF-8打印字符编码spring.banner.charset
		String charset = environment.getProperty("spring.banner.charset", "UTF-8");

		// 20201203 输出spring.banner.charset="UFT-8"
		return baos.toString(charset);
	}

	/**
	 * {@link Banner} comprised of other {@link Banner Banners}.
	 */
	// 20201203 {@link Banner}由其他{@link Banner}组成。
	private static class Banners implements Banner {
		// 20201203 Banner集合
		private final List<Banner> banners = new ArrayList<>();

		// 20201203 如果对应的banner实例不为空, 则添加该实例
		void addIfNotNull(Banner banner) {
			if (banner != null) {
				this.banners.add(banner);
			}
		}

		// 20201203 判断是否至少包括一个Banner实例
		boolean hasAtLeastOneBanner() {
			return !this.banners.isEmpty();
		}

		@Override
		public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
			for (Banner banner : this.banners) {
				banner.printBanner(environment, sourceClass, out);
			}
		}

	}

	/**
	 * Decorator that allows a {@link Banner} to be printed again without needing to
	 * specify the source class.
	 */
	// 20201203 一个修饰符，它允许在不需要指定源类的情况下再次打印{@link Banner}。
	private static class PrintedBanner implements Banner {

		private final Banner banner;

		private final Class<?> sourceClass;

		// 20201203 构造PrintedBanner
		PrintedBanner(Banner banner, Class<?> sourceClass) {
			this.banner = banner;
			this.sourceClass = sourceClass;
		}

		@Override
		public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
			sourceClass = (sourceClass != null) ? sourceClass : this.sourceClass;
			this.banner.printBanner(environment, sourceClass, out);
		}

	}

}
