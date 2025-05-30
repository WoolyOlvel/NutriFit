package com.ascrib.nutrifit.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatDate(date: Date?, pattern: String = "dd/MM/yyyy"): String {
        return date?.let {
            SimpleDateFormat(pattern, Locale.getDefault()).format(it)
        } ?: "No disponible"
    }
}