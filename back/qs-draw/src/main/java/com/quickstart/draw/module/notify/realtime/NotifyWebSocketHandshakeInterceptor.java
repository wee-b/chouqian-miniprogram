package com.quickstart.draw.module.notify.realtime;

import com.quickstart.common.domain.user.User;
import com.quickstart.draw.mapper.UserReadMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class NotifyWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserReadMapper userReadMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String memberCode = request.getHeaders().getFirst("X-User-Code");
        if (!StringUtils.hasText(memberCode) && request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            memberCode = httpRequest.getParameter("userCode");
        }
        if (!StringUtils.hasText(memberCode)) {
            log.warn("Notify websocket handshake rejected: memberCode missing");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        User user = userReadMapper.selectByMemberCode(memberCode);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            log.warn("Notify websocket handshake rejected: invalid user, memberCode={}", memberCode);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("userId", user.getUserId());
        attributes.put("userCode", user.getUserCode());
        log.info("Notify websocket handshake accepted: userId={}, memberCode={}", user.getUserId(), memberCode);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
