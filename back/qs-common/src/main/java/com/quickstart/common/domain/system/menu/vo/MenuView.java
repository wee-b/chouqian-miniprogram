package com.quickstart.common.domain.system.menu.vo;

import com.quickstart.common.domain.system.menu.Menu;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MenuView {

    private Long menuId;

    private String menuName;

    private Long parentId;

    private Integer orderNum;

    private String path;

    private String component;

    private Integer isFrame;

    private Integer menuType;

    private Integer visible;

    private String perms;

    private String icon;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static MenuView fromEntity(Menu menu) {
        if (menu == null) {
            return null;
        }
        MenuView view = new MenuView();
        view.setMenuId(menu.getMenuId());
        view.setMenuName(menu.getMenuName());
        view.setParentId(menu.getParentId());
        view.setOrderNum(menu.getOrderNum());
        view.setPath(menu.getPath());
        view.setComponent(menu.getComponent());
        view.setIsFrame(menu.getIsFrame());
        view.setMenuType(menu.getMenuType());
        view.setVisible(menu.getVisible());
        view.setPerms(menu.getPerms());
        view.setIcon(menu.getIcon());
        view.setStatus(menu.getStatus());
        view.setCreateTime(menu.getCreateTime());
        view.setUpdateTime(menu.getUpdateTime());
        return view;
    }
}
