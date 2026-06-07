package com.quickstart.common.domain.draw.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DrawDetailVO {
    private Long drawId;
    private Long publisherUserId;
    private String title;
    private String description;
    private String shareToken;
    private String drawCode;
    private Integer status;
    private LocalDateTime joinDeadline;
    private LocalDateTime drawTime;
    private Integer participantCount;
    private Integer codeCount;
    private Boolean joined;
    private Boolean opened;
    private String winnerCodeValue;
}
