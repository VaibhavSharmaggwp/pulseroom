package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Floating pill-shaped action bar anchored at the bottom of the collaborative room screen.
 * Contains audio mute toggle, raise hand action, and primary add note CTA.
 *
 * @param onAddNoteClick Callback invoked when user clicks the "Add note" button.
 * @param onRaiseHandClick Callback invoked when user clicks the "Raise hand" button.
 * @param modifier Optional external modifier for positioning or styling.
 */
@Composable
fun BottomActionBar(
    onAddNoteClick: () -> Unit,
    onRaiseHandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Design Spec Colors
    val ember500 = Color(0xFFFF5722)
    val canary50 = Color(0xFFFFF406).copy(alpha = 0.2f) // Light yellow tint for Raise Hand
    val textEspresso = Color(0xFF101917)
    val bgWhite = Color.White

    // Floating white pill container holding action buttons
    Surface(
        color = bgWhite,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 8.dp,
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Mute Icon
            IconButton(onClick = { /* Mute logic */ }) {
                Icon(
                    imageVector = Icons.Outlined.MicOff,
                    contentDescription = "Mute",
                    tint = textEspresso
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Raise Hand Button
            Button(
                onClick = onRaiseHandClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = canary50,
                    contentColor = textEspresso
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = "Raise hand icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Raise hand",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Add Note Button (Primary action)
            Button(
                onClick = onAddNoteClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ember500,
                    contentColor = Color.White
                ),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add note icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add note",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
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