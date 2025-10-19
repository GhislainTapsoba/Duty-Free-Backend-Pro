package com.djbc.dutyfree.config;

import com.djbc.dutyfree.websocket.NotificationWebSocketHandler;
import com.djbc.dutyfree.websocket.SaleWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SaleWebSocketHandler saleWebSocketHandler;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(saleWebSocketHandler, "/ws/sales")
                .setAllowedOrigins("*");

        registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
                .setAllowedOrigins("*");
    }
}