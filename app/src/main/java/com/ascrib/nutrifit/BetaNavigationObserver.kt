package com.ascrib.nutrifit

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.navigation.NavController
import com.ascrib.nutrifit.api.models.BetaCheckResponse
import com.ascrib.nutrifit.R

class BetaNavigationObserver(
    private val application: Application,
    private val navController: NavController
) : LifecycleObserver {

    private var currentDestinationId: Int? = null
    private var isObserving = false

    private val liveObserver = androidx.lifecycle.Observer<BetaCheckResponse?> { betaResponse ->
        betaResponse?.let { handleBetaStatusChange(it) }
    }

    private val destListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        currentDestinationId = destination.id
    }

    fun startObserving() {
        if (!isObserving) {
            BetaMonitoringService.betaStatus.observeForever(liveObserver)
            navController.addOnDestinationChangedListener(destListener)
            isObserving = true

            // Forzar chequeo inmediato al empezar a observar
            BetaMonitoringService.forceCheck(application)
        }
    }

    fun stopObserving() {
        if (isObserving) {
            BetaMonitoringService.betaStatus.removeObserver(liveObserver)
            navController.removeOnDestinationChangedListener(destListener)
            isObserving = false
        }
    }

    fun handleBetaStatusChange(betaResponse: BetaCheckResponse) {
        try {
            // Verificar si el navController todavía está válido
            if (!navController.isValid()) return

            when {
                betaResponse.isFinished() && !isInDestination(R.id.finishedFragment) -> {
                    navigateSafely(R.id.action_global_to_finished)
                }
                betaResponse.isWaiting() && !isInDestination(R.id.waitingFragment) -> {
                    navigateSafely(R.id.action_global_to_waiting)
                }
                (betaResponse.isActive() || betaResponse.isOficial()) &&
                        (isInDestination(R.id.waitingFragment) || isInDestination(R.id.finishedFragment)) -> {
                    navigateSafely(R.id.action_bienvenidaFragment_a_loginFragment)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isInDestination(destinationId: Int): Boolean {
        return currentDestinationId == destinationId
    }

    private fun navigateSafely(actionId: Int) {
        try {
            if (navController.isValid() && navController.currentDestination != null) {
                navController.navigate(actionId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun NavController.isValid(): Boolean {
        return try {
            this.currentDestination != null
        } catch (e: Exception) {
            false
        }
    }

    fun onDestroy() {
        stopObserving()
    }
}