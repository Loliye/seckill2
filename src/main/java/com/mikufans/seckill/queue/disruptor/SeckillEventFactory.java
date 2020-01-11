package com.mikufans.seckill.queue.disruptor;

import com.lmax.disruptor.EventFactory;

public class SeckillEventFactory implements EventFactory<SeckillEvent>
{
    @Override
    public SeckillEvent newInstance()
    {
        return new SeckillEvent();
    }
}
