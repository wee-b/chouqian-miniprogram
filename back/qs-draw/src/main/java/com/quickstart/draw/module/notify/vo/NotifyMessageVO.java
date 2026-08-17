package com.quickstart.draw.module.notify.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotifyMessageVO {

    private Long notifyId;

    private String bizType;

    private String bizId;

    private String title;

    private String content;

    private String payload;

    private Integer readFlag;

    private LocalDateTime createTime;
}
