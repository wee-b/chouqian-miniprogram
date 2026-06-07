package com.quickstart.common.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流 key 前缀，最终 Redis key = "qs:rate_limit:" + key */
    String key();

    /** 每秒生成的令牌数 */
    long permits() default 50;

    /** 获取令牌最大等待时间，单位毫秒。0 = 不等待，拿不到直接返回限流 */
    long acquireTimeout() default 0;
}
