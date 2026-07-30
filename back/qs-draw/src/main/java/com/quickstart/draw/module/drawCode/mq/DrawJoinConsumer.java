package com.quickstart.draw.module.drawCode.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.draw.config.RabbitMqConfig;
import com.quickstart.common.domain.draw.Draw;

import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.drawCode.mq.DrawJoinMessage;
import com.quickstart.draw.constant.RedisConstant;
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
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Resource
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMqConfig.DRAW_JOIN_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleJoinDraw(DrawJoinMessage message, Channel channel, Message amqpMessage) throws IOException {
        Long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        Long drawId = message.getDrawId();
        Long userId = message.getUserId();
        try {
            // 1. 查询抽签活动并校验是否可参与
            Draw draw = drawMapper.selectById(drawId);
            ensureJoinable(draw);

            // 2. 幂等性校验：防止重复消费
            LambdaQueryWrapper<DrawCode> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(DrawCode::getDrawId, drawId);
            existWrapper.eq(DrawCode::getUserId, userId);
            if (drawCodeMapper.selectCount(existWrapper) > 0) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 3. 获取每人可生成的码数量
            int perCodeNum = draw.getPerCodeNum();
            if (perCodeNum <= 0) {
                throw new IllegalArgumentException("每人参与码数量配置错误");
            }

            // 4. 生成唯一抽签码
            List<String> codeValues = drawCodeGenerator.batchGenerate(perCodeNum);

            // 5. 组装批量插入数据
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

            // 6. 批量插入
            drawCodeMapper.batchInsert(drawCodeList);

            // 7. 更新抽签统计
            draw.setParticipantCount(draw.getParticipantCount() + 1);
            draw.setCodeCount(draw.getCodeCount() + perCodeNum);
            drawMapper.updateById(draw);

            // 手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理参与抽签消息失败: drawId={}, userId={}", drawId, userId, e);
            // 回滚 Redis 参与计数
            rollbackPartCount(drawId, userId);
            // 拒绝消息，不重新入队 -> 进入死信队列
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void rollbackPartCount(Long drawId, Long userId) {
        try {
            String countKey = RedisConstant.PART_COUNT_PREFIX + ":" + drawId;
            String userIdStr = String.valueOf(userId);
            redisTemplate.opsForHash().increment(countKey, userIdStr, -1);
        } catch (Exception ex) {
            log.error("回滚参与计数失败: drawId={}, userId={}", drawId, userId, ex);
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
