package com.quickstart.api.dubbo;

import com.quickstart.api.dto.RiskContextDTO;
import com.quickstart.api.dto.RiskResultDTO;

/**
 * 风控规则 Dubbo 服务。
 * 由 qs-ops 实现。
 *
 * 【Dubbo 价值体现点】
 * 参与抽奖热路径上调用，用户等着响应，延迟敏感。
 * Feign HTTP 在热路径上每次握手延迟（即使连接池也有空闲检查/复用开销）；
 * Dubbo 长连接省握手，热路径延迟更低。
 */
public interface RiskRuleDubboService {

    /**
     * 实时风控校验：基于设备/IP/行为频次计算风险分，超阈值拦截或转人工审核。
     *
     * @param ctx 风控上下文
     * @return 风控结果（pass/riskScore/suggest）
     */
    RiskResultDTO check(RiskContextDTO ctx);

    /**
     * 黑名单查询：用户/设备维度，Redis Set 存储，O(1) 查询。
     *
     * @param userId   用户ID（可空）
     * @param deviceId 设备指纹（可空）
     * @return true 命中黑名单
     */
    boolean isBlacklisted(String userId, String deviceId);
}
