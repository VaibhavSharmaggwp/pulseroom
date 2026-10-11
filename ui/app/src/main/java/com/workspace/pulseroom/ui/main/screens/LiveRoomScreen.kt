package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.components.BottomActionBar
import com.workspace.pulseroom.ui.main.components.DraggableStickyNote
import com.workspace.pulseroom.ui.main.components.RoomHeader
import com.workspace.pulseroom.ui.main.components.WorkspaceGridBackground
import com.workspace.pulseroom.ui.main.model.StickyNoteModel
import java.util.UUID

/**
 * LiveRoomScreen:
 * Yeh app ka main Live Collaborative Room screen hai.
 * Yahan par:
 * 1. Starting me screen par koi note nahi hota (empty canvas).
 * 2. Jab user "Add note" click karta hai, tabhi ek naya note spawn hota hai.
 * 3. Notes pure dotted canvas par freely kahi bhi drag kiye ja sakte hain.
 * 4. Top Header aur Bottom Action Bar fixed HUD ki tarah rehte hain.
 */
@Composable
fun LiveRoomScreen(
    onBackClick: () -> Unit = {}
) {
    // 1. DYNAMIC STATE FOR NOTES:
    // Shuruat me list bilkul EMPTY rehti hai. Koi pre-existing note nahi hoga.
    var notesList by remember {
        mutableStateOf(emptyList<StickyNoteModel>())
    }

    // Naye notes ke liye pastel peach color
    val peachNoteColor = Color(0xFFFFDAB9)

    Box(modifier = Modifier.fillMaxSize()) {

        // ==========================================
        // LAYER 0 & 1: Dotted Background Canvas + Draggable Notes
        // ==========================================
        WorkspaceGridBackground {
            // Sirf wahi notes draw honge jo user ne "Add note" click karke banaye hain
            notesList.forEach { note ->
                DraggableStickyNote(
                    note = note,
                    onPositionChanged = { newPos ->
                        // Pure dotted canvas par note freely drag hoga aur nayi position state me save hogi
                        notesList = notesList.map {
                            if (it.id == note.id) it.copy(position = newPos) else it
                        }
                    }
                )
            }
        }

        // ==========================================
        // LAYER 2 (TOP): Fixed Room Header HUD
        // ==========================================
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            RoomHeader(
                roomTitle = "Q4 launch war room",
                participantCount = 24,
                timerText = "38:12",
                onBackClick = onBackClick
            )
        }

        // ==========================================
        // LAYER 3 (BOTTOM): Compact Floating Action Bar HUD
        // ==========================================
        // Bottom center me thoda padding dekar float karega taaki navigation bar se chipke nahi
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            BottomActionBar(
                onAddNoteClick = {
                    // JAB USER "Add note" CLICK KAREGA:
                    // 1. Naya note create karo screen ke center ke paas (X: 250, Y: 400)
                    val newNote = StickyNoteModel(
                        id = UUID.randomUUID().toString(),
                        title = "New Idea",
                        body = "Tap to edit text...",
                        author = "Vaibhav",
                        position = Offset(250f, 400f),
                        color = peachNoteColor
                    )
                    // 2. List me naya note add ho jayega aur canvas par pop up ho kar freely draggable ban jayega!
                    notesList = notesList + newNote
                },
                onRaiseHandClick = {
                    println("Raise hand trigger hua!")
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LiveRoomScreenPreview() {
    PulseRoomTheme {
        LiveRoomScreen()
    }
}
