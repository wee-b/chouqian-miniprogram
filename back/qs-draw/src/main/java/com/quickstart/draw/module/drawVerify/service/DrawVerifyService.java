package com.quickstart.draw.module.drawVerify.service;

import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.draw.vo.DrawVerifyVO;
import com.quickstart.common.domain.drawCode.DrawCode;

import java.util.List;

/**
 * 可验证开奖服务。
 */
public interface DrawVerifyService {

    /**
     * 初始化开奖承诺。仅填充内存对象字段，是否落库由调用方控制。
     */
    void initCommitment(Draw draw);

    /**
     * 确保已有开奖承诺，老数据缺失时补齐并落库。
     */
    void ensureCommitment(Draw draw);

    /**
     * 生成参与码集合哈希。
     */
    String buildCodesHash(List<DrawCode> codes);

    /**
     * 按可验证随机算法排序参与码。
     */
    List<DrawCode> sortCodes(Long drawId, String seed, List<DrawCode> codes);

    /**
     * 查询可验证开奖信息。
     */
    DrawVerifyVO getVerifyInfo(Long drawId);
}
