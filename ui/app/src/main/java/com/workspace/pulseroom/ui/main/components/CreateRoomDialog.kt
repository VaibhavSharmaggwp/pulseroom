package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.workspace.pulseroom.theme.PulseRoomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreateClick: (roomName: String, maxMembers: Int, password: String) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var maxMembers by remember { mutableFloatStateOf(6f) } // Default 6 members like Among Us

    val ember500 = Color(0xFFFF5722)
    val textEspresso = Color(0xFF101917)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Room Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textEspresso)
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Room Name
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Workspace Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 2. Optional Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Admin Password (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Max Members Slider
                Text(text = "Max Members: ${maxMembers.toInt()}", color = textEspresso, fontWeight = FontWeight.Medium)
                Slider(
                    value = maxMembers,
                    onValueChange = { maxMembers = it },
                    valueRange = 2f..10f,
                    steps = 7, // 2 to 10
                    colors = SliderDefaults.colors(
                        thumbColor = ember500,
                        activeTrackColor = ember500
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (roomName.isNotBlank()) {
                                onCreateClick(roomName, maxMembers.toInt(), password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ember500)
                    ) {
                        Text("Create & Join")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateRoomDialogPreview() {
    PulseRoomTheme {
        CreateRoomDialog(
            onDismiss = {},
            onCreateClick = { _, _, _ -> }
        )
    }
}
