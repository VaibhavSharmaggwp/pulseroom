package com.workspace.pulseroom.ui.main.model
// Yeh hamara Kotlin object hai jo backend se aane wale JSON ko represent karega
data class RoomInfo(
    val id: String,
    val name: String,
    val host: String,
    val participants: Int
)
