package com.example.pricelistdedup

import android.view.View
import android.view.animation.AlphaAnimation

object AnimationUtils {
    fun fadeIn(view: View, duration: Long = 500) {
        val animation = AlphaAnimation(0f, 1f)
        animation.duration = duration
        view.startAnimation(animation)
    }

    fun fadeOut(view: View, duration: Long = 500) {
        val animation = AlphaAnimation(1f, 0f)
        animation.duration = duration
        view.startAnimation(animation)
    }
}
