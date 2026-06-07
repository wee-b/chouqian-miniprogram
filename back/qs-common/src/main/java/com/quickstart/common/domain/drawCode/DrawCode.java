package com.quickstart.common.domain.drawCode;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 抽签参与码表
 */
@Data
@TableName("qs_draw_code")
public class DrawCode {

    /**
     * 参与码ID
     */
    @TableId(value = "draw_code_id", type = IdType.AUTO)
    private Long drawCodeId;

    /**
     * 关联用户
     */
    private Long userId;

    /**
     * 关联抽签
     */
    private Long drawId;

    /**
     * 关联奖品,null就是没中奖
     */
    private Long prizeId;

    /**
     * 参与码
     */
    private String codeValue;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}