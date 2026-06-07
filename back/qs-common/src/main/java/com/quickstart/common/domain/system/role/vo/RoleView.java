package com.quickstart.common.domain.system.role.vo;

import com.quickstart.common.domain.system.role.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RoleView {

    private Long roleId;

    private String roleName;

    private String roleKey;

    private Integer roleSort;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<Long> menuIds = new ArrayList<>();

    public static RoleView fromEntity(Role role, List<Long> menuIds) {
        if (role == null) {
            return null;
        }
        RoleView view = new RoleView();
        view.setRoleId(role.getRoleId());
        view.setRoleName(role.getRoleName());
        view.setRoleKey(role.getRoleKey());
        view.setRoleSort(role.getRoleSort());
        view.setRemark(role.getRemark());
        view.setStatus(role.getStatus());
        view.setCreateTime(role.getCreateTime());
        view.setUpdateTime(role.getUpdateTime());
        if (menuIds != null) {
            view.setMenuIds(menuIds);
        }
        return view;
    }
}
