package com.idwise.simple.extensions

import android.os.Handler
import android.os.Looper
import android.view.View

fun View.preventMultipleTap() {
    preventMultipleTap(1000L)
}

/**
 * Prevent Multiple Tap
 *
 * @param view          : View On Which To Stop Multiple Tap
 * @param timeInMillis: Disable Time
 */
fun View.preventMultipleTap(timeInMillis: Long) {
    try {
        if (timeInMillis > 0) {
            isEnabled = false
            Handler(Looper.getMainLooper())
                .postDelayed({ isEnabled = true }, timeInMillis)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}