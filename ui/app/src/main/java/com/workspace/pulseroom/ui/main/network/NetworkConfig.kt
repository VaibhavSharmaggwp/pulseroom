package com.workspace.pulseroom.ui.main.network

import android.os.Build
import android.util.Log

object NetworkConfig {
    const val PORT = 8080

    // Fallback LAN IP for physical device testing on local Wi-Fi
    var physicalDeviceHost: String = "192.168.1.17"

    fun isGenymotion(): Boolean {
        return Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
    }

    /**
     * Detects if the app is executing inside an Android emulator.
     * Android emulators cannot reach the host machine via 'localhost' or '127.0.0.1'.
     * Standard Google Android Emulator routes host machine loopback to 10.0.2.2.
     * Genymotion routes to 10.0.3.2.
     */
    fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("sdk_gphone", ignoreCase = true)
                || Build.HARDWARE.contains("goldfish", ignoreCase = true)
                || Build.HARDWARE.contains("ranchu", ignoreCase = true)
                || Build.MODEL.contains("google_sdk", ignoreCase = true)
                || Build.MODEL.contains("Emulator", ignoreCase = true)
                || Build.MODEL.contains("Android SDK", ignoreCase = true)
                || Build.MODEL.contains("sdk_gphone", ignoreCase = true)
                || Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)
                || Build.PRODUCT.contains("sdk_google", ignoreCase = true)
                || Build.PRODUCT.contains("google_sdk", ignoreCase = true)
                || Build.PRODUCT.contains("sdk_gphone", ignoreCase = true)
                || Build.PRODUCT.contains("vbox86p", ignoreCase = true)
                || Build.PRODUCT.contains("emulator", ignoreCase = true)
                || Build.PRODUCT.contains("simulator", ignoreCase = true)
                || Build.BOARD.contains("goldfish", ignoreCase = true)
                || Build.BOARD.contains("ranchu", ignoreCase = true)
    }

    val serverHost: String
        get() = when {
            isGenymotion() -> "10.0.3.2"
            isEmulator() -> "10.0.2.2"
            else -> physicalDeviceHost
        }

    val httpBaseUrl: String
        get() = "http://$serverHost:$PORT"

    val createRoomUrl: String
        get() = "$httpBaseUrl/api/rooms/create"

    val wsUrl: String
        get() = "ws://$serverHost:$PORT/ws"
}
