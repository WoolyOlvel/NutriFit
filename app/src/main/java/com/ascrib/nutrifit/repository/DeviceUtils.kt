package com.ascrib.nutrifit.repository

import android.content.Context
import android.provider.Settings
import java.util.*

object DeviceUtils {
    fun getDeviceId(context: Context): String {
        return try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: generateFallbackId()
        } catch (e: Exception) {
            generateFallbackId()
        }
    }

    private fun generateFallbackId(): String {
        return UUID.randomUUID().toString()
    }
}