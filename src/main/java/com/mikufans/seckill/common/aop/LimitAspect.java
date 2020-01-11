package com.mikufans.seckill.common.aop;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.mikufans.seckill.common.exception.RrException;
import com.mikufans.seckill.common.util.IPUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 限流aop
 */
@Aspect
@Configuration
public class LimitAspect
{
    /**
     * 根据ip分不同的令牌桶，每天自动清理缓存
     */
    private static LoadingCache<String, RateLimiter> caches = CacheBuilder.newBuilder()
            .maximumSize(1000).expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, RateLimiter>()
            {
                @Override
                public RateLimiter load(String key) throws Exception
                {
                    //新的ip初始化，每秒只发出5个令牌
                    return RateLimiter.create(5);
                }
            });

    /**
     * service层切面  限流
     */
    @Pointcut("@annotation(com.mikufans.seckill.common.aop.ServiceLimit)")
    public void serviceAspect() {}

    @Around("serviceAspect()")
    public Object around(ProceedingJoinPoint joinPoint)
    {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ServiceLimit limit = method.getAnnotation(ServiceLimit.class);
        ServiceLimit.LimitType limitType = limit.limitType();
        String key = limit.key();

        Object object;
        try
        {
            if (limitType.equals(ServiceLimit.LimitType.IP))
                key = IPUtil.getIpAddr();

            RateLimiter rateLimiter = caches.get(key);
            Boolean flag = rateLimiter.tryAcquire();
            if (flag)
                object = joinPoint.proceed();
            else throw new RrException("你访问的太频繁了");

        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
            throw new RrException("你访问的太频繁了");
        }
        return object;
    }

}
