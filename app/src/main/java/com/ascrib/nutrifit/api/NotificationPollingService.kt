package com.ascrib.nutrifit.api

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.ascrib.nutrifit.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.ascrib.nutrifit.repository.NotificacionRepository

class NotificationPollingService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val pollingInterval = 15000L // 15 segundos

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startPolling()
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "notification_channel")
            .setContentTitle("NutriFit")
            .setContentText("Monitoreando notificaciones")
            .setSmallIcon(R.drawable.notificacion)
            .build()

        startForeground(1, notification)
    }

    private fun startPolling() {
        runnable = object : Runnable {
            override fun run() {
                checkNotifications()
                handler.postDelayed(this, pollingInterval)
            }
        }
        handler.post(runnable)
    }

    private fun checkNotifications() {
        val pacienteId = getPacienteId()
        if (pacienteId == 0) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val count = NotificacionRepository().contarNotificacionesNoLeidas(pacienteId)
                sendBroadcast(count)
            } catch (e: Exception) {
                // Reintentar más rápido si hay error
                handler.postDelayed(runnable, 5000)
            }
        }
    }

    private fun getPacienteId(): Int {
        val prefs = getSharedPreferences("user_data", MODE_PRIVATE)
        return prefs.getInt("Paciente_ID", 0)
    }

    private fun sendBroadcast(count: Int) {
        val intent = Intent("NOTIFICATION_UPDATE_ACTION").apply {
            putExtra("count", count)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }
}