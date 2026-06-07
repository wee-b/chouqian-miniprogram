package com.quickstart.common.aspect;

import com.quickstart.common.annotation.RateLimit;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Slf4j
public class RateLimitAspect {

    // 限流令牌key
    private static final String Limit_redis_key =  "qs:rate_limit:";

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {

        // 1. 构建 Redis key
        String redisKey = Limit_redis_key + rateLimit.key();

        // 2. 获取或创建 RRateLimiter
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(redisKey);

        // 3. 初始化令牌桶（trySetRate 幂等，第一次设置后不再变更）
        //    RateType.OVERALL：所有实例共享同一个桶
        //    permits：令牌数
        //    1, RateIntervalUnit.SECONDS：时间窗口（1秒）
        rateLimiter.trySetRate(RateType.OVERALL,
                rateLimit.permits(), 1, RateIntervalUnit.SECONDS);

        // 4. 尝试获取令牌
        //    acquireTimeout <= 0 → tryAcquire() 非阻塞
        //    acquireTimeout > 0  → tryAcquire(timeout, MILLISECONDS) 等待
        boolean acquired;
        long timeout = rateLimit.acquireTimeout();
        if (timeout <= 0) {
            acquired = rateLimiter.tryAcquire();
        } else {
            acquired = rateLimiter.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        }


        // 5. 获取失败 → 抛出异常
        if (!acquired) {
            log.warn("接口被限流: key={}, permits={}/s", rateLimit.key(), rateLimit.permits());
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 6. 获取成功 → 放行
        return pjp.proceed();
    }

}
