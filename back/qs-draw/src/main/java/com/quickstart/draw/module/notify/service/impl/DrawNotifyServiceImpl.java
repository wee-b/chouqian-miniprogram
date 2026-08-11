package com.quickstart.draw.module.notify.service.impl;

import com.quickstart.draw.module.notify.service.DrawNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DrawNotifyServiceImpl implements DrawNotifyService {

    @Async
    @Override
    public void notifyDrawOpenedAsync(Long drawId) {
        log.info("准备发送开奖成功异步通知, drawId={}", drawId);

        // TODO 后续这里可以：
        // 1. 发 RabbitMQ 消息
        // 2. 调 qs-ops 的 Dubbo 通知服务
        // 3. 推 WebSocket / SSE
        // 4. 写站内信
    }
}
