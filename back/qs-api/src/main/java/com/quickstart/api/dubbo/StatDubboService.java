package com.quickstart.api.dubbo;

/**
 * 实时统计 Dubbo 服务。
 * 由 qs-ops 实现。
 *
 * 【Dubbo 价值体现点】
 * 参与人数实时聚合，高频写。Dubbo 长连接适合高频小消息，
 * 比 HTTP 短连接的握手开销更优。
 */
public interface StatDubboService {

    /**
     * 参与人数自增（Redis 原子计数，参与抽奖时调用）。
     *
     * @param drawId 抽签活动ID
     */
    void incrPartCount(Long drawId);

    /**
     * 获取活动参与人数。
     *
     * @param drawId 抽签活动ID
     * @return 当前参与人数
     */
    long getPartCount(Long drawId);

    /**
     * 获取活动在线观看人数（WebSocket 连接数）。
     *
     * @param drawId 抽签活动ID
     * @return 在线人数
     */
    long getOnlineCount(Long drawId);
}
