package com.workspace.collaborative_room.websocket;

import com.workspace.collaborative_room.model.RoomEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Room ID -> us room mein connected sabhi users ke WebSockets ka Set
    private final ConcurrentHashMap<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // Session ID -> Room ID mapping (disconnect handle karne ke liye)
    private final ConcurrentHashMap<String, String> sessionToRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Naya connection open hua: " + session.getId());
        // Abhi user kis room mein hai yeh nahi pata, wo pehle message se pata chalega.
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        RoomEvent event = objectMapper.readValue(payload, RoomEvent.class);
        String roomId = event.getRoomId();

        System.out.println("Room: " + roomId + " | Event: " + event.getType() + " | Client: " + event.getClientId());

        // 1. Agar user pehli baar message bhej raha hai, toh use room mein add karo
        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(roomId).add(session);
        sessionToRoomMap.put(session.getId(), roomId);

        // 2. Room ke sabhi active users ko message broadcast karo (Khud ko chhod kar)
        Set<WebSocketSession> clientsInRoom = roomSessions.get(roomId);
        for (WebSocketSession client : clientsInRoom) {
            if (client.isOpen() && !client.getId().equals(session.getId())) {
                client.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = sessionToRoomMap.remove(session.getId());
        if (roomId != null && roomSessions.containsKey(roomId)) {
            roomSessions.get(roomId).remove(session);
            System.out.println("Client " + session.getId() + " room " + roomId + " se leave kar gaya.");

            // Memory leak bachane ke liye: Agar room khali ho gaya toh map se hata do
            if (roomSessions.get(roomId).isEmpty()) {
                roomSessions.remove(roomId);
                System.out.println("Room " + roomId + " empty ho gaya aur memory se clear kar diya.");
            }
        }
    }
}