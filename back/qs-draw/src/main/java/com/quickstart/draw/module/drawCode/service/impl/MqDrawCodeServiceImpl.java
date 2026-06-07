package com.quickstart.draw.module.drawCode.service.impl;

import com.quickstart.draw.config.RabbitMqConfig;
import com.quickstart.common.domain.drawCode.mq.DrawJoinMessage;
import com.quickstart.common.domain.drawCode.vo.DrawCodeVO;
import com.quickstart.common.domain.winner.vo.WinnerVO;
import com.quickstart.draw.module.drawCode.service.DrawCodeService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "true")
public class MqDrawCodeServiceImpl implements DrawCodeService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<String> joinDraw(Long drawId, Long userId) {
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
