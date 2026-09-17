package com.workspace.collaborative_room.websocket;


import com.workspace.collaborative_room.model.RoomEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.w3c.dom.Text;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory store for Phase 1: Track active sessions
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions =
            new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        // Jab naya client connect hoga
        activeSessions.put(session.getId(), session);
        System.out.println("Connected to " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        // Client se aane wala JSON message yahan aayega
        String payload = message.getPayload();

        // JSON string ko Java object (RoomEvent) mein convert kar rahe hain
        RoomEvent event = objectMapper.readValue(payload, RoomEvent.class);
        System.out.println("Message aaya Room: " + event.getRoomId() + " se. Event Type: " + event.getType());

        // V1 Logic: Server event ko validate aur broadcast karega[cite: 1].
        // TODO Abhi ke liye hum sirf receive kar rahe hain. Aage hum isey Redis aur same room ke baaki clients ko bhejenge.
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        // Jab client disconnect ho jaye (Network issue ya tab close)
        activeSessions.remove(session.getId());
        System.out.println("Disconnected from " + session.getId());
    }
}
