package com.mikufans.seckill.service;

import com.mikufans.seckill.common.entity.Result;

public interface SeckillDistributedService
{
    /**
     * 秒杀一 单个商品
     * @param seckillId 秒杀商品id
     * @param userId 用户id
     * @return
     */
    Result startSeckillRedisLock(long seckillId,long userId);

    /**
     * 秒杀一 单个商品
     * @param seckillId 秒杀商品id
     * @param userId 用户id
     * @return
     */
    Result startSeckillZkLock(long seckillId,long userId);

    /**
     * 秒杀二 多个商品
     * @param seckillId 秒杀商品id
     * @param userId 用户id
     * @param number 秒杀商品的数量
     * @return
     */
    Result startSeckillLock(long seckillId,long userId,long number);
}
