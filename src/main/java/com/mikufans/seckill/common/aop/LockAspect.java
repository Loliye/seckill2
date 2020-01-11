package com.mikufans.seckill.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 同步锁aop
 * order 越小最小执行  但是最小执行 最后结束 ，order的默认值2147483647
 */
@Component
@Scope
@Aspect
@Order(1)
public class LockAspect
{

    /**
     * todo 为什么不用synchronized？
     * service默认是单例的，并发下lock只有一个实例
     */
    private static Lock lock = new ReentrantLock(true);

    /**
     * service层切点  用户记录错误日志
     */
    @Pointcut("@annotation(com.mikufans.seckill.common.aop.ServiceLock)")
    public void lockAspect() {}

    @Around("lockAspect()")
    public Object around(ProceedingJoinPoint joinPoint)
    {
        lock.lock();
        Object obj = null;
        try{
            obj=joinPoint.proceed();
        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }finally
        {
            lock.unlock();
        }
        return obj;
    }

}
