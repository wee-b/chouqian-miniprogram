package com.quickstart.draw.module.drawCode.service;

/**
 * 开奖域：处理手动开奖逻辑
 */
public interface DrawOpenService {

    /**
     * 手动开奖
     *
     * @param drawId 抽签ID
     * @param userId 操作人（发布者）ID
     */
    void openDraw(Long drawId, Long userId);
}
