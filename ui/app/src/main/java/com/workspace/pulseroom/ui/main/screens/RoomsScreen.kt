package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.components.RoomCard
import com.workspace.pulseroom.ui.main.model.RoomInfo
import com.workspace.pulseroom.ui.main.viewmodels.RoomsViewModel

@Composable
fun RoomsScreen(
    viewModel: RoomsViewModel = viewModel() // Compose automatically ViewModel inject kar dega
) {
    val roomList by viewModel.rooms.collectAsState()

    RoomsScreenContent(
        roomList = roomList,
        onRoomClick = { roomId ->
            println("User wants to join room: $roomId")
        }
    )
}

@Composable
fun RoomsScreenContent(
    roomList: List<RoomInfo>,
    onRoomClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F2)) // App Background Cream[cite: 14]
    ) {
        // Screen Header
        Text(
            text = "Your Rooms",
            color = Color(0xFF101917), // Espresso[cite: 14]
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 16.dp)
        )

        // The Recycler
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Har card ke beech mein 12dp ka gap[cite: 15]
        ) {
            items(
                items = roomList,
                key = { room -> room.id }
            ) { room ->
                RoomCard(
                    roomName = room.name,
                    hostName = room.host,
                    participantCount = room.participants,
                    onClick = { onRoomClick(room.id) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoomsScreenPreview() {
    PulseRoomTheme {
        RoomsScreenContent(
            roomList = listOf(
                RoomInfo("room_1", "Q4 launch war room", "Priya R.", 24),
                RoomInfo("room_2", "Onboarding crit", "Marco A.", 8),
                RoomInfo("room_3", "Pipeline sync", "Dev K.", 3)
            )
        )
    }
}