package com.quickstart.common.domain.drawCode;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qs_draw_task")
public class DrawTask {
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;
    private Long drawId;
    private Long drawParticipantId;
    private String taskType;
    private Integer taskStatus;
    private Integer retryCount;
    private String messageBody;
    private String lastError;
    private LocalDateTime nextRetryTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
