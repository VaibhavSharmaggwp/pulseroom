package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
fun RoomCard(
    roomName: String,
    hostName: String,
    participantCount: Int,
    onClick: () -> Unit
){
    // Spec constraints: radius 16, 1px border (#FBEAE1), pad 16, gap 12[cite: 15]
    Surface (
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFFBEAE1)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ){
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 1. Top Section: Room Title and Live Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = roomName,
                    color = Color(0xFF101917), // Espresso[cite: 14]
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f) // Teacher Note: Yeh line title ko saari bachi hui jagah lene deti hai, jisse badge ekdum right side push ho jata hai.
                )
                LiveBadge(count = participantCount)
            }
            Spacer(modifier = Modifier.height(12.dp)) // 12px gap between title and meta info[cite: 15]

            // 2. Meta Information
            Text(
                text = "Hosted by $hostName",
                color = Color(0xFF78716C),
                fontSize = 14.sp
            )

        }
    }
}

@Preview
@Composable
private fun RoomCardPreview() {
    RoomCard(
        roomName = "Q4 launch war room", //[cite: 16]
        hostName = "Priya R.", //[cite: 16]
        participantCount = 24, //[cite: 16]
        onClick = {}
    )
}