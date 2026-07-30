package com.quickstart.common.domain.draw.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DrawVO {

    // 主键ID
    private Long drawId;
    private Long publisherUserId;    // 发布者用户ID

    // ====================== 手动设置部分字段 ======================

    private String title;            // 抽签标题
    private String drawCover;        // 抽签封面
    private String description;      // 抽签说明
    private Integer hasPrize;        // 有无奖品：0-无奖品 1-有奖品
    private Integer drawingWay;      // 开奖方式：0-按时间 1-按人数
    private LocalDateTime joinDeadline; // 参与截止时间
    private Integer minPerson;       // 最少参与人数
    private Integer perCodeNum;      // 每人抽签码数量
    private Integer partLimit;       // 每人可参与次数上限

    // ====================== 要求新增字段 ======================
    private String drawNo;           // 唯一抽奖编号
    private LocalDateTime createTime; // 创建时间

    // ====================== 状态与统计字段 ======================
    private Integer status;          // 状态：0-草稿 1-参与中 2-已开奖 3-流局
    private Integer participantCount; // 参与人数
    private LocalDateTime drawTime;  // 开奖时间
    private Boolean isOwner;         // 当前用户是否为发布者
}