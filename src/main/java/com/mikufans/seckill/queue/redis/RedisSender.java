package com.mikufans.seckill.queue.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * redis生产者
 */
@Service
public class RedisSender
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 向通道发送消息
     * @param channel
     * @param message
     */
    public void sendChannelMessage(String channel,String message)
    {
        stringRedisTemplate.convertAndSend(channel,message);
    }
}
