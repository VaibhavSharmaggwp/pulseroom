package com.workspace.collaborative_room.websocket;

import com.workspace.collaborative_room.kafka.KafkaEventProducer;
import com.workspace.collaborative_room.model.RoomEvent;
import com.workspace.collaborative_room.service.NoteService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {

    // New dependencies for split of Kafka and Redis
    private final KafkaEventProducer kafkaProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, String> sessionToClientMap = new ConcurrentHashMap<>();

    private final NoteService noteService;

    public RoomWebSocketHandler(StringRedisTemplate redisTemplate, KafkaEventProducer kafkaProducer, NoteService noteService) {
        this.redisTemplate = redisTemplate;
        this.kafkaProducer = kafkaProducer;
        this.noteService = noteService;
    }

    // Room ID -> us room mein connected sabhi users ke WebSockets ka Set
    private final ConcurrentHashMap<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // Session ID -> Room ID mapping (disconnect handle karne ke liye)
    private final ConcurrentHashMap<String, String> sessionToRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Naya connection open hua: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        RoomEvent event = objectMapper.readValue(payload, RoomEvent.class);

        String roomId = event.getRoomId();
        String eventType = event.getType();
        String eventId = event.getEventId();
        String clientId = event.getClientId();

        if (roomId == null) {
            System.err.println("Event received without roomId: " + payload);
            return;
        }

        // 1. STEP 1: Room Validation Check (Database save se PEHLE aayega)
        String redisKey = "room:" + roomId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            if (isDefaultOrTestRoom(roomId)) {
                // Development / Test rooms ko auto-activate karo 12 ghante ke liye
                redisTemplate.opsForValue().set(redisKey, "ACTIVE", 12, TimeUnit.HOURS);
                System.out.println("Default/Test room auto-activated in Redis: " + roomId);
            } else {
                System.err.println("Illegal Entry Room expire ho chuka hai ya galat hai: " + roomId);
                session.close(new CloseStatus(4004, "Room Expired or Invalid"));
                return;
            }
        }

        // 2. Presence Event: USER_JOINED Check
        if (!sessionToRoomMap.containsKey(session.getId())) {
            sessionToRoomMap.put(session.getId(), roomId);
            sessionToClientMap.put(session.getId(), clientId); // this to remember which client came which left

            roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
            roomSessions.get(roomId).add(session);

            // UI ko batane ke liye System Event generate karo
            RoomEvent joinEvent = new RoomEvent();
            joinEvent.setEventId(java.util.UUID.randomUUID().toString());
            joinEvent.setType("USER_JOINED");
            joinEvent.setRoomId(roomId);
            joinEvent.setClientId(clientId);
            joinEvent.setServerSeq(redisTemplate.opsForValue().increment("room_seq:" + roomId));

            System.out.println(clientId + " room " + roomId + " mein aaya. USER_JOINED broadcast kar rahe hain.");
            redisTemplate.convertAndSend("room-events", objectMapper.writeValueAsString(joinEvent));
        }

        // 3. Idempotency Check (Duplicate Prevention)
        String duplicateCheckKey = "event:" + eventId;
        Boolean isNewEvent = redisTemplate.opsForValue().setIfAbsent(duplicateCheckKey, "PROCESSED", 5, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(isNewEvent)) {
            System.out.println("Duplicate event pakda gaya! EventId: " + eventId);
            return;
        }

        // 4. Traffic Controller Logic (Validation ke baad secure tarike se process karo)
        switch (event.getType()) {
            case "NOTE_ADDED":
            case "NOTE_DROPPED":
            case "NOTE_EDITED":
                // Ungli uth gayi ya note update hua -> Database mein save karo!
                noteService.processNoteEvent(event);
                break;

            case "NOTE_DRAGGING":
            case "USER_JOINED":
            case "MIC_MUTED":
                // Inko DB mein save nahi karna hai, direct Redis pe fek do real-time UI ke liye
                break;
            default:
                System.out.println("Unknown event type: " + event.getType());
        }

        // 5. Server Sequencing (serverSeq assign karna)
        String seqKey = "room_seq:" + roomId;
        Long newSeq = redisTemplate.opsForValue().increment(seqKey); // redis automatically + 1 kardega
        event.setServerSeq(newSeq);

        String updatedPayload = objectMapper.writeValueAsString(event);

        // Connection tracking
        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(roomId).add(session);
        sessionToRoomMap.put(session.getId(), roomId);

        // Raasta 1: Live Broadcast (Redis par publish karo taaki sabhi instances aur clients ko mil sake)
        System.out.println("Redis 'room-events' channel par message publish kar rahe hain...");
        redisTemplate.convertAndSend("room-events", updatedPayload);

        // Raasta 2: Durable Backup (Agar event drawing wala hai, toh usko disk pe save karne ke liye Kafka mein buffer karo)
        if ("DRAW_START".equals(eventType) || "DRAW_POINTS".equals(eventType) || "DRAW_END".equals(eventType)) {
            kafkaProducer.sendDrawingEvent(roomId, updatedPayload);
        }
    }

    private boolean isDefaultOrTestRoom(String roomId) {
        if (roomId == null) return false;
        return "room_3".equalsIgnoreCase(roomId)
                || "room_2".equalsIgnoreCase(roomId)
                || "D0429C".equalsIgnoreCase(roomId)
                || roomId.toLowerCase().startsWith("room_");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = sessionToRoomMap.remove(session.getId());
        String clientId = sessionToClientMap.remove(session.getId()); // Pata lagao kaunsa user gaya

        if (roomId != null && clientId != null) {
            if (roomSessions.containsKey(roomId)) {
                Set<WebSocketSession> sessions = roomSessions.get(roomId);
                if (sessions != null) {
                    sessions.remove(session);
                    System.out.println("Client " + session.getId() + " (" + clientId + ") room " + roomId + " se leave kar gaya.");

                    // Memory leak bachane ke liye: Agar room khali ho gaya toh map se hata do
                    if (sessions.isEmpty()) {
                        roomSessions.remove(roomId);
                        System.out.println("Room " + roomId + " empty ho gaya aur memory se clear kar diya.");
                    }
                }
            }

            // Presence Event: USER_LEFT Broadcast karo
            RoomEvent leftEvent = new RoomEvent();
            leftEvent.setEventId(java.util.UUID.randomUUID().toString());
            leftEvent.setType("USER_LEFT");
            leftEvent.setRoomId(roomId);
            leftEvent.setClientId(clientId);
            leftEvent.setServerSeq(redisTemplate.opsForValue().increment("room_seq:" + roomId));

            System.out.println(clientId + " ka connection toot gaya. USER_LEFT broadcast kar rahe hain.");
            redisTemplate.convertAndSend("room-events", objectMapper.writeValueAsString(leftEvent));
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