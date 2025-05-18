package com.ascrib.nutrifit.api

import okhttp3.ConnectionPool
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
    private const val BASE_URL = "http://192.168.50.221:8000/"
    private var token: String? = null

    private fun getOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(15, 15, TimeUnit.MINUTES)) // Pool de conexiones
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

    fun updateToken(newToken: String?) {
        token = newToken
        // Regenera apiService con el nuevo token
        apiService = getRetrofit().create(ApiService::class.java)
    }
}