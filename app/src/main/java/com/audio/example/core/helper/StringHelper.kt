package com.audio.example.core.helper

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.audio.example.core.custom.text.CustomTypefaceSpan
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

object StringHelper {
    fun generateRandomImageFileName(): String {
        val randomNumber = (100000000000..999999999999).random()
        return "IMG_$randomNumber.png"
    }
    fun generateRandomVideoFileName(): String {
        val randomNumber = (100000000000..999999999999).random()
        return "VD_$randomNumber.mp4"
    }

    fun generateRandomString(length: Int = 12): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }

    fun formatNumber(input: String): String {
        val number = input.toLongOrNull() ?: return input
        val formatter = NumberFormat.getInstance(Locale.GERMANY) as DecimalFormat
        return formatter.format(number)
    }

    internal fun upperFirstCharacter(str: String): String {
        return str.capitalize(Locale.ROOT)
    }

    internal fun convertToLowerCase(input: String): String {
        return input.lowercase()
    }

    internal fun formatDecimal(number: Double, decimalPlaces: Int): String {
        val pattern = "#." + "0".repeat(decimalPlaces)
        val decimalFormat = DecimalFormat(pattern)
        return decimalFormat.format(number)
    }

    fun changeColor(
        context: Context,
        text: String,
        color: Int,
        fontfamily: Int,
    ): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(context.getColor(color)),
            0,
            text.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val font = ResourcesCompat.getFont(context, fontfamily)
        val typefaceSpan = CustomTypefaceSpan("", font)
        spannableString.setSpan(
            typefaceSpan, 0, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    fun changeGradientText(textView: AppCompatTextView) {
        textView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (textView.width > 0 && textView.height > 0) {
                    val textShader: Shader = LinearGradient(
                        0f, 0f, 0f, textView.textSize.toFloat(),
                        intArrayOf(
                            "#51C7FF".toColorInt(),
                            "#A6E2FF".toColorInt()
                        ), floatArrayOf(0.25f, 1f), Shader.TileMode.CLAMP
                    )
                    textView.paint.setShader(textShader)
                }
                textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }
}
