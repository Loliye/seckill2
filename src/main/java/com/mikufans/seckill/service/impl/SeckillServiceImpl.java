package com.mikufans.seckill.service.impl;

import com.mikufans.seckill.common.aop.ServiceLimit;
import com.mikufans.seckill.common.aop.ServiceLock;
import com.mikufans.seckill.common.dynamicquery.DynamicQuery;
import com.mikufans.seckill.common.entity.Result;
import com.mikufans.seckill.common.entity.Seckill;
import com.mikufans.seckill.common.entity.SuccessKilled;
import com.mikufans.seckill.common.enums.SeckillStateEnum;
import com.mikufans.seckill.repository.SeckillRepository;
import com.mikufans.seckill.service.SeckillService;
import com.sun.net.httpserver.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillServiceImpl implements SeckillService
{

    /**
     * 思考：为什么不用synchronized
     * service 默认是单例的，并发下lock只有一个实例
     */
    private Lock lock = new ReentrantLock(true);

    @Autowired
    private DynamicQuery dynamicQuery;

    @Autowired
    private SeckillRepository seckillRepository;


    @Override
    public List<Seckill> getSeckillList()
    {
        return seckillRepository.findAll();
    }

    @Override
    public Seckill getById(long seckillId)
    {
        return seckillRepository.getOne(seckillId);
    }

    @Override
    public Long getSeckillCount(long seckillId)
    {
        String nativeSql = "select count(*) from success_killed where seckill_id=?";
        Object object = dynamicQuery.nativeQueryObject(nativeSql, new Object[]{seckillId});
        return ((Number) object).longValue();
    }

    @Override
    public void deleteSeckill(long seckillId)
    {
        String sql = "delete from success_killed where seckill_id=?";
        dynamicQuery.nativeExecuteUpdate(sql, new Object[]{seckillId});
        sql = "update seckill set number = 100 where seckill_id=?";
        dynamicQuery.nativeExecuteUpdate(sql, new Object[]{seckillId});
    }

    @Override
    @ServiceLimit(limitType = ServiceLimit.LimitType.IP)
    @Transactional
    public Result startSeckill(long seckillId, long userId)
    {
        //校验库存
        String sql = "select number from seckill where seckill_id=?";
        Object object = dynamicQuery.nativeQueryObject(sql, new Object[]{seckillId});
        Long number = ((Number) object).longValue();
        if (number > 0)
        {
            //减少库存
            sql = "update seckill set number=number-1 where seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(sql, new Object[]{seckillId});
            //创建订单
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            Timestamp createTime = new Timestamp(new Date().getTime());
            killed.setCreateTime(createTime);
            killed.setSeckillId(0);
            dynamicQuery.save(killed);
            /**
             * 这里仅仅是分表而已，提供一种思路，供参考，测试的时候自行建表
             * 按照用户 ID 来做 hash分散订单数据。
             * 要扩容的时候，为了减少迁移的数据量，一般扩容是以倍数的形式增加。
             * 比如原来是8个库，扩容的时候，就要增加到16个库，再次扩容，就增加到32个库。
             * 这样迁移的数据量，就小很多了。
             * 这个问题不算很大问题，毕竟一次扩容，可以保证比较长的时间，而且使用倍数增加的方式，已经减少了数据迁移量。
             */

            String table = "success_killed_" + userId % 8;
            sql = "INSERT INTO " + table + " (seckill_id, user_id,state,create_time)VALUES(?,?,?,?)";
            Object[] params = new Object[]{seckillId, userId, 0, createTime};
            dynamicQuery.nativeExecuteUpdate(sql, params);
            //todo  支付服务


            return Result.ok(SeckillStateEnum.SUCCESS);
        } else return Result.error(SeckillStateEnum.END);
    }

    @Override
    @Transactional
    public Result startSeckillLock(long seckillId, long userId)
    {
        try
        {
            lock.lock();
            /**
             * 1)这里、不清楚为啥、总是会被超卖101、难道锁不起作用、lock是同一个对象
             * 2)来自热心网友 zoain 的细心测试思考、然后自己总结了一下,事物未提交之前，锁已经释放(事物提交是在整个方法执行完)，导致另一个事物读取到了这个事物未提交的数据，也就是传说中的脏读。建议锁上移
             * 3)给自己留个坑思考：为什么分布式锁(zk和redis)没有问题？(事实是有问题的，由于redis释放锁需要远程通信，不那么明显而已)
             * 4)2018年12月35日，更正一下,之前的解释（脏读）可能给大家一些误导,数据库默认的事务隔离级别为 可重复读(repeatable-read)，也就不可能出现脏读
             * 哪个这个级别是只能是幻读了？分析一下：幻读侧重于新增或删除，这里显然不是，那这里到底是什么，给各位大婶留个坑~~~~
             */

            String sql = "select number from seckill where seckill_id=?";
            Object object = dynamicQuery.nativeQueryObject(sql, new Object[]{seckillId});
            Long number = ((Number) object).longValue();
            if (number > 0)
            {
                sql = "update seckill set number=number-1 where seckill_id=?";
                dynamicQuery.nativeExecuteUpdate(sql, new Object[]{seckillId});
                SuccessKilled killed = new SuccessKilled();
                killed.setSeckillId(seckillId);
                killed.setUserId(userId);
                killed.setState(Short.parseShort(number + ""));
                killed.setCreateTime(new Timestamp(new Date().getTime()));
                dynamicQuery.save(killed);
            } else return Result.error(SeckillStateEnum.END);

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
        }
        return Result.ok(SeckillStateEnum.SUCCESS);
    }

    @Override
    @ServiceLock
    @Transactional
    public Result startSeckillAopLock(long seckillId, long userId)
    {
        //使用aop+锁实现
        String sql = "select number from seckill where seckill_id=?";
        Object object = dynamicQuery.nativeQueryObject(sql, new Object[]{seckillId});
        Long number = ((Number) object).longValue();
        if (number > 0)
        {
            sql = "update seckill set number=number-1 where seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(sql, new Object[]{seckillId});
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState(Short.parseShort(number + ""));
            killed.setCreateTime(new Timestamp(new Date().getTime()));
            dynamicQuery.save(killed);
        } else
        {
            return Result.error(SeckillStateEnum.END);
        }
        return Result.ok(SeckillStateEnum.SUCCESS);

    }

    @ServiceLimit(limitType = ServiceLimit.LimitType.IP)
    @Transactional
    @Override
    public Result startSeckillDBPCC_ONE(long seckillId, long userId)
    {
        //单用户抢购一件商品或者多件都没有问题
        String nativeSql = "UPDATE seckill  SET number=number-1 WHERE seckill_id=? AND number>0";//UPDATE锁表
        Object object = dynamicQuery.nativeQueryObject(nativeSql, new Object[]{seckillId});
        Long number = ((Number) object).longValue();
        if (number > 0)
        {
            nativeSql = "UPDATE seckill  SET number=number-1 WHERE seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short) 0);
            killed.setCreateTime(new Timestamp(new Date().getTime()));
            dynamicQuery.save(killed);
            return Result.ok(SeckillStateEnum.SUCCESS);
        } else
        {
            return Result.error(SeckillStateEnum.END);
        }
    }

    /**
     * SHOW STATUS LIKE 'innodb_row_lock%';
     * 如果发现锁争用比较严重，如InnoDB_row_lock_waits和InnoDB_row_lock_time_avg的值比较高
     */
    @Override
    @Transactional
    public Result startSeckilDBPCC_TWO(long seckillId, long userId)
    {
        //单用户抢购一件商品没有问题、但是抢购多件商品不建议这种写法
        String nativeSql = "UPDATE seckill  SET number = number - 1 WHERE seckill_id = ? AND number > 0";//UPDATE锁表
        int count = dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
        if (count > 0)
        {
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short) 0);
            killed.setCreateTime(new Timestamp(new Date().getTime()));
            dynamicQuery.save(killed);
            return Result.ok(SeckillStateEnum.SUCCESS);
        } else
        {
            return Result.error(SeckillStateEnum.END);
        }
    }

    @Override
    @Transactional
    public Result startSeckilDBOCC(long seckillId, long userId, long number)
    {
        Seckill kill = seckillRepository.getOne(seckillId);
        //if(kill.getNumber()>0){
        if (kill.getNumber() >= number)
        {//剩余的数量应该要大于等于秒杀的数量
            //乐观锁
            String nativeSql = "UPDATE seckill  SET number=number-?,version=version+1 WHERE seckill_id=? AND version = ?";
            int count = dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{number, seckillId, kill.getVersion()});
            if (count > 0)
            {
                SuccessKilled killed = new SuccessKilled();
                killed.setSeckillId(seckillId);
                killed.setUserId(userId);
                killed.setState((short) 0);
                killed.setCreateTime(new Timestamp(new Date().getTime()));
                dynamicQuery.save(killed);
                return Result.ok(SeckillStateEnum.SUCCESS);
            } else
            {
                return Result.error(SeckillStateEnum.END);
            }
        } else
        {
            return Result.error(SeckillStateEnum.END);
        }
    }

    @Override
    public Result startSeckilTemplate(long seckillId, long userId, long number)
    {
        return null;
    }
}
