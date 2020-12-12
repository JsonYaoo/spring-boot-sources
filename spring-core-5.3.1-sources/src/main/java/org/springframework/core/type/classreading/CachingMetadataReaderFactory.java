/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.core.type.classreading;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * Caching implementation of the {@link MetadataReaderFactory} interface,
 * caching a {@link MetadataReader} instance per Spring {@link Resource} handle
 * (i.e. per ".class" file).
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 2.5
 */
// 20201205 缓存{@link MetadataReaderFactory}接口的实现，每个Spring {@link Resource}句柄（即每个“ .class”文件）缓存一个{@link MetadataReader}实例(用于访问类元数据)。
public class CachingMetadataReaderFactory extends SimpleMetadataReaderFactory {

	// 20201205 本地MetadataReader缓存的默认最大条目数：256。
	/** Default maximum number of entries for a local MetadataReader cache: 256. */
	public static final int DEFAULT_CACHE_LIMIT = 256;

	// 20201205 MetadataReader缓存：本地的或在ResourceLoader级别共享的。
	/** MetadataReader cache: either local or shared at the ResourceLoader level. */
	@Nullable
	private Map<Resource, MetadataReader> metadataReaderCache;

	/**
	 * Create a new CachingMetadataReaderFactory for the default class loader,
	 * using a local resource cache.
	 */
	// 20201212 使用本地资源缓存为默认的类加载器创建一个新的CachingMetadataReaderFactory。
	public CachingMetadataReaderFactory() {
		super();
		setCacheLimit(DEFAULT_CACHE_LIMIT);
	}

	/**
	 * Create a new CachingMetadataReaderFactory for the given {@link ClassLoader},
	 * using a local resource cache.
	 * @param classLoader the ClassLoader to use
	 */
	public CachingMetadataReaderFactory(@Nullable ClassLoader classLoader) {
		super(classLoader);
		setCacheLimit(DEFAULT_CACHE_LIMIT);
	}

	/**
	 * Create a new CachingMetadataReaderFactory for the given {@link ResourceLoader},
	 * using a shared resource cache if supported or a local resource cache otherwise.
	 *
	 * // 20201205 Spring ResourceLoader使用（也确定要使用的ClassLoader）
	 * @param resourceLoader the Spring ResourceLoader to use
	 * (also determines the ClassLoader to use)
	 * @see DefaultResourceLoader#getResourceCache
	 */
	// 20201205 为给定的{@link ResourceLoader}创建一个新的CachingMetadataReaderFactory，如果支持则使用共享资源缓存，否则使用本地资源缓存。
	public CachingMetadataReaderFactory(@Nullable ResourceLoader resourceLoader) {
		// 20201205 为给定的资源加载器创建一个新的CachingMetadataReaderFactory
		super(resourceLoader);

		// 20201205 如果资源加载器属于默认的资源加载器
		if (resourceLoader instanceof DefaultResourceLoader) {
			// 20201205 如果存在(如MetadataReader.class)对应的缓存, 则返回, 否则创建新的缓存ConcurrentHashMap
			this.metadataReaderCache = ((DefaultResourceLoader) resourceLoader).getResourceCache(MetadataReader.class);
		}

		// 20201205 否则如果是其他资源加载器,
		else {
			// 20201205 指定MetadataReader缓存的最大条目数256
			setCacheLimit(DEFAULT_CACHE_LIMIT);
		}
	}


	/**
	 * Specify the maximum number of entries for the MetadataReader cache.
	 * <p>Default is 256 for a local cache, whereas a shared cache is
	 * typically unbounded. This method enforces a local resource cache,
	 * even if the {@link ResourceLoader} supports a shared resource cache.
	 */
	// 20201205 指定MetadataReader缓存的最大条目数。 本地缓存的默认值为256，而共享缓存通常不受限制。 即使{@link ResourceLoader}支持共享资源缓存，此方法也将强制执行本地资源缓存。
	public void setCacheLimit(int cacheLimit) {
		// 20201205 如果指定条目数<=0, 则需要清空MetadataReader缓存
		if (cacheLimit <= 0) {
			this.metadataReaderCache = null;
		}

		// 20201205 否则如果大于0, 且MetadataReader缓存属于本地MetadataReader缓存
		else if (this.metadataReaderCache instanceof LocalResourceCache) {
			// 20201205 则指定本地MetadataReader缓存的最大条目数
			((LocalResourceCache) this.metadataReaderCache).setCacheLimit(cacheLimit);
		}

		// 20201205 否则说明是共享MetadataReader缓存
		else {
			// 20201205 则重新构造并指定本地MetadataReader缓存的最大条目数
			this.metadataReaderCache = new LocalResourceCache(cacheLimit);
		}
	}

	/**
	 * Return the maximum number of entries for the MetadataReader cache.
	 */
	public int getCacheLimit() {
		if (this.metadataReaderCache instanceof LocalResourceCache) {
			return ((LocalResourceCache) this.metadataReaderCache).getCacheLimit();
		}
		else {
			return (this.metadataReaderCache != null ? Integer.MAX_VALUE : 0);
		}
	}


	@Override
	public MetadataReader getMetadataReader(Resource resource) throws IOException {
		if (this.metadataReaderCache instanceof ConcurrentMap) {
			// No synchronization necessary...
			MetadataReader metadataReader = this.metadataReaderCache.get(resource);
			if (metadataReader == null) {
				metadataReader = super.getMetadataReader(resource);
				this.metadataReaderCache.put(resource, metadataReader);
			}
			return metadataReader;
		}
		else if (this.metadataReaderCache != null) {
			synchronized (this.metadataReaderCache) {
				MetadataReader metadataReader = this.metadataReaderCache.get(resource);
				if (metadataReader == null) {
					metadataReader = super.getMetadataReader(resource);
					this.metadataReaderCache.put(resource, metadataReader);
				}
				return metadataReader;
			}
		}
		else {
			return super.getMetadataReader(resource);
		}
	}

	/**
	 * Clear the local MetadataReader cache, if any, removing all cached class metadata.
	 */
	public void clearCache() {
		if (this.metadataReaderCache instanceof LocalResourceCache) {
			synchronized (this.metadataReaderCache) {
				this.metadataReaderCache.clear();
			}
		}
		else if (this.metadataReaderCache != null) {
			// Shared resource cache -> reset to local cache.
			setCacheLimit(DEFAULT_CACHE_LIMIT);
		}
	}

	// 20201205 本地MetadataReader缓存
	@SuppressWarnings("serial")
	private static class LocalResourceCache extends LinkedHashMap<Resource, MetadataReader> {
		// 20201295 本地MetadataReader缓存的最大条目数
		private volatile int cacheLimit;

		// 20201205 构造并指定本地MetadataReader缓存的最大条目数
		public LocalResourceCache(int cacheLimit) {
			super(cacheLimit, 0.75f, true);
			this.cacheLimit = cacheLimit;
		}

		// 20201205 指定本地MetadataReader缓存的最大条目数
		public void setCacheLimit(int cacheLimit) {
			this.cacheLimit = cacheLimit;
		}

		public int getCacheLimit() {
			return this.cacheLimit;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<Resource, MetadataReader> eldest) {
			return size() > this.cacheLimit;
		}
	}

}
