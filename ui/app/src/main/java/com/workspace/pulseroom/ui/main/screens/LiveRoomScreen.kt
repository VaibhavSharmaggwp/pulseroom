package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.workspace.pulseroom.theme.PulseRoomTheme
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
                title = "Ship checklist", //[cite: 15]
                body = "7 of 9 done. Store copy + press embargo left.", //[cite: 15]
                author = "Lena", //[cite: 15]
                initialPosition = Offset(x = 100f, y = 200f), // Screen mein X: 100, Y: 200 par girega
                noteColor = marigoldNote
            )
            // Drop note 2
            DraggableStickyNote(
                title = "Pricing FAQ", //[cite: 15]
                body = "Who owns the annual-plan answer?", //[cite: 15]
                author = "Marco", //[cite: 15]
                initialPosition = Offset(x = 500f, y = 150f), // Thoda right side mein
                noteColor = Color.White
            )
        }

        // Layer 3: HUD (Heads-Up Display) overlaying the canvas
        // Aligning to the TopCenter ensures it stays fixed while the canvas underneath pans
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            RoomHeader(
                roomTitle = "Q4 launch war room",
                participantCount = 24,
                timerText = "38:12",
                onBackClick = onBackClick
            )
        }

        // We will add the Bottom Sheet (People 24, Chat 3, Raise hand) here aligned to Alignment.BottomCenter
    }
}

@Preview(showBackground = true)
@Composable
private fun LiveRoomScreenPreview() {
    PulseRoomTheme {
        LiveRoomScreen()
    }
}
