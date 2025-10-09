package com.audio.example.core.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd

// Hiệu ứng nhấn (scale nhỏ rồi về lại)
fun View.animateScaleEffect(scaleDownFactor: Float = 0.9f, duration: Long = 100L) {
    val scaleDownX = ObjectAnimator.ofFloat(this, "scaleX", 1f, scaleDownFactor)
    val scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", 1f, scaleDownFactor)
    scaleDownX.duration = duration
    scaleDownY.duration = duration

    val scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", scaleDownFactor, 1f)
    val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", scaleDownFactor, 1f)
    scaleUpX.duration = duration
    scaleUpY.duration = duration

    AnimatorSet().apply {
        play(scaleDownX).with(scaleDownY)
        play(scaleUpX).with(scaleUpY).after(scaleDownX)
        start()
    }
}

// Hiệu ứng nhấn (scale lớn rồi về lại)
fun View.animateScaleOutEffect(scaleUpFactor: Float = 1.1f, duration: Long = 100L) {
    val scaleUpX = ObjectAnimator.ofFloat(this, "scaleX", 1f, scaleUpFactor)
    val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", 1f, scaleUpFactor)
    scaleUpX.duration = duration
    scaleUpY.duration = duration

    val scaleDownX = ObjectAnimator.ofFloat(this, "scaleX", scaleUpFactor, 1f)
    val scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", scaleUpFactor, 1f)
    scaleDownX.duration = duration
    scaleDownY.duration = duration

    AnimatorSet().apply {
        play(scaleUpX).with(scaleUpY)
        play(scaleDownX).with(scaleDownY).after(scaleUpX)
        start()
    }
}

// Rung ngang
fun View.shakeViewEffect(duration: Long = 100, repeatCount: Int = 3, shakeDistance: Float = 5f) {
    val animator =
        ObjectAnimator.ofFloat(this, "translationX", -shakeDistance, shakeDistance).apply {
            this.duration = duration
            this.repeatCount = repeatCount
            this.repeatMode = ObjectAnimator.REVERSE
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    this@shakeViewEffect.translationX = 0f
                }
            })
        }
    animator.start()
}

// Quay vòng vô hạn
fun View.startRotationInfinity() {
    ObjectAnimator.ofFloat(this, "rotation", 0f, -360f).apply {
        duration = 500
        repeatCount = ObjectAnimator.INFINITE
        repeatMode = ObjectAnimator.RESTART
        start()
    }
}

// Quay 1 vòng
fun View.startRotation(durationEnd: Long = 500, action: () -> Unit) {
    val anim = ObjectAnimator.ofFloat(this, "rotation", 0f, -360f).apply {
        duration = durationEnd
    }
    anim.doOnEnd {
        action.invoke()
    }
    anim.start()
}

// Zoom out từ 0 -> end, kèm callback khi kết thúc
fun View.animateZoom(scaleEnd: Float, duration: Long, onEnd: (() -> Unit)? = null) {
    val scaleXAnimation = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, scaleEnd)
    val scaleYAnimation = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f, scaleEnd)

    AnimatorSet().apply {
        playTogether(scaleXAnimation, scaleYAnimation)
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { doOnEnd { it() } }
        start()
    }
}

// Zoom in từ start -> 1f
fun View.animateZoomIn(scaleStart: Float, duration: Long) {
    val scaleXAnimation = ObjectAnimator.ofFloat(this, View.SCALE_X, scaleStart, 1f)
    val scaleYAnimation = ObjectAnimator.ofFloat(this, View.SCALE_Y, scaleStart, 1f)

    AnimatorSet().apply {
        playTogether(scaleXAnimation, scaleYAnimation)
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        start()
    }
}

fun View.zoomEffect(
    zoomScale: Float = 1.2f,
    fadeAlpha: Float = 0.8f,
    duration: Long = 150
) {
    // Zoom in + fade
    val zoomInX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, zoomScale)
    val zoomInY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, zoomScale)
    val fadeOut = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, fadeAlpha)

    // Zoom out + fade in
    val zoomOutX = ObjectAnimator.ofFloat(this, View.SCALE_X, zoomScale, 1f)
    val zoomOutY = ObjectAnimator.ofFloat(this, View.SCALE_Y, zoomScale, 1f)
    val fadeIn = ObjectAnimator.ofFloat(this, View.ALPHA, fadeAlpha, 1f)

    // Rung ngang (giống dây guitar)

    zoomInX.duration = duration
    zoomInY.duration = duration
    fadeOut.duration = duration
    zoomOutX.duration = duration
    zoomOutY.duration = duration
    fadeIn.duration = duration

    zoomInX.interpolator = DecelerateInterpolator()
    zoomInY.interpolator = DecelerateInterpolator()
    zoomOutX.interpolator = OvershootInterpolator()
    zoomOutY.interpolator = OvershootInterpolator()

    AnimatorSet().apply {
        playTogether(zoomInX, zoomInY, fadeOut)
        playTogether(zoomOutX, zoomOutY, fadeIn)
        playSequentially(
            AnimatorSet().apply { playTogether(zoomInX, zoomInY, fadeOut) },
            AnimatorSet().apply { playTogether(zoomOutX, zoomOutY, fadeIn) }
        )
        start()
    }
}

