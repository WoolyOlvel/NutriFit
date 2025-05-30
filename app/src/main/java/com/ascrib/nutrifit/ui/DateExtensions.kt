package com.ascrib.nutrifit.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateExtensions {
    fun String.formatFechaConsulta(estado: String?): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(this) ?: return this

            when(estado?.toIntOrNull()) {
                1 -> { // En progreso
                    val now = Calendar.getInstance()
                    val consultaDate = Calendar.getInstance().apply { time = date }

                    if (consultaDate.after(now)) {
                        val diff = consultaDate.timeInMillis - now.timeInMillis
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                        "Falta $days días con $hours horas para su consulta"
                    } else {
                        "Consulta en curso"
                    }
                }
                2 -> { // Próxima consulta
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy 'a las' hh:mm a", Locale.getDefault())
                    outputFormat.format(date)
                }
                3, 4 -> { // Realizado o En espera
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    outputFormat.format(date) + if (estado == "4") " (En Espera)" else ""
                }
                else -> this
            }
        } catch (e: Exception) {
            this
        }
    }
}