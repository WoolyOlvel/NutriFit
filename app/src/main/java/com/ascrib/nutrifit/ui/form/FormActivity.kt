package com.ascrib.nutrifit.ui.form

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.ascrib.nutrifit.BetaNavigationObserver
import com.ascrib.nutrifit.NutriFitApplication
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.ActivityFormBinding
import com.ascrib.nutrifit.util.Statusbar

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private lateinit var betaObserver: BetaNavigationObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vincula el layout con la actividad usando DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_form)

        // Aplica estilo al status bar
        Statusbar.setStatusbarTheme(this, window, 0, binding.root)

        // Inicializa la navegación
        val navController = makeNavHost()

        // Configurar el observer de navegación beta
        betaObserver = BetaNavigationObserver(application, navController)
        betaObserver.startObserving()

        // Registrar el observer en la aplicación global
        (application as NutriFitApplication).setCurrentBetaObserver(betaObserver)

        // Manejar destino inicial desde intent extras
        handleInitialDestination(intent)
    }

    // Establece el fragmento de navegación principal y retorna el NavController
    private fun makeNavHost(): androidx.navigation.NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.dashboard_nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    private fun handleInitialDestination(intent: android.content.Intent?) {
        val destination = intent?.getStringExtra("destination")
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.dashboard_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        when (destination) {
            "finished" -> {
                if (navController.currentDestination?.id != R.id.finishedFragment) {
                    navController.navigate(R.id.action_global_to_finished)
                }
            }
            "waiting" -> {
                if (navController.currentDestination?.id != R.id.waitingFragment) {
                    navController.navigate(R.id.action_global_to_waiting)
                }
            }
        }
    }

    // Cambia el color de íconos del status bar
    fun statusBarIconDark(isDark: Boolean) {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isDark
    }

    override fun onDestroy() {
        super.onDestroy()
        betaObserver.onDestroy()
        // Limpiar el observer de la aplicación global
        (application as NutriFitApplication).setCurrentBetaObserver(null)
    }
}