package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

/**
 * BottomActionBar:
 * Screen ke bottom par floating pill action bar.
 * Isme Mute button, Raise hand button aur Add note button perfectly horizontal aligned rehte hain.
 *
 * @param onAddNoteClick Jab user "Add note" par click kare.
 * @param onRaiseHandClick Jab user "Raise hand" par click kare.
 * @param modifier Optional external modifier.
 */
@Composable
fun BottomActionBar(
    onAddNoteClick: () -> Unit,
    onRaiseHandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Design Spec Colors
    val ember500 = Color(0xFFFF5722) // Primary Orange Add Note
    val canary50 = Color(0xFFFFF406).copy(alpha = 0.25f) // Subtle yellow tint for Raise Hand
    val textEspresso = Color(0xFF101917)
    val bgWhite = Color.White

    // Compact floating pill Surface - wrapContentSize ke sath
    Surface(
        color = bgWhite,
        shape = CircleShape,
        shadowElevation = 10.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Mute Icon Button
            IconButton(
                onClick = { /* Mute logic */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MicOff,
                    contentDescription = "Mute",
                    tint = textEspresso,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Raise Hand Button (Same height, CircleShape aur single line text)
            Button(
                onClick = onRaiseHandClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = canary50,
                    contentColor = textEspresso
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = "Raise hand icon",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Raise hand",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // 3. Add Note Button (Primary CTA - Same height, CircleShape aur single line text)
            Button(
                onClick = onAddNoteClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ember500,
                    contentColor = Color.White
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add note icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add note",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun BottomActionBarPreview() {
    BottomActionBar(
        onAddNoteClick = {},
        onRaiseHandClick = {}
    )
}