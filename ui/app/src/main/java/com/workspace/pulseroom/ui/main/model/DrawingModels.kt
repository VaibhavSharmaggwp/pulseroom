package com.workspace.pulseroom.ui.main.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

// Ek stroke (line) ko represent karta hai
data class Stroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

data class StickyNoteModel(
    val id: String,
    val title: String,
    val body: String,
    val author: String,
    val position: Offset,
    val color: Color
)