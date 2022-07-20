package io.gallery.db.bean;

import io.gallery.db.mapper.DataBaseMapper;
import io.gallery.db.service.IDataBaseCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * 自定义缓存配置
 * <p>
 * Configuration
 */
public class ConfigCacheLocal implements IDataBaseCache {
    private static CacheManager cacheManager;

    @Bean
    @Override
    public CacheManager getCacheManager() {
        return Optional.ofNullable(cacheManager).orElse(cacheManager = new ConcurrentMapCacheManager());
    }

    @Override
    public Cache get(String cacheName) {
        cacheName = Optional.ofNullable(cacheName).orElse(DataBaseMapper.CACHE_COLUMNS_NAME);
        return cacheManager.getCache(cacheName);
    }

    @Override
    public void clear(String cacheName) {
        get(cacheName).clear();
    }

    @Override
    public Object keys(String cacheName) {
        return ((ConcurrentMapCache) get(cacheName)).getNativeCache().keySet().stream().map(Object::toString);
    }
}
