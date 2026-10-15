package com.workspace.pulseroom.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workspace.pulseroom.theme.PulseRoomTheme

@Composable
fun DashboardScreen(
    onJoinRoom: (String) -> Unit = {}, // Jab user join dabaye toh yeh trigger hoga
    onCreateRoom: () -> Unit = {}      // Naya room banane ke liye
) {
    var roomCode by remember { mutableStateOf("") }
    // Emberly design colors
    val ember500 = Color(0xFFFF5722)
    val textEspresso = Color(0xFF101917)
    val bgWhite = Color(0xFFFBF8F8)

    Surface(modifier = Modifier.fillMaxSize(), color = bgWhite) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PulseRoom",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textEspresso
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Join a workspace to collaborate", color = Color.Gray)
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = roomCode,
                onValueChange = { roomCode = it.uppercase() }, // Code hamesha capital mein
                label = { Text("Enter Room Code (e.g. XY9A)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (roomCode.isNotBlank()) onJoinRoom(roomCode)
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = textEspresso)
            ) {
                Text("Join Room", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "OR", color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // 3. Create Room Button
            Button(
                onClick = onCreateRoom,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ember500)
            ) {
                Text("+ Start a new room", fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    PulseRoomTheme {
        DashboardScreen()
    }
}
