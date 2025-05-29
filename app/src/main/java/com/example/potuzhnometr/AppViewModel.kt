package com.example.potuzhnometr

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel

enum class ModeType (val value: Int) {
    SINGLE_TAP(0),
    HOLD(1),
    RANDOM(2),
    MANUAL(3);

    companion object {
        fun fromInt(value: Int): ModeType? = entries.find { it.value == value }
    }
}

enum class SensType (val value: Int) {
    LOW (0),
    MEDIUM (1),
    HIGH (2);

    companion object {
        fun fromInt(value: Int): SensType? = entries.find { it.value == value }
    }
}

class AppViewModel : ViewModel() {
    // State variables
    var counter = 0
    var targetCounter = 0
    var lastChangedColor = Color.TRANSPARENT

    // Mode and sensitivity
    var sensType = SensType.LOW
    var modeType = ModeType.SINGLE_TAP

    // Boolean flags
    var isSoundPlaying = false
    var isPressed = false
    var isExploded = false
    var isColorCleared = false
    var isExplosionInProgress = false
    var potuzhno = false

    // Settings
    var isDarkTheme = false
    var playAlertSound = true
    var playSirenSound = true
    var playPotuzhnometrExplosion = true

    // Media players
    var alertPlayer: MediaPlayer? = null
    var sirenPlayer: MediaPlayer? = null

    // Handler for all background operations
    val handler = Handler(Looper.getMainLooper())

    // Media player management
    fun releaseMediaPlayers() {
        alertPlayer?.release()
        sirenPlayer?.release()
        alertPlayer = null
        sirenPlayer = null
    }

    fun resetState() {
        isPressed = false
        isExploded = false
        isExplosionInProgress = false
        potuzhno = false
        isSoundPlaying = false
    }
}