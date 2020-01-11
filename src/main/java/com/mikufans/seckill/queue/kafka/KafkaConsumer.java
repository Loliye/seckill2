package com.mikufans.seckill.queue.kafka;

import com.mikufans.seckill.common.config.WebSocketServer;
import com.mikufans.seckill.common.entity.Result;
import com.mikufans.seckill.common.redis.RedisUtil;
import com.mikufans.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * kafka 消费者
 */
@Component
public class KafkaConsumer
{
    @Autowired
    private SeckillService seckillService;

    private static RedisUtil redisUtil=new RedisUtil();

    /**
     * 监听seckill主题 ，有消息就读取
     * @param message
     */
    @KafkaListener(topics = {"seckill"})
    public void receiveMessage(String message)
    {
        String[] array=message.split(";");

        if(redisUtil.getValue(array[0])==null)
        {
            Result result=seckillService.startSeckill(Long.parseLong(array[0]),Long.parseLong(array[1]));

            //推送给前台
            if(result.equals(Result.ok()))
            {
                WebSocketServer.sendInfo(array[0],"秒杀成功");
            }else
            {

                WebSocketServer.sendInfo(array[0],"渺少失败");
                //秒杀结束
                redisUtil.cacheValue(array[0],"ok");
            }
        }else
        {
            WebSocketServer.sendInfo(array[0],"秒杀失败");
        }
    }
}
