package com.mikufans.seckill.queue.disruptor;

import com.lmax.disruptor.EventHandler;
import com.mikufans.seckill.common.config.SpringUtil;
import com.mikufans.seckill.service.SeckillService;

public class SeckillEventConsumer implements EventHandler<SeckillEvent>
{
    private SeckillService seckillService= SpringUtil.getBean(SeckillService.class);

    @Override
    public void onEvent(SeckillEvent event, long sequence, boolean endOfBatch) throws Exception
    {
        seckillService.startSeckill(event.getSeckillId(),event.getUserId());
    }
}
