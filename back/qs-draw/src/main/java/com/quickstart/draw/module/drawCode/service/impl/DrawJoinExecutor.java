package com.quickstart.draw.module.drawCode.service.impl;

import com.quickstart.api.dto.OpLogDTO;
import com.quickstart.api.dto.RiskContextDTO;
import com.quickstart.api.dto.RiskResultDTO;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.cache.DrawRedisService;
import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.dubbo.DrawDubboConsumer;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.util.DrawCodeGenerator;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 参与域核心执行器 —— Local / MQ 两条路径共享同一套业务逻辑。
 * <p>
 * Local 路径：Controller → LocalDrawJoinServiceImpl → 本类
 * MQ   路径：Controller → MqDrawJoinServiceImpl → RabbitMQ → DrawJoinConsumer → 本类
 */
@Component
public class DrawJoinExecutor {

    @Resource
    private DrawCodeGenerator drawCodeGenerator;
    @Autowired
    private DrawCodeMapper drawCodeMapper;
    @Autowired
    private DrawMapper drawMapper;
    @Resource
    private DrawRedisService drawRedisService;
    @Autowired
    private DrawDubboConsumer dubboConsumer;

    /**
     * 参与抽签核心流程（Local / MQ 共用）
     *
     * @param drawId 抽签ID
     * @param userId 用户ID
     * @param ip     客户端IP（用于风控，MQ 路径可为 null）
     * @return 生成的抽签码值列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> executeJoin(Long drawId, Long userId, String ip) {
        boolean partCountAcquired = false;
        try {
            // 1. Dubbo: 校验用户状态（qs-client）
            if (!dubboConsumer.user().checkUserStatus(userId)) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "用户状态异常，无法参与");
            }

            // 2. 查询抽签活动并校验是否可参与
            Draw draw = drawMapper.selectById(drawId);
            ensureJoinable(draw);

            // 3. 幂等校验：重复参与直接返回，不再占用 Redis 参与次数
            LambdaQueryWrapper<DrawCode> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(DrawCode::getDrawId, drawId);
            existWrapper.eq(DrawCode::getUserId, userId);
            if (drawCodeMapper.selectCount(existWrapper) > 0) {
                return List.of();
            }

            // 4. Dubbo: 实时风控校验（qs-ops，黑名单+IP滑动窗口）
            if (ip != null) {
                RiskContextDTO riskCtx = new RiskContextDTO();
                riskCtx.setUserId(userId);
                riskCtx.setIp(ip);
                riskCtx.setAction("JOIN_DRAW");
                riskCtx.setScene("LOTTERY");
                RiskResultDTO riskResult = dubboConsumer.riskRule().check(riskCtx);
                if (!riskResult.getPass()) {
                    throw new BusinessException(ErrorCode.BUSINESS_ERROR,
                            "风控拦截：" + riskResult.getHitRule());
                }
            }

            // 5. 检查参与次数上限（Redis 原子计数）
            partCountAcquired = drawRedisService.acquirePartCount(drawId, userId);

            // 6. 生成抽签码
            int perCodeNum = draw.getPerCodeNum();
            if (perCodeNum <= 0) {
                throw new IllegalArgumentException("每人参与码数量配置错误");
            }
            List<String> codeValues = drawCodeGenerator.batchGenerate(perCodeNum);

            // 7. 组装批量插入
            List<DrawCode> drawCodeList = new ArrayList<>(codeValues.size());
            LocalDateTime now = LocalDateTime.now();
            for (String codeValue : codeValues) {
                DrawCode code = new DrawCode();
                code.setDrawId(drawId);
                code.setUserId(userId);
                code.setCodeValue(codeValue);
                code.setCreateTime(now);
                drawCodeList.add(code);
            }
            drawCodeMapper.batchInsert(drawCodeList);

            // 8. 更新抽签统计
            draw.setParticipantCount(draw.getParticipantCount() + 1);
            draw.setCodeCount(draw.getCodeCount() + perCodeNum);
            drawMapper.updateById(draw);

            // 9. Dubbo: 实时参与人数自增（qs-ops）
            dubboConsumer.stat().incrPartCount(drawId);

            // 10. Dubbo: 异步操作日志（qs-ops，不阻塞主流程）
            String joinNo = dubboConsumer.id().nextCode("JN", "DRAW_JOIN");
            OpLogDTO opLog = new OpLogDTO();
            opLog.setTraceId(joinNo);
            opLog.setUserId(userId);
            opLog.setAction("JOIN_DRAW");
            opLog.setModule("drawCode");
            opLog.setParams("drawId=" + drawId + ",codeCount=" + perCodeNum);
            opLog.setIp(ip);
            opLog.setSuccess(true);
            dubboConsumer.opLog().recordAsync(opLog);

            return codeValues;
        } catch (RuntimeException e) {
            if (partCountAcquired) {
                drawRedisService.releasePartCount(drawId, userId);
            }
            throw e;
        }
    }

    private void ensureJoinable(Draw draw) {
        if (draw == null) {
            throw new IllegalArgumentException("抽奖不存在");
        }
        if (draw.getStatus() == null || draw.getStatus() != DrawConstants.DRAW_STATUS_RUNNING) {
            throw new IllegalArgumentException("当前抽奖已结束，无法参与");
        }
        if (draw.getJoinDeadline() != null && draw.getJoinDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("抽奖已截止，无法参与");
        }
    }
}
