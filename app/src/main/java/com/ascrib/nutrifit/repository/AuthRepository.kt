package com.ascrib.nutrifit.repository

import android.content.Context
import android.util.Log
import com.ascrib.nutrifit.api.RetrofitClient

class AuthRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService

    suspend fun autoLogin(): Result<Boolean> {
        return try {
            val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPrefs.getString("remember_token", null)

            if (token != null) {
                val response = apiService.autoLogin(token)
                val authResponse = response.body()

                if (response.isSuccessful && authResponse?.user != null) {
                    Log.d("AuthRepository", "Usuario autenticado: ${authResponse.user.nombre}")
                    Result.success(true)
                } else {
                    Log.d("AuthRepository", "Token inválido o usuario null")
                    Result.failure(Exception("Token inválido"))
                }
            } else {
                Log.d("AuthRepository", "Token no encontrado en SharedPreferences")
                Result.failure(Exception("Token no encontrado"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en autoLogin: ${e.message}")
            Result.failure(e)
        }
    }



}

