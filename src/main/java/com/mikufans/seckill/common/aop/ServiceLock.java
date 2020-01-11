package com.mikufans.seckill.common.aop;

import java.lang.annotation.*;

/**
 * 自定义注解  同步锁
 */
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceLock
{
    String description() default "";
}
