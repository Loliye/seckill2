package com.mikufans.seckill.queue.redis;

import com.mikufans.seckill.common.config.WebSocketServer;
import com.mikufans.seckill.common.entity.Result;
import com.mikufans.seckill.common.enums.SeckillStateEnum;
import com.mikufans.seckill.common.redis.RedisUtil;
import com.mikufans.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisConsumer {

    @Autowired
    private SeckillService seckillService;
    @Autowired
    private RedisUtil redisUtil;

    public void receiveMessage(String message) {
        Thread th=Thread.currentThread();
        System.out.println("Tread name:"+th.getName());
        //收到通道的消息之后执行秒杀操作(超卖)
        String[] array = message.split(";");
        if(redisUtil.getValue(array[0])==null){//control层已经判断了，其实这里不需要再判断了
            Result result = seckillService.startSeckilDBPCC_TWO(Long.parseLong(array[0]), Long.parseLong(array[1]));
            if(result.equals(Result.ok(SeckillStateEnum.SUCCESS))){
                WebSocketServer.sendInfo(array[0], "秒杀成功");//推送给前台
            }else{
                WebSocketServer.sendInfo(array[0], "秒杀失败");//推送给前台
                redisUtil.cacheValue(array[0], "ok");//秒杀结束
            }
        }else{
            WebSocketServer.sendInfo(array[0], "秒杀失败");//推送给前台
        }
    }
}

//@Slf4j
//@Service
//public class RedisConsumer
//{
//    @Autowired
//    private SeckillService seckillService;
//
//    @Autowired
//    private RedisUtil redisUtil;
//
//    public void receivMessage(String message)
//    {
//        Thread thread = Thread.currentThread();
//        log.info("Thread name: {}", thread.getName());
//        String[] array = message.split(";");
//
//        if (redisUtil.getValue(array[0]) == null)
//        {//control层已经判断了，其实这里不需要再判断了
//            Result result = seckillService.startSeckilDBPCC_TWO(Long.parseLong(array[0]), Long.parseLong(array[1]));
//            if (result.equals(Result.ok(SeckillStateEnum.SUCCESS)))
//            {
//                WebSocketServer.sendInfo(array[0], "秒杀成功");//推送给前台
//            } else
//            {
//                WebSocketServer.sendInfo(array[0], "秒杀失败");//推送给前台
//                redisUtil.cacheValue(array[0], "ok");//秒杀结束
//            }
//        } else
//        {
//            WebSocketServer.sendInfo(array[0], "秒杀失败");//推送给前台
//        }
//    }
//}
