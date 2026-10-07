package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.components.RoomHeader
import com.workspace.pulseroom.ui.main.components.WorkspaceGridBackground

@Composable
fun LiveRoomScreen(
    onBackClick: () -> Unit = {}
) {
    // The Box is our Z-axis container. The first item is at the bottom.
    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 0 & Layer 1: Grid Background and Canvas Content
        WorkspaceGridBackground {
            // NOTE: Next, we will build the Draggable Sticky Notes and Canvas Engine here.
            // For now, it is an empty dotted board.
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
