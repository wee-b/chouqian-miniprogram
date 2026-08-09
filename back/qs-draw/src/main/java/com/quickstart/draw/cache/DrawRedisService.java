package com.quickstart.draw.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.draw.vo.DrawSmallVO;
import com.quickstart.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽签服务 Redis 操作统一出口。
 * 【职责】
 * 收敛 qs-draw 内所有 StringRedisTemplate 调用：key 前缀拼接、TTL 策略、序列化、原子操作，
 * 调用方只传业务参数（drawId / userId / passCode），不再感知 key / ttl / opsForXxx。
 * 【为什么独立成类】
 * 原先 5 个类各自注入 StringRedisTemplate，key 前缀散落在 RedisConstant + 各类硬编码 + 方法内拼接，
 * TTL 散落（5min 硬编码 / deadline 动态算 / hours 传参），还有两处 checkAndIncrement 逻辑完全重复。
 * 统一出口后：key 集中、TTL 内聚、重复消除，后续调缓存策略只改本类。
 * 【边界】
 * - 仅封装 qs-draw 业务的 Redis 用途（官方缓存 / 参与限制 / 参与计数 / 口令码）。
 * - qs-ops 的 Redis（id / oplog / stat / risk）由 OpsRedisService 另管，不在此类。
 * - Redisson 分布式锁（DrawCodeGenerator 用）不走 StringRedisTemplate，仍在 RedisConstant.CODE_GENERATOR_LOCK。
 */
