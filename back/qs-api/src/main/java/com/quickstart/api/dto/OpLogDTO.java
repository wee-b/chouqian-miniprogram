package com.quickstart.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志 DTO。
 * 高频内部调用场景：每个接口都打日志，故用 Dubbo 异步（CompletableFuture）不阻塞主流程。
 */
@Data
public class OpLogDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 链路追踪ID */
    private String traceId;
    /** 用户ID */
    private Long userId;
    /** 用户编码 */
    private String userCode;
    /** 操作动作（如 JOIN_DRAW / OPEN_DRAW / LOGIN） */
    private String action;
    /** 所属模块 */
    private String module;
    /** 请求参数（JSON） */
    private String params;
    /** 客户端IP */
    private String ip;
    /** 接口耗时(ms) */
    private Long cost;
    /** 是否成功 */
    private Boolean success;
    /** 失败错误信息 */
    private String errorMsg;
    /** 操作时间 */
    private LocalDateTime gmtCreate;
}
