package com.quickstart.client.module.menu.adminController;

import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.system.menu.Menu;
import com.quickstart.common.domain.system.menu.dto.MenuSaveRequest;
import com.quickstart.common.domain.system.menu.vo.MenuTreeView;
import com.quickstart.common.domain.system.menu.vo.MenuView;
import com.quickstart.client.module.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;

@Validated
@Tag(name = "Menu Management")
@RestController
@RequestMapping("/admin/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public ResponseDTO<List<MenuTreeView>> tree() {
        return ResponseDTO.ok(menuService.listMenuTree());
    }

    @GetMapping("/{menuId}")
    @Operation(summary = "查询菜单详情")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public ResponseDTO<MenuView> detail(@PathVariable Long menuId) {
        Menu menu = menuService.findById(menuId);
        if (menu == null) {
            return ResponseDTO.userErrorParam("菜单不存在");
        }
        return ResponseDTO.ok(MenuView.fromEntity(menu));
    }

    @PostMapping
    @Operation(summary = "新增菜单")
    @PreAuthorize("hasAuthority('system:menu:add')")
    public ResponseDTO<MenuView> create(@RequestBody @Valid MenuSaveRequest request) {
        return ResponseDTO.ok(MenuView.fromEntity(menuService.create(request)));
    }

    @PutMapping("/{menuId}")
    @Operation(summary = "修改菜单")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public ResponseDTO<MenuView> update(@PathVariable Long menuId, @RequestBody @Valid MenuSaveRequest request) {
        return ResponseDTO.ok(MenuView.fromEntity(menuService.update(menuId, request)));
    }

    @PutMapping("/{menuId}/status")
    @Operation(summary = "修改菜单状态")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public ResponseDTO<Void> updateStatus(@PathVariable Long menuId, @RequestParam @NotNull Integer status) {
        boolean success = menuService.updateStatus(menuId, status);
        if (!success) {
            return ResponseDTO.userErrorParam("菜单不存在");
        }
        return ResponseDTO.okMsg("状态更新成功");
    }
}
