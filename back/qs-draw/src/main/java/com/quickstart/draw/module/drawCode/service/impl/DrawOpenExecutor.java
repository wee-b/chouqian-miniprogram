package com.quickstart.draw.module.drawCode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.prize.Prize;
import com.quickstart.common.domain.user.User;
import com.quickstart.common.domain.winner.Winner;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.mapper.UserReadMapper;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.module.drawCode.mapper.WinnerMapper;
import com.quickstart.draw.module.drawCode.service.DrawOpenService;
import com.quickstart.draw.module.drawVerify.service.DrawVerifyService;
import com.quickstart.draw.module.prize.mapper.PrizeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 开奖域核心执行器 —— Local / MQ 两条路径共享同一套业务逻辑。
 */
@Component
public class DrawOpenExecutor implements DrawOpenService {

    @Autowired
    private DrawMapper drawMapper;
    @Autowired
    private DrawCodeMapper drawCodeMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private WinnerMapper winnerMapper;
    @Autowired
    private UserReadMapper userReadMapper;
    @Autowired
    private DrawVerifyService drawVerifyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openDraw(Long drawId, Long userId) {
        // 1. 查询抽签并校验
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }
        if (!draw.getPublisherUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能开奖自己发布的抽签");
        }
        if (draw.getStatus() != DrawConstants.DRAW_STATUS_RUNNING) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "只有进行中的抽签才能开奖");
        }

        // 2. 查询所有抽签码（未中奖的）
        LambdaQueryWrapper<DrawCode> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(DrawCode::getDrawId, drawId);
        codeWrapper.isNull(DrawCode::getPrizeId);
        List<DrawCode> codes = drawCodeMapper.selectList(codeWrapper);

        if (codes.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "没有可开奖的抽签码，参与者不足");
        }

        // 3. 确保开奖承诺存在，并锁定当前参与码集合
        drawVerifyService.ensureCommitment(draw);
        String codesHash = drawVerifyService.buildCodesHash(codes);

        // 4. 查询奖品（按奖品等级升序）
        LambdaQueryWrapper<Prize> prizeWrapper = new LambdaQueryWrapper<>();
        prizeWrapper.eq(Prize::getDrawId, drawId);
        prizeWrapper.orderByAsc(Prize::getPrizeType);
        List<Prize> prizes = prizeMapper.selectList(prizeWrapper);

        // 5. 可验证随机开奖：按 SHA-256 分数升序排列
        List<DrawCode> sortedCodes = drawVerifyService.sortCodes(drawId, draw.getServerSeed(), codes);
        List<Winner> winners = new ArrayList<>();
        int codeIndex = 0;

        if (prizes.isEmpty()) {
            // 没有奖品：随机选1个人
            DrawCode selected = sortedCodes.get(0);
            selected.setPrizeId(0L);
            drawCodeMapper.updateById(selected);
            winners.add(buildWinner(selected, drawId, 0L));
        } else {
            // 有奖品：按奖品等级和数量依次分配
            for (Prize prize : prizes) {
                int amount = prize.getAmount();
                for (int i = 0; i < amount && codeIndex < sortedCodes.size(); i++) {
                    DrawCode selected = sortedCodes.get(codeIndex);
                    selected.setPrizeId(prize.getPrizeId());
                    drawCodeMapper.updateById(selected);
                    winners.add(buildWinner(selected, drawId, prize.getPrizeId()));
                    codeIndex++;
                }
            }
        }

        if (winners.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "参与人数不足或奖品配置异常，开奖失败");
        }

        // 6. 批量写入中奖记录
        winnerMapper.batchInsert(winners);

        // 7. 更新抽签状态为已开奖，并保存验证快照
        draw.setStatus(DrawConstants.DRAW_STATUS_OPENED);
        draw.setDrawTime(LocalDateTime.now());
        draw.setCodesHash(codesHash);
        draw.setUpdateTime(LocalDateTime.now());
        drawMapper.updateById(draw);
    }

    private Winner buildWinner(DrawCode selectedCode, Long drawId, Long prizeId) {
        User winnerUser = userReadMapper.selectById(selectedCode.getUserId());
        Winner winner = new Winner();
        winner.setUserId(selectedCode.getUserId());
        winner.setDrawId(drawId);
        winner.setPrizeId(prizeId);
        winner.setWinnerCodeId(selectedCode.getDrawCodeId());
        winner.setUserName(winnerUser != null ? winnerUser.getUserName() : "");
        winner.setAvatar(winnerUser != null ? winnerUser.getAvatar() : null);
        return winner;
    }
}
