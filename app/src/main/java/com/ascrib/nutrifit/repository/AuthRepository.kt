package com.ascrib.nutrifit.repository

import android.content.Context
import android.content.SharedPreferences
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.AuthResponse
import com.ascrib.nutrifit.api.models.RegisterRequest
import com.ascrib.nutrifit.api.models.SocialLoginRequest
import com.ascrib.nutrifit.api.models.User

/**
 * Repositorio para manejar la autenticación con el backend
 */
class AuthRepository(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    /**
     * Guarda el token de autenticación en SharedPreferences
     */
    private fun saveAuthToken(token: String) {
        sharedPreferences.edit().apply {
            putString("auth_token", token)
            apply()
        }
        // Actualiza el token en el cliente Retrofit
        RetrofitClient.updateToken(token)
    }

    /**
     * Obtiene el token de autenticación guardado
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    /**
     * Guarda la información del usuario en SharedPreferences
     */
    private fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putInt("user_id", user.id ?: 0)
            putString("user_name", user.name)
            putString("user_email", user.email)
            apply()
        }
    }

    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = RetrofitClient.apiService.login(email, password)

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                if (authResponse.success && authResponse.token != null) {
                    // Guardar token y usuario
                    saveAuthToken(authResponse.token)
                    authResponse.user?.let { saveUser(it) }
                }
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error en la autenticación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario
     */
    suspend fun register(name: String, email: String, password: String, passwordConfirmation: String): Result<AuthResponse> {
        return try {
            val registerRequest = RegisterRequest(name, email, password, passwordConfirmation)
            val response = RetrofitClient.apiService.register(registerRequest)

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                if (authResponse.success && authResponse.token != null) {
                    // Guardar token y usuario
                    saveAuthToken(authResponse.token)
                    authResponse.user?.let { saveUser(it) }
                }
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error en el registro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión con Google
     */
    suspend fun loginWithGoogle(token: String, email: String? = null, name: String? = null): Result<AuthResponse> {
        return try {
            val request = SocialLoginRequest(token, email, name)
            val response = RetrofitClient.apiService.googleLogin(request)

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                if (authResponse.success && authResponse.token != null) {
                    // Guardar token y usuario
                    saveAuthToken(authResponse.token)
                    authResponse.user?.let { saveUser(it) }
                }
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error en autenticación con Google: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión con Facebook
     */
    suspend fun loginWithFacebook(token: String, email: String? = null, name: String? = null): Result<AuthResponse> {
        return try {
            val request = SocialLoginRequest(token, email, name)
            val response = RetrofitClient.apiService.facebookLogin(request)

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                if (authResponse.success && authResponse.token != null) {
                    // Guardar token y usuario
                    saveAuthToken(authResponse.token)
                    authResponse.user?.let { saveUser(it) }
                }
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Error en autenticación con Facebook: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Intenta auto-login con el token guardado
     */
    suspend fun autoLogin(): Result<AuthResponse> {
        val token = getAuthToken() ?: return Result.failure(Exception("No hay token guardado"))

        // Asegurarse de que el token está configurado en el cliente
        RetrofitClient.updateToken(token)

        return try {
            val response = RetrofitClient.apiService.autoLogin()

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                // Si hay error, limpiamos el token
                logout()
                Result.failure(Exception("Error en auto-login: ${response.code()}"))
            }
        } catch (e: Exception) {
            logout()
            Result.failure(e)
        }
    }

    /**
     * Cierra sesión
     */
    suspend fun logout(): Result<Boolean> {
        return try {
            val response = RetrofitClient.apiService.logout()

            // Limpiar token y datos de usuario localmente
            sharedPreferences.edit().clear().apply()
            RetrofitClient.updateToken(null)

            Result.success(true)
        } catch (e: Exception) {
            // Aún así, limpiamos los datos locales
            sharedPreferences.edit().clear().apply()
            RetrofitClient.updateToken(null)

            Result.failure(e)
        }
    }
}