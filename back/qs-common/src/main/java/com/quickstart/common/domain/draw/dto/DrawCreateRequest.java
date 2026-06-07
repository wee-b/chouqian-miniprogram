package com.quickstart.common.domain.draw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建/更新抽签活动请求参数")
public class DrawCreateRequest {

    @Schema(description = "抽签ID（更新时必填）")
    private Long drawId;

    @NotBlank(message = "抽签标题不能为空")
    @Schema(description = "抽签标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "抽签封面")
    private String drawCover;

    @Schema(description = "抽签说明")
    private String description;

    @NotNull(message = "是否有奖品不能为空")
    @Schema(description = "有无奖品：0-无奖品(随机选人) 1-有奖品(抽奖)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer hasPrize;

    @NotNull(message = "开奖方式不能为空")
    @Schema(description = "开奖方式：0-按时间开奖 1-按人数开奖 2-预留", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer drawingWay;

    @NotNull(message = "参与截止时间不能为空")
    @Future(message = "参与截止时间必须晚于当前时间")
    @Schema(description = "参与截止时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime joinDeadline;

    @Schema(description = "最少参与人数")
    private Integer minPerson;

    @Schema(description = "每人每次可获得多少抽签码")
    private Integer perCodeNum;

    @Schema(description = "状态：0-保存草稿 1-保存并发布，不传默认0")
    private Integer status;
}