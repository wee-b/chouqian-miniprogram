package com.quickstart.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户信息 DTO（Dubbo 跨服务传输）。
 * 仅承载查询所需字段，不暴露密码等敏感信息。
 */
@Data
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;
    /** 用户编码（业务唯一标识，如 LL000123） */
    private String userCode;
    /** 手机号 */
    private String phone;
    /** 昵称 */
    private String nickName;
    /** 头像URL */
    private String avatar;
    /** 状态：1正常 0禁用 */
    private Integer status;
}
