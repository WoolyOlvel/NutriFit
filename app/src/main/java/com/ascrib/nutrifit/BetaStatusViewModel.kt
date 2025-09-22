package com.ascrib.nutrifit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.BetaCheckResponse
import com.ascrib.nutrifit.repository.DeviceUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.*

class BetaStatusViewModel : ViewModel() {

    private val _betaStatus = MutableLiveData<BetaCheckResponse>()
    val betaStatus: LiveData<BetaCheckResponse> = _betaStatus

    private var isMonitoring = false

    fun startMonitoring(deviceId: String) {
        if (isMonitoring) return

        isMonitoring = true
        viewModelScope.launch {
            while (isMonitoring) {
                checkBetaStatus(deviceId)
                delay(10000) // Chequear cada 10 segundos
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
    }

    private fun checkBetaStatus(deviceId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.checkBetaAccess(deviceId)
                if (response.isSuccessful) {
                    response.body()?.let { betaResponse ->
                        _betaStatus.postValue(betaResponse)
                    }
                }
            } catch (e: Exception) {
                // Silenciar errores
            }
        }
    }

    fun forceCheck(deviceId: String) {
        viewModelScope.launch {
            checkBetaStatus(deviceId)
        }
    }
}