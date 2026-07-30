package com.quickstart.draw.module.drawCode.service.impl;

import com.quickstart.draw.config.RabbitMqConfig;
import com.quickstart.draw.constant.RedisConstant;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.drawCode.mq.DrawJoinMessage;
import com.quickstart.common.domain.drawCode.vo.DrawCodeVO;
import com.quickstart.common.domain.winner.vo.WinnerVO;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.module.drawCode.service.DrawCodeService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "true")
public class MqDrawCodeServiceImpl implements DrawCodeService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public List<String> joinDraw(Long drawId, Long userId) {
        // 前置检查：参与次数上限（Redis 原子计数预留名额）
        checkAndIncrementPartCount(drawId, userId);

        DrawJoinMessage message = new DrawJoinMessage();
        message.setDrawId(drawId);
        message.setUserId(userId);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.DRAW_EXCHANGE,
                RabbitMqConfig.DRAW_JOIN_ROUTING_KEY,
                message
        );
        return List.of();
    }

    private void checkAndIncrementPartCount(Long drawId, Long userId) {
        String limitKey = RedisConstant.PART_LIMIT_PREFIX + ":" + drawId;
        String limitStr = redisTemplate.opsForValue().get(limitKey);
        if (limitStr == null) {
            return;
        }
        int limit = Integer.parseInt(limitStr);
        String countKey = RedisConstant.PART_COUNT_PREFIX + ":" + drawId;
        String userIdStr = String.valueOf(userId);

        Long newCount = redisTemplate.opsForHash().increment(countKey, userIdStr, 1);
        if (newCount != null && newCount > limit) {
            redisTemplate.opsForHash().increment(countKey, userIdStr, -1);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "您已达到参与次数上限");
        }
    }

    @Override
    public List<DrawCodeVO> getMyCodes(Long drawId, Long userId) {
        return List.of();
    }

    @Override
    public void openDraw(Long drawId, Long userId) {

    }

    @Override
    public List<WinnerVO> getWinners(Long drawId) {
        return List.of();
    }

}
