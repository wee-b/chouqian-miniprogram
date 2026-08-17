package com.quickstart.draw.module.notify.dto;

import com.quickstart.common.domain.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotifyPageDTO extends PageQueryDTO {

    private Integer readFlag;

    private String bizType;
}
