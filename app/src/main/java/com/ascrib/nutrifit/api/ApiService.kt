package com.ascrib.nutrifit.api

import com.ascrib.nutrifit.api.models.AuthResponse
import com.ascrib.nutrifit.api.models.LoginRequest
import com.ascrib.nutrifit.api.models.PacienteResponse
import com.ascrib.nutrifit.api.models.ProfileResponse
import com.ascrib.nutrifit.api.models.RegisterRequest
import com.ascrib.nutrifit.api.models.SocialLoginRequest
import com.ascrib.nutrifit.api.models.UpdatePacienteRequest
import com.ascrib.nutrifit.api.models.UpdateProfileRequest
import com.ascrib.nutrifit.api.models.UserResponse


import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


/**
 * Interfaz principal para la comunicación con la API de NutriFit
 */

interface ApiService {
// ===== AUTENTICACIÓN =====

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/logout")
    suspend fun logout(
        @Header("remember-token") token: String
    ): Response<ResponseBody>

    @GET("api/auto-login")
    suspend fun autoLogin(@Header("remember-token") token: String): Response<AuthResponse>

    // ===== AUTENTICACIÓN SOCIAL =====

    @POST("api/social-login/google")
    suspend fun googleLogin(@Body socialLoginRequest: SocialLoginRequest): Response<AuthResponse>

    @POST("api/social-login/facebook")
    suspend fun facebookLogin(@Body socialLoginRequest: SocialLoginRequest): Response<AuthResponse>


    // ===== PROFILE ======

    @GET("api/users/{user_id}/profile")
    suspend fun getProfileUser(@Path("user_id") userId: Int): Response<ProfileResponse>

    @PUT("api/users/{user_id}/update")
    suspend fun updateUserProfile(
        @Path("user_id") userId: Int,
        @Body request: UpdateProfileRequest
    ): Response<AuthResponse>

    @Multipart
    @POST("api/pacientes/create")
    suspend fun createPaciente(
        @Part foto: MultipartBody.Part?,
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("email") email: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("usuario") usuario: RequestBody,
        @Part("rol_id") rol_id: RequestBody,
        @Part("enfermedad") enfermedad: RequestBody,
        @Part("status") status: RequestBody,
        @Part("estado") estado: RequestBody,
        @Part("ciudad") ciudad: RequestBody,
        @Part("localidad") localidad: RequestBody,
        @Part("edad") edad: RequestBody,
        @Part("fecha_nacimiento") fecha_nacimiento: RequestBody,
        @Part("fecha_creacion") fecha_creacion: RequestBody
    ): Response<ResponseBody>


    // En tu ApiService.kt, agrega este nuevo endpoint
    @GET("api/pacientest/por-email")
    suspend fun getPacienteByEmail(
        @Query("email") email: String
    ): Response<PacienteResponse>

    // Cambia los endpoints para que usen email en lugar de paciente_id
    @PUT("api/pacientest/update-by-email")
    suspend fun updatePacienteByEmail(
        @Query("email") email: String,
        @Body request: UpdatePacienteRequest
    ): Response<ResponseBody>

    @Multipart
    @PUT("api/pacientest/update-with-photo-by-email")
    suspend fun updatePacienteWithPhotoByEmail(
        @Query("email") email: String,
        @Part foto: MultipartBody.Part,
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("usuario") usuario: RequestBody,
        @Part("enfermedad") enfermedad: RequestBody,
        @Part("ciudad") ciudad: RequestBody,
        @Part("localidad") localidad: RequestBody,
        @Part("edad") edad: RequestBody,
        @Part("fecha_nacimiento") fecha_nacimiento: RequestBody
    ): Response<ResponseBody>

    // ===== FIN PROFILE =====

}