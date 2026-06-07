package com.quickstart.common.domain.winner;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("qs_winner")
public class Winner {

    @TableId(value = "winner_id", type = IdType.AUTO)
    private Long winnerId;

    private Long userId;

    private Long drawId;

    private Long prizeId;

    private Long winnerCodeId;

    private String userName;

    private String avatar;
}
