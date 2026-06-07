package com.quickstart.draw.module.drawCode.mq;

import com.quickstart.draw.config.RabbitMqConfig;
import com.quickstart.common.domain.draw.Draw;

import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.drawCode.mq.DrawJoinMessage;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.util.DrawCodeGenerator;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "true")
public class DrawJoinConsumer {

    @Resource
    private DrawCodeGenerator drawCodeGenerator;

    @Resource
    private DrawCodeMapper drawCodeMapper;

    @Resource
    private DrawMapper drawMapper;

    @RabbitListener(queues = RabbitMqConfig.DRAW_JOIN_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleJoinDraw(DrawJoinMessage message, Channel channel, Message amqpMessage) throws IOException {
        Long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            Long drawId = message.getDrawId();
            Long userId = message.getUserId();

            // 1. 查询抽签活动并校验是否可参与
            Draw draw = drawMapper.selectById(drawId);
            ensureJoinable(draw);

            // 2. 获取每人可生成的码数量
            int perCodeNum = draw.getPerCodeNum();
            if (perCodeNum <= 0) {
                throw new IllegalArgumentException("每人参与码数量配置错误");
            }

            // 3. 生成唯一抽签码
            List<String> codeValues = drawCodeGenerator.batchGenerate(perCodeNum);

            // 4. 组装批量插入数据
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

            // 5. 批量插入
            drawCodeMapper.batchInsert(drawCodeList);

            // 手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理参与抽签消息失败: drawId={}, userId={}", message.getDrawId(), message.getUserId(), e);
            // 拒绝消息，不重新入队 -> 进入死信队列
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void ensureJoinable(Draw draw) {
        if (draw.getStatus() == null || draw.getStatus() != DrawConstants.DRAW_STATUS_RUNNING) {
            throw new IllegalArgumentException("当前抽奖已结束，无法参与");
        }
        if (draw.getJoinDeadline() != null && draw.getJoinDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("抽奖已截止，无法参与");
        }
    }
}
