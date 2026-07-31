package com.quickstart.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 风控结果 DTO。
 */
@Data
public class RiskResultDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否放行 */
    private Boolean pass;
    /** 风险分（0-100，越高越危险） */
    private Integer riskScore;
    /** 命中规则编码 */
    private String hitRule;
    /** 处置建议：PASS 通过 / REVIEW 人工审核 / BLOCK 拦截 */
    private String suggest;

    public static RiskResultDTO pass() {
        RiskResultDTO r = new RiskResultDTO();
        r.setPass(true);
        r.setRiskScore(0);
        r.setSuggest("PASS");
        return r;
    }

    public static RiskResultDTO block(String hitRule, int score) {
        RiskResultDTO r = new RiskResultDTO();
        r.setPass(false);
        r.setRiskScore(score);
        r.setHitRule(hitRule);
        r.setSuggest("BLOCK");
        return r;
    }
}
