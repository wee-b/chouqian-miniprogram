package com.quickstart.client.module.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickstart.client.module.role.mapper.RoleMapper;
import com.quickstart.client.module.role.mapper.RoleMenuMapper;
import com.quickstart.client.module.role.service.RoleService;
import com.quickstart.common.domain.system.menu.Menu;
import com.quickstart.common.domain.system.role.Role;
import com.quickstart.common.domain.system.role.dto.RoleSaveRequest;
import com.quickstart.common.domain.system.role.vo.RoleView;
import com.quickstart.client.module.menu.mapper.MenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper, MenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public IPage<Role> pageRoles(long pageNo, long pageSize, String roleName, Integer status) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<Role>()
                .eq(Role::getDeletedFlag, 0)
                .like(StringUtils.hasText(roleName), Role::getRoleName, roleName)
                .eq(status != null, Role::getStatus, status)
                .orderByAsc(Role::getRoleSort)
                .orderByAsc(Role::getRoleId);
        return roleMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
    }

    @Override
    public Role findById(Long roleId) {
        if (roleId == null) {
            return null;
        }
        return roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleId, roleId)
                .eq(Role::getDeletedFlag, 0));
    }

    @Override
    public RoleView findViewById(Long roleId) {
        Role role = findById(roleId);
        if (role == null) {
            return null;
        }
        return RoleView.fromEntity(role, roleMenuMapper.selectMenuIdsByRoleId(roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role create(RoleSaveRequest request) {
        validateUnique(null, request);
        List<Long> menuIds = normalizeMenuIds(request.getMenuIds());
        validateMenus(menuIds);
        Role role = new Role();
        fillEntity(role, request);
        role.setDeletedFlag(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        saveRoleMenus(role.getRoleId(), menuIds);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role update(Long roleId, RoleSaveRequest request) {
        Role role = findRequiredRole(roleId);
        validateUnique(roleId, request);
        List<Long> menuIds = normalizeMenuIds(request.getMenuIds());
        validateMenus(menuIds);
        fillEntity(role, request);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        saveRoleMenus(roleId, menuIds);
        return role;
    }

    @Override
    public boolean updateStatus(Long roleId, Integer status) {
        Role role = findById(roleId);
        if (role == null) {
            return false;
        }
        role.setStatus(status);
        role.setUpdateTime(LocalDateTime.now());
        return roleMapper.updateById(role) > 0;
    }

    private Role findRequiredRole(Long roleId) {
        Role role = findById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        return role;
    }

    private void fillEntity(Role role, RoleSaveRequest request) {
        role.setRoleName(request.getRoleName().trim());
        role.setRoleKey(request.getRoleKey().trim());
        role.setRoleSort(request.getRoleSort());
        role.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        role.setStatus(request.getStatus());
    }

    private void validateUnique(Long roleId, RoleSaveRequest request) {
        long roleNameCount = roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeletedFlag, 0)
                .eq(Role::getRoleName, request.getRoleName().trim())
                .ne(roleId != null, Role::getRoleId, roleId));
        if (roleNameCount > 0) {
            throw new IllegalArgumentException("角色名称已存在");
        }
        long roleKeyCount = roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeletedFlag, 0)
                .eq(Role::getRoleKey, request.getRoleKey().trim())
                .ne(roleId != null, Role::getRoleId, roleId));
        if (roleKeyCount > 0) {
            throw new IllegalArgumentException("角色权限标识已存在");
        }
    }

    private List<Long> normalizeMenuIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(menuIds);
        return uniqueIds.stream().toList();
    }

    private void validateMenus(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return;
        }
        long count = menuMapper.selectCount(new LambdaQueryWrapper<Menu>().in(Menu::getMenuId, menuIds));
        if (count != menuIds.size()) {
            throw new IllegalArgumentException("存在无效的菜单ID");
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (!menuIds.isEmpty()) {
            roleMenuMapper.batchInsert(roleId, menuIds);
        }
    }
}
