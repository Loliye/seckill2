package com.mikufans.seckill.common.aop;

import java.lang.annotation.*;

/**
 * 自定义注解  限流
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceLimit
{
    /**
     * 描述
     *
     * @return
     */
    String description() default "";

    /**
     * key
     *
     * @return
     */
    String key() default "";

    /**
     * 限流类型
     *
     * @return
     */
    LimitType limitType() default LimitType.CUSTOMER;


    enum LimitType
    {
        /**
         * 自定义key
         */
        CUSTOMER,
        /**
         * ip做key
         */
        IP
    }
}
