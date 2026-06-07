package com.quickstart.client.module.role.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quickstart.common.domain.system.role.Role;
import com.quickstart.common.domain.system.role.dto.RoleSaveRequest;
import com.quickstart.common.domain.system.role.vo.RoleView;

public interface RoleService {

    IPage<Role> pageRoles(long pageNo, long pageSize, String roleName, Integer status);

    Role findById(Long roleId);

    RoleView findViewById(Long roleId);

    Role create(RoleSaveRequest request);

    Role update(Long roleId, RoleSaveRequest request);

    boolean updateStatus(Long roleId, Integer status);
}
