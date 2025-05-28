package com.example.potuzhnometr.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.potuzhnometr.AppViewModel
import com.example.potuzhnometr.R

class UIAnimator(
    private val viewModel: AppViewModel,
    private val activity: Activity,
    private val screenLayout: View,
    private val window: android.view.Window,
    private val hiddenPanel: View,
    private val videoContainer: RelativeLayout,
    private val seekbarContainer: View,
    private val loadingAnimation: LottieAnimationView,
    private val updateDialog: View,
    private val updateMsg: TextView,
) {

    // ========== SYSTEM COLOR ANIMATIONS ==========

    fun createColorAnimator(): ValueAnimator {
        return ValueAnimator.ofObject(
            ArgbEvaluator(),
            "#EE4848".toColorInt(),
            "#630808".toColorInt()
        ).apply {
            duration = 600
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                viewModel.lastChangedColor = color
                applySystemBarsColor(color)
                applyScreenLayoutColor(color)
            }
        }
    }

    fun createVideoEnterAnimator(onAnimationEnd: () -> Unit): ValueAnimator {
        val animationDuration = 300L
        val startColor = viewModel.lastChangedColor
        val endColor = Color.BLACK

        return ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = animationDuration
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val progress = animator.animatedFraction
                val blendedColor = ArgbEvaluator().evaluate(progress, startColor, endColor) as Int

                videoContainer.setBackgroundColor(blendedColor)
                videoContainer.alpha = progress

                applyScreenLayoutColor(blendedColor)
                applySystemBarsColor(blendedColor)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    videoContainer.visibility = View.VISIBLE
                    videoContainer.alpha = 0f
                }

                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd() // виклик функції переданої як аргумент
                }

            })
        }
    }

    fun updateBackgroundTheme(isNightMode: Boolean) {
        if (isNightMode) {
            screenLayout.setBackgroundColor(
                ContextCompat.getColor(activity, R.color.app_background)
            )
        } else {
            screenLayout.background = ContextCompat.getDrawable(
                activity,
                R.drawable.gradient_light
            )
        }
    }

    fun resetSystemBars() {
        applySystemBarsColor(
            ContextCompat.getColor(activity, R.color.app_statusBarColor),
            ContextCompat.getColor(activity, R.color.app_navigationBarColor)
        )
    }

    private fun applySystemBarsColor(statusBar: Int, navBar: Int = statusBar) {
        window.statusBarColor = statusBar
        window.navigationBarColor = navBar
    }

    private fun applyScreenLayoutColor(color: Int) {
        screenLayout.setBackgroundColor(color)
    }

    private fun animateStatusBar(fromRes: Int, toRes: Int) {
        ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(activity, fromRes),
            ContextCompat.getColor(activity, toRes)
        ).apply {
            duration = 200L
            addUpdateListener {
                window.statusBarColor = it.animatedValue as Int
            }
        }.start()
    }

    private fun animateNavBar(fromRes: Int, toRes: Int) {
        ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(activity, fromRes),
            ContextCompat.getColor(activity, toRes)
        ).apply {
            duration = 200L
            addUpdateListener {
                window.navigationBarColor = it.animatedValue as Int
            }
        }.start()
    }

    fun playBarAnimation(view: View, isActive: Boolean) {
        if (isActive) {
            view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        } else {
            view.scaleX = 1f
            view.scaleY = 1f
        }
    }

    // ========== PANEL + SEEKBAR ==========

    fun showHiddenPanel() {
        hiddenPanel.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(200).start()
        }

        animateStatusBar(
            R.color.app_statusBarColor,
            R.color.app_statusBarDarkColor
        )

        animateNavBar(
            R.color.app_navigationBarColor,
            R.color.app_navigationBarDarkColor
        )
    }

    fun hideHiddenPanel() {
        hiddenPanel.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction { hiddenPanel.visibility = View.GONE }
            .start()

        animateStatusBar(
            R.color.app_statusBarDarkColor,
            R.color.app_statusBarColor
        )

        animateNavBar(
            R.color.app_navigationBarDarkColor,
            R.color.app_navigationBarColor
        )
    }

    fun showSeekbar() {
        seekbarContainer.visibility = View.VISIBLE
        seekbarContainer.animate()
            .scaleY(1f)
            .setDuration(200)
            .start()
    }

    fun hideSeekbar() {
        seekbarContainer.animate()
            .scaleY(0f)
            .setDuration(200)
            .withEndAction { seekbarContainer.visibility = View.GONE }
            .start()
    }

    // ========== LOTTIE LOADING & UPDATE DIALOG ==========

    fun startLoadingAnimation() {
        loadingAnimation.apply {
            visibility = View.VISIBLE
            repeatCount = LottieDrawable.INFINITE
            repeatMode = LottieDrawable.RESTART
            playAnimation()
        }
    }

    fun stopLoadingAnimation() {
        loadingAnimation.apply {
            cancelAnimation()
            progress = 0f
            visibility = View.GONE
        }
    }

    fun showUpdateDialog() {
        resetUpdateMessage()

        updateDialog.apply {
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(300).start()
        }
    }

    fun hideUpdateDialog() {
        updateDialog.apply {
            animate().alpha(0f).setDuration(300).withEndAction{
                visibility = View.GONE
                resetUpdateMessage()
            }.start()
        }
    }

    fun showUpdateMessage(msg: String) {
        stopLoadingAnimation()

        updateMsg.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = msg
        }
    }

    private fun resetUpdateMessage() {
        updateMsg.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
    }
}