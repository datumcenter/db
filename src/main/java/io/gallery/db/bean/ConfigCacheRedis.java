package io.gallery.db.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import io.gallery.db.mapper.DataBaseMapper;
import io.gallery.db.service.IDataBaseCache;
import io.gallery.db.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * Redis 缓存配置
 * 默认配置（可移除继承关系，自定义实现CacheManager，并加上@Bean注解）
 *
 * @Configuration
 */
public class ConfigCacheRedis extends CachingConfigurerSupport implements IDataBaseCache {
    protected static CacheManager cacheManager;

    @Autowired
    DataBaseConfig dataBaseConfig;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        Optional.ofNullable(cacheManager).orElse(cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(dataBaseConfig.getTtl())))
                .transactionAware()
                .build());
        return cacheManager;
    }

    @Bean
    public RedisTemplate<String, Object> redisDataBaseTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 配置连接工厂
        redisTemplate.setConnectionFactory(factory);
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许出现特殊字符和转义符
        om.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        // 允许出现单引号
        om.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 字段保留，将null值转为""
        om.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString("");
            }
        });
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        om.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        om.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        jacksonSeial.setObjectMapper(om);
        // 值采用json序列化
        redisTemplate.setValueSerializer(jacksonSeial);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置hash key 和value序列化模式
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jacksonSeial);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Override
    public CacheManager getCacheManager() {
        return cacheManager;
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
        return RedisUtil.keys("*");
    }

}