package com.mikufans.seckill.web;

import com.mikufans.seckill.common.entity.Result;
import com.mikufans.seckill.common.redis.RedisUtil;
import com.mikufans.seckill.queue.activeMq.ActiveMQSender;
import com.mikufans.seckill.queue.kafka.KafkaSender;
import com.mikufans.seckill.queue.redis.RedisSender;
import com.mikufans.seckill.service.SeckillDistributedService;
import com.mikufans.seckill.service.SeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Api(tags = "分布式秒杀")
@RestController
@RequestMapping("/seckillDistributed")
public class SeckillDistributedController
{

    private final Logger logger= LoggerFactory.getLogger(SeckillDistributedController.class);
    private static int corePoolSize = Runtime.getRuntime().availableProcessors();

    //调整队列数  拒绝服务
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, corePoolSize + 1, 101,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000));


    @Autowired
    private SeckillService seckillService;

    @Autowired
    private SeckillDistributedService seckillDistributedService;

    @Autowired
    private RedisSender redisSender;

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private ActiveMQSender activeMQSender;

    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "秒杀一（redis分布式锁）")
    @PostMapping("/startRedisLock")
    public Result startRedisLock(long seckillId)
    {
        seckillService.deleteSeckill(seckillId);

        final long killId = seckillId;
        logger.info("开始秒杀一");
        for (int i = 0; i < 1000; i++)
        {
            final long userId = i;
            Runnable task = () -> {
                Result result = seckillDistributedService.startSeckillRedisLock(killId, userId);
                logger.info("用户:{}{}", userId, result.get("msg"));
            };
            executor.execute(task);
        }

        try
        {
            Thread.sleep(15000);
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            logger.info("一共秒杀出{}件商品", seckillCount);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return Result.ok();
    }


    @ApiOperation(value = "秒杀二（zookeeper分布式锁）")
    @PostMapping("/startZkLock")
    public Result startZkLock(long seckillId)
    {
        seckillService.deleteSeckill(seckillId);

        final long killId = seckillId;
        logger.info("开始秒杀二");
        for (int i = 0; i < 1000; i++)
        {
            final long userId = i;
            Runnable task = () -> {
                Result result = seckillDistributedService.startSeckillZkLock(killId, userId);
                logger.info("用户:{}{}", userId, result.get("msg"));
            };
            executor.execute(task);
        }
        try
        {
            Thread.sleep(10000);
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            logger.info("一共秒杀出{}件商品", seckillCount);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀三(Redis分布式队列-订阅监听)", nickname = "科帮网")
    @PostMapping("/startRedisQueue")
    public Result startRedisQueue(long seckillId)
    {
        redisUtil.cacheValue(seckillId + "", null);//秒杀结束
        seckillService.deleteSeckill(seckillId);
        final long killId = seckillId;
        logger.info("开始秒杀三");
        for (int i = 0; i < 1000; i++)
        {
            final long userId = i;
            Runnable task = () -> {
                if (redisUtil.getValue(killId + "") == null)
                {
                    //思考如何返回给用户信息ws
                    redisSender.sendChannelMessage("seckill", killId + ";" + userId);
                } else
                {
                    //秒杀结束
                }
            };
            executor.execute(task);
        }
        try
        {
            Thread.sleep(10000);
            redisUtil.cacheValue(killId + "", null);
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            logger.info("一共秒杀出{}件商品", seckillCount);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀四(Kafka分布式队列)", nickname = "科帮网")
    @PostMapping("/startKafkaQueue")
    public Result startKafkaQueue(long seckillId)
    {
        seckillService.deleteSeckill(seckillId);
        final long killId = seckillId;
        logger.info("开始秒杀四");
        for (int i = 0; i < 1000; i++)
        {
            final long userId = i;
            Runnable task = () -> {
                if (redisUtil.getValue(killId + "") == null)
                {
                    //思考如何返回给用户信息ws
                    kafkaSender.sendChannelMess("seckill", killId + ";" + userId);
                } else
                {
                    //秒杀结束
                }
            };
            executor.execute(task);
        }
        try
        {
            Thread.sleep(10000);
            redisUtil.cacheValue(killId + "", null);
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            logger.info("一共秒杀出{}件商品", seckillCount);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀五(ActiveMQ分布式队列)", nickname = "科帮网")
    @PostMapping("/startActiveMQQueue")
    public Result startActiveMQQueue(long seckillId)
    {
        seckillService.deleteSeckill(seckillId);
        final long killId = seckillId;
        logger.info("开始秒杀五");
        for (int i = 0; i < 1000; i++)
        {
            final long userId = i;
            Runnable task = () -> {
                if (redisUtil.getValue(killId + "") == null)
                {
                    Destination destination = new ActiveMQQueue("seckill.queue");
                    //思考如何返回给用户信息ws
                    activeMQSender.sendChannelMess(destination, killId + ";" + userId);
                } else
                {
                    //秒杀结束
                }
            };
            executor.execute(task);
        }
        try
        {
            Thread.sleep(10000);
            redisUtil.cacheValue(killId + "", null);
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            logger.info("一共秒杀出{}件商品", seckillCount);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return Result.ok();
    }

}
