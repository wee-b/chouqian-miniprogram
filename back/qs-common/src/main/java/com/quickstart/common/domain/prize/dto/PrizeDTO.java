package com.quickstart.common.domain.prize.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrizeDTO {

    @NotNull(message = "关联抽签id不能为空")
    @Schema(description = "关联抽签id", requiredMode = Schema.RequiredMode.REQUIRED,example = "1")
    private Long drawId;

    @NotNull(message = "奖品名称不能为空")
    @Schema(description = "奖品名称", requiredMode = Schema.RequiredMode.REQUIRED,example = "奖品1")
    private String prizeName;


    @Schema(description = "奖品封面", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String prizeCover;

    @NotNull(message = "奖品类型不能为空")
    @Schema(description = "奖品类型 1-一等奖，2-二等奖，3-三等奖，最多9个",
            requiredMode = Schema.RequiredMode.REQUIRED,example = "1")
    private Integer prizeType;

    @NotNull(message = "奖品数量不能为空")
    @Schema(description = "奖品数量", requiredMode = Schema.RequiredMode.REQUIRED,example = "10")
    private Integer amount;


    @NotNull(message = "发放方式不能为空")
    @Schema(description = "发放方式 1-快递邮寄，2-联系发布者，3-中奖者填写信息，4-其他",
            requiredMode = Schema.RequiredMode.REQUIRED,example = "4")
    private Integer giveaway;
}
