package com.quickstart.common.domain.temporaryCode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**   生成抽签口令   */
@Data
public class GenerateCodeDTO {

    @NotNull
    @Schema(description = "抽签id", requiredMode = Schema.RequiredMode.REQUIRED,example = "1001")
    private Long drawId;

    // 有效期（小时）
    @NotNull
    @Schema(description = "有效期（小时）", requiredMode = Schema.RequiredMode.REQUIRED,example = "24")
    private Integer expireHours;
}