@Slf4j
@Service
public class DrawRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== key 前缀集中管理（不再散落到业务类）====================
    /** 官方抽签列表缓存（String 存 JSON） */
    private static final String OFFICIAL_DRAW_KEY = "cache:officialDraws";
    /** 参与次数上限：draw:partLimit:{drawId} → 数值 */
    private static final String PART_LIMIT_PREFIX = "draw:partLimit:";
    /** 每人参与计数：draw:partCount:{drawId} → Hash{ userId: count } */
    private static final String PART_COUNT_PREFIX = "draw:partCount:";
    /** 口令码双向映射：client:draw:passcode:{passcode} → drawId；client:draw:passcode:draw:{drawId} → passcode */
    private static final String PASSCODE_PREFIX = "client:draw:passcode:";
    private static final String PASSCODE_DRAW_SUFFIX = "draw:";

    /** 官方抽签缓存 TTL */
    private static final Duration OFFICIAL_DRAW_TTL = Duration.ofMinutes(5);

    public DrawRedisService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // ==================== 1. 官方抽签缓存 ====================

    /**
     * 读官方抽签列表缓存（L2 Redis），命中返回列表，未命中或解析失败返回 null（调用方回源 MySQL）。
     */
    public List<DrawSmallVO> getOfficialDrawCache() {
        String json = redisTemplate.opsForValue().get(OFFICIAL_DRAW_KEY);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("官方抽签缓存反序列化失败，将回源 MySQL", e);
            return null;
        }
    }

    /**
     * 写官方抽签列表缓存（L2 Redis），TTL 5min，序列化内置。
     */
    public void setOfficialDrawCache(List<DrawSmallVO> list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            redisTemplate.opsForValue().set(OFFICIAL_DRAW_KEY, json, OFFICIAL_DRAW_TTL);
        } catch (Exception e) {
            log.warn("官方抽签缓存写入失败", e);
        }
    }

    /**
     * 失效官方抽签缓存。
     */
    public void evictOfficialDrawCache() {
        redisTemplate.delete(OFFICIAL_DRAW_KEY);
    }

    // ==================== 2. 参与次数限制 ====================

    /**
     * 读取某抽签的每人参与次数上限，未设置返回 null。
     */
    public Integer getPartLimit(Long drawId) {
        String v = redisTemplate.opsForValue().get(PART_LIMIT_PREFIX + drawId);
        return v == null ? null : Integer.parseInt(v);
    }

    /**
     * 同步参与次数上限到 Redis，TTL 随 joinDeadline 自然到期。
     */
    public void setPartLimit(Long drawId, Integer partLimit, LocalDateTime joinDeadline) {
        if (partLimit == null || partLimit <= 0) {
            return;
        }
        String key = PART_LIMIT_PREFIX + drawId;
        redisTemplate.opsForValue().set(key, String.valueOf(partLimit));
        if (joinDeadline != null) {
            long ttlSeconds = Duration.between(LocalDateTime.now(), joinDeadline).getSeconds();
            if (ttlSeconds > 0) {
                redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
            }
        }
    }

    // ==================== 3. 每人参与计数（Hash 原子）====================

    /**
     * 原子增减某用户在某抽签的参与次数，返回最新计数值。
     */
    public long incrPartCount(Long drawId, Long userId, int delta) {
        return redisTemplate.opsForHash()
                .increment(PART_COUNT_PREFIX + drawId, String.valueOf(userId), delta);
    }

    /**
     * 获取某用户在某抽签的当前参与次数。
     */
    public long getPartCount(Long drawId, Long userId) {
        Object v = redisTemplate.opsForHash()
                .get(PART_COUNT_PREFIX + drawId, String.valueOf(userId));
        return v == null ? 0L : Long.parseLong(v.toString());
    }

    /**
     * 检查并占用一次参与名额：未设上限直接放行；超限则回退并抛业务异常。
     * 原子操作，原先散落在 LocalDrawCodeServiceImpl / MqDrawCodeServiceImpl 重复实现，现收敛至此。
     */
    public boolean acquirePartCount(Long drawId, Long userId) {
        Integer limit = getPartLimit(drawId);
        if (limit == null) {
            return false; // 未设置上限，无需占用计数
        }
        long newCount = incrPartCount(drawId, userId, 1);
        if (newCount > limit) {
            incrPartCount(drawId, userId, -1); // 回退
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "您已达到参与次数上限");
        }
        return true;
    }

    /**
     * 释放一次已占用的参与名额。用于业务事务失败后的 Redis 计数补偿。
     */
    public void releasePartCount(Long drawId, Long userId) {
        Integer limit = getPartLimit(drawId);
        if (limit == null) {
            return;
        }
        long newCount = incrPartCount(drawId, userId, -1);
        if (newCount < 0) {
            incrPartCount(drawId, userId, 1);
        }
    }

    // ==================== 4. 口令码双向映射 ====================

    /**
     * 建立口令码 ↔ 抽签ID 双向映射，TTL = expireHours 小时。
     */
    public void setPassCode(String passCode, Long drawId, int expireHours) {
        Duration ttl = Duration.ofHours(expireHours);
        redisTemplate.opsForValue().set(PASSCODE_PREFIX + passCode, String.valueOf(drawId), ttl);
        redisTemplate.opsForValue().set(PASSCODE_PREFIX + PASSCODE_DRAW_SUFFIX + drawId, passCode, ttl);
    }

    /**
     * 校验口令码是否已存在（用于生成时去重，hasKey 比 get 更高效）。
     */
    public boolean existsPassCode(String passCode) {
        return redisTemplate.hasKey(PASSCODE_PREFIX + passCode);
    }

    /**
     * 按口令码查抽签ID，不存在返回 null。
     */
    public Long getDrawIdByPassCode(String passCode) {
        String v = redisTemplate.opsForValue().get(PASSCODE_PREFIX + passCode);
        return v == null ? null : Long.valueOf(v);
    }

    /**
     * 按抽签ID查口令码，不存在返回 null。
     */
    public String getPassCodeByDrawId(Long drawId) {
        return redisTemplate.opsForValue().get(PASSCODE_PREFIX + PASSCODE_DRAW_SUFFIX + drawId);
    }

    /**
     * 查口令码剩余有效期（秒），-2 表示 key 不存在，-1 表示无 TTL。
     */
    public long getPassCodeTTLSeconds(Long drawId) {
        return redisTemplate.getExpire(PASSCODE_PREFIX + PASSCODE_DRAW_SUFFIX + drawId);
    }

    /**
     * 禁用口令码：删除双向映射。
     */
    public void banPassCode(String passCode, Long drawId) {
        redisTemplate.delete(PASSCODE_PREFIX + passCode);
        redisTemplate.delete(PASSCODE_PREFIX + PASSCODE_DRAW_SUFFIX + drawId);
    }
}
