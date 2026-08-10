package com.quickstart.common.domain.draw.vo;

import lombok.Data;

import java.util.List;

/**
 * 可验证开奖信息。
 */
@Data
public class DrawVerifyVO {

    private Long drawId;

    private Integer status;

    private String seed;

    private String seedHash;

    private String codesHash;

    private String algorithm;

    private String message;

    private List<DrawVerifyCodeVO> codes;

    private List<String> winnerCodes;
}
