package com.mikufans.seckill.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 */

@Component
public class RedisUtil
{
    public static final String KEY_PREFIX_VALUE = "itstyle:seckill:value:";
    /**
     * 前缀
     */

    private final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    @Resource
    private RedisTemplate<Serializable, Serializable> redisTemplate;

    /**
     * 缓存value操作
     *
     * @param k
     * @param v
     * @param time
     * @return
     */
    public boolean cacheValue(String k, Serializable v, long time)
    {
        String key = KEY_PREFIX_VALUE + k;
        ValueOperations<Serializable, Serializable> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, v);
        if (time > 0)
        {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
            return true;
        } else
        {
            logger.error("缓存[{}]失败,value[{}]", key, v);
            return false;
        }
    }

    public boolean cacheValue(String k, Serializable v, long time, TimeUnit unit)
    {
        String key = KEY_PREFIX_VALUE + k;
        ValueOperations<Serializable, Serializable> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, v);
        if (time > 0)
        {
            redisTemplate.expire(key, time, unit);
            return true;
        } else
        {
            logger.error("缓存[{}]失败,value[{}]", key, v);
            return false;
        }
    }

    public boolean cacheValue(String k, Serializable v)
    {
        return cacheValue(k, v, -1);
    }

    /**
     * 是否存在key
     *
     * @param k
     * @return
     */
    public boolean containsValueKey(String k)
    {
        String key = KEY_PREFIX_VALUE + k;
        try
        {
            return redisTemplate.hasKey(key);
        } catch (Throwable throwable)
        {
            logger.error("判断缓存存在失败key[" + key + ", error[" + throwable + "]");
        }
        return false;
    }

    /**
     * 取缓存
     *
     * @param k
     * @return
     */
    public Serializable getValue(String k)
    {
        String key = KEY_PREFIX_VALUE + k;
        try
        {
            ValueOperations<Serializable, Serializable> valueOperations = redisTemplate.opsForValue();
            return valueOperations.get(key);
        } catch (Throwable throwable)
        {
            logger.error("获取缓存失败key[" + KEY_PREFIX_VALUE + k + ", error[" + throwable + "]");
        }
        return null;
    }

    /**
     * 移除缓存
     *
     * @param k
     * @return
     */
    public boolean removeValue(String k)
    {
        String key = KEY_PREFIX_VALUE + k;
        try
        {
            redisTemplate.delete(key);
            return true;
        } catch (Throwable t)
        {
            logger.error("获取缓存失败key[" + key + ", error[" + t + "]");
        }
        return false;
    }


}
