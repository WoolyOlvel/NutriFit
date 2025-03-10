package com.ascrib.nutrifit.util

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.resources.TypefaceUtils

class NutriFit: Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        TypefaceUtil.overrideFont(this, "SERIF", "font/google_sans.tff")
    }
}