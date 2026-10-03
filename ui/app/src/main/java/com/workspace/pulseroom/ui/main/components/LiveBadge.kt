package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveBadge(count: Int){
    // Colors from spec: Mint 50 background, Emerald 700 text[cite: 14, 15]
    val badgeBg = Color(0xFFD1FAE5)
    val textEmerald = Color(0xFF047857)
    val liveDot = Color(0xFF10B981) // Emerald 500[cite: 14]

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = badgeBg, shape = CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ){
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = liveDot, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "LIVE - $count",
            color = textEmerald,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}