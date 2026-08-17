package com.quickstart.draw.module.notify.realtime;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class NotifyWebSocketConfig implements WebSocketConfigurer {

    @Resource
    private NotifyWebSocketHandler notifyWebSocketHandler;
    @Resource
    private NotifyWebSocketHandshakeInterceptor notifyWebSocketHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notifyWebSocketHandler, "/ws/notify")
                .addInterceptors(notifyWebSocketHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
