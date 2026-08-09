package com.quickstart.draw.module.drawCode.service;

import java.util.List;

/**
 * 参与域：处理抽签参与逻辑
 */
public interface DrawJoinService {

    /**
     * 参与抽奖
     *
     * @param drawId 抽签ID
     * @param userId 用户ID
     * @param ip     客户端IP（用于风控）
     * @return 生成的抽签码列表
     */
    List<String> joinDraw(Long drawId, Long userId, String ip);
}
