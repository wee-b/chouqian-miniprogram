package com.quickstart.draw.module.notify.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickstart.draw.module.notify.constant.NotifyConstants;
import com.quickstart.draw.module.notify.domain.NotifyMessage;
import com.quickstart.draw.module.notify.mapper.NotifyMessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class NotifyRealtimePushServiceImpl implements NotifyRealtimePushService {

    @Resource
    private NotifyWebSocketSessionRegistry sessionRegistry;
    @Resource
    private NotifyMessageMapper notifyMessageMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean pushToUser(NotifyMessage message) {
        if (message == null || message.getUserId() == null) {
            return false;
        }
        if (!sessionRegistry.hasOnlineSession(message.getUserId())) {
            log.info("Skip notify websocket push, user offline: userId={}, notifyId={}",
                    message.getUserId(), message.getNotifyId());
            return false;
        }

        boolean pushed = false;
        Set<WebSocketSession> sessions = sessionRegistry.getSessions(message.getUserId());
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(buildPayload(message)));
                pushed = true;
                log.info("Notify websocket pushed: userId={}, notifyId={}, sessionId={}",
                        message.getUserId(), message.getNotifyId(), session.getId());
            } catch (Exception e) {
                log.warn("Push notify websocket failed: userId={}, notifyId={}, sessionId={}",
                        message.getUserId(), message.getNotifyId(), session.getId(), e);
            }
        }

        if (pushed) {
            message.setPushStatus(NotifyConstants.PUSH_STATUS_SUCCESS);
            message.setUpdateTime(LocalDateTime.now());
            notifyMessageMapper.updateById(message);
        }
        return pushed;
    }

    private String buildPayload(NotifyMessage message) throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("event", "NOTIFY_MESSAGE");
        data.put("notifyId", message.getNotifyId());
        data.put("bizType", message.getBizType());
        data.put("bizId", message.getBizId());
        data.put("title", message.getTitle());
        data.put("content", message.getContent());
        data.put("payload", message.getPayload());
        data.put("readFlag", message.getReadFlag());
        data.put("createTime", message.getCreateTime());
        return objectMapper.writeValueAsString(data);
    }
}
