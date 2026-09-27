package com.workspace.collaborative_room.controller;


import com.workspace.collaborative_room.repository.RoomDocument;
import com.workspace.collaborative_room.repository.RoomDocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/rooms")
public class RoomController {
    private final RoomDocumentRepository documentRepository;

    public RoomController(RoomDocumentRepository documentRepository){
        this.documentRepository = documentRepository;
    }

    // Client join karne se pehle room ka purana data yahan se mangega
    @GetMapping("/{code}/snapshot")
    public ResponseEntity<RoomDocument> getRoomSnapshot(@PathVariable String code){
        System.out.println("Snapshot request aayi room: " + code + " ke liye.");

        return documentRepository.findById(code)
                .map(ResponseEntity ::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
