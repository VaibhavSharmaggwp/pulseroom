package com.workspace.collaborative_room.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

// Iska kaam sirf itna hai ki high-speed drawing events ko safely Kafka ke topic mein daal dena.
@Service
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    // Yeh humare Kafka topic ka naam hai
    private static final String TOPIC = "room-drawing-events";

    public KafkaEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDrawingEvent(String roomId, String payload) {
        if (roomId == null || payload == null) {
            System.err.println("Kafka send skipped: roomId ya payload null hai");
            return;
        }

        // Hum roomId ko as a "Key" use kar rahe hain.
        // Isse guarantee milti hai ki same room ki saari drawing events same partition mein in-order save hongi.
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, roomId, payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Kafka Send Drawing Event Success -> Room: " + roomId
                        + " | Partition: " + result.getRecordMetadata().partition()
                        + " | Offset: " + result.getRecordMetadata().offset());
            } else {
                System.err.println("Kafka Send Drawing Event Error -> Room: " + roomId + " | Error: " + ex.getMessage());
            }
        });
    }
}
