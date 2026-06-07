package com.quickstart.common.domain.temporaryCode.vo;

import lombok.Data;

/**   抽签口令   */
@Data
public class PassCodeVO {

    private String passCode;

    // 有效期剩余时间
    private Long remainValidSecond;

    /** 过期时间戳（毫秒） */
    private Long expireTime;
}
