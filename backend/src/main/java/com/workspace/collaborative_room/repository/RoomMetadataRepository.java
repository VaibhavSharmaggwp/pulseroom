package com.workspace.collaborative_room.repository;

import com.workspace.collaborative_room.entity.RoomMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomMetadataRepository extends JpaRepository<RoomMetadataEntity, String> {

}
