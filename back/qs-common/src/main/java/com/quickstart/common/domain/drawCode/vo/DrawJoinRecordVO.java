package com.quickstart.common.domain.drawCode.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 抽签参与记录。
 */
@Data
public class DrawJoinRecordVO {

    private Long drawCodeId;

    private Long userId;

    private String userName;

    private String avatar;

    private LocalDateTime joinTime;

    private String codeValue;
}
