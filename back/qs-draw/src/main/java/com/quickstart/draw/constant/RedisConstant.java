package com.quickstart.draw.constant;

import com.quickstart.draw.cache.DrawRedisService;

/**
 * Redis key 常量。
 * 注意：qs-draw 业务的 StringRedisTemplate key 前缀（官方缓存/参与限制/参与计数/口令码）
 * 已统一收敛至 {@link DrawRedisService} 私有常量，不再在此暴露。
 * 此处仅保留不走 StringRedisTemplate 的 key。
 */
public class RedisConstant {

    /** 抽签码生成分布式锁（Redisson RLock 使用，非 StringRedisTemplate 操作） */
    public static final String CODE_GENERATOR_LOCK = "draw:lock:codeGenerator";
}
