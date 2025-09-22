package com.ascrib.nutrifit

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.BetaCheckResponse
import com.ascrib.nutrifit.repository.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BetaMonitoringService : Service() {

    companion object {
        val betaStatus = MutableLiveData<BetaCheckResponse?>()
        var isMonitoring = false
        private var monitoringJob: Job? = null
        private var lastStatus: BetaCheckResponse? = null

        // Método estático para forzar un chequeo desde cualquier parte (pasa context)
        fun forceCheck(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val deviceId = DeviceUtils.getDeviceId(context)
                    val response = RetrofitClient.apiService.checkBetaAccess(deviceId)
                    if (response.isSuccessful) {
                        response.body()?.let { newStatus ->
                            // COMPARAR solo por el campo 'estado' (más robusto)
                            if (lastStatus == null || newStatus.estado != lastStatus?.estado) {
                                lastStatus = newStatus
                                betaStatus.postValue(newStatus)
                            }
                        }
                    } else {
                        // Log o manejar error: response.code()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isMonitoring) startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        isMonitoring = true
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (isMonitoring) {
                instanceCheck() // usa la versión que tiene applicationContext
                delay(15000)
            }
        }
    }

    private suspend fun instanceCheck() {
        try {
            val deviceId = DeviceUtils.getDeviceId(applicationContext)
            val response = RetrofitClient.apiService.checkBetaAccess(deviceId)
            if (response.isSuccessful) {
                response.body()?.let { newStatus ->
                    if (lastStatus == null || newStatus.estado != lastStatus?.estado) {
                        lastStatus = newStatus
                        betaStatus.postValue(newStatus)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun forceCheck() {
        // instancia del servicio en ejecución: simplemente llama a instanceCheck desde un coroutine
        CoroutineScope(Dispatchers.IO).launch { instanceCheck() }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }

    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
    }
}
