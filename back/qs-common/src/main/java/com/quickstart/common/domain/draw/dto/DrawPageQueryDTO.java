package com.quickstart.common.domain.draw.dto;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.quickstart.common.domain.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DrawPageQueryDTO extends PageQueryDTO {

    /** 查询类型：1-我参与的 2-我发布的 3-我中奖的 */
    @NotNull(message = "查询类型不能为空")
    @Schema(description = "查询类型：1-我参与的 2-我发布的 3-我中奖的",
            requiredMode = Schema.RequiredMode.REQUIRED,example = "1")
    private Integer queryType;
}
