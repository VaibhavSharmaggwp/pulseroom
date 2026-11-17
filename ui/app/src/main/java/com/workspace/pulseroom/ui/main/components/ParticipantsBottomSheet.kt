package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.model.ParticipantModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsBottomSheet(
    participants: List<ParticipantModel>,
    currentUserIsAdmin: Boolean,
    onDismiss: () -> Unit,
    onMuteClick: (String) -> Unit,
    onKickClick: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        ParticipantsContent(
            participants = participants,
            currentUserIsAdmin = currentUserIsAdmin,
            onMuteClick = onMuteClick,
            onKickClick = onKickClick
        )
    }
}

@Composable
fun ParticipantsContent(
    participants: List<ParticipantModel>,
    currentUserIsAdmin: Boolean,
    onMuteClick: (String) -> Unit = {},
    onKickClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "In this room (${participants.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101917)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.padding(bottom = 32.dp)) {
            items(participants) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "User", tint = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name & Role
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = user.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(
                            text = user.role,
                            color = if (user.role == "Admin") Color(0xFFFF5722) else Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    // Admin Actions (Kick & Mute) - Sirf tab dikhenge jab current user Admin ho
                    // aur target user Admin na ho
                    if (currentUserIsAdmin && user.role != "Admin") {
                        IconButton(onClick = { onMuteClick(user.id) }) {
                            Icon(Icons.Default.MicOff, contentDescription = "Mute", tint = Color.Gray)
                        }
                        IconButton(onClick = { onKickClick(user.id) }) {
                            Icon(Icons.Default.Block, contentDescription = "Kick", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantsBottomSheetAdminPreview() {
    PulseRoomTheme {
        Surface(color = Color.White) {
            ParticipantsContent(
                participants = listOf(
                    ParticipantModel(id = "1", name = "Priya Sharma", role = "Admin"),
                    ParticipantModel(id = "2", name = "Rahul Verma", role = "Member"),
                    ParticipantModel(id = "3", name = "Ananya Iyer", role = "Member", isMuted = true)
                ),
                currentUserIsAdmin = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantsBottomSheetMemberPreview() {
    PulseRoomTheme {
        Surface(color = Color.White) {
            ParticipantsContent(
                participants = listOf(
                    ParticipantModel(id = "1", name = "Priya Sharma", role = "Admin"),
                    ParticipantModel(id = "2", name = "Rahul Verma", role = "Member")
                ),
                currentUserIsAdmin = false
            )
        }
    }
}
