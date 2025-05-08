package com.ascrib.nutrifit.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Cliente Retrofit para las llamadas a la API de NutriFit
 */
object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.189:8000/"
    private var token: String? = null

    /**
     * Interceptor para agregar el token de autorización a cada solicitud
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Si hay un token disponible, lo añadimos al encabezado
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    /**
     * Interceptor para registrar información de las solicitudes y respuestas
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP para las solicitudes
     */
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Instancia del cliente Retrofit
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    /**
     * API Service para realizar las llamadas
     */
    val apiService: ApiService = retrofit.create(ApiService::class.java)

    /**
     * Actualiza el token de autenticación
     * @param newToken Nuevo token de autenticación
     */
    fun updateToken(newToken: String?) {
        token = newToken
    }
}