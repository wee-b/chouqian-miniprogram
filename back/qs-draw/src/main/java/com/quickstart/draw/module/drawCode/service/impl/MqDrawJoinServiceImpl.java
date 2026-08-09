package com.quickstart.draw.module.drawCode.service.impl;

import com.quickstart.common.domain.drawCode.mq.DrawJoinMessage;
import com.quickstart.draw.config.RabbitMqConfig;
import com.quickstart.draw.module.drawCode.service.DrawJoinService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参与域实现（MQ 异步模式，rabbitmq.enabled=true 时激活）
 * 仅发送 MQ 消息，核心逻辑由 DrawJoinConsumer → DrawJoinExecutor 执行
 */
@Service
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "true")
public class MqDrawJoinServiceImpl implements DrawJoinService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<String> joinDraw(Long drawId, Long userId, String ip) {
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
}
