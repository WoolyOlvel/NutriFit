package com.ascrib.nutrifit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.ascrib.nutrifit.databinding.ActivityMainBinding
import com.ascrib.nutrifit.repository.AuthRepository
import com.ascrib.nutrifit.ui.dashboard.DashboardActivity
import com.ascrib.nutrifit.ui.form.FormActivity
import com.ascrib.nutrifit.util.Statusbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Configurar la barra de estado
        Statusbar.setStatusbarTheme(this, window, 0, binding.root)

        // Inicializar el repositorio de autenticación
        authRepository = AuthRepository(applicationContext)

        // Intentar auto-login si hay token guardado
        autoLogin()
    }

    private fun autoLogin() {
        lifecycleScope.launch {
            try {
                val result = authRepository.autoLogin()
                if (result.isSuccess) {
                    // Si el auto-login es exitoso, navegar a la pantalla principal
                    navigateToMain()
                } else {
                    // Si no hay sesión activa, ir a la pantalla de formulario de login
                    navigateToForm()
                }
            } catch (e: Exception) {
                // En caso de error, ir a la pantalla de formulario de login
                navigateToForm()
            }
        }
    }

    private fun navigateToForm() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(applicationContext, FormActivity::class.java))
            finish()
        }, 500)
    }

    private fun navigateToMain() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(applicationContext, DashboardActivity::class.java))
            finish()
        }, 500)
    }
}
