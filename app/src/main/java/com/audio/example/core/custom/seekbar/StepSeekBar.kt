package com.audio.example.core.custom.seekbar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.audio.example.R
import kotlin.math.roundToInt

class StepSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var stepCount = 7
    var progress = 0
        set(value) {
            val newValue = value.coerceIn(0, stepCount - 1)
            if (newValue != field) {
                field = newValue
                invalidate()
            }
        }

    private var activeColor: Int = Color.parseColor("#4FC3F7")
    private var inactiveColor: Int = Color.parseColor("#E0E0E0")

    private var thumbDrawable: Drawable? = null
    private var thumbSize = 28f * resources.displayMetrics.density
    private var tickActiveSize = 7f * resources.displayMetrics.density
    private var tickInactiveSize = 5f * resources.displayMetrics.density
    private var lineHeight = 6f * resources.displayMetrics.density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL
    }

    private var trackLeft = 0f
    private var trackRight = 0f
    private var trackY = 0f

    var onStepChanged: ((Int) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.StepSeekBar, 0, 0).apply {
            try {
                stepCount = getInteger(R.styleable.StepSeekBar_stepCount, stepCount)
                progress = getInteger(R.styleable.StepSeekBar_progress, progress)
                activeColor = getColor(R.styleable.StepSeekBar_activeColor, activeColor)
                inactiveColor = getColor(R.styleable.StepSeekBar_inactiveColor, inactiveColor)
                thumbDrawable = getDrawable(R.styleable.StepSeekBar_thumbDrawable)
                thumbSize = getDimension(R.styleable.StepSeekBar_thumbStepSize, thumbSize)
                tickActiveSize = getDimension(R.styleable.StepSeekBar_tickActiveSize, tickActiveSize)
                tickInactiveSize = getDimension(R.styleable.StepSeekBar_tickInactiveSize, tickInactiveSize)
                lineHeight = getDimension(R.styleable.StepSeekBar_lineHeight, lineHeight)
            } finally {
                recycle()
            }
        }

        if (thumbDrawable == null) {
            thumbDrawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (thumbSize * 2).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        trackLeft = paddingLeft + thumbSize / 2
        trackRight = width - paddingRight - thumbSize / 2
        trackY = height / 2f

        val stepSpacing = (trackRight - trackLeft) / (stepCount - 1)
        val activeEndX = trackLeft + progress * stepSpacing

        // --- Vẽ line inactive ---
        paint.color = inactiveColor
        paint.strokeWidth = lineHeight
        canvas.drawLine(trackLeft, trackY, trackRight, trackY, paint)

        // --- Vẽ line active ---
        paint.color = activeColor
        canvas.drawLine(trackLeft, trackY, activeEndX, trackY, paint)

        // --- Vẽ tick ---
        for (i in 0 until stepCount) {
            val x = trackLeft + i * stepSpacing
            if (i <= progress) {
                paint.color = activeColor
                canvas.drawCircle(x, trackY, tickActiveSize, paint)
            } else {
                paint.color = inactiveColor
                canvas.drawCircle(x, trackY, tickInactiveSize, paint)
            }
        }

        // --- Vẽ thumb (drawable hoặc circle) ---
        val thumbX = activeEndX
        val thumbY = trackY

        thumbDrawable?.let { d ->
            val half = thumbSize / 2
            d.setBounds(
                (thumbX - half).toInt(),
                (thumbY - half).toInt(),
                (thumbX + half).toInt(),
                (thumbY + half).toInt()
            )
            d.draw(canvas)
        } ?: run {
            paint.color = activeColor
            canvas.drawCircle(thumbX, thumbY, thumbSize / 2, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                val x = event.x.coerceIn(trackLeft, trackRight)
                val stepSpacing = (trackRight - trackLeft) / (stepCount - 1)
                val nearestStep = ((x - trackLeft) / stepSpacing).roundToInt()
                if (nearestStep != progress) {
                    progress = nearestStep
                    onStepChanged?.invoke(progress)
                }
                invalidate()
            }
        }
        return true
    }
}
