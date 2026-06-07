package com.quickstart.common.domain.drawCode.mq;

import lombok.Data;

@Data
public class DrawJoinMessage {
    private Long drawId;
    private Long userId;
}
