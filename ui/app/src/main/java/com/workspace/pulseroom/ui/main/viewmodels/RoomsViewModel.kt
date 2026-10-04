package com.workspace.pulseroom.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import com.workspace.pulseroom.ui.main.model.RoomEvent
import com.workspace.pulseroom.ui.main.model.RoomInfo
import com.workspace.pulseroom.ui.main.network.WorkspaceWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


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

    // VERY IMPORTANT: Memory Management
    // Jab ViewModel actually mar raha ho (user exits app), toh network pipe band karo
    override fun onCleared() {
        super.onCleared()
        println("ViewModel Destroy ho raha hai, WebSocket disconnect kar rahe hain.")
        webSocketClient?.disconnect()
    }
}