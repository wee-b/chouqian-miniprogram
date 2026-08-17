package com.quickstart.draw.module.notify.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qs_notify_message")
public class NotifyMessage {

    @TableId(value = "notify_id", type = IdType.AUTO)
    private Long notifyId;

    private Long userId;

    private String bizType;

    private String bizId;

    private String title;

    private String content;

    private String payload;

    private Integer readFlag;

    private Integer pushStatus;

    private Integer deletedFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
