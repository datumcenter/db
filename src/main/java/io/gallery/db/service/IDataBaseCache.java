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
     * @return
     */
    CacheManager getCacheManager();

    /**
     * 获取缓存配置信息
     *
     * @param cacheName
     * @return
     */
    Cache get(String cacheName);

    /**
     * 清空缓存
     *
     * @param cacheName
     */
    void clear(String cacheName);

    /**
     * 获取缓存主键
     *
     * @param cacheName
     * @return
     */
    Object keys(String cacheName);
}
