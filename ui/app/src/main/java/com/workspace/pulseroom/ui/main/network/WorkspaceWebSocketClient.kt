package com.workspace.pulseroom.ui.main.network

import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.workspace.pulseroom.ui.main.model.RoomEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WorkspaceWebSocketClient(
    private val onEventReceived:(RoomEvent)-> Unit  // Callback to send data to ViewModel
){
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    // OkHttpClient with timeouts for persistent connections
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
    }

    fun connect(roomCode: String){
        // Emulator uses 10.0.2.2. Physical phone on LAN uses host IP 192.168.1.17
        val host = if (isEmulator()) "10.0.2.2" else "192.168.1.17"
        val wsUrl = "ws://$host:8080/ws"
        Log.d("WebSocket", "Connecting to $wsUrl for room: $roomCode")

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener(){
            override fun onOpen(webSocket: WebSocket, response: Response){
                this@WorkspaceWebSocketClient.webSocket = webSocket
                Log.d("WebSocket", "Connection Opened Successfully!")

                // Connection open hote hi hum apna pehla JOIN message bhejenge
                val joinEvent = RoomEvent(
                    eventId = java.util.UUID.randomUUID().toString(),
                    type = "USER_JOINED",
                    roomId = roomCode,
                    clientId = "android_user_01"
                )
                sendMessage(joinEvent)
            }

            override fun onMessage(webSocket: WebSocket, text: String){
                Log.d("WebSocket", "Message Received: $text")
                try{
                    // String JSON ko Kotlin Object mein convert karo
                    val event = gson.fromJson(text, RoomEvent::class.java)
                    // ViewModel ko event pass kar do
                    onEventReceived(event)
                }catch (e: Exception){
                    Log.e("WebSocket", "JSON Parsing Error: ${e.message}")
                }
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection Failed: ${t.message}")
            }
        })
    }

    fun sendMessage(event: RoomEvent) {
        val jsonString = gson.toJson(event)
        webSocket?.send(jsonString)
        Log.d("WebSocket", "Message Sent: $jsonString")
    }

    fun disconnect() {
        webSocket?.close(1000, "User left screen")
        webSocket = null
    }
}