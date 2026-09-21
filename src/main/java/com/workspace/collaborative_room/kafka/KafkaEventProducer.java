//Iska kaam sirf itna hai ki high-speed drawing events ko safely Kafka ke tape recorder (topic) mein daal dena.


package com.workspace.collaborative_room.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    // Yeh humare Kafka topic (register/diary) ka naam hai
    private static final String TOPIC = "room-drawing-events";

    public KafkaEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDrawingEvent(String roomId, String payload){
        // Hum roomId ko as a "Key" use kar rahe hain.
        // Isse guarantee milti hai ki ek room ki saari drawing in-order save hogi (sequence kharab nahi hoga).
        kafkaTemplate.send(TOPIC, payload);
        System.out.println("Kafka Send Drawing Event-->" + roomId);
    }

}
