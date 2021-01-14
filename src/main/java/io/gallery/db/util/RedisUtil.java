package io.gallery.db.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RedisUtil {

    private static final Log logger = LogFactory.getLog(RedisUtil.class);
    private static RedisTemplate redisTemplate;

    public static RedisTemplate getRedisTemplate() {//ConfigCacheRedis中定义了RedisTemplate
        return Optional.ofNullable(redisTemplate).orElse(redisTemplate = (RedisTemplate) DataBaseSpringUtil.getBean("redisDataBaseTemplate"));
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public static boolean expire(String key, long time) {
        try {
            if (time > 0) {
                getRedisTemplate().expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            logger.error("expire [" + key + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public static long getExpire(String key) {
        return getRedisTemplate().getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public static boolean hasKey(String key) {
        try {
            return getRedisTemplate().hasKey(key);
        } catch (Exception e) {
            logger.error("hasKey [" + key + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 获取Key
     *
     * @param pattern
     * @return
     */
    public static Set<String> keys(String pattern) {
        try {
            return getRedisTemplate().keys(pattern);
        } catch (Exception e) {
            logger.error("keys [" + pattern + "] fail: " + e.getMessage(), e.getCause());
            return Collections.EMPTY_SET;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                getRedisTemplate().delete(key[0]);
            } else {
                getRedisTemplate().delete(CollectionUtils.arrayToList(key));
            }
        }
    }
// ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return
     */
    public static Object get(String key) {
        return key == null ? null : getRedisTemplate().opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public static boolean set(String key, Object value) {
        try {
            if (value == null) return false;
            getRedisTemplate().opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("set key [" + key + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public static boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                if (value == null) return false;
                getRedisTemplate().opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("set [" + key + "] with expire time [" + time + "s] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public static long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return getRedisTemplate().opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public static long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return getRedisTemplate().opsForValue().increment(key, -delta);
    }
// ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public static Object hget(String key, String item) {
        return getRedisTemplate().opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public static Map<Object, Object> hmget(String key) {
        return getRedisTemplate().opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public static boolean hmset(String key, Map<String, Object> map) {
        try {
            getRedisTemplate().opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            logger.error("hmset [" + key + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public static boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            getRedisTemplate().opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("hmset [" + key + "] with expire [" + time + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public static boolean hset(String key, String item, Object value) {
        try {
            getRedisTemplate().opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            logger.error("hset [" + key + "] with fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public static boolean hset(String key, String item, Object value, long time) {
        try {
            getRedisTemplate().opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("hset [" + key + "] with expire [" + time + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public static void hdel(String key, Object... item) {
        getRedisTemplate().opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public static boolean hHasKey(String key, String item) {
        return getRedisTemplate().opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public static double hincr(String key, String item, double by) {
        return getRedisTemplate().opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public static double hdecr(String key, String item, double by) {
        return getRedisTemplate().opsForHash().increment(key, item, -by);
    }
// ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public static Set<Object> sGet(String key) {
        try {
            return getRedisTemplate().opsForSet().members(key);
        } catch (Exception e) {
            logger.error("sGet [" + key + "] fail: " + e.getMessage(), e.getCause());
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public static boolean sHasKey(String key, Object value) {
        try {
            return getRedisTemplate().opsForSet().isMember(key, value);
        } catch (Exception e) {
            logger.error("sHasKey [" + key + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public static long sSet(String key, Object... values) {
        try {
            return getRedisTemplate().opsForSet().add(key, values);
        } catch (Exception e) {
            logger.error("sSet [" + key + "] fail: " + e.getMessage(), e.getCause());
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public static long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = getRedisTemplate().opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            logger.error("sSetAndTime [" + key + "] with expire [" + time + "] fail: " + e.getMessage(), e.getCause());
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public static long sGetSetSize(String key) {
        try {
            return getRedisTemplate().opsForSet().size(key);
        } catch (Exception e) {
            logger.error("sGetSetSize [" + key + "] fail: " + e.getMessage(), e.getCause());
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public static long setRemove(String key, Object... values) {
        try {
            Long count = getRedisTemplate().opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            logger.error("setRemove [" + key + "] fail: " + e.getMessage(), e.getCause());
            return 0;
        }
    }
// ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public static List<Object> lGet(String key, long start, long end) {
        try {
            return getRedisTemplate().opsForList().range(key, start, end);
        } catch (Exception e) {
            logger.error("lGet [" + key + "] fail: " + e.getMessage(), e.getCause());
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public static long lGetListSize(String key) {
        try {
            return getRedisTemplate().opsForList().size(key);
        } catch (Exception e) {
            logger.error("lGetListSize [" + key + "] fail: " + e.getMessage(), e.getCause());
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public static Object lGetIndex(String key, long index) {
        try {
            return getRedisTemplate().opsForList().index(key, index);
        } catch (Exception e) {
            logger.error("lGetIndex [" + key + "] fail: " + e.getMessage(), e.getCause());
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public static boolean lSet(String key, Object value) {
        try {
            getRedisTemplate().opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            logger.error("lSet [" + key + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public static boolean lSet(String key, Object value, long time) {
        try {
            getRedisTemplate().opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            logger.error("lSet [" + key + "] with expire [" + time + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public static boolean lSet(String key, List<Object> value) {
        try {
            getRedisTemplate().opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            logger.error("lSet [" + key + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public static boolean lSet(String key, List<Object> value, long time) {
        try {
            getRedisTemplate().opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("lSet [" + key + "] with expire [" + time + "] fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public static boolean lUpdateIndex(String key, long index, Object value) {
        try {
            getRedisTemplate().opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            logger.error("lUpdateIndex [" + key + "] with fail: " + e.getMessage(), e.getCause());
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public static long lRemove(String key, long count, Object value) {
        try {
            Long remove = getRedisTemplate().opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            logger.error("lRemove [" + key + "] fail: " + e.getMessage(), e.getCause());
            return 0;
        }
    }
}