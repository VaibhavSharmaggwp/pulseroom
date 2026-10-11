package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.components.BottomActionBar
import com.workspace.pulseroom.ui.main.components.DraggableStickyNote
import com.workspace.pulseroom.ui.main.components.RoomHeader
import com.workspace.pulseroom.ui.main.components.WorkspaceGridBackground

@Composable
fun LiveRoomScreen(
    onBackClick: () -> Unit = {}
) {
    // The Box is our Z-axis container. The first item is at the bottom.
    Box(modifier = Modifier.fillMaxSize()) {

        val marigoldNote = Color(0xFFFFCC66)
        val peachNote = Color(0xFFFFDAB9)
        // Layer 0 & Layer 1: Grid Background and Canvas Content
        WorkspaceGridBackground {
            DraggableStickyNote(
                title = "Ship checklist",
                body = "7 of 9 done. Store copy + press embargo left.",
                author = "Lena",
                initialPosition = Offset(x = 100f, y = 200f),
                noteColor = marigoldNote
            )
            // Drop note 2
            DraggableStickyNote(
                title = "Pricing FAQ",
                body = "Who owns the annual-plan answer?",
                author = "Marco",
                initialPosition = Offset(x = 500f, y = 150f),
                noteColor = Color.White
            )
        }

        // Layer 3: HUD (Heads-Up Display) overlaying the canvas
        // Top Header
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            RoomHeader(
                roomTitle = "Q4 launch war room",
                participantCount = 24,
                timerText = "38:12",
                onBackClick = onBackClick
            )
        }

        // Layer 4: Bottom Action Bar anchored at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BottomActionBar(
                onAddNoteClick = {
                    println("Note add karne ka UI trigger hua!")
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
