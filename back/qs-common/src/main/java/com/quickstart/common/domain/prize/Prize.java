package com.quickstart.common.domain.prize;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 奖品表 实体类
 * 对应数据库表：qs_prize
 *
 * @author auto generated
 */
@Data
@TableName("qs_prize")
public class Prize implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 奖品ID
     */
    @TableId(type = IdType.AUTO)
    private Long prizeId;

    /**
     * 关联抽签ID
     */
    private Long drawId;

    /**
     * 奖品名称
     */
    private String prizeName;

    /**
     * 奖品封面
     */
    private String prizeCover;

    /**
     * 奖品类型
     * 1-一等奖，2-二等奖，3-三等奖，最多9个
     */
    private Integer prizeType;

    /**
     * 奖品份数
     */
    private Integer amount;

    /**
     * 发放方式
     * 1-快递邮寄，2-联系发布者，3-中奖者填写信息，4-其他
     */
    private Integer giveaway;
}