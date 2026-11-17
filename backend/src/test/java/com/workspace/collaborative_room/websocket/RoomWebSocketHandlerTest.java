package com.workspace.collaborative_room.websocket;

import com.workspace.collaborative_room.entity.RoomMetadataEntity;
import com.workspace.collaborative_room.kafka.KafkaEventProducer;
import com.workspace.collaborative_room.model.RoomEvent;
import com.workspace.collaborative_room.repository.RoomMetadataRepository;
import com.workspace.collaborative_room.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomWebSocketHandlerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private KafkaEventProducer kafkaProducer;

    @Mock
    private NoteService noteService;

    @Mock
    private RoomMetadataRepository roomRepository;

    @Mock
    private WebSocketSession session1; // Represents Emulator 1

    @Mock
    private WebSocketSession session2; // Represents Emulator 2

    private RoomWebSocketHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RoomMetadataEntity testRoom;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L, 4L);

        handler = new RoomWebSocketHandler(redisTemplate, kafkaProducer, noteService, roomRepository);

        testRoom = new RoomMetadataEntity("TEST_ROOM", "Test Room", "admin1", 6, null, 0);
        lenient().when(roomRepository.findById("TEST_ROOM")).thenReturn(Optional.of(testRoom));
        lenient().when(roomRepository.save(any(RoomMetadataEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock sessions
        lenient().when(session1.getId()).thenReturn("sess-emu-1");
        lenient().when(session1.getUri()).thenReturn(URI.create("ws://localhost:8080/ws?roomId=TEST_ROOM"));
        lenient().when(session1.isOpen()).thenReturn(true);

        lenient().when(session2.getId()).thenReturn("sess-emu-2");
        lenient().when(session2.getUri()).thenReturn(URI.create("ws://localhost:8080/ws?roomId=TEST_ROOM"));
        lenient().when(session2.isOpen()).thenReturn(true);
    }

    @Test
    @DisplayName("End-to-End Simulation: Emulator 1 connects -> Count 1, Emulator 2 connects -> Count 2, Emulator 2 kills app -> Count 1")
    void testLiveParticipantCountLifecycle() throws Exception {
        System.out.println("===============================================================");
        System.out.println("TEST STEP 1: Emulator 1 opens app & joins room TEST_ROOM");
        System.out.println("===============================================================");

        // Emulator 1 connects
        handler.afterConnectionEstablished(session1);

        // Verify DB count updated to 1
        assertEquals(1, testRoom.getActiveMembers(), "DB activeMembers should be 1 after Emulator 1 connects");

        // Verify Redis published MEMBER_COUNT_UPDATED with count = 1
        ArgumentCaptor<String> eventCaptor1 = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate, atLeastOnce()).convertAndSend(eq("room-events"), eventCaptor1.capture());

        RoomEvent event1 = objectMapper.readValue(eventCaptor1.getValue(), RoomEvent.class);
        assertEquals("MEMBER_COUNT_UPDATED", event1.getType());
        assertEquals("TEST_ROOM", event1.getRoomId());
        assertEquals(1, ((Number) event1.getPayload().get("count")).intValue(), "Payload count should be 1 for Emulator 1");
        System.out.println("✔ Emulator 1 joined -> Active Count: " + event1.getPayload().get("count"));

        System.out.println("\n===============================================================");
        System.out.println("TEST STEP 2: Emulator 2 opens app & joins SAME room TEST_ROOM");
        System.out.println("===============================================================");

        // Emulator 2 connects
        handler.afterConnectionEstablished(session2);

        // Verify DB count updated to 2
        assertEquals(2, testRoom.getActiveMembers(), "DB activeMembers should be 2 after Emulator 2 connects");

        // Verify Redis published MEMBER_COUNT_UPDATED with count = 2
        ArgumentCaptor<String> eventCaptor2 = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate, atLeast(2)).convertAndSend(eq("room-events"), eventCaptor2.capture());

        RoomEvent event2 = objectMapper.readValue(eventCaptor2.getValue(), RoomEvent.class);
        assertEquals("MEMBER_COUNT_UPDATED", event2.getType());
        assertEquals(2, ((Number) event2.getPayload().get("count")).intValue(), "Payload count should be 2");
        System.out.println("✔ Emulator 2 joined -> Live Count updated to: " + event2.getPayload().get("count"));

        // Verify that broadcastLocally sends this count update to Emulator 1 without refresh!
        handler.broadcastLocally("TEST_ROOM", eventCaptor2.getValue());
        ArgumentCaptor<TextMessage> msgCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1, atLeastOnce()).sendMessage(msgCaptor.capture());
        assertTrue(msgCaptor.getValue().getPayload().contains("\"count\":2"));
        System.out.println("✔ Magic confirmed: Emulator 1 received real-time WebSocket push: count = 2 (screen updates without refresh)");

        System.out.println("\n===============================================================");
        System.out.println("TEST STEP 3: Emulator 2 swipes up / kills app (connection closes)");
        System.out.println("===============================================================");

        // Emulator 2 disconnects
        handler.afterConnectionClosed(session2, CloseStatus.NORMAL);

        // Verify DB count decremented back to 1
        assertEquals(1, testRoom.getActiveMembers(), "DB activeMembers should decrement back to 1");

        // Verify Redis published MEMBER_COUNT_UPDATED with count = 1
        ArgumentCaptor<String> eventCaptor3 = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate, atLeast(3)).convertAndSend(eq("room-events"), eventCaptor3.capture());

        RoomEvent event3 = objectMapper.readValue(eventCaptor3.getValue(), RoomEvent.class);
        assertEquals("MEMBER_COUNT_UPDATED", event3.getType());
        assertEquals(1, ((Number) event3.getPayload().get("count")).intValue(), "Payload count should be 1 after disconnect");
        System.out.println("✔ Emulator 2 disconnected -> Live Count decremented back to: " + event3.getPayload().get("count"));

        // Verify Emulator 1 receives the decrement event live
        reset(session1);
        lenient().when(session1.isOpen()).thenReturn(true);
        handler.broadcastLocally("TEST_ROOM", eventCaptor3.getValue());
        ArgumentCaptor<TextMessage> msgCaptor2 = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(msgCaptor2.capture());
        assertTrue(msgCaptor2.getValue().getPayload().contains("\"count\":1"));
        System.out.println("✔ Magic confirmed: Emulator 1 received real-time WebSocket push: count = 1");
        System.out.println("===============================================================");
        System.out.println("ALL 3 STEPS PASSED SUCCESSFULLY! 🚀");
        System.out.println("===============================================================");
    }
}
