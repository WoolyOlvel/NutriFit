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

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    /**
     * Guarda el token de autenticación en SharedPreferences
     */
    private fun saveAuthToken(token: String) {
        sharedPreferences.edit().apply {
            putString("remember_token", token)
            apply()
        }
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
    suspend fun register(
        nombre: String,
        apellidos: String,
        email: String,
        usuario: String,
        password: String,
        rol_id: Int = 2
    ): Result<AuthResponse> {
        return try {
            val registerRequest =
                RegisterRequest(nombre, apellidos, email, usuario, password, rol_id)
            val response = RetrofitClient.apiService.register(registerRequest)

            // Registramos la respuesta completa para depuración
            val responseBody = response.body()
            val responseCode = response.code()
            val isSuccessful = response.isSuccessful

            if (isSuccessful && responseBody != null) {
                // Aquí está el truco: no analizamos el mensaje, solo nos fijamos en el flag success
                // y pasamos la respuesta tal como está
                Result.success(responseBody)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                Result.failure(Exception("Error en el registro: $responseCode - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recoverPassword(email: String): Result<Boolean> {
        return try {
            val response = RetrofitClient.apiService.recoverPassword(email)

            if (response.isSuccessful) {
                // Si la respuesta es exitosa, podemos devolver true para indicar que el enlace fue enviado
                Result.success(true)
            } else {
                Result.failure(Exception("Error en la recuperación de contraseña: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Inicia sesión con Google
     */
    suspend fun loginWithGoogle(
        token: String,
        email: String? = null,
        name: String? = null
    ): Result<AuthResponse> {
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
    suspend fun loginWithFacebook(
        token: String,
        email: String? = null,
        name: String? = null
    ): Result<AuthResponse> {
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
        val token = sharedPreferences.getString("remember_token", null)

        if (token != null) {
            // Actualiza el token en Retrofit
            RetrofitClient.updateToken(token)

            // Ahora hacemos la solicitud de auto-login
            val response = RetrofitClient.apiService.autoLogin()

            return if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                // Si hay error, eliminamos el token y redirigimos al login
                logout()
                Result.failure(Exception("Error en auto-login: ${response.code()}"))
            }
        } else {
            return Result.failure(Exception("No hay token guardado"))
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