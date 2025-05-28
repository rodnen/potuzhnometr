package com.example.potuzhnometr.counter

import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.potuzhnometr.AppViewModel
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class CounterManager(
    private val viewModel: AppViewModel,
    private val powerText: TextView,
    private val updateUI: () -> Unit,
    private val resetAfterRelease: () -> Unit,
    private val playAlertAndExplode: () -> Unit,
    private val incrementSpeed: Array<Long>,
    private val decrementSpeed: Array<Long>,
    private val randomSpeed: Array<Long>
) {
    val increment = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun run() {
            if(viewModel.isPressed) {
                if (viewModel.counter in 0..99) {
                    viewModel.counter++
                    powerText.text = viewModel.counter.toString()
                    updateUI() // проблема
                    postDelayed(this, incrementSpeed[viewModel.sensType.value])

                } else {
                    if (!viewModel.potuzhno) {
                        viewModel.potuzhno = true
                        updateUI()
                        playAlertAndExplode()
                    }
                }
            }
            else{
                viewModel.handler.removeCallbacks(this)
            }

        }
    }

    val decrement = object : Runnable {
        override fun run() {
            if (viewModel.isPressed && viewModel.isExploded) {
                viewModel.isPressed = false
            }

            if (!viewModel.isPressed && viewModel.counter > 0 && !viewModel.isSoundPlaying) {
                viewModel.counter = if (viewModel.isExploded) 0 else viewModel.counter - 1

                resetAfterRelease()
                updateUI()
                viewModel.handler.postDelayed(this, decrementSpeed[viewModel.sensType.value])
            } else {
                viewModel.handler.removeCallbacks(this)
            }
        }
    }

    val random = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun run() {
            val step = Random.nextInt(2, 8)
            if (viewModel.counter != viewModel.targetCounter) {
                val diff = viewModel.targetCounter - viewModel.counter
                viewModel.counter += when {
                    diff > 0 -> min(step, diff)
                    diff < 0 -> max(-step, diff)
                    else -> 0
                }
                updateUI()
            } else {
                viewModel.targetCounter = Random.nextInt(0, 101)
            }

            if (viewModel.counter in 0..99) {
                viewModel.handler.postDelayed(this, randomSpeed[viewModel.sensType.value])
            } else {
                viewModel.isPressed = true
                viewModel.potuzhno = true
                playAlertAndExplode()
                viewModel.handler.removeCallbacks(this)
            }
        }
    }

    fun togglePressState() {
        viewModel.isPressed = !viewModel.isPressed
        stopAll()

        if (viewModel.isPressed) {
            post(increment)
        } else {
            post(decrement)
        }
    }

    fun stopAll() {
        viewModel.handler.removeCallbacks(increment)
        viewModel.handler.removeCallbacks(decrement)
        viewModel.handler.removeCallbacks(random)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun isRunning(runnable: Runnable): Boolean {
        return viewModel.handler.hasCallbacks(runnable)
    }

    fun post(runnable: Runnable) {
        viewModel.handler.post(runnable)
    }

    fun postDelayed(runnable: Runnable, delayMillis: Long) {
        viewModel.handler.postDelayed(runnable, delayMillis)
    }

    fun remove(runnable: Runnable) {
        viewModel.handler.removeCallbacks(runnable)
    }
}
