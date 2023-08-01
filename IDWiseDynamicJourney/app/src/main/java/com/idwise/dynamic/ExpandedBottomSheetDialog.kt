package com.idwise.dynamic

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StyleRes
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class ExpandedBottomSheetDialog : BottomSheetDialog {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, @StyleRes theme: Int) : super(context, theme) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the initial state of the bottom sheet to expanded
        setInitialState()
    }

    private fun setInitialState() {
        // You can use the bottom sheet view and set its state here
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}