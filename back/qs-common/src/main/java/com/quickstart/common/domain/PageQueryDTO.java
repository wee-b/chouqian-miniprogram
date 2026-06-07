package com.quickstart.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
public class PageQueryDTO {

    @NotNull
    @Schema(description = "查询第几页",requiredMode = REQUIRED,example = "1")
    private Integer page;

    @NotNull
    @Schema(description = "分页补偿",requiredMode = REQUIRED,example = "10")
    private Integer pageSize;
}
