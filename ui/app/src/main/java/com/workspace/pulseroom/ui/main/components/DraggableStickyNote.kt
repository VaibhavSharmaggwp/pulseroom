package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workspace.pulseroom.ui.main.model.StickyNoteModel
import kotlin.math.roundToInt

/**
 * DraggableStickyNote:
 * Screen par ek single sticky note render karta hai jise pure canvas par freely drag kiya ja sakta hai.
 *
 * @param note Model jisme title, body, author, color aur current position hoti hai.
 * @param modifier Optional external layout modifier.
 * @param onPositionChanged Callback jab user note drag karke uski position badalta hai.
 */
@Composable
fun DraggableStickyNote(
    note: StickyNoteModel,
    modifier: Modifier = Modifier,
    onPositionChanged: (Offset) -> Unit = {}
) {
    val textEspresso = Color(0xFF101917)

    // Dragging ko buttery-smooth aur stutter-free rakhne ke liye local offset state
    var currentOffset by remember(note.id) { mutableStateOf(note.position) }

    // Agar external update aaye (jaise WebSocket se naye coordinates) toh local offset sync ho jaye
    LaunchedEffect(note.position) {
        currentOffset = note.position
    }

    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)

    Column(
        modifier = modifier
            // 1. POSITIONING: Note ko uske current X, Y pixel coordinates par shift karta hai
            .offset { IntOffset(currentOffset.x.roundToInt(), currentOffset.y.roundToInt()) }

            // 2. SMOOTH DRAG GESTURE: Canvas par note ko freely drag karne ke liye
            .pointerInput(note.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newPosition = Offset(
                        x = currentOffset.x + dragAmount.x,
                        y = currentOffset.y + dragAmount.y
                    )
                    currentOffset = newPosition
                    currentOnPositionChanged(newPosition)
                }
            }
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
            .background(color = note.color, shape = RoundedCornerShape(12.dp))
            .width(180.dp)
            .padding(16.dp)
    ) {
        if (note.title.isNotEmpty()) {
            Text(
                text = note.title,
                color = textEspresso,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = note.body,
            color = textEspresso.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "- ${note.author}",
            color = textEspresso.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DraggableStickyNotePreview() {
    DraggableStickyNote(
        note = StickyNoteModel(
            id = "preview_1",
            title = "Sprint Goal",
            body = "Deliver real-time collaborative canvas with monotonic sequence validation.",
            author = "Priya R.",
            position = Offset(40f, 40f),
            color = Color(0xFFFEF3C7)
        )
    )
}