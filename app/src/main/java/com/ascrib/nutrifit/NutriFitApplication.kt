package com.ascrib.nutrifit

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.ascrib.nutrifit.api.models.BetaCheckResponse
import com.ascrib.nutrifit.ui.form.FormActivity

class NutriFitApplication : Application() {

    private var betaNavigationObserver: BetaNavigationObserver? = null
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        startBetaMonitoringService()
        setupGlobalBetaObserver()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            currentActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {
            currentActivity = activity
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
        }

        override fun onActivityPaused(activity: Activity) {
            // No cambiar currentActivity aquí para evitar race conditions
        }

        override fun onActivityStopped(activity: Activity) {
            if (currentActivity == activity) {
                currentActivity = null
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            if (currentActivity == activity) {
                currentActivity = null
            }
            // Limpiar observer si la FormActivity se destruye
            if (activity is FormActivity) {
                setCurrentBetaObserver(null)
            }
        }
    }

    private fun setupGlobalBetaObserver() {
        BetaMonitoringService.betaStatus.observeForever(Observer { betaResponse ->
            betaResponse?.let { handleGlobalBetaStatusChange(it) }
        })
    }

    private fun handleGlobalBetaStatusChange(betaResponse: BetaCheckResponse) {
        // Verificar si estamos en FormActivity
        if (isFormActivityVisible()) {
            betaNavigationObserver?.handleBetaStatusChange(betaResponse)
            return
        }

        // Si no estamos en FormActivity, manejar redirecciones globales
        when {
            betaResponse.isFinished() -> redirectToFinished()
            betaResponse.isWaiting() -> redirectToWaiting()
            betaResponse.isActive() || betaResponse.isOficial() -> {
                // Si estamos en Dashboard y el estado cambia a activo/oficial
                if (isDashboardActivityVisible()) {
                    // No hacer nada o manejar según necesidad
                }
            }
        }
    }

    private fun isFormActivityVisible(): Boolean {
        return currentActivity is FormActivity && !currentActivity!!.isFinishing
    }

    private fun isDashboardActivityVisible(): Boolean {
        return currentActivity is com.ascrib.nutrifit.ui.dashboard.DashboardActivity &&
                !currentActivity!!.isFinishing
    }

    private fun redirectToFinished() {
        val intent = Intent(this, FormActivity::class.java).apply {
            putExtra("destination", "finished")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun redirectToWaiting() {
        val intent = Intent(this, FormActivity::class.java).apply {
            putExtra("destination", "waiting")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    fun setCurrentBetaObserver(observer: BetaNavigationObserver?) {
        betaNavigationObserver = observer
    }

    private fun startBetaMonitoringService() {
        val intent = Intent(this, BetaMonitoringService::class.java)
        startService(intent)
    }

    fun getBetaStatus() = BetaMonitoringService.betaStatus

    fun forceBetaCheck() {
        BetaMonitoringService.forceCheck(this)
    }
}