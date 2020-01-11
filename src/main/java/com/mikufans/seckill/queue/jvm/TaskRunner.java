package com.mikufans.seckill.queue.jvm;

import com.mikufans.seckill.common.entity.SuccessKilled;
import com.mikufans.seckill.service.SeckillService;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TaskRunner implements ApplicationRunner
{
    @Autowired
    private SeckillService seckillService;

    @Override
    public void run(ApplicationArguments args) throws Exception
    {
        while (true)
        {
            SuccessKilled killed = SeckillQueue.getMailQueue().consume();
            if (killed != null)
                seckillService.startSeckill(killed.getSeckillId(), killed.getUserId());
        }
    }
}
