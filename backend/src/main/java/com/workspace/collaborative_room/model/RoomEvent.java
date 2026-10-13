package com.workspace.collaborative_room.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomEvent {
    private String eventId;
    private String type;  // Jaise: TEXT_UPDATE, DRAW_START, DRAW_POINTS
    private String roomId;
    private String clientId;
    private Long serverSeq;   // Server assign karega for ordering
    private Long clientSeq;
    private Long timestamp;
    private Map<String, Object> payload;
}


