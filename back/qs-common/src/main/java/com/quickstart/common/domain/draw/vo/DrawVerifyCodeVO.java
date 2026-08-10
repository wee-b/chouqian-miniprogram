package com.quickstart.common.domain.draw.vo;

import lombok.Data;

/**
 * 可验证开奖中的参与码计算结果。
 */
@Data
public class DrawVerifyCodeVO {

    private Long drawCodeId;

    private String codeValue;

    private String score;

    private Boolean winner;
}
