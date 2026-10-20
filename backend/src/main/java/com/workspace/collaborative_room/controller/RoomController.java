package com.workspace.collaborative_room.controller;

import com.workspace.collaborative_room.entity.RoomMetadataEntity;
import com.workspace.collaborative_room.repository.RoomDocument;
import com.workspace.collaborative_room.repository.RoomDocumentRepository;
import com.workspace.collaborative_room.repository.RoomMetadataRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomMetadataRepository roomRepository;
    private final RoomDocumentRepository documentRepository;
    private final StringRedisTemplate redisTemplate;

    public RoomController(RoomMetadataRepository roomRepository,
                          RoomDocumentRepository documentRepository,
                          StringRedisTemplate redisTemplate) {
        this.roomRepository = roomRepository;
        this.documentRepository = documentRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> requestData) {
        String roomName = (String) requestData.get("roomName");
        int maxMembers = 6;
        Object maxMembersObj = requestData.get("maxMembers");
        if (maxMembersObj instanceof Number) {
            maxMembers = ((Number) maxMembersObj).intValue();
        }
        String rawPassword = (String) requestData.get("password");
        String adminId = requestData.get("adminId") != null
                ? (String) requestData.get("adminId")
                : "android_user_01";

        // 1. Generate a 4-character unique short code (Like Among Us)
        String roomCode = generateShortCode();

        // 2. Password Hashing (Basic security)
        // Note: Production mein BCryptPasswordEncoder use karte hain (Spring Security module se).
        // Abhi ke liye hum simple String manipulation/hash dikha rahe hain.
        String hashedPassword = null;
        if (rawPassword != null && !rawPassword.isEmpty()) {
            hashedPassword = Integer.toHexString(rawPassword.hashCode());
        }

        // 3. Entity banao aur Database mein save karo
        RoomMetadataEntity newRoom = new RoomMetadataEntity(
                roomCode, roomName, adminId, maxMembers, hashedPassword
        );
        roomRepository.save(newRoom);

        // Redis mein bhi active mark kar do taaki WebSocket / joinRoom connect ho sake
        String redisKey = "room:" + roomCode;
        redisTemplate.opsForValue().set(redisKey, "ACTIVE", 12, TimeUnit.HOURS);

        System.out.println("New room created via admin API: " + roomCode + " (" + roomName + ")");

        // 4. Android ko newly created Room Code bhej do
        return ResponseEntity.ok(Map.of(
                "success", true,
                "roomId", roomCode,
                "message", "Room created successfully"
        ));
    }

    // 1. Naya Room Create Karna (Redis TTL ke sath)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRoom() {
        // Ek chota 6-character ka random room code generate karte hain
        String roomcode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        // Redis mein is room ko save karo, jo exactly 12 ghante baad auto-delete ho jayega
        String redisKey = "room:" + roomcode;
        redisTemplate.opsForValue().set(redisKey, "ACTIVE", 12, TimeUnit.HOURS);

        System.out.println("New room created : " + roomcode + " (Valid for 12 hours)");

        Map<String, Object> response = new HashMap<>();
        response.put("roomCode", roomcode);
        response.put("mode", "NOTES_AND_WHITEBOARD");

        return ResponseEntity.ok(response);
    }

    // 2. Room Join/Validate Karna
    @PostMapping("/{code}/join")
    public ResponseEntity<Map<String, String>> joinRoom(@PathVariable String code) {
        String redisKey = "room:" + code;
        // Check karo ki kya room Redis mein zinda hai ya database mein exist karta hai
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (!Boolean.TRUE.equals(exists)) {
            exists = roomRepository.existsById(code);
        }

        if (Boolean.TRUE.equals(exists)) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Room is valid.");
            return ResponseEntity.ok(response);
        } else {
            // Agar room expire ho gaya ya galat code hai
            return ResponseEntity.status(404).body(Map.of("error", "Room not found or expired"));
        }
    }

    // Snapshot wala API (Jaisa tha waisa hi rahega)
    @GetMapping("/{code}/snapshot")
    public ResponseEntity<RoomDocument> getRoomSnapshot(@PathVariable String code) {
        return documentRepository.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper function 4-character random code banane ke liye (e.g. "A7K9")
    private String generateShortCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (roomRepository.existsById(code));
        return code;
    }
}
