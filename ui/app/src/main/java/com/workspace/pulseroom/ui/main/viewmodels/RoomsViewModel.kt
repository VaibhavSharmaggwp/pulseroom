package com.workspace.pulseroom.ui.main.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.workspace.pulseroom.ui.main.model.RoomEvent
import com.workspace.pulseroom.ui.main.model.RoomInfo
import com.workspace.pulseroom.ui.main.model.StickyNoteModel
import com.workspace.pulseroom.ui.main.network.WorkspaceWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class RoomsViewModel : ViewModel() {
    // Current client ID (jo humne har event me use kiya hai)
    val currentClientId: String = "android_user_01"

    // Active room tracker
    private var activeRoomId: String = "room_3"

    // MutableStateFlow (Private): Sirf ViewModel isme data change kar sakta hai
    private val _rooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    // StateFlow (Public): UI sirf ise 'read' ya observe kar sakti hai, modify nahi
    val rooms: StateFlow<List<RoomInfo>> = _rooms.asStateFlow()

    // 2. StateFlow for Notes: Backend se jo bhi notes aayenge ya user banayega wo isme rahenge
    private val _notes = MutableStateFlow<List<StickyNoteModel>>(emptyList())
    val notes: StateFlow<List<StickyNoteModel>> = _notes.asStateFlow()

    // 3. StateFlow: Live incoming WebSocket messages ke liye
    private val _lastRecievedEvent = MutableStateFlow<RoomEvent?>(null)
    val lastRecievedEvent: StateFlow<RoomEvent?> = _lastRecievedEvent.asStateFlow()

    // OKHTTP engine
    private var webSocketClient: WorkspaceWebSocketClient? = null

    init {
        // V1 Test: Hardcoded data UI test karne ke liye.
        _rooms.value = listOf(
            RoomInfo("D0429C", "Q4 launch war room", "Priya R.", 24),
            RoomInfo("room_2", "Onboarding crit", "Marco A.", 8),
            RoomInfo("room_3", "Pipeline sync", "Dev K.", 3)
        )
    }

    // Yeh function UI tab call karegi jab user room join karega
    fun joinRoom(roomCode: String) {
        if (webSocketClient != null && activeRoomId == roomCode) {
            println("ViewModel: Already connected to room $roomCode.")
            return
        }
        webSocketClient?.disconnect()
        _notes.value = emptyList()
        activeRoomId = roomCode
        println("ViewModel: Connecting to WebSocket for room $roomCode...")

        webSocketClient = WorkspaceWebSocketClient(
            onEventReceived = { incomingEvent ->
                // Yeh lambda network thread se fire hoga
                println("ViewModel: Naya event aaya -> ${incomingEvent.type}")

                // StateFlow automatically is event ko safely UI thread par push kar dega
                _lastRecievedEvent.value = incomingEvent

                // Backend se aane wale naye note ya update ko process karo
                handleIncomingNoteEvent(incomingEvent)
            }
        )
        webSocketClient?.connect(roomCode)
    }

    // Room se bahar aane par connection clean karo aur notes reset karo
    fun leaveRoom() {
        println("ViewModel: Leaving room $activeRoomId...")
        webSocketClient?.disconnect()
        webSocketClient = null
        _notes.value = emptyList()
    }

    /**
     * Backend se jab koi NOTE event aata hai (NOTE_ADDED, NOTE_DRAGGING, NOTE_DROPPED, NOTE_EDITED),
     * yeh method use parse karke _notes StateFlow ko update karega taaki Compose automatically screen draw kar sake.
     */
    private fun handleIncomingNoteEvent(event: RoomEvent) {
        val payload = event.payload ?: return

        when (event.type) {
            "NOTE_ADDED" -> {
                val incomingNote = parseNoteFromPayload(payload) ?: return
                val existing = _notes.value.any { it.id == incomingNote.id }
                if (!existing) {
                    println("ViewModel: Naya note add kiya list mein -> ${incomingNote.id}")
                    _notes.value = _notes.value + incomingNote
                } else {
                    _notes.value = _notes.value.map {
                        if (it.id == incomingNote.id) incomingNote else it
                    }
                }
            }

            "NOTE_DRAGGING" -> {
                // Agar dusre client ne drag kiya hai, tabhi position update karo (apne dragging echo se jitter avoid karo)
                if (event.clientId != currentClientId) {
                    val noteId = payload["id"] as? String ?: return
                    val posX = (payload["positionX"] as? Number)?.toFloat() ?: return
                    val posY = (payload["positionY"] as? Number)?.toFloat() ?: return
                    val newPos = Offset(posX, posY)

                    _notes.value = _notes.value.map {
                        if (it.id == noteId) it.copy(position = newPos) else it
                    }
                }
            }

            "NOTE_DROPPED" -> {
                val updatedNote = parseNoteFromPayload(payload) ?: return
                _notes.value = _notes.value.map {
                    if (it.id == updatedNote.id) updatedNote else it
                }
            }

            "NOTE_EDITED" -> {
                if (event.clientId != currentClientId) {
                    val updatedNote = parseNoteFromPayload(payload) ?: return
                    _notes.value = _notes.value.map {
                        if (it.id == updatedNote.id) updatedNote else it
                    }
                }
            }
        }
    }

    private fun parseHexColor(colorHex: String?, defaultColor: Color = Color(0xFFFFDAB9)): Color {
        if (colorHex.isNullOrBlank()) return defaultColor
        return try {
            val cleanHex = colorHex.removePrefix("#")
            val colorInt = when (cleanHex.length) {
                6 -> (0xFF000000 or cleanHex.toLong(16)).toInt()
                8 -> cleanHex.toLong(16).toInt()
                else -> return defaultColor
            }
            Color(colorInt)
        } catch (_: Exception) {
            defaultColor
        }
    }

    private fun parseNoteFromPayload(payload: Map<String, Any>): StickyNoteModel? {
        val id = payload["id"] as? String ?: return null
        val title = payload["title"] as? String ?: ""
        val body = payload["body"] as? String ?: ""
        val author = payload["author"] as? String ?: "Anonymous"
        val posX = (payload["positionX"] as? Number)?.toFloat() ?: 300f
        val posY = (payload["positionY"] as? Number)?.toFloat() ?: 400f
        val colorHex = payload["colorHex"] as? String
        val color = parseHexColor(colorHex)

        return StickyNoteModel(
            id = id,
            title = title,
            body = body,
            author = author,
            position = Offset(posX, posY),
            color = color
        )
    }

    // Local actions triggered from UI:
    fun addNote(note: StickyNoteModel, roomId: String = activeRoomId) {
        // Optimistic update: UI par turant dikhao
        if (_notes.value.none { it.id == note.id }) {
            _notes.value = _notes.value + note
        }
        sendNoteEvent("NOTE_ADDED", note, roomId)
    }

    fun updateNotePosition(noteId: String, newPos: Offset, isDragging: Boolean, roomId: String = activeRoomId) {
        val note = _notes.value.find { it.id == noteId } ?: return
        val updatedNote = note.copy(position = newPos)
        _notes.value = _notes.value.map { if (it.id == noteId) updatedNote else it }

        val eventType = if (isDragging) "NOTE_DRAGGING" else "NOTE_DROPPED"
        sendNoteEvent(eventType, updatedNote, roomId)
    }

    fun updateNoteText(noteId: String, newText: String, roomId: String = activeRoomId) {
        val note = _notes.value.find { it.id == noteId } ?: return
        val updatedNote = note.copy(body = newText)
        _notes.value = _notes.value.map { if (it.id == noteId) updatedNote else it }

        sendNoteEvent("NOTE_EDITED", updatedNote, roomId)
    }

    fun sendDrawingEvent(payload: Map<String, Any>) {
        val event = RoomEvent(
            eventId = UUID.randomUUID().toString(),
            type = "DRAW_POINTS",
            roomId = activeRoomId,
            clientId = currentClientId,
            payload = payload
        )
        webSocketClient?.sendMessage(event)
    }

    fun sendNoteEvent(eventType: String, note: StickyNoteModel, roomId: String = activeRoomId) {
        val payload = mapOf(
            "id" to note.id,
            "title" to note.title,
            "body" to note.body,
            "author" to note.author,
            "positionX" to note.position.x.toDouble(),
            "positionY" to note.position.y.toDouble(),
            "colorHex" to String.format("#%06X", (0xFFFFFF and note.color.toArgb()))
        )

        val event = RoomEvent(
            eventId = UUID.randomUUID().toString(),
            type = eventType,
            roomId = roomId,
            clientId = currentClientId,
            payload = payload
        )

        webSocketClient?.sendMessage(event)
    }

    override fun onCleared() {
        super.onCleared()
        println("ViewModel Destroy ho raha hai, WebSocket disconnect kar rahe hain.")
        webSocketClient?.disconnect()
    }
}