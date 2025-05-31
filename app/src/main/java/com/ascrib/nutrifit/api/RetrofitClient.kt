package com.ascrib.nutrifit.api

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier

/**
 * Cliente Retrofit para las llamadas a la API de NutriFit
 */
object RetrofitClient {
    private const val BASE_URL = "https://nutrifitplanner.site"
    private var token: String? = null

    private fun getOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            // Agregar headers adicionales para compatibilidad
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("Content-Type", "application/json")
            requestBuilder.addHeader("User-Agent", "NutriFit-Android-App")
            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return try {
            // Intentar crear cliente con SSL estricto primero
            createSecureClient(authInterceptor, loggingInterceptor)
        } catch (e: Exception) {
            // Si falla, usar cliente con SSL relajado (solo para desarrollo)
            createRelaxedSSLClient(authInterceptor, loggingInterceptor)
        }
    }

    private fun createSecureClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            .build()
    }

    private fun createRelaxedSSLClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return try {
            // Crear trust manager que acepta certificados específicos
            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    // Verificar que el certificado es para nutrifitplanner.site
                    val cert = chain[0]
                    val subjectDN = cert.subjectDN.name
                    if (!subjectDN.contains("nutrifitplanner.site")) {
                        throw Exception("Certificado no válido para nutrifitplanner.site")
                    }
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())

            // Hostname verifier más específico
            val hostnameVerifier = HostnameVerifier { hostname, _ ->
                hostname == "nutrifitplanner.site" || hostname == "www.nutrifitplanner.site"
            }

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .hostnameVerifier(hostnameVerifier)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(ConnectionPool(80, 80, TimeUnit.MINUTES))
                .build()
        } catch (e: Exception) {
            throw RuntimeException("Error configurando SSL: ${e.message}", e)
        }
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

    // Función para verificar la conectividad SSL
    fun testSSLConnection(): Boolean {
        return try {
            val client = createSecureClient(
                Interceptor { it.proceed(it.request()) },
                HttpLoggingInterceptor()
            )
            val request = okhttp3.Request.Builder()
                .url("$BASE_URL/api/test")
                .build()
            val response = client.newCall(request).execute()
            response.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}