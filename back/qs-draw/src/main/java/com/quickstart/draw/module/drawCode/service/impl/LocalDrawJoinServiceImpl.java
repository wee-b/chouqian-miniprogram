package com.quickstart.draw.module.drawCode.service.impl;

import com.quickstart.draw.module.drawCode.service.DrawJoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参与域实现（本地同步模式，rabbitmq.enabled=false 时激活）
 * 仅做路由，核心逻辑委托给 DrawJoinExecutor
 */
@Service
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "false")
public class LocalDrawJoinServiceImpl implements DrawJoinService {

    @Autowired
    private DrawJoinExecutor executor;

    @Override
    public List<String> joinDraw(Long drawId, Long userId, String ip) {
        return executor.executeJoin(drawId, userId, ip);
    }
}
