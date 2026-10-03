package com.workspace.pulseroom.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import com.workspace.pulseroom.ui.main.model.RoomInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class RoomsViewModel: ViewModel(){
    // MutableStateFlow (Private): Sirf ViewModel isme data change kar sakta hai
    private val _rooms = MutableStateFlow<List<RoomInfo>>(emptyList())

    // StateFlow (Public): UI sirf ise 'read' ya observe kar sakti hai, modify nahi
    val rooms: StateFlow<List<RoomInfo>> = _rooms.asStateFlow()

    init {
        // V1 Test: Abhi hum hardcoded data daal rahe hain UI test karne ke liye.
        // Aage chal kar yahan OkHttp WebSocket ka data aayega!
        _rooms.value = listOf(
            RoomInfo("room_1", "Q4 launch war room", "Priya R.", 24),
            RoomInfo("room_2", "Onboarding crit", "Marco A.", 8),
            RoomInfo("room_3", "Pipeline sync", "Dev K.", 3)
        )
    }
}