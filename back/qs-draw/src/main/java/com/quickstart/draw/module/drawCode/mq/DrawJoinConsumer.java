package com.quickstart.draw.module.drawCode.mq;

import com.quickstart.common.domain.drawCode.mq.DrawJoinMessage;
import com.quickstart.draw.config.RabbitMqConfig;
import com.quickstart.draw.module.drawCode.service.impl.DrawJoinExecutor;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MQ 消费者 —— 仅负责消息收发 + 失败补偿，核心业务逻辑委托给 DrawJoinExecutor。
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "true")
public class DrawJoinConsumer {

    @Resource
    private DrawJoinExecutor executor;

    @RabbitListener(queues = RabbitMqConfig.DRAW_JOIN_QUEUE)
    public void handleJoinDraw(DrawJoinMessage message, Channel channel, Message amqpMessage) throws IOException {
        Long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        Long drawId = message.getDrawId();
        Long userId = message.getUserId();
        try {
            // 核心逻辑全部委托给 executor（校验→生码→入库→统计→Dubbo）
            executor.executeJoin(drawId, userId, null);

            // 手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理参与抽签消息失败: drawId={}, userId={}", drawId, userId, e);
            // 业务侧补偿由 DrawJoinExecutor 负责；消息层只决定是否进入死信队列
            // 拒绝消息，不重新入队 -> 进入死信队列
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
