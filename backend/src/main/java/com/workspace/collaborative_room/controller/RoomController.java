package com.workspace.collaborative_room.controller;


import com.workspace.collaborative_room.repository.RoomDocument;
import com.workspace.collaborative_room.repository.RoomDocumentRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/rooms")
public class RoomController {
    private final RoomDocumentRepository documentRepository;
    private final StringRedisTemplate redisTemplate;

    public RoomController(RoomDocumentRepository documentRepository, StringRedisTemplate redisTemplate){
        this.documentRepository = documentRepository;
        this.redisTemplate = redisTemplate;
    }

    // 1. Naya Room Create Karna (Redis TTL ke sath)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRoom(){
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
    public ResponseEntity<Map<String, String>> joinRoom(@PathVariable String code){
        String redisKey = "room:" + code;
        // Check karo ki kya room Redis mein zinda hai
        Boolean exists = redisTemplate.hasKey(redisKey);

        if(Boolean.TRUE.equals(exists)){
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Room is valid.");
            return ResponseEntity.ok(response);
        }else{
            // Agar room expire ho gaya ya galat code hai
            return ResponseEntity.status(404).body(Map.of("error", "Room not found or expired"));
        }
    }

    // Purana Snapshot wala API (Jaisa tha waisa hi rahega)
    @GetMapping("/{code}/snapshot")
    public ResponseEntity<RoomDocument> getRoomSnapshot(@PathVariable String code){
        return documentRepository.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
