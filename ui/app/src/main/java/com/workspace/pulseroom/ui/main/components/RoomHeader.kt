package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoomHeader(
    roomTitle: String,
    participantCount: Int,
    timerText: String,
    onBackClick: () -> Unit
) {
    val textEspresso = Color(0xFF101917) // Headings & body
    val textMuted = Color(0xFF78716C) // Meta text

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // We use a slight transparent white gradient in production, but solid white for now
            .background(Color.White.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textEspresso)
        }

        // Title and Live Status
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = roomTitle,
                color = textEspresso,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                LiveBadge(count = participantCount) // Reusing our component from earlier
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = timerText,
                    color = textMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Icons
        IconButton(onClick = { /* Handle Share */ }) {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", tint = textEspresso)
        }
        IconButton(onClick = { /* Handle More */ }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = textEspresso)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoomHeaderPreview() {
    RoomHeader(
        roomTitle = "Q4 launch war room",
        participantCount = 24,
        timerText = "38:12",
        onBackClick = {}
    )
}
