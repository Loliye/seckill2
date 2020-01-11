package com.mikufans.seckill.service.impl;

import com.mikufans.seckill.common.entity.Result;
import com.mikufans.seckill.service.SeckillDistributedService;
import org.springframework.stereotype.Service;

@Service
public class SeckillDistributeServiceImpl implements SeckillDistributedService
{
    @Override
    public Result startSeckillRedisLock(long seckillId, long userId)
    {
        return null;
    }

    @Override
    public Result startSeckillZkLock(long seckillId, long userId)
    {
        return null;
    }

    @Override
    public Result startSeckillLock(long seckillId, long userId, long number)
    {
        return null;
    }
}
