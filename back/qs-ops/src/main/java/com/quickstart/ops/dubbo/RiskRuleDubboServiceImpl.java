package com.quickstart.ops.dubbo;

import com.quickstart.api.dto.RiskContextDTO;
import com.quickstart.api.dto.RiskResultDTO;
import com.quickstart.api.dubbo.RiskRuleDubboService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 风控规则 Dubbo Provider 实现。
 *
 * 【Dubbo 价值体现点】
 * 参与抽奖热路径上调用，用户等着响应，延迟敏感。
 * Dubbo 长连接省去 HTTP 每次握手，热路径延迟更低。
 *
 * 实现：
 * - 黑名单三件套：用户/设备维度，Redis Set 存储，O(1) 查询
 * - 滑动窗口限流：单 IP 60 秒内参与频次，Redis SortedSet（分数存时间戳）
 */
@DubboService
public class RiskRuleDubboServiceImpl implements RiskRuleDubboService {

    private final StringRedisTemplate redisTemplate;

    public RiskRuleDubboServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RiskResultDTO check(RiskContextDTO ctx) {
        // 1. 黑名单优先校验
        String userId = ctx.getUserId() == null ? null : String.valueOf(ctx.getUserId());
        if (isBlacklisted(userId, ctx.getDeviceId())) {
            return RiskResultDTO.block("BLACKLIST", 100);
        }

        // 2. 滑动窗口：单 IP 60 秒内参与次数
        if (ctx.getIp() != null) {
            String ipKey = "risk:ip:" + ctx.getIp();
            long now = System.currentTimeMillis();
            // 用时间戳作 score 和 member，追加本次记录
            redisTemplate.opsForZSet().add(ipKey, String.valueOf(now), now);
            // 清理 60 秒前的记录（滑动窗口）
            redisTemplate.opsForZSet().removeRangeByScore(ipKey, 0, now - 60_000L);
            Long count = redisTemplate.opsForZSet().size(ipKey);
            if (count != null && count > 10) {
                return RiskResultDTO.block("IP_FREQ_LIMIT", 80);
            }
        }
        return RiskResultDTO.pass();
    }

    @Override
    public boolean isBlacklisted(String userId, String deviceId) {
        if (userId != null
                && Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("risk:blacklist:user", userId))) {
            return true;
        }
        return deviceId != null
                && Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("risk:blacklist:device", deviceId));
    }
}
