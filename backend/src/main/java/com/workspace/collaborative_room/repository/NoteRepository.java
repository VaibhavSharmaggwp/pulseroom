package com.workspace.collaborative_room.repository;

import com.workspace.collaborative_room.entity.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, String> {
    // Bas is method ka naam padh kar Spring Boot khud SQL query bana dega:
    // SELECT * FROM sticky_notes WHERE room_id = ?
    List<NoteEntity> findByRoomId(String roomId);
}
