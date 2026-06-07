package com.quickstart.client.module.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.client.module.menu.mapper.MenuMapper;
import com.quickstart.client.module.menu.service.MenuService;
import com.quickstart.common.domain.system.menu.Menu;
import com.quickstart.common.domain.system.menu.dto.MenuSaveRequest;
import com.quickstart.common.domain.system.menu.vo.MenuTreeView;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuTreeView> listMenuTree() {
        List<Menu> menus = menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                .orderByAsc(Menu::getParentId)
                .orderByAsc(Menu::getOrderNum)
                .orderByAsc(Menu::getMenuId));
        return buildTree(menus);
    }

    @Override
    public Menu findById(Long menuId) {
        if (menuId == null) {
            return null;
        }
        return menuMapper.selectById(menuId);
    }

    @Override
    public Menu create(MenuSaveRequest request) {
        validateParent(request.getParentId(), null);
        Menu menu = new Menu();
        fillEntity(menu, request);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.insert(menu);
        return menu;
    }

    @Override
    public Menu update(Long menuId, MenuSaveRequest request) {
        Menu menu = findRequiredMenu(menuId);
        validateParent(request.getParentId(), menuId);
        fillEntity(menu, request);
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.updateById(menu);
        return menu;
    }

    @Override
    public boolean updateStatus(Long menuId, Integer status) {
        Menu menu = findById(menuId);
        if (menu == null) {
            return false;
        }
        menu.setStatus(status);
        menu.setUpdateTime(LocalDateTime.now());
        return menuMapper.updateById(menu) > 0;
    }

    private Menu findRequiredMenu(Long menuId) {
        Menu menu = findById(menuId);
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        return menu;
    }

    private void validateParent(Long parentId, Long currentMenuId) {
        if (parentId == null || parentId <= 0) {
            return;
        }
        if (currentMenuId != null && currentMenuId.equals(parentId)) {
            throw new IllegalArgumentException("上级菜单不能选择自己");
        }
        Menu parentMenu = findById(parentId);
        if (parentMenu == null) {
            throw new IllegalArgumentException("上级菜单不存在");
        }
    }

    private void fillEntity(Menu menu, MenuSaveRequest request) {
        menu.setMenuName(request.getMenuName());
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setOrderNum(request.getOrderNum());
        menu.setPath(StringUtils.hasText(request.getPath()) ? request.getPath().trim() : "");
        menu.setComponent(StringUtils.hasText(request.getComponent()) ? request.getComponent().trim() : null);
        menu.setIsFrame(request.getIsFrame());
        menu.setMenuType(request.getMenuType());
        menu.setVisible(request.getVisible());
        menu.setPerms(StringUtils.hasText(request.getPerms()) ? request.getPerms().trim() : null);
        menu.setIcon(StringUtils.hasText(request.getIcon()) ? request.getIcon().trim() : "#");
        menu.setStatus(request.getStatus());
    }

    private List<MenuTreeView> buildTree(List<Menu> menus) {
        Map<Long, MenuTreeView> nodeMap = new LinkedHashMap<>();
        List<MenuTreeView> roots = new ArrayList<>();
        for (Menu menu : menus) {
            nodeMap.put(menu.getMenuId(), MenuTreeView.fromEntity(menu));
        }
        for (Menu menu : menus) {
            MenuTreeView current = nodeMap.get(menu.getMenuId());
            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
            MenuTreeView parent = nodeMap.get(parentId);
            if (parent == null || parentId <= 0) {
                roots.add(current);
                continue;
            }
            parent.getChildren().add(current);
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<MenuTreeView> nodes) {
        nodes.sort(Comparator.comparing(MenuTreeView::getOrderNum, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuTreeView::getMenuId, Comparator.nullsLast(Long::compareTo)));
        for (MenuTreeView node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }
}
