package com.workspace.collaborative_room.service;

import com.workspace.collaborative_room.entity.NoteEntity;
import com.workspace.collaborative_room.model.RoomEvent;
import com.workspace.collaborative_room.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NoteService {
    private final NoteRepository noteRepository;

    // Dependency Injection (Constructor Injection is best practice)

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    // Yeh method naye room me join hone par saare purane notes load karega
    public List<NoteEntity> getNotesFromRoom(String roomId){
        return noteRepository.findByRoomId(roomId);
    }

    // Yeh method WebSocket se aane wale naye/moved notes ko process karega
    public void processNoteEvent(RoomEvent event){
        Map<String, Object> payload = event.getPayload();
        if(payload == null || !payload.containsKey("id")){
            return; // Agar payload khali hai ya ID nahi hai, toh aage mat bado
        }

        String noteId  = payload.get("id").toString();
        String roomId = event.getRoomId();

        // 1. Database mein check karo kya yeh note pehle se hai?
        // Agar nahi hai, toh ek naya NoteEntity bana do.
        NoteEntity note = noteRepository.findById(noteId).orElse(new NoteEntity());

        note.setId(noteId);
        note.setRoomId(roomId);

        // 2. Payload mein jo jo naya data aaya hai, usko Entity mein set karo
        if (payload.containsKey("title")) note.setTitle(payload.get("title").toString());
        if (payload.containsKey("body")) note.setBody(payload.get("body").toString());
        if (payload.containsKey("author")) note.setAuthor(payload.get("author").toString());
        if (payload.containsKey("colorHex")) note.setColorHex(payload.get("colorHex").toString());

        // Coordinates ko safely Double mein convert karo
        if(payload.containsKey("positionX")){
            note.setPositionX(Double.valueOf(payload.get("positionX").toString()));
        }

        if (payload.containsKey("positionY")) {
            note.setPositionY(Double.valueOf(payload.get("positionY").toString()));
        }

        // 3. Database mein save kar do (Agar ID pehle se hai toh UPDATE hoga, varna INSERT)
        noteRepository.save(note);
        System.out.println("Note " + noteId + " database mein save ho gaya!");
    }
}
