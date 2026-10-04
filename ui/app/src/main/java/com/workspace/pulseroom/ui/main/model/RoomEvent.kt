package com.workspace.pulseroom.ui.main.model

// Yeh same envelope hai jo humne PRD ke hisaab se backend par design kiya tha
data class RoomEvent(
    val eventId: String,
    val type: String,
    val roomId: String,
    val clientId: String,
    val serverSeq: Long? = null,
    val clientSeq: Long? = null,
    val timestamp: Long? = null,
    // Any is used because payload can be text, points, or metadata
    val payload: Map<String, Any>? = null
)