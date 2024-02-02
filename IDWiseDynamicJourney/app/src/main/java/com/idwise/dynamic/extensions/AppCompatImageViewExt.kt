package com.idwise.dynamic.extensions

import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat

fun ImageView.setDrawable(@DrawableRes drawableId: Int) {
    setImageDrawable(AppCompatResources.getDrawable(context, drawableId))
}

fun ImageView.setImageTint(color: Int) {
    setColorFilter(ContextCompat.getColor(this.context, color))
}