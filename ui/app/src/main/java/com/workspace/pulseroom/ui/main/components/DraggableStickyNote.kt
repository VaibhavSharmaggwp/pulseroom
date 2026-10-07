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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlin.math.roundToInt

/**
 * DraggableStickyNote:
 * A standalone sticky note card that can be dragged smoothly around the screen/canvas.
 *
 * @param title Header title of the sticky note.
 * @param body Main descriptive text or idea content.
 * @param author The contributor/user who created the note.
 * @param initialPosition The initial (X, Y) pixel coordinates on the canvas.
 * @param noteColor Background color representing category or theme of the note.
 * @param modifier Optional external Modifier for customization.
 */
@Composable
fun DraggableStickyNote(
    title: String,
    body: String,
    author: String,
    initialPosition: Offset,
    noteColor: Color,
    modifier: Modifier = Modifier
) {
    // 1. Position State: Remembers the current (X, Y) pixel coordinates across recompositions
    var currentPosition by remember { mutableStateOf(initialPosition) }

    // Spec color for readable typography (Warm Espresso)
    val textEspresso = Color(0xFF101917)

    Column(
        modifier = modifier
            // 2. Location Offset: Dynamically shifts the view based on currentPosition state
            .offset {
                IntOffset(
                    x = currentPosition.x.roundToInt(),
                    y = currentPosition.y.roundToInt()
                )
            }
            // 3. Pointer & Drag Gesture: Tracks user touch/drag gestures
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    // Consume touch change event to prevent background scroll interference
                    change.consume()

                    // Accumulate drag delta onto the current position
                    currentPosition = Offset(
                        x = currentPosition.x + dragAmount.x,
                        y = currentPosition.y + dragAmount.y
                    )
                }
            }
            // 4. Styling & Elevation: Drop shadow and rounded background
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
            .background(color = noteColor, shape = RoundedCornerShape(12.dp))
            .width(180.dp) // Fixed width for consistent sticky note layout
            .padding(16.dp)
    ) {
        // Note Title
        Text(
            text = title,
            color = textEspresso,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Note Body Description
        Text(
            text = body,
            color = textEspresso.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Author attribution tag at bottom
        Text(
            text = "- $author",
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
        title = "Sprint Goal",
        body = "Deliver real-time collaborative canvas with monotonic sequence validation.",
        author = "Priya R.",
        initialPosition = Offset(40f, 40f),
        noteColor = Color(0xFFFEF3C7) // Pastel yellow sticky note
    )
}