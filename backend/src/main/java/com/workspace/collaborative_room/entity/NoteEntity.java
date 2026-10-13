package com.workspace.collaborative_room.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter


@Table(name = "sticky_notes")
public class NoteEntity {

    @Id // primary key in the table
    private String id;

    @Column(nullable = false)
    private String roomId;

    private String title;

    @Column(length = 1000) // Body text we write can lengthy so 1000 word limit
    private String body;

    private String author;

    // Canvas par note kahan rakha hai uske X aur Y coordinates
    private Double positionX;
    private Double positionY;

    private String  colorHex; // Compose ke color ko hum Hex String ("#FFDAB9") mein save karenge




}
