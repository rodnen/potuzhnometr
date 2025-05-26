package com.example.potuzhnometr

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import kotlin.random.Random
import kotlin.math.min
import kotlin.math.max
import androidx.core.view.isVisible
import androidx.activity.viewModels
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(){
    private val viewModel: AppViewModel by viewModels()
    private val barViews = mutableListOf<View>()

    // Colors
    private val notActiveColor = "#454545".toColorInt()
    private val activeColors = arrayOf(
        "#13FE01", "#2FFB0B", "#44F913", "#58F71A", "#6CF422", "#81F129",
        "#93EF30", "#A9EC38", "#BFEA40", "#D3E748", "#E6E54F", "#FBE256",
        "#FECE50", "#FEBA48", "#FFA33F", "#FF8D37", "#FF772E", "#FF6126",
        "#FF4B1D", "#FF3414", "#FF200C", "#FF0000"
    ).map { it.toColorInt() }

    // Constants
    private companion object {
        const val NUMBER_OF_BARS = 20
        const val CORNER_RADIUS = 16f

        lateinit var SENS_NAMES :Array<String>
        lateinit var MODE_NAMES :Array<String>

        val INCREMENT_SPEED = arrayOf(100L, 60L, 20L)
        val DECREMENT_SPEED = arrayOf(150L, 100L, 50L)
        val RANDOM_SPEED    = arrayOf(500L, 350L, 200L)

        // Sound resources
        val alertSounds = listOf(R.raw.alert_1, R.raw.alert_2, R.raw.alert_3, R.raw.alert_4, R.raw.alert_5)
        val explodeSounds = listOf(R.raw.explode_1, R.raw.explode_2, R.raw.explode_3)
        val sirenSound = R.raw.siren

        // Emoji mapping
        val emojiMap = mapOf(
            15 to R.drawable.yawn,
            30 to R.drawable.pokerface,
            40 to R.drawable.fearful,
            60 to R.drawable.sweat,
            80 to R.drawable.omg,
            100 to R.drawable.explode
        )
    }

    // Views
    private lateinit var powerText: TextView
    private lateinit var screenLayout: RelativeLayout
    private lateinit var image: ImageView
    private lateinit var sensButton: TextView
    private lateinit var modeButton: TextView
    private lateinit var videoContainer: FrameLayout
    private lateinit var seekBar: SeekBar
    private lateinit var seekbarContainer: LinearLayout

    // Hidden layout
    private lateinit var hiddenPanel: FrameLayout

    // Settings views
    private lateinit var settingsContent: RelativeLayout
    private lateinit var settingsButton: ImageView
    private lateinit var switchAlertSound: Switch
    private lateinit var switchSirenSound: Switch
    private lateinit var switchExplosion: Switch

    // Info
    private lateinit var infoContent: RelativeLayout
    private lateinit var infoButton :ImageView
    private lateinit var versionText : TextView
    private lateinit var checkUpdButton : RelativeLayout
    private lateinit var updateDialog : FrameLayout

    // Animation
    private val colorAnimator by lazy { createColorAnimator() }
    private val videoEnterAnimator by lazy { createVideoEnterAnimator() }

    // State
    private var isBeingClosed = false
    private var backPressedOnce = false
    private var alreadyClicked = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupVariables()
        setupWindow()

        initViews()
        initBars()
        initCards()
        initSwitches()
        syncSwitch()

        setupVersion()
        setupListeners()
        loadSettings()

        updateUI()
    }

    private fun setupVariables() {
        SENS_NAMES = arrayOf(
            getString(R.string.sens_1),
            getString(R.string.sens_2),
            getString(R.string.sens_3)
        )

        MODE_NAMES = arrayOf(
            getString(R.string.mode_1),
            getString(R.string.mode_2),
            getString(R.string.mode_3),
            getString(R.string.mode_4)
        )

        AppData.init(applicationContext)
        AppData.alertPlayer = viewModel.alertPlayer
        AppData.sirenPlayer = viewModel.sirenPlayer
    }

    private fun setupWindow() {
        enableEdgeToEdge()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupListeners() {
        setupTouchListener()
        setupSensitivityButton()
        setupModeButton()
        setupSeekBarListener()
        setupSettingsButton()
        setupExitListeners()
        setupInfoButton()
        setupUpdateButton()
    }

    private fun setupVersion() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)

        val version = packageInfo.versionName
        val lastUpdateTime = packageInfo.lastUpdateTime

        val formattedDate = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(lastUpdateTime))
        versionText.text = "Версія: $version (${formattedDate})"
    }

    private fun createColorAnimator(): ValueAnimator {
        return ValueAnimator.ofObject(ArgbEvaluator(), "#EE4848".toColorInt(), "#630808".toColorInt()).apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                screenLayout.setBackgroundColor(color)
                window.statusBarColor = color
                window.navigationBarColor = color
            }
        }
    }

    private fun createVideoEnterAnimator(): ValueAnimator {
        val animationDuration = 300L
        return ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(this, R.color.app_statusBarColor),
            Color.BLACK
        ).apply {
            duration = animationDuration
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                screenLayout.setBackgroundColor(color)
                window.statusBarColor = color
                window.navigationBarColor = color
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    videoContainer.visibility = View.INVISIBLE
                }

                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onAnimationEnd(animation: Animator) {
                    videoContainer.visibility = View.VISIBLE
                    viewModel.handler.postDelayed({
                        startVideoPlayback(R.raw.explosion)
                        playExplosionSound()
                    }, animationDuration)
                }
            })
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        isBeingClosed = true
    }

    override fun onStop() {
        super.onStop()
        if (isBeingClosed && !isChangingConfigurations) {
            logAppClosedBySwiping()
        }
        isBeingClosed = false
    }

    private fun logAppClosedBySwiping() {
        restoreSettings()
    }


    override fun onBackPressed() {
        if (settingsContent.isVisible) {
            hideSettings()
            return
        }

        if (infoContent.isVisible) {
            hideInfo()
            return
        }

        if (backPressedOnce) {
            restoreSettings()
            super.onBackPressed()
            return
        }

        backPressedOnce = true

        CToast.makeText(this, "Натисність ще раз, щоб вийти", Toast.LENGTH_SHORT).show()

        backPressHandler.postDelayed({
            backPressedOnce = false
        }, 2000)
    }

    private fun restoreSettings() {
        stopRunnables()
        AppData.stopAudio()

        updateUI()
    }

    private fun stopRunnables() {
        viewModel.handler.removeCallbacks(incrementRunnable)
        viewModel.handler.removeCallbacks(decrementRunnable)
        viewModel.handler.removeCallbacks(randomRunnable)
        viewModel.handler.removeCallbacks(incrementRunnable)
    }


    private fun saveSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("isDarkTheme", viewModel.isDarkTheme)
            putBoolean("playAlertSound", viewModel.playAlertSound)
            putBoolean("playSirenSound", viewModel.playSirenSound)
            putBoolean("playPotuzhnometrExplosion", viewModel.playPotuzhnometrExplosion)
            apply()
        }
    }


    private fun loadSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        viewModel.isDarkTheme               = prefs.getBoolean("isDarkTheme", false)
        viewModel.playAlertSound            = prefs.getBoolean("playAlertSound", true)
        viewModel.playSirenSound            = prefs.getBoolean("playSirenSound", true)
        viewModel.playPotuzhnometrExplosion = prefs.getBoolean("playPotuzhnometrExplosion", true)

        updateUI()
    }

    private fun checkTheme() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                viewModel.isDarkTheme = true
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                viewModel.isDarkTheme = false
            }
        }
    }

    private fun initViews() {
        image = findViewById(R.id.emoji_start)
        screenLayout = findViewById(R.id.main_screen)
        powerText = findViewById(R.id.power_number)
        sensButton = findViewById(R.id.sensitivity)
        modeButton = findViewById(R.id.mode)
        videoContainer = findViewById(R.id.video_container)
        seekBar = findViewById(R.id.sensitivitySeekBar)
        seekbarContainer = findViewById(R.id.seekbar_container)

        // hidden layout
        hiddenPanel = findViewById(R.id.hidden_panel)

        // settings
        settingsButton = findViewById(R.id.settings_button)
        settingsContent = findViewById(R.id.settings_content)

        switchAlertSound = findViewById(R.id.switch_alert_sound)
        switchSirenSound = findViewById(R.id.switch_siren_sound)
        switchExplosion = findViewById(R.id.switch_explosion)

        //info
        infoButton = findViewById(R.id.info_button)
        infoContent = findViewById(R.id.info_content)
        versionText = findViewById(R.id.version)
        updateDialog = findViewById(R.id.update_dialog)
        checkUpdButton = findViewById(R.id.check_updates)
    }

    private fun initCards() {
        val borderColor = "#262626".toColorInt()
        arrayOf(
            R.id.cardView,
            R.id.cardView_load,
            R.id.cardView_number,
        ).forEach { findViewById<CardView>(it).setCardBackgroundColor(borderColor) }
    }

    private fun initBars() {
        val barsLayout = findViewById<LinearLayout>(R.id.bars_layout)

        for (i in 0 until NUMBER_OF_BARS) {
            val view = createBarView(i)
            barViews.add(view)
            barsLayout.addView(view)
        }
    }

    private fun initSwitches() {
        /*switchDarkTheme.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isDarkTheme = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                isDarkTheme = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }*/

        switchAlertSound.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.playAlertSound = true
            } else {
                viewModel.playAlertSound = false
            }

            saveSettings()
        }

        switchSirenSound.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.playSirenSound = true
            } else {
                viewModel.playSirenSound = false
            }

            saveSettings()
        }

        switchExplosion.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.playPotuzhnometrExplosion = true
            } else {
                viewModel.playPotuzhnometrExplosion = false
            }

            saveSettings()
        }
    }

    private fun createBarView(index: Int): View {
        val view = View(this)
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(notActiveColor)
            cornerRadius = CORNER_RADIUS
        }
        view.background = drawable

        val container = findViewById<View>(R.id.bars_layout)
        container.post {
            val height = container.height
            val minHeight = height / 3.5
            val maxHeight = height * 0.6
            val barHeight = minHeight + ((maxHeight - minHeight) * index / (NUMBER_OF_BARS - 1))

            view.layoutParams = LinearLayout.LayoutParams(0, barHeight.toInt(), 1f).apply {
                setMargins(6, 0, 6, 0)
            }
        }

        return view
    }

    private fun updateBarsAndImage() {
        val filledBars = (viewModel.counter.toFloat() / 100 * barViews.size).toInt()

        barViews.forEachIndexed { i, view ->
            val isActive = i < filledBars
            val drawable = view.background as GradientDrawable

            drawable.setColor(if (isActive) activeColors[i] else notActiveColor)

            // Анімація для активних елементів
            if (isActive) {
                view.animate()
                    .scaleX(1.2f)  // Збільшення по ширині на 20%
                    .scaleY(1.2f)  // Збільшення по висоті на 20%
                    .setDuration(150)
                    .withEndAction {
                        // Плавне повернення до нормального розміру
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            } else {
                // Скидання анімації для неактивних
                view.scaleX = 1f
                view.scaleY = 1f
            }

        }

        val newImageRes = emojiMap.entries.find { viewModel.counter <= it.key }?.value
        newImageRes?.let {
            val newDrawable = ContextCompat.getDrawable(this, it)
            if (image.drawable != newDrawable) {
                image.setImageDrawable(newDrawable)
            }
        }
    }

    private fun showHiddenPanel(){
        hiddenPanel.visibility = View.VISIBLE
        hiddenPanel.alpha = 0f
        hiddenPanel.animate().alpha(1f).setDuration(200).start()

        ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(this, R.color.app_statusBarColor),
            ContextCompat.getColor(this, R.color.app_statusBarDarkColor)
        ).apply {
            duration = 200L // Тривалість анімації 200 мс
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int

                // Оновлюємо статус бар
                window.statusBarColor = color
            }
        }.start()

        ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(this, R.color.app_navigationBarColor),
            ContextCompat.getColor(this, R.color.app_navigationBarDarkColor)
        ).apply {
            duration = 200L // Тривалість анімації 200 мс
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int

                // Оновлюємо навігаційну панель
                window.navigationBarColor = color
            }
        }.start()
    }

    private fun hideHiddenPanel() {
        hiddenPanel.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                hiddenPanel.visibility = View.GONE
            }
            .start()

        ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(this, R.color.app_statusBarDarkColor),
            ContextCompat.getColor(this, R.color.app_statusBarColor)
        ).apply {
            duration = 200L // Тривалість анімації 200 мс
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int

                // Оновлюємо статус бар
                window.statusBarColor = color
            }
        }.start()

        ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(this, R.color.app_navigationBarDarkColor),
            ContextCompat.getColor(this, R.color.app_navigationBarColor)
        ).apply {
            duration = 200L // Тривалість анімації 200 мс
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int

                // Оновлюємо навігаційну панель
                window.navigationBarColor = color
            }
        }.start()
    }

    private fun showSeekbar() {
        seekbarContainer.visibility = View.VISIBLE
        seekbarContainer.animate()
            .scaleY(1f)
            .setDuration(200)
            .start()
    }

    private fun hideSeekbar() {
        seekbarContainer.animate()
            .scaleY(0f)
            .setDuration(200)
            .withEndAction {
                seekbarContainer.visibility = View.GONE
            }
            .start()
    }

    private fun showSettings() {
        showHiddenPanel()
        settingsContent.visibility  = View.VISIBLE
    }

    private fun hideSettings() {
        hideHiddenPanel()
        settingsContent.visibility = View.GONE
    }

    private fun showInfo() {
        showHiddenPanel()
        infoContent.visibility  = View.VISIBLE
    }

    private fun hideInfo() {
        hideHiddenPanel()
        infoContent.visibility = View.GONE
    }

    private fun showUpdateDialog() {
        updateDialog.visibility = View.VISIBLE
        updateDialog.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun hideUpdateDialog() {
        updateDialog.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                updateDialog.visibility = View.GONE
            }
            .start()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        screenLayout.setOnTouchListener { _, event ->
            when (viewModel.modeType) {
                ModeType.SINGLE_TAP -> { // Один клік
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            viewModel.isPressed = !viewModel.isPressed
                            alreadyClicked = !alreadyClicked
                            if(viewModel.isPressed && alreadyClicked) {
                                viewModel.handler.removeCallbacks(decrementRunnable)
                                viewModel.handler.post(incrementRunnable)
                            }
                            else {
                                viewModel.handler.removeCallbacks(incrementRunnable)
                                viewModel.handler.post(decrementRunnable)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            // Можемо скинути після відпускання, якщо потрібно
                            true
                        }
                        else -> false
                    }
                }
                ModeType.HOLD -> { // Як твій приклад (затиснув — інкремент, відпустив — декремент)
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            viewModel.isPressed = true
                            viewModel.handler.removeCallbacks(decrementRunnable)
                            viewModel.handler.post(incrementRunnable)
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            viewModel.isPressed = false
                            viewModel.handler.removeCallbacks(incrementRunnable)
                            viewModel.handler.post(decrementRunnable)
                            true
                        }
                        else -> false
                    }
                }
                ModeType.RANDOM -> {

                    if (!viewModel.handler.hasCallbacks(randomRunnable)) {
                        viewModel.handler.post(randomRunnable)
                    }

                    false
                }

                ModeType.MANUAL -> {
                    false
                }

                else -> false
            }
        }
    }


    private fun setupSensitivityButton() {
        sensButton.setOnClickListener {
            // Оновлюємо значення ПРЯМО у ViewModel
            viewModel.sensType = SensType.fromInt(
                (viewModel.sensType.value + 1) % SensType.entries.size
            )!!

            // Оновлюємо текст кнопки
            sensButton.text = SENS_NAMES[viewModel.sensType.value]
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupModeButton() {
        modeButton.setOnClickListener {
            resetPotuzhnometer()

            if(seekbarContainer.isVisible){
                hideSeekbar()
            }

            if (viewModel.handler.hasCallbacks(randomRunnable)) {
                viewModel.handler.removeCallbacks(randomRunnable)
            }

            if (!viewModel.handler.hasCallbacks(decrementRunnable)) {
                viewModel.handler.post(decrementRunnable)
            }

            // Оновлюємо значення ПРЯМО у ViewModel
            viewModel.modeType = ModeType.fromInt(
                (viewModel.modeType.value + 1) % ModeType.entries.size
            )!!

            // Оновлюємо текст кнопки
            modeButton.text = MODE_NAMES[viewModel.modeType.value]

            if(viewModel.modeType == ModeType.RANDOM){
                viewModel.handler.post(randomRunnable)
            }

            if(viewModel.modeType == ModeType.MANUAL){
                if (viewModel.handler.hasCallbacks(decrementRunnable)) {
                    viewModel.handler.removeCallbacks(decrementRunnable)
                }
                showSeekbar()
                syncSeekBarCounter()
            }
        }
    }

    private fun setupSettingsButton(){
        settingsButton.setOnClickListener(){
            showSettings()
        }
    }

    private fun setupInfoButton(){
        infoButton.setOnClickListener(){
            showInfo()
        }
    }

    private fun setupUpdateButton() {
        checkUpdButton.setOnClickListener() {
            showUpdateDialog()
        }
    }

    private fun  setupExitListeners(){
        hiddenPanel.setOnClickListener(){
            hideSettings()
            hideInfo()
        }

        updateDialog.setOnClickListener(){
            hideUpdateDialog()
        }
    }

    private fun syncSeekBarCounter(){
        seekBar.progress = viewModel.counter
    }

    private fun syncSwitch(){

        checkTheme()
        /* switchDarkTheme.isChecked  = isDarkTheme*/
        switchAlertSound.isChecked = viewModel.playAlertSound
        switchSirenSound.isChecked = viewModel.playSirenSound
        switchExplosion.isChecked  = viewModel.playPotuzhnometrExplosion
    }

    private fun setupSeekBarListener(){
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Викликається при кожній зміні прогресу
                viewModel.counter = progress
                updateUI()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Викликається при початку перетягування повзунка
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if(viewModel.counter == 100){
                    viewModel.potuzhno = true
                    viewModel.isPressed = true
                    updateUI()
                    playAlertAndExplode()
                }
                // Викликається при завершенні перетягування
            }
        })
    }

    //Відновлення після закінчення циклу потужноментра
    private fun resetAfterRelease() {
        AppData.stopAlertSound()
        AppData.stopSirenSound()

        powerText.setTextColor(Color.WHITE)
        updateBackgroundTheme()
        resetSystemBars()

        colorAnimator.cancel()
        viewModel.potuzhno = false
        viewModel.isExploded = false
    }

    private fun syncButtonNames() {
        modeButton.text = MODE_NAMES[viewModel.modeType.value]
        sensButton.text = SENS_NAMES[viewModel.sensType.value]
    }

    private fun updateUI() {
        powerText.text = viewModel.counter.toString()
        updateBarsAndImage()
        syncSeekBarCounter()
        syncButtonNames()
    }

    private fun updateBackgroundTheme() {
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        screenLayout.apply {
            if (isNightMode) {
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.app_background))
            } else {
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.gradient_light)
            }
        }
    }

    private fun resetSystemBars() {
        window.apply {
            statusBarColor = ContextCompat.getColor(applicationContext, R.color.app_statusBarColor)
            navigationBarColor = ContextCompat.getColor(applicationContext, R.color.app_navigationBarColor)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startVideoPlayback(resId: Int) {
        val videoUri = "android.resource://${packageName}/$resId".toUri()

        val videoView = VideoView(this).apply {
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
            viewModel.handler.post(decrementRunnable)
            resetPotuzhnometer()

            videoContainer.removeAllViews()
            videoContainer.visibility = View.GONE
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun resetPotuzhnometer(){
        viewModel.potuzhno = false
        viewModel.isPressed = false
        alreadyClicked = false
        viewModel.targetCounter = 0
        seekBar.isEnabled = true
    }

    @SuppressLint("ResourceAsColor")
    private fun playFullscreenVideo() {
        colorAnimator.cancel()
        videoEnterAnimator.start()
    }

    private fun playExplosionSound() {
        val soundRes = explodeSounds.random()
        val explosionPlayer = MediaPlayer.create(this, soundRes)

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
    private fun playAlertAndExplode() {
        if (viewModel.isExplosionInProgress) return // Захист від повторного запуску

        viewModel.isExplosionInProgress = true
        viewModel.isSoundPlaying = true

        powerText.setTextColor("#EE4848".toColorInt())
        colorAnimator.start()
        viewModel.isColorCleared = false

        val alert = alertSounds.random()

        if(viewModel.playSirenSound) {
            viewModel.sirenPlayer?.release()
            viewModel.sirenPlayer = MediaPlayer.create(this, sirenSound).apply {
                isLooping = true
                setVolume(0.3f, 0.3f)
                start()
            }
        }

        if(viewModel.playAlertSound) {
            viewModel.alertPlayer?.release()
            viewModel.alertPlayer = MediaPlayer.create(this, alert).apply {
                isLooping = false
                start()
                setOnCompletionListener {
                    release()
                    viewModel.handler.postDelayed({
                        if (!viewModel.isPressed) {
                            cancelExplosionSequence()
                        } else {
                            executeExplosion()
                        }
                    }, 2000L)
                }
            }
        }

        else{
            viewModel.handler.postDelayed({
                if (!viewModel.isPressed) {
                    cancelExplosionSequence()
                } else {
                    executeExplosion()
                }
            }, 2000L)
        }
    }

    private fun cancelExplosionSequence() {
        viewModel.isExplosionInProgress = false
        viewModel.isSoundPlaying = false
        viewModel.sirenPlayer?.release()
        viewModel.sirenPlayer = null
        viewModel.handler.post(decrementRunnable)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun executeExplosion() {
        if (!viewModel.isExplosionInProgress) return

        viewModel.sirenPlayer?.release()
        viewModel.sirenPlayer = null

        if(viewModel.playPotuzhnometrExplosion)
            playFullscreenVideo()
        else{
            viewModel.isExploded = true
            viewModel.handler.post(decrementRunnable)
            resetPotuzhnometer()

            videoContainer.removeAllViews()
            videoContainer.visibility = View.GONE
        }
        viewModel.isExplosionInProgress = false
    }


    private val incrementRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun run() {
            if(viewModel.isPressed){
                if (viewModel.counter in 0..99) {
                    viewModel.counter++
                    powerText.text = viewModel.counter.toString()
                    updateUI()
                    viewModel.handler.postDelayed(this, INCREMENT_SPEED[viewModel.sensType.value])
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

    private val decrementRunnable = object : Runnable {
        override fun run() {
            if (viewModel.isPressed && viewModel.isExploded) viewModel.isPressed = false

            if (!viewModel.isPressed && viewModel.counter > 0 && !viewModel.isSoundPlaying) {
                if (viewModel.isExploded) {
                    viewModel.counter = 0
                    syncSeekBarCounter()
                } else {
                    viewModel.counter--
                }

                resetAfterRelease()
                updateUI()
                viewModel.handler.postDelayed(this, DECREMENT_SPEED[viewModel.sensType.value])
            } else {
                viewModel.handler.removeCallbacks(this)
            }

        }
    }

    private val randomRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun run() {

            val step = Random.nextInt(2,8)
            if (viewModel.counter != viewModel.targetCounter) {
                // Плавне збільшення або зменшення
                val diff = viewModel.targetCounter - viewModel.counter
                viewModel.counter += when {
                    diff > 0 -> min(step, diff)
                    diff < 0 -> max(-step, diff)
                    else -> 0
                }
                updateUI()
            } else {
                // Досягнуто цілі, встановлюємо нову
                viewModel.targetCounter = Random.nextInt(0, 101)
            }

            if(viewModel.counter in 0..99){
                viewModel.handler.postDelayed(this, RANDOM_SPEED[viewModel.sensType.value])
            }
            else{
                viewModel.isPressed = true
                viewModel.potuzhno = true
                playAlertAndExplode()
                viewModel.handler.removeCallbacks(this)
            }
        }
    }

    fun isUpdateAvailable(currentVersion: String, latestVersion: String): Boolean {
        return currentVersion != latestVersion
    }
}
