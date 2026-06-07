package com.quickstart.common.domain.draw;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qs_draw")
public class Draw {

    /**
     * 抽签ID
     */
    @TableId(value = "draw_id", type = IdType.AUTO)
    private Long drawId;

    /**
     * 发布用户ID(id为0代表是官方抽签)
     */
    private Long publisherUserId;

    /**
     * 抽签标题
     */
    private String title;

    /**
     * 抽签封面
     */
    private String drawCover;

    /**
     * 抽签说明
     */
    private String description;

    /**
     * 有无奖品：0-无奖品(随机选人) 1-有奖品(抽奖)
     */
    private Integer hasPrize;

    /**
     * 开奖方式：0-按时间开奖 1-按人数开奖 2-预留
     */
    private Integer drawingWay;

    /**
     * 参与截止时间
     */
    private LocalDateTime joinDeadline;

    /**
     * 最少参与人数
     */
    private Integer minPerson;

    /**
     * 每人每次可获得多少抽签码
     */
    private Integer perCodeNum;


    /**
     * 唯一抽奖编号
     */
    private String drawNo;
    private LocalDateTime drawTime;  // 开奖时间
    private Integer participantCount;
    private Integer codeCount;

    /** 状态：0-草稿 1-参与中 2-已开奖 3-流局 */
    private Integer status;
    private Integer deletedFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}