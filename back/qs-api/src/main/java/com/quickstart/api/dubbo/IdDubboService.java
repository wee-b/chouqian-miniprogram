package com.quickstart.api.dubbo;

/**
 * 分布式 ID Dubbo 服务。
 * 由 qs-ops 实现（美团 Leaf 式：号段 + Snowflake 双 buffer）。
 *
 * 【Dubbo 价值体现点】
 * 超高频 + 强一致同步调用。Dubbo 同步调用比 HTTP 更适合。
 * 同时替代 qs-draw 中硬编码 workerId=1/datacenterId=1 的 Hutool Snowflake
 * （多实例部署会生成重复 ID 的 bug）。
 */
public interface IdDubboService {

    /**
     * 生成全局唯一自增ID（纯数字）。
     *
     * @param bizKey 业务标识（如 DRAW_CODE / ORDER / USER），用于号段分配
     * @return 全局唯一ID
     */
    long nextId(String bizKey);

    /**
     * 生成带业务前缀的编码（如 LL000123、D20260730001）。
     *
     * @param prefix 业务前缀
     * @param bizKey 业务标识
     * @return 带前缀的唯一编码
     */
    String nextCode(String prefix, String bizKey);
}
