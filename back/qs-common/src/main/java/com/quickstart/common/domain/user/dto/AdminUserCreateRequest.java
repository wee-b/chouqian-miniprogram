package com.quickstart.common.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserCreateRequest {
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @NotBlank(message = "用户昵称不能为空")
    private String userName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;
}
