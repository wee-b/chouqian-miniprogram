package com.quickstart.client.module.menu.service;

import com.quickstart.common.domain.system.menu.Menu;
import com.quickstart.common.domain.system.menu.dto.MenuSaveRequest;
import com.quickstart.common.domain.system.menu.vo.MenuTreeView;

import java.util.List;

public interface MenuService {

    List<MenuTreeView> listMenuTree();

    Menu findById(Long menuId);

    Menu create(MenuSaveRequest request);

    Menu update(Long menuId, MenuSaveRequest request);

    boolean updateStatus(Long menuId, Integer status);
}
