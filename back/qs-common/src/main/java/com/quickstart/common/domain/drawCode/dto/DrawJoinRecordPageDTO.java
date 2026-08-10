package com.quickstart.common.domain.drawCode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 抽签参与记录分页查询。
 */
@Data
public class DrawJoinRecordPageDTO {

    @NotNull(message = "抽签ID不能为空")
    @Schema(description = "抽签ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long drawId;

    @NotNull(message = "页码不能为空")
    @Schema(description = "查询第几页", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer page;

    @Schema(description = "每页条数，前端不传时默认 20", example = "20")
    private Integer pageSize = 20;
}
