package com.workspace.pulseroom.ui.main.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.workspace.pulseroom.ui.main.model.RoomEvent
import com.workspace.pulseroom.ui.main.model.RoomInfo
import com.workspace.pulseroom.ui.main.model.StickyNoteModel
import com.workspace.pulseroom.ui.main.network.NetworkConfig
import com.workspace.pulseroom.ui.main.network.WorkspaceWebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

class RoomsViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Dynamic observable Room ID for UI header
    var currentRoomId by mutableStateOf("room_3")
        private set

    // Current client ID (jo humne har event me use kiya hai)
    val currentClientId: String = "android_user_01"

    // Active room tracker
    var activeRoomId: String
        get() = currentRoomId
        set(value) {
            currentRoomId = value
        }

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
    // API call karne ka function
    fun createRoomOnServer(
        roomName: String,
        maxMembers: Int = 6,
        password: String = "",
        onError: ((String) -> Unit)? = null,
        onSuccess: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonMap = mapOf(
                    "roomName" to roomName,
                    "maxMembers" to maxMembers,
                    "password" to password,
                    "adminId" to currentClientId
                )

                val jsonString = Gson().toJson(jsonMap)
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())

                // Dynamic URL: Uses 10.0.2.2 for Android Emulator, 10.0.3.2 for Genymotion, or LAN IP for physical device
                val url = NetworkConfig.createRoomUrl
                println("ViewModel: Creating room at URL: $url")

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val responseMap = Gson().fromJson(responseBody, Map::class.java)
                    val newRoomId = responseMap["roomId"]?.toString() ?: ""

                    if (newRoomId.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            activeRoomId = newRoomId
                            joinRoom(newRoomId)
                            onSuccess(newRoomId) // UI ko batao ki room ban gaya aur navigate karo
                        }
                    } else {
                        val errMsg = "Server returned empty roomId"
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            errorMessage = errMsg
                            onError?.invoke(errMsg)
                        }
                    }
                } else {
                    val errMsg = "Server error: ${response.code}"
                    println("ViewModel: $errMsg")
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        errorMessage = errMsg
                        onError?.invoke(errMsg)
                    }
                }
            } catch (e: Exception) {
                val errMsg = "Network failure: ${e.localizedMessage ?: e.message}"
                println("ViewModel: $errMsg")
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = errMsg
                    onError?.invoke(errMsg)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("ViewModel Destroy ho raha hai, WebSocket disconnect kar rahe hain.")
        webSocketClient?.disconnect()
    }
}