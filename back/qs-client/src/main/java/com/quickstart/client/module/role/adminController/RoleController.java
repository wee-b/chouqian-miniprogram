package com.quickstart.client.module.role.adminController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.system.role.Role;
import com.quickstart.common.domain.system.role.dto.RoleSaveRequest;
import com.quickstart.common.domain.system.role.vo.RoleView;
import com.quickstart.client.module.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Role Management")
@RestController
@RequestMapping("/admin/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ResponseDTO<IPage<RoleView>> page(@RequestParam(defaultValue = "1") @Min(1) long pageNo,
                                             @RequestParam(defaultValue = "10") @Min(1) long pageSize,
                                             @RequestParam(required = false) String roleName,
                                             @RequestParam(required = false) Integer status) {
        IPage<RoleView> page = roleService.pageRoles(pageNo, pageSize, roleName, status)
                .convert(role -> RoleView.fromEntity(role, null));
        return ResponseDTO.ok(page);
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "查询角色详情")
    @PreAuthorize("hasAuthority('system:role:query')")
    public ResponseDTO<RoleView> detail(@PathVariable Long roleId) {
        RoleView roleView = roleService.findViewById(roleId);
        if (roleView == null) {
            return ResponseDTO.userErrorParam("角色不存在");
        }
        return ResponseDTO.ok(roleView);
    }

    @PostMapping
    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('system:role:add')")
    public ResponseDTO<RoleView> create(@RequestBody @Valid RoleSaveRequest request) {
        Role role = roleService.create(request);
        return ResponseDTO.ok(roleService.findViewById(role.getRoleId()));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseDTO<RoleView> update(@PathVariable Long roleId, @RequestBody @Valid RoleSaveRequest request) {
        roleService.update(roleId, request);
        return ResponseDTO.ok(roleService.findViewById(roleId));
    }

    @PutMapping("/{roleId}/status")
    @Operation(summary = "修改角色状态")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseDTO<Void> updateStatus(@PathVariable Long roleId, @RequestParam @NotNull Integer status) {
        boolean success = roleService.updateStatus(roleId, status);
        if (!success) {
            return ResponseDTO.userErrorParam("角色不存在");
        }
        return ResponseDTO.okMsg("状态更新成功");
    }
}
