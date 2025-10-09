package com.audio.example.core.custom.progress

import android.R.attr.height
import android.R.attr.width
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.audio.example.R
import androidx.core.graphics.toColorInt
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularProgressViewPlaying @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var strokeWidth = 10f
    private val startAngle = 90f
    private var maxProgress = 100L
    private var progress = 0f
    private val rectF = RectF()

    var thumbDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_thumb_start)
    var startDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_thumb_start)

    private var thumbSize = 12f

    // 👇 Padding cho progress
    private var progressPadding = 8f.dp
    private var startSize = 24f.dp

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CircularProgressViewPlaying, 0, 0).apply {
            thumbSize = getDimension(R.styleable.CircularProgressViewPlaying_thumbSize, thumbSize)
            strokeWidth = getDimension(R.styleable.CircularProgressViewPlaying_progressWidth, strokeWidth)
            progressPadding = getDimension(R.styleable.CircularProgressViewPlaying_progressPadding, progressPadding)
            recycle()
        }
    }

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#2CC3FF".toColorInt()
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressViewPlaying.strokeWidth
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressViewPlaying.strokeWidth
        strokeCap = Paint.Cap.ROUND
        color = "#2CC3FF".toColorInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - strokeWidth / 2f - progressPadding
        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Nền
        canvas.drawCircle(cx, cy, radius, paintCircle)

        // Sweep angle
        val sweepAngle = (progress / maxProgress) * 360f
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paintProgress)

        // --- Vẽ start drawable (luôn ở góc 90°) ---
        val startAngleRad = Math.toRadians(startAngle.toDouble())
        val startX = (cx + radius * cos(startAngleRad)).toFloat()
        val startY = (cy + radius * sin(startAngleRad)).toFloat()
        startDrawable?.let { d ->
            val half = startSize / 2
            d.setBounds(
                (startX - half).toInt(),
                (startY - half).toInt(),
                (startX + half).toInt(),
                (startY + half).toInt()
            )
            d.draw(canvas)
        }

        // --- Vẽ thumb (đè lên) ---
        val angleRad = Math.toRadians((startAngle + sweepAngle).toDouble())
        val thumbX = (cx + radius * cos(angleRad)).toFloat()
        val thumbY = (cy + radius * sin(angleRad)).toFloat()

        thumbDrawable?.let { d ->
            val half = thumbSize / 2
            d.setBounds(
                (thumbX - half).toInt(),
                (thumbY - half).toInt(),
                (thumbX + half).toInt(),
                (thumbY + half).toInt()
            )
            d.draw(canvas)
        }
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, maxProgress.toFloat())
        invalidate()
    }

    fun setMaxDuration(value: Long) {
        maxProgress = value
    }

    private val Float.dp get() = this * resources.displayMetrics.density
}

