package com.quickstart.common.domain.system.role.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleSaveRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30")
    private String roleName;

    @NotBlank(message = "角色权限标识不能为空")
    @Size(max = 100, message = "角色权限标识长度不能超过100")
    private String roleKey;

    @NotNull(message = "显示顺序不能为空")
    @Min(value = 0, message = "显示顺序不能小于0")
    private Integer roleSort;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态取值不合法")
    @Max(value = 2, message = "状态取值不合法")
    private Integer status;

    private List<Long> menuIds;
}
