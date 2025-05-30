package com.ascrib.nutrifit.repository

import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.AuthResponse
import com.ascrib.nutrifit.api.models.RegisterRequest
import retrofit2.HttpException
import java.io.IOException

class RegisterRepository {

    suspend fun registerUser(registerRequest: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = RetrofitClient.apiService.register(registerRequest)
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception("Registro fallido: $errorMsg"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}