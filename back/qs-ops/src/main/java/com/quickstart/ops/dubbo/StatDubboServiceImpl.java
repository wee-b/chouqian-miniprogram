package com.quickstart.ops.dubbo;

import com.quickstart.api.dubbo.StatDubboService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 实时统计 Dubbo Provider 实现。
 *
 * 【Dubbo 价值体现点】
 * 参与人数实时聚合，高频写。Dubbo 长连接适合高频小消息，
 * 比 HTTP 短连接的握手开销更优。
 *
 * 实现：Redis 原子计数，参与时 incr，查询时 get。
 * 在线人数由 WebSocket 连接建立/断开时维护。
 */
@DubboService
public class StatDubboServiceImpl implements StatDubboService {

    private final StringRedisTemplate redisTemplate;

    public StatDubboServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void incrPartCount(Long drawId) {
        redisTemplate.opsForValue().increment("ops:stat:part:" + drawId);
    }

    @Override
    public long getPartCount(Long drawId) {
        String v = redisTemplate.opsForValue().get("ops:stat:part:" + drawId);
        return v == null ? 0L : Long.parseLong(v);
    }

    @Override
    public long getOnlineCount(Long drawId) {
        String v = redisTemplate.opsForValue().get("ops:stat:online:" + drawId);
        return v == null ? 0L : Long.parseLong(v);
    }
}
