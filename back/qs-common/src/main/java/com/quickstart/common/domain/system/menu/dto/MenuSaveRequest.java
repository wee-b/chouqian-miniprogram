package com.quickstart.common.domain.system.menu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuSaveRequest {

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50")
    private String menuName;

    private Long parentId = 0L;

    @NotNull(message = "显示顺序不能为空")
    @Min(value = 0, message = "显示顺序不能小于0")
    private Integer orderNum;

    @Size(max = 200, message = "路由地址长度不能超过200")
    private String path;

    @Size(max = 255, message = "组件路径长度不能超过255")
    private String component;

    @NotNull(message = "是否外链不能为空")
    @Min(value = 0, message = "是否外链取值不合法")
    @Max(value = 1, message = "是否外链取值不合法")
    private Integer isFrame;

    @NotNull(message = "菜单类型不能为空")
    @Min(value = 1, message = "菜单类型取值不合法")
    @Max(value = 3, message = "菜单类型取值不合法")
    private Integer menuType;

    @NotNull(message = "显示状态不能为空")
    @Min(value = 0, message = "显示状态取值不合法")
    @Max(value = 1, message = "显示状态取值不合法")
    private Integer visible;

    @Size(max = 100, message = "权限标识长度不能超过100")
    private String perms;

    @Size(max = 100, message = "图标长度不能超过100")
    private String icon;

    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态取值不合法")
    @Max(value = 2, message = "状态取值不合法")
    private Integer status;
}
