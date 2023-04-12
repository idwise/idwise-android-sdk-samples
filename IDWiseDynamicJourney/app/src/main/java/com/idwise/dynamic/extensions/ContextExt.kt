package com.idwise.dynamic.extensions

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.Window
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.idwise.dynamic.R


fun Context.showInfoLoginDialog(
    title: String,
    message: String,
    callback: (() -> Unit)? = null
) {
    val dialog = Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_info_login)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        findViewById<TextView>(R.id.txt_title).text = title
        findViewById<TextView>(R.id.txt_message).text = message
        // Clear : for load the default colors of btn_action
        findViewById<MaterialButton>(R.id.btn_action).apply {
            // load the default colors of btn_action
            setTextColor((context.resources.getColor(R.color.white, null)))
            setBackgroundColor(context.resources.getColor(R.color.blue_color, null))
            strokeColor =
                ColorStateList.valueOf((context.resources.getColor(R.color.blue_color, null)))

            text = context.getString(R.string.ok)
            setOnClickListener {
                dismiss()
                callback?.invoke()
            }
        }
    }
    dialog.show()
}