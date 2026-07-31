package com.quickstart.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 风控上下文 DTO。
 * 参与抽奖热路径上调用，延迟敏感，Dubbo 长连接省握手。
 */
@Data
public class RiskContextDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;
    /** 用户编码 */
    private String userCode;
    /** 设备指纹（前端采集，防多账号薅羊毛） */
    private String deviceId;
    /** 客户端IP */
    private String ip;
    /** 触发动作（如 JOIN_DRAW） */
    private String action;
    /** 风控场景（如 LOTTERY / REGISTER） */
    private String scene;
}
