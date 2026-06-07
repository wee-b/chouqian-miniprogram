package com.quickstart.common.domain.system.menu.vo;

import com.quickstart.common.domain.system.menu.Menu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeView {

    private Long menuId;

    private Long parentId;

    private String menuName;

    private Integer orderNum;

    private Integer menuType;

    private String path;

    private String component;

    private String perms;

    private String icon;

    private Integer status;

    private Integer visible;

    private List<MenuTreeView> children = new ArrayList<>();

    public static MenuTreeView fromEntity(Menu menu) {
        MenuTreeView view = new MenuTreeView();
        view.setMenuId(menu.getMenuId());
        view.setParentId(menu.getParentId());
        view.setMenuName(menu.getMenuName());
        view.setOrderNum(menu.getOrderNum());
        view.setMenuType(menu.getMenuType());
        view.setPath(menu.getPath());
        view.setComponent(menu.getComponent());
        view.setPerms(menu.getPerms());
        view.setIcon(menu.getIcon());
        view.setStatus(menu.getStatus());
        view.setVisible(menu.getVisible());
        return view;
    }
}
