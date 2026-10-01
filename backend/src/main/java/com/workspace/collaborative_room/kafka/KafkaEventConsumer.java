package com.workspace.collaborative_room.kafka;

import com.workspace.collaborative_room.model.RoomEvent;
import com.workspace.collaborative_room.repository.RoomDocument;
import com.workspace.collaborative_room.repository.RoomDocumentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class KafkaEventConsumer {
    private final RoomDocumentRepository documentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaEventConsumer(RoomDocumentRepository documentRepository){
        this.documentRepository = documentRepository;
    }

    // Yeh worker lagatar "room-drawing-events" topic ko listen karta rahega
    @KafkaListener(topics = "room-drawing-events", groupId = "workspace-group")
    public void consumeDrawingEvents(String message){
        try{
            RoomEvent event = objectMapper.readValue(message, RoomEvent.class);
            // Hum DB mein sirf tabhi write karenge jab ek stroke poora ho jaye (Snapshot)
            if("DRAW_END".equals(event.getType())){
                System.out.println("Kafka Worker ne DRAW_END detect kiya. DB update kar rahe hain...");

                RoomDocument doc = documentRepository.findById(event.getRoomId())
                        .orElseGet(RoomDocument::new); // Agar room naya hai toh naya object banao

                doc.setRoomId(event.getRoomId());

                // V1 Implementation: Abhi ke liye hum latest payload ko hi save kar rahe hain.
                // Actual app mein yahan purane JSON aur naye JSON ko merge (append) karte hain.
                doc.setContentJson(objectMapper.writeValueAsString(event.getPayload()));
                doc.setUpdatedAt(System.currentTimeMillis());

                documentRepository.save(doc); // Postgres mein permanent save
                System.out.println("Room " + event.getRoomId() + " ka state PostgreSQL mein save ho gaya.");
            }
        } catch (Exception e) {
            System.err.println("Kafka message process karne mein DB error: " + e.getMessage());
        }
    }
}
