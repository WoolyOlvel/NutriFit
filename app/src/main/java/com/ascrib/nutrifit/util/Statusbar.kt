package com.ascrib.nutrifit.util

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.ascrib.nutrifit.R

class Statusbar {
    companion object {
        fun setStatusbarTheme(
            context: Context,
            window: Window,
            @ColorRes id: Int = 0,
            view: View? = null,
            fromEdge: Boolean = false
        ) {
            if (id == 0) {
                if (Build.VERSION.SDK_INT in 21..28) {
                    window.statusBarColor = Color.TRANSPARENT
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                } else if (Build.VERSION.SDK_INT >= 29) {
                    if (view != null) {
                        setStatusBarTransparent(window, view, context)
                    }
                }
            } else {
                window.statusBarColor = ContextCompat.getColor(context, id)
                // Making status bar overlaps with the activity
                WindowCompat.setDecorFitsSystemWindows(window, fromEdge)
            }

        }

        private fun setStatusBarTransparent(window: Window, view: View, context: Context) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context, R.color.transparent)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInset ->
                val inset = windowInset.getInsets(WindowInsetsCompat.Type.systemBars())
                root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = inset.left
                    bottomMargin = inset.bottom
                    rightMargin = inset.right
                }
                WindowInsetsCompat.CONSUMED
            }

        }
    }
}