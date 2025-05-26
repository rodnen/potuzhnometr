package com.example.potuzhnometr

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

class CToast private constructor(
    private val context: Context,
    private val message: String,
    private val duration: Int,
    @DrawableRes private val background: Int?,
    @ColorInt private val textColor: Int
) {

    fun show() {
        // Створюємо кастомний вигляд через LayoutInflater
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.custom_toast_layout, null)

        val textView = view.findViewById<TextView>(R.id.custom_toast_text)
        textView.text = message
        textView.setTextColor(textColor)

        background?.let {
            view.background = context.getDrawable(it)
        }

        val toast = Toast(context).apply {
            setDuration(duration)
            setView(view)
            // Налаштування позиції (за бажанням)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        }
        toast.show()
    }

    class Builder(private val context: Context) {
        private var message: String = ""
        private var duration: Int = Toast.LENGTH_SHORT
        private var background: Int? = R.drawable.toast_background
        private var textColor: Int = Color.WHITE  // Змінив на білий за замовчуванням

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setDuration(duration: Int): Builder {
            this.duration = duration
            return this
        }

        fun setBackground(@DrawableRes drawableRes: Int): Builder {
            this.background = drawableRes
            return this
        }

        fun setTextColor(@ColorInt color: Int): Builder {
            this.textColor = color
            return this
        }

        fun build(): CToast {
            return CToast(
                context,
                message,
                duration,
                background,
                textColor
            )
        }
    }

    companion object {
        fun makeText(
            context: Context,
            message: String,
            duration: Int = Toast.LENGTH_SHORT
        ): CToast {
            return Builder(context)
                .setMessage(message)
                .setDuration(duration)
                .build()
        }
    }
}