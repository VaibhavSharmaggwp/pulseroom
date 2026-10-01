package com.workspace.collaborative_room.redis;

import com.workspace.collaborative_room.model.RoomEvent;
import com.workspace.collaborative_room.websocket.RoomWebSocketHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RedisSubscriber {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RoomWebSocketHandler webSocketHandler;

    public RedisSubscriber(RoomWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    // Yeh method tab automatically call hoga jab Redis par koi naya message aayega
    public void onMessage(String message, String channel){
        try{
            System.out.println("Message received from Redis: " + message);
            RoomEvent event = objectMapper.readValue(message, RoomEvent.class);

            // Ab WebSocket handler ko bolenge ki apne local connected clients ko bhej do
            webSocketHandler.broadcastLocally(event.getRoomId(), message);
        }catch (Exception e){
            System.err.println("Redis message parse karne mein error: " + e.getMessage());
        }
    }

}
