package com.workspace.pulseroom.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WorkspaceGridBackground(
    modifier: Modifier = Modifier,
    dotColor: Color = Color(0xFFE5E0DA), // Subtle muted Earth tone for dots
    backgroundColor: Color = Color(0xFFFBF8F8), // Collaborative canvas bg
    spacing: Dp = 32.dp,
    dotRadius: Float = 3f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawBehind {
                val spacePx = spacing.toPx()
                val width = size.width
                val height = size.height

                // Calculate how many dots fit in the screen
                val columns = (width / spacePx).toInt()
                val rows = (height / spacePx).toInt()

                // Draw the grid
                for (x in 0..columns) {
                    for (y in 0..rows) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(x * spacePx, y * spacePx)
                        )
                    }
                }
            }
    ) {
        content() // The rest of the layers will be injected here
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkspaceGridBackgroundPreview() {
    WorkspaceGridBackground {
    }
}