package com.example.potuzhnometr

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer

@SuppressLint("StaticFieldLeak")
object AppData {
    public var alertPlayer: MediaPlayer? = null
    public var sirenPlayer: MediaPlayer? = null
    public var explosionPlayer: MediaPlayer? = null

    private const val PREF_NAME = "app_settings"
    private lateinit var context: Context

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    fun resetSettings() {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

    }

    fun stopAudio() {
        stopAlertSound()
        stopSirenSound()
        stopExplosionSound()
    }

    fun stopAlertSound() {
        alertPlayer?.stop()
        alertPlayer?.release()
        alertPlayer = null
    }

    fun stopSirenSound() {
        sirenPlayer?.stop()
        sirenPlayer?.release()
        sirenPlayer = null
    }

    fun stopExplosionSound() {
        explosionPlayer?.stop()
        explosionPlayer?.release()
        explosionPlayer = null
    }
}