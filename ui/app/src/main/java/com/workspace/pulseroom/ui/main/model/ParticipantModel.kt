package com.workspace.pulseroom.ui.main.model

data class ParticipantModel(
    val id: String,
    val name: String,
    val role: String, // Admin or Member
    val isMuted: Boolean = false
)