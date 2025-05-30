package com.ascrib.nutrifit.repository

import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.AuthResponse
import com.ascrib.nutrifit.api.models.LoginRequest
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class LoginRepository {

    suspend fun loginUser(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = RetrofitClient.apiService.login(request)
            processResponse(response)
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun autoLogin(token: String): Result<AuthResponse> {
        return try {
            val response = RetrofitClient.apiService.autoLogin(token)
            processResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(token: String): Result<String> {
        return try {
            val response: Response<ResponseBody> = RetrofitClient.apiService.logout(token)
            if (response.isSuccessful) {
                Result.success("Sesión cerrada correctamente")
            } else {
                Result.failure(Exception("Error al cerrar sesión"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun processResponse(response: Response<AuthResponse>): Result<AuthResponse> {
        return if (response.isSuccessful) {
            val authResponse = response.body()
            if (authResponse != null) {
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Respuesta vacía del servidor"))
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            Result.failure(Exception("Fallo: $errorMsg"))
        }
    }
}
