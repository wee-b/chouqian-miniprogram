package com.quickstart.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum EntityStatusEnum {
    // 0-未删除 1-已删除
    // 图片：0-未绑定，1-已绑定

    TO_INSPECT_STATUS(0,"待审核"),
    NORMAL_STATUS(1,"启用"),
    UNSHOW_STATUS(2,"停用"),
    BANNED_STATUS(5,"禁用");

    private final Integer value;
    private final String desc;



}
