package com.workspace.pulseroom.ui.main.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workspace.pulseroom.theme.PlusJakartaSans
import com.workspace.pulseroom.theme.PulseRoomTheme
import com.workspace.pulseroom.ui.main.components.WorkspaceGridBackground

@Composable
fun DashboardScreen(
    onJoinRoom: (String) -> Unit = {},
    onCreateRoom: () -> Unit = {}
) {
    var roomCode by remember { mutableStateOf("") }

    // PulseRoom Design Tokens
    val ember500 = Color(0xFFFF5722)
    val textEspresso = Color(0xFF101917)
    val textMuted = Color(0xFF78716C)
    val borderSand = Color(0xFFF0EAE3)
    val cardBackground = Color(0xFFFFFFFF)
    val inputBackground = Color(0xFFFDFBF9)

    WorkspaceGridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BRAND HERO SECTION
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                // Live Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFD1FAE5), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE WORKSPACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857),
                        letterSpacing = 1.sp,
                        fontFamily = PlusJakartaSans
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PulseRoom",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textEspresso,
                    letterSpacing = (-1).sp,
                    fontFamily = PlusJakartaSans
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Collaborative real-time whiteboards & interactive sticky notes for agile teams.",
                    fontSize = 14.sp,
                    color = textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontFamily = PlusJakartaSans
                )
            }

            // CENTER ACTIONS CARD
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardBackground,
                border = BorderStroke(1.dp, borderSand),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Section 1 Header: Join Room
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF3ED),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = "Room",
                                    tint = ember500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Join Workspace",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textEspresso,
                                fontFamily = PlusJakartaSans
                            )
                            Text(
                                text = "Enter code shared by your teammate",
                                fontSize = 12.sp,
                                color = textMuted,
                                fontFamily = PlusJakartaSans
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Room Code TextField
                    OutlinedTextField(
                        value = roomCode,
                        onValueChange = { input ->
                            // Auto uppercase and limit to 8 characters max
                            if (input.length <= 8) {
                                roomCode = input.uppercase().trim()
                            }
                        },
                        placeholder = {
                            Text(
                                text = "e.g. D0429C",
                                color = Color(0xFFA8A29E),
                                fontSize = 16.sp,
                                fontFamily = PlusJakartaSans
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = roomCode.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(onClick = { roomCode = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textEspresso,
                            letterSpacing = if (roomCode.isNotEmpty()) 2.sp else 0.sp
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBackground,
                            unfocusedContainerColor = inputBackground,
                            focusedBorderColor = ember500,
                            unfocusedBorderColor = borderSand,
                            cursorColor = ember500
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (roomCode.isNotBlank()) onJoinRoom(roomCode)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Join Button
                    Button(
                        onClick = {
                            if (roomCode.isNotBlank()) onJoinRoom(roomCode)
                        },
                        enabled = roomCode.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = textEspresso,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE7E2DC),
                            disabledContentColor = Color(0xFFA8A29E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Enter Room",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = PlusJakartaSans
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Clean Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = borderSand
                        )
                        Text(
                            text = "OR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA8A29E),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 14.dp),
                            fontFamily = PlusJakartaSans
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = borderSand
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section 2: Create New Room Button (Vibrant Ember Gradient)
                    Button(
                        onClick = onCreateRoom,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = Color(0x33FF5722),
                                spotColor = Color(0x44FF5722)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF7A45), ember500)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start a New Room",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = PlusJakartaSans
                                )
                            }
                        }
                    }
                }
            }

            // FOOTER BRAND LABEL
            Text(
                text = "⚡ Real-time canvas powered by WebSocket & Redis",
                fontSize = 12.sp,
                color = Color(0xFFA8A29E),
                fontFamily = PlusJakartaSans,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
