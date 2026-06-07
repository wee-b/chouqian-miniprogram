package com.quickstart.common.domain.draw.vo;

import lombok.Data;

@Data
public class DrawStatisticsVO {

    // 参加统计
    private Long joinedCount;

    // 发布统计
    private Long publishedCount;

    // 中奖统计
    private Long rewardCount;
}
