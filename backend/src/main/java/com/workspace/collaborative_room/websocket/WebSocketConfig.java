package com.workspace.collaborative_room.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final RoomWebSocketHandler roomWebSocketHandler;

    public WebSocketConfig(RoomWebSocketHandler roomWebSocketHandler) {
        this.roomWebSocketHandler = roomWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Client is URL par connect karega: ws://localhost:8080/ws ya ws://localhost:8080/ws/rooms
        registry.addHandler(roomWebSocketHandler, "/ws", "/ws/rooms")
                .setAllowedOrigins("*"); // Development ke liye sabhi origins allow kar rahe hain
    }
}
