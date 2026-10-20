package com.workspace.collaborative_room.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomMetadataEntity {
    @Id
    private String roomId; //(e.g., "XY9A")
    private String roomName;
    private String adminId; // the host of the room
    private int MaxMembers;  // Default 6, ya jo user ne slider par set kiya
    private String hashedPassword;
}
