package com.quickstart.ops.dubbo;

import com.quickstart.api.dubbo.IdDubboService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 分布式 ID Dubbo Provider 实现。
 *
 * 【Dubbo 价值体现点】
 * 超高频 + 强一致同步调用，Dubbo 同步调用比 HTTP 更适合。
 * 替代 qs-draw 中硬编码 workerId=1/datacenterId=1 的 Hutool Snowflake
 * （多实例部署会生成重复 ID 的 bug）。
 *
 * 当前用 Redis incr 实现（简单可靠）；后续可升级为美团 Leaf 式
 * 号段 + Snowflake 双 buffer（减少 Redis 压力）。
 */
@DubboService
public class IdDubboServiceImpl implements IdDubboService {

    private final StringRedisTemplate redisTemplate;

    public IdDubboServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public long nextId(String bizKey) {
        // 按 bizKey 分桶自增，不同业务不互相阻塞
        return redisTemplate.opsForValue().increment("ops:id:" + bizKey);
    }

    @Override
    public String nextCode(String prefix, String bizKey) {
        long id = nextId(bizKey);
        return prefix + String.format("%06d", id);
    }
}
