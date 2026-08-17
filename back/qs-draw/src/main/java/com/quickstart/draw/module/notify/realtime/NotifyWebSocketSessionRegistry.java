package com.quickstart.draw.module.notify.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotifyWebSocketSessionRegistry {

    private static final String USER_ID_ATTR = "userId";

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void add(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            closeQuietly(session);
            return;
        }
        sessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("Notify websocket connected: userId={}, sessionId={}", userId, session.getId());
    }

    public void remove(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return;
        }
        userSessions.remove(session);
        if (userSessions.isEmpty()) {
            sessions.remove(userId);
        }
        log.info("Notify websocket disconnected: userId={}, sessionId={}", userId, session.getId());
    }

    public Set<WebSocketSession> getSessions(Long userId) {
        return sessions.getOrDefault(userId, Collections.emptySet());
    }

    public boolean hasOnlineSession(Long userId) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        return userSessions != null && userSessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    private Long getUserId(WebSocketSession session) {
        Object value = session.getAttributes().get(USER_ID_ATTR);
        if (value instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            log.warn("Close anonymous notify websocket failed: sessionId={}", session.getId(), e);
        }
    }
}
