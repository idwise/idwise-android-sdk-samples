package com.idwise.dynamic.extensions

import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

fun AppCompatTextView.setColor(@ColorRes color: Int) {
    setTextColor(ContextCompat.getColor(this.context, color))
}