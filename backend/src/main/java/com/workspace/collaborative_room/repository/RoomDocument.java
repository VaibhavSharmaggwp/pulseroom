package com.workspace.collaborative_room.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table
@Data
public class RoomDocument {
    @Id
    private String roomId;  // Room code hi primary key hogi
    @Column(columnDefinition = "TEXT")
    private String contentJson; // Canvas ke saare strokes JSON format mein yahan rahenge
    private Long updatedAt;
}
