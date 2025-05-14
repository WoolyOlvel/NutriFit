package com.ascrib.nutrifit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.ascrib.nutrifit.api.RetrofitClient
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

        Statusbar.setStatusbarTheme(this, window, 0, binding.root)
        authRepository = AuthRepository(applicationContext)

        lifecycleScope.launch {
            val result = autoLogin()
            if (result.isSuccess) {
                navigateToMain()
            } else {
                navigateToForm()
            }
        }
    }

    suspend fun autoLogin(): Result<Boolean> {
        return try {
            val sharedPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            val token = sharedPrefs.getString("remember_token", null)

            if (token != null) {
                val response = RetrofitClient.apiService.autoLogin(token)
                if (response.isSuccessful && response.body()?.user != null) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Token inválido o usuario no autenticado"))
                }
            } else {
                Result.failure(Exception("Token no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
