package com.quickstart.common.domain.drawCode.vo;

import lombok.Data;

@Data
public class DrawJoinVO {
    private Long drawId;
    private Long drawParticipantId;
    private Integer joinStatus;
    private String message;
}
