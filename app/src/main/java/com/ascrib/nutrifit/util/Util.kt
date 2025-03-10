package com.ascrib.nutrifit.util

import android.app.Activity
import android.graphics.Rect
import android.widget.Toast

fun Activity.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.top
}

fun Activity.makeToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}