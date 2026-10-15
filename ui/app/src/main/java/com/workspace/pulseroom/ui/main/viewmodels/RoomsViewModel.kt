package com.workspace.pulseroom.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import com.workspace.pulseroom.ui.main.model.RoomEvent
import com.workspace.pulseroom.ui.main.model.RoomInfo
import com.workspace.pulseroom.ui.main.model.StickyNoteModel
import com.workspace.pulseroom.ui.main.network.WorkspaceWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import androidx.compose.ui.graphics.toArgb


class RoomsViewModel: ViewModel(){
    // MutableStateFlow (Private): Sirf ViewModel isme data change kar sakta hai
    private val _rooms = MutableStateFlow<List<RoomInfo>>(emptyList())

    // StateFlow (Public): UI sirf ise 'read' ya observe kar sakti hai, modify nahi
    val rooms: StateFlow<List<RoomInfo>> = _rooms.asStateFlow()

    // 2. Naya StateFlow: Live incoming WebSocket messages ke liye
    private val _lastRecievedEvent = MutableStateFlow<RoomEvent?>(null)
    val lastRecievedEvent: StateFlow<RoomEvent?> = _lastRecievedEvent.asStateFlow()

    // OKHTTP engine
    private var webSocketClient: WorkspaceWebSocketClient? = null

    init {
        // V1 Test: Abhi hum hardcoded data daal rahe hain UI test karne ke liye.
        // Aage chal kar yahan OkHttp WebSocket ka data aayega!
        _rooms.value = listOf(
            RoomInfo("D0429C", "Q4 launch war room", "Priya R.", 24),
            RoomInfo("room_2", "Onboarding crit", "Marco A.", 8),
            RoomInfo("room_3", "Pipeline sync", "Dev K.", 3)
        )
    }

    // Yeh function UI (RoomsScreen) tab call karegi jab user "Join" par click karega
    fun joinRoom(roomCode: String){
        println("ViewModel: Connecting to WebSocket for room $roomCode...")

        webSocketClient = WorkspaceWebSocketClient(
            onEventReceived = {incomingEvent->
                // Yeh lambda network thread se fire hoga
                println("ViewModel: Naya event aaya -> ${incomingEvent.type}")

                // StateFlow automatically is event ko safely UI thread par push kar dega
                _lastRecievedEvent.value = incomingEvent
            }
        )
        webSocketClient?.connect(roomCode)
    }

    // Agar WebSocket connected hai aur user canvas par kuch draw/type karna chahta hai
    fun sendDrawingEvent(payload: Map<String, Any>){
        val event = RoomEvent(
            eventId = java.util.UUID.randomUUID().toString(),
            type = "DRAW_POINTS",
            roomId = "D0429C", // Hardcoded for test
            clientId = "android_user_01",
            payload = payload
        )
        webSocketClient?.sendMessage(event)

    }

    fun sendNoteEvent(eventType: String, note: StickyNoteModel, roomId: String = "room_3") {
        // 1. Note ke data ko Map (JSON payload) mein convert karo
        val payload = mapOf(
            "id" to note.id,
            "title" to note.title,
            "body" to note.body,
            "author" to note.author,
            "positionX" to note.position.x.toDouble(), // Backend Double expect kar raha hai
            "positionY" to note.position.y.toDouble(),
            // Color object ko Hex String mein convert karna taaki database samajh sake
            "colorHex" to String.format("#%06X", (0xFFFFFF and note.color.toArgb()))
        )

        // 2. RoomEvent event class banao
        val event = RoomEvent(
            eventId = UUID.randomUUID().toString(),
            type = eventType, // Yahan "NOTE_DRAGGING", "NOTE_DROPPED", ya "NOTE_ADDED" aayega
            roomId = roomId,
            clientId = "android_user_01", // Humara hardcoded user
            payload = payload
        )

        // Websocket ke through event send karo
        webSocketClient?.sendMessage(event)

    }

    // VERY IMPORTANT: Memory Management
    // Jab ViewModel actually mar raha ho (user exits app), toh network pipe band karo
    override fun onCleared() {
        super.onCleared()
        println("ViewModel Destroy ho raha hai, WebSocket disconnect kar rahe hain.")
        webSocketClient?.disconnect()
    }
}