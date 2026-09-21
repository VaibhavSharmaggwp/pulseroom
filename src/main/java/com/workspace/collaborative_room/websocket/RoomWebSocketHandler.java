package com.workspace.collaborative_room.websocket;

import com.workspace.collaborative_room.kafka.KafkaEventProducer;
import com.workspace.collaborative_room.model.RoomEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
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


    // New dependencies for split of Kafka and Redis
    private final KafkaEventProducer kafkaProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StringRedisTemplate redisTemplate;

    public RoomWebSocketHandler(StringRedisTemplate redisTemplate, KafkaEventProducer kafkaProducer) {
        this.redisTemplate = redisTemplate;
        this.kafkaProducer = kafkaProducer;
    }

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
        String eventType = event.getType();

        if (roomId == null) {
            System.err.println("Event received without roomId: " + payload);
            return;
        }

        // Connection track karo
        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(roomId).add(session);
        sessionToRoomMap.put(session.getId(), roomId);

        // Raasta 1: Live Broadcast (Redis par publish karo taaki sabhi instances aur clients ko mil sake)
        System.out.println("Redis 'room-events' channel par message publish kar rahe hain...");
        redisTemplate.convertAndSend("room-events", payload);

        // Raasta 2: Durable Backup (Agar event drawing wala hai, toh usko disk pe save karne ke liye Kafka mein buffer karo)
        if ("DRAW_START".equals(eventType) || "DRAW_POINTS".equals(eventType) || "DRAW_END".equals(eventType)) {
            kafkaProducer.sendDrawingEvent(roomId, payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = sessionToRoomMap.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                System.out.println("Client " + session.getId() + " room " + roomId + " se leave kar gaya.");

                // Memory leak bachane ke liye: Agar room khali ho gaya toh map se hata do
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomId);
                    System.out.println("Room " + roomId + " empty ho gaya aur memory se clear kar diya.");
                }
            }
        }
    }

    // Yeh naya method add karo jise RedisSubscriber call karega
    public void broadcastLocally(String roomId, String payload) {
        Set<WebSocketSession> clientsInRoom = roomSessions.get(roomId);
        if (clientsInRoom != null) {
            for (WebSocketSession client : clientsInRoom) {
                if (client.isOpen()) {
                    try {
                        synchronized (client) {
                            client.sendMessage(new TextMessage(payload));
                        }
                    } catch (Exception e) {
                        System.err.println("Client " + client.getId() + " ko message bhejne mein error: " + e.getMessage());
                    }
                }
            }
        }
    }
}