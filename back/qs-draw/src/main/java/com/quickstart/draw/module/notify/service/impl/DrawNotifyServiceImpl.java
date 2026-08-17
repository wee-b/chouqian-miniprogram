package com.quickstart.draw.module.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.winner.Winner;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.module.drawCode.mapper.WinnerMapper;
import com.quickstart.draw.module.notify.domain.NotifyMessage;
import com.quickstart.draw.module.notify.enums.NotifyBizType;
import com.quickstart.draw.module.notify.realtime.NotifyRealtimePushService;
import com.quickstart.draw.module.notify.service.DrawNotifyService;
import com.quickstart.draw.module.notify.service.NotifyMessageService;
import com.quickstart.draw.module.notify.support.AfterCommitExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class DrawNotifyServiceImpl implements DrawNotifyService {

    @Resource
    private NotifyMessageService notifyMessageService;
    @Resource
    private NotifyRealtimePushService notifyRealtimePushService;
    @Resource
    private DrawMapper drawMapper;
    @Resource
    private DrawCodeMapper drawCodeMapper;
    @Resource
    private WinnerMapper winnerMapper;
    @Resource
    private AfterCommitExecutor afterCommitExecutor;
    @Resource(name = "drawNotifyExecutor")
    private Executor drawNotifyExecutor;

    @Override
    public void notifyDrawOpenedAsync(Long drawId) {
        drawNotifyExecutor.execute(() -> notifyDrawOpened(drawId));
    }

    @Override
    public void notifyDrawJoinedAsync(Long drawId, Long userId, List<String> codeValues) {
        drawNotifyExecutor.execute(() -> notifyDrawJoined(drawId, userId, codeValues));
    }

    @Override
    public void notifyDrawOpenedAfterCommit(Long drawId) {
        afterCommitExecutor.execute(() -> notifyDrawOpenedAsync(drawId));
    }

    @Override
    public void notifyDrawJoinedAfterCommit(Long drawId, Long userId, List<String> codeValues) {
        afterCommitExecutor.execute(() -> notifyDrawJoinedAsync(drawId, userId, codeValues));
    }

    private void notifyDrawOpened(Long drawId) {
        try {
            Draw draw = drawMapper.selectById(drawId);
            if (draw == null) {
                log.warn("Skip draw opened notify, draw not found: drawId={}", drawId);
                return;
            }

            LambdaQueryWrapper<Winner> winnerWrapper = new LambdaQueryWrapper<>();
            winnerWrapper.eq(Winner::getDrawId, drawId);
            List<Winner> winners = winnerMapper.selectList(winnerWrapper);
            Set<Long> winnerUserIds = new LinkedHashSet<>();
            for (Winner winner : winners) {
                winnerUserIds.add(winner.getUserId());
                createWinnerNotice(draw, winner);
            }

            LambdaQueryWrapper<DrawCode> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(DrawCode::getDrawId, drawId);
            List<DrawCode> codes = drawCodeMapper.selectList(codeWrapper);
            Set<Long> participantUserIds = new LinkedHashSet<>();
            for (DrawCode code : codes) {
                participantUserIds.add(code.getUserId());
            }
            for (Long userId : participantUserIds) {
                if (!winnerUserIds.contains(userId)) {
                    createDrawOpenedNotice(draw, userId);
                }
            }
        } catch (Exception e) {
            log.error("Create draw opened notify failed: drawId={}", drawId, e);
        }
    }

    private void notifyDrawJoined(Long drawId, Long userId, List<String> codeValues) {
        try {
            Draw draw = drawMapper.selectById(drawId);
            if (draw == null) {
                log.warn("Skip draw joined notify, draw not found: drawId={}", drawId);
                return;
            }
            String codeText = String.join(",", codeValues);
            NotifyMessage message = new NotifyMessage();
            message.setUserId(userId);
            message.setBizType(NotifyBizType.DRAW_CODE_GENERATED.name());
            message.setBizId(drawId + ":" + userId);
            message.setTitle("抽签码已生成");
            message.setContent("你参与的抽签「" + draw.getTitle() + "」已生成抽签码：" + codeText);
            message.setPayload("{\"drawId\":" + drawId + ",\"codes\":\"" + escapeJson(codeText) + "\"}");
            createAndPush(message);
        } catch (Exception e) {
            log.error("Create draw joined notify failed: drawId={}, userId={}", drawId, userId, e);
        }
    }

    private void createWinnerNotice(Draw draw, Winner winner) {
        NotifyMessage message = new NotifyMessage();
        message.setUserId(winner.getUserId());
        message.setBizType(NotifyBizType.WINNER_NOTICE.name());
        message.setBizId(String.valueOf(winner.getWinnerCodeId()));
        message.setTitle("恭喜中奖");
        message.setContent("你参与的抽签「" + draw.getTitle() + "」已开奖，恭喜你中奖！");
        message.setPayload("{\"drawId\":" + draw.getDrawId()
                + ",\"winnerCodeId\":" + winner.getWinnerCodeId()
                + ",\"prizeId\":" + winner.getPrizeId() + "}");
        createAndPush(message);
    }

    private void createDrawOpenedNotice(Draw draw, Long userId) {
        NotifyMessage message = new NotifyMessage();
        message.setUserId(userId);
        message.setBizType(NotifyBizType.DRAW_OPENED.name());
        message.setBizId(draw.getDrawId() + ":" + userId);
        message.setTitle("抽签已开奖");
        message.setContent("你参与的抽签「" + draw.getTitle() + "」已开奖，快去查看结果。");
        message.setPayload("{\"drawId\":" + draw.getDrawId() + "}");
        createAndPush(message);
    }

    private void createAndPush(NotifyMessage message) {
        NotifyMessage saved = notifyMessageService.createIfAbsent(message);
        notifyRealtimePushService.pushToUser(saved);
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
