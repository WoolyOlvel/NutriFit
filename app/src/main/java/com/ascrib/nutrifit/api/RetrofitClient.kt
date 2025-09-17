package com.ascrib.nutrifit.api

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para pruebas locales de la API de NutriFit
 * ⚡ Usa la IP de tu máquina en la red local
 * Ejemplo:
 * - Emulador Android Studio: "http://10.0.2.2:8000/"
 * - Dispositivo físico: "http://192.168.1.100:8000/"
 */
object RetrofitClient {

    private const val BASE_URL = "http://192.168.50.220:8000/" // Cambia por tu IP local
    private var token: String? = null

    private fun getOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // Token si existe
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            // Headers comunes
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("Content-Type", "application/json")
            requestBuilder.addHeader("User-Agent", "NutriFit-Android-App")

            chain.proceed(requestBuilder.build())
        }

        // Logs solo para debug
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(80, 15, TimeUnit.MINUTES))
            .build()
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient())
            .build()
    }

    var apiService: ApiService = getRetrofit().create(ApiService::class.java)

    /**
     * Actualiza el token dinámicamente y regenera el apiService
     */
    fun updateToken(newToken: String?) {
        token = newToken
        apiService = getRetrofit().create(ApiService::class.java)
    }
}
