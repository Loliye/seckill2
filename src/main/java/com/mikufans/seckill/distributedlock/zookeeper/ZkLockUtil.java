package com.mikufans.seckill.distributedlock.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

/**
 * zookeeper 分布式锁
 */
@Slf4j
public class ZkLockUtil
{
    public static CuratorFramework client;
    private static String address = "127.0.0.1:2181";

    static
    {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(address, retryPolicy);
        client.start();
    }

    /**
     * 私有的默认构造子，保证外界无法直接实例化
     */
    private ZkLockUtil() {}

    public static InterProcessMutex getMutex()
    {
        return SingletonHolder.mutex;
    }

    /**
     * 获取锁
     *
     * @param time
     * @param unit
     * @return
     */
    public static boolean acquire(long time, TimeUnit unit)
    {
        try
        {
            return getMutex().acquire(time, unit);
        } catch (Exception e)
        {
            log.error("zkLockUtil acquire lock is error {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 释放锁
     */
    public static void release()
    {
        try
        {
            getMutex().release();
        } catch (Exception e)
        {
            log.error("zkLockUtil release lock is error {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用到才会被装载，从而实现了延迟加载
     * 针对一件闪屏实现，多见商品同时秒杀建议实现一个map
     */
    private static class SingletonHolder
    {
        /**
         * 静态初始化器，由jvm来保证线程安全
         * 参考：http://ifeve.com/zookeeper-lock/
         * 这里建议 new 一个
         */
        private static InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
    }

}
