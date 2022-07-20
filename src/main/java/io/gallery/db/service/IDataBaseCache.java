package io.gallery.db.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 缓存 need @EnableCaching
 */
public interface IDataBaseCache {
    /**
     * 自定义需要 @Bean注解
     *
     * @return CacheManager
     */
    CacheManager getCacheManager();

    /**
     * 获取缓存配置信息
     *
     * @param cacheName String
     * @return Cache
     */
    Cache get(String cacheName);

    /**
     * 清空缓存
     *
     * @param cacheName String
     */
    void clear(String cacheName);

    /**
     * 获取缓存主键
     *
     * @param cacheName String
     * @return Object
     */
    Object keys(String cacheName);
}
