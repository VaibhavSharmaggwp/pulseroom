package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.components.BottomActionBar
import com.workspace.pulseroom.ui.main.components.DraggableStickyNote
import com.workspace.pulseroom.ui.main.components.FancyToast
import com.workspace.pulseroom.ui.main.components.RoomHeader
import com.workspace.pulseroom.ui.main.components.WorkspaceGridBackground
import com.workspace.pulseroom.ui.main.model.StickyNoteModel
import com.workspace.pulseroom.ui.main.viewmodels.RoomsViewModel
import java.util.UUID
import kotlinx.coroutines.delay

/**
 * LiveRoomScreen:
 * Yeh app ka main Live Collaborative Room screen hai.
 * Yahan par:
 * 1. Starting me screen par koi note nahi hota ya backend se load hote hain.
 * 2. Jab user "Add note" click karta hai ya backend se koi naya note aata hai, tab note canvas par draw hota hai.
 * 3. Notes pure dotted canvas par freely kahi bhi drag kiye ja sakte hain.
 * 4. Top Header aur Bottom Action Bar fixed HUD ki tarah rehte hain.
 */
@Composable
fun LiveRoomScreen(
    onBackClick: () -> Unit = {},
    viewModel: RoomsViewModel = viewModel()
) {
    // 1. DYNAMIC STATE FOR NOTES:
    // Ab yeh ViewModel ke StateFlow se automatically sync rehti hai!
    val notesList by viewModel.notes.collectAsState()

    // Fancy Toast state
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Auto-connect to WebSocket room
    LaunchedEffect(Unit) {
        viewModel.joinRoom("room_3")
    }

    // Toast auto-dismiss timer (2.5 seconds)
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    // Naye notes ke liye pastel peach color
    val peachNoteColor = Color(0xFFFFDAB9)

    Box(modifier = Modifier.fillMaxSize()) {

        // ==========================================
        // LAYER 0 & 1: Dotted Background Canvas + Draggable Notes
        // ==========================================
        WorkspaceGridBackground {
            // Notes automatically observe viewModel.notes
            notesList.forEach { note ->
                DraggableStickyNote(
                    note = note,
                    onPositionChanged = { newPos ->
                        // 1. Real-time drag: ViewModel update aur Redis ko bhejo
                        viewModel.updateNotePosition(note.id, newPos, isDragging = true)
                    },
                    onDragEnd = { finalPos ->
                        // 2. Drag khatam: DB mein save karo
                        viewModel.updateNotePosition(note.id, finalPos, isDragging = false)
                    },
                    onTextChanged = { newText ->
                        // 3. Text edit hua: ViewModel aur DB update karo
                        viewModel.updateNoteText(note.id, newText)
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
        // LAYER 2.5: Fancy Toast Notification HUD
        // ==========================================
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 76.dp)
        ) {
            FancyToast(
                message = toastMessage,
                onDismiss = { toastMessage = null }
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
                    // Safety feature: Max 20 notes Spam Protection
                    if (notesList.size >= 20) {
                        toastMessage = "Max 20 notes allowed per screen!"
                    } else {
                        // JAB USER "Add note" CLICK KAREGA:
                        // 1. Naya note create karo screen ke center ke paas (X: 300, Y: 400)
                        val newNote = StickyNoteModel(
                            id = UUID.randomUUID().toString(),
                            title = "", // Empty title taaki focus direct body par ho
                            body = "Type here...",
                            author = "Vaibhav",
                            position = Offset(300f, 400f),
                            color = peachNoteColor
                        )
                        // 2. ViewModel me add karo (UI update + WebSocket broadcast)
                        viewModel.addNote(newNote)
                    }
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
