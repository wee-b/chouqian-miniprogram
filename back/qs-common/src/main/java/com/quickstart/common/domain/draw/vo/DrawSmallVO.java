package com.quickstart.common.domain.draw.vo;

import lombok.Data;

@Data
public class DrawSmallVO {

    private Long drawId;
    private String title;
    private String drawCover;
    private Integer status;

    // 发布者,选填
    private String publisherMCode;
    // 发布者头像,选填
    private String publisherAvatar;
}
