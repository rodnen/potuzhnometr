/*
package com.example.potuzhnometr.media

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.annotation.RequiresApi
import com.example.potuzhnometr.AppData
import com.example.potuzhnometr.AppViewModel
import com.example.potuzhnometr.MainActivity
import com.example.potuzhnometr.R
import com.example.potuzhnometr.counter.CounterManager
import com.example.potuzhnometr.ui.UIAnimator
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

class MediaHandler(
    private val activity: Activity,
    private val viewModel: AppViewModel,
    private val powerText: TextView,
    private val videoContainer: FrameLayout,
    private val uiAnimator: UIAnimator,
    private val counterManager: CounterManager
) {

    fun playExplosionSound() {
        val soundRes = MainActivity.explodeSounds.random()
        val explosionPlayer = MediaPlayer.create(activity, soundRes)

        AppData.explosionPlayer = explosionPlayer

        explosionPlayer.apply {
            setOnCompletionListener {
                AppData.stopExplosionSound()
                viewModel.isSoundPlaying = false
            }
            start()
            viewModel.isSoundPlaying = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun playAlertAndExplode() {
        if (viewModel.isExplosionInProgress) return

        viewModel.isExplosionInProgress = true
        viewModel.isSoundPlaying = true

        powerText.setTextColor("#EE4848".toColorInt())

        uiAnimator.createColorAnimator().start()
        viewModel.isColorCleared = false

        val alertRes = MainActivity.alertSounds.random()

        if (viewModel.playSirenSound) {
            viewModel.sirenPlayer?.release()
            viewModel.sirenPlayer = MediaPlayer.create(activity, MainActivity.sirenSound).apply {
                isLooping = true
                setVolume(0.3f, 0.3f)
                start()
            }
        }

        if (viewModel.playAlertSound) {
            viewModel.alertPlayer?.release()
            viewModel.alertPlayer = MediaPlayer.create(activity, alertRes).apply {
                isLooping = false
                start()
                setOnCompletionListener {
                    release()
                    viewModel.handler.postDelayed({
                        if (!viewModel.isPressed) cancelExplosionSequence()
                        else executeExplosion()
                    }, 2000)
                }
            }
        } else {
            viewModel.handler.postDelayed({
                if (!viewModel.isPressed) cancelExplosionSequence()
                else executeExplosion()
            }, 2000)
        }
    }

    private fun cancelExplosionSequence() {
        viewModel.isExplosionInProgress = false
        viewModel.isSoundPlaying = false
        viewModel.sirenPlayer?.release()
        viewModel.sirenPlayer = null
        counterManager.post(counterManager.decrement)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun executeExplosion() {
        if (!viewModel.isExplosionInProgress) return

        viewModel.sirenPlayer?.release()
        viewModel.sirenPlayer = null

        if (viewModel.playPotuzhnometrExplosion) {
            startVideoPlayback()
        } else {
            viewModel.isExploded = true
            counterManager.post(counterManager.decrement)
            resetPotuzhnometer()
            videoContainer.removeAllViews()
            videoContainer.visibility = View.GONE
        }

        viewModel.isExplosionInProgress = false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startVideoPlayback() {
        val videoUri = "android.resource://${activity.packageName}/${R.raw.explosion}".toUri()

        val videoView = VideoView(activity).apply {
            setVideoURI(videoUri)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setZOrderOnTop(true)
            setBackgroundColor(Color.BLACK)
        }

        videoContainer.addView(videoView)
        videoView.start()

        videoView.setOnCompletionListener {
            viewModel.isExploded = true
            counterManager.post(counterManager.decrement)
            resetPotuzhnometer()

            videoContainer.removeAllViews()
            videoContainer.visibility = View.GONE
        }
    }

    private fun resetPotuzhnometer() {
        viewModel.potuzhno = false
        viewModel.isPressed = false
        viewModel.targetCounter = 0
        */
/* viewModel.potuzhno = false
        viewModel.isPressed = false
        alreadyClicked = false
        viewModel.targetCounter = 0
        seekBar.isEnabled = true*//*

    }
}
*/
