package com.quickstart.draw.module.drawCode.service;

import com.quickstart.common.domain.drawCode.vo.DrawCodeVO;
import com.quickstart.common.domain.winner.vo.WinnerVO;

import java.util.List;

/**
 * 查询域：处理抽签码查询、中奖名单查询
 */
public interface DrawQueryService {

    /**
     * 查询我的抽奖码
     */
    List<DrawCodeVO> getMyCodes(Long drawId, Long userId);

    /**
     * 查询中奖名单
     */
    List<WinnerVO> getWinners(Long drawId);
}
