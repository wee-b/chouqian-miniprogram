package com.quickstart.common.domain.system.menu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qs_menu")
public class Menu {

    @TableId(value = "menu_id", type = IdType.AUTO)
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
}
