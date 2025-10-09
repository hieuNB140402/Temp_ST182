package com.audio.example.core.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.sqrt

class GestureHelper(
    context: Context,
    private val listener: GestureCallback
) : GestureDetector.SimpleOnGestureListener(),
    ScaleGestureDetector.OnScaleGestureListener,
    SensorEventListener {

    private val gestureDetector = GestureDetector(context, this)
    private val scaleGestureDetector = ScaleGestureDetector(context, this)
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var lastShakeTime: Long = 0

    // ---- Xử lý TouchEvent ----
    fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        if (event.pointerCount >= 2) {
            if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                listener.onHoldMultiTouch()
            }
        }
        return true
    }

    // ---- Click & Double Click ----
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        listener.onClick()
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        listener.onDoubleClick()
        return true
    }

    // ---- Long Press ----
    override fun onLongPress(e: MotionEvent) {
        listener.onHold()
    }

    // ---- Vuốt ----
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        listener.onSwipe()
        return true
    }

    // ---- Vuốt nhiều ngón (Pinch / Zoom) ----
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        listener.onSwipeMultiTouch()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector) = true
    override fun onScaleEnd(detector: ScaleGestureDetector) {}

    // ---- Shake (Lắc thiết bị) ----
    fun registerShakeListener() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun unregisterShakeListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: android.hardware.SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration =
                sqrt((x * x + y * y + z * z).toDouble()) / SensorManager.GRAVITY_EARTH

            val currentTime = System.currentTimeMillis()
            if (acceleration > 1.8 && currentTime - lastShakeTime > 500) {
                lastShakeTime = currentTime
                listener.onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ---- Callback Interface ----
    interface GestureCallback {
        fun onClick()
        fun onDoubleClick()
        fun onSwipe()
        fun onSwipeMultiTouch()
        fun onHold()
        fun onHoldMultiTouch()
        fun onShake()
    }
}