package com.mikufans.seckill.service;

import com.mikufans.seckill.common.entity.Result;
import com.mikufans.seckill.common.entity.Seckill;

import java.util.List;


public interface SeckillService
{
    /**
     * 查询全部的秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 查询秒杀售卖商品
     * @param seckillId
     * @return
     */
    Long getSeckillCount(long seckillId);

    /**
     * 删除秒杀商品记录
     * @param seckillId
     */
    void deleteSeckill(long seckillId);

    /**
     * 秒杀一、 会出现数量错误
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckill(long seckillId,long userId);

    /**
     * 秒杀二、程序锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckillLock(long seckillId,long userId);

    /**
     * 秒杀二、程序锁aop
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckillAopLock(long seckillId,long userId);

    /**
     * 秒杀二、数据库悲观锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckillDBPCC_ONE(long seckillId,long userId);

    /**
     * 秒杀 三、数据库悲观锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilDBPCC_TWO(long seckillId,long userId);
    /**
     * 秒杀 三、数据库乐观锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilDBOCC(long seckillId,long userId,long number);

    /**
     * 秒杀 四、事物模板
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilTemplate(long seckillId,long userId,long number);

}
