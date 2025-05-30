package com.ascrib.nutrifit.api

import com.ascrib.nutrifit.api.models.ApiResponse
import com.ascrib.nutrifit.api.models.AuthResponse
import com.ascrib.nutrifit.api.models.ConsultaData
import com.ascrib.nutrifit.api.models.ConsultaDetalleResponse
import com.ascrib.nutrifit.api.models.ConsultaGraficas
import com.ascrib.nutrifit.api.models.ConsultaResponse
import com.ascrib.nutrifit.api.models.ConsultaResponse2
import com.ascrib.nutrifit.api.models.DuplicarPacienteRequest
import com.ascrib.nutrifit.api.models.DuplicarPacienteResponse
import com.ascrib.nutrifit.api.models.GenericResponse
import com.ascrib.nutrifit.api.models.ListaNutriologosResponse
import com.ascrib.nutrifit.api.models.LoginRequest
import com.ascrib.nutrifit.api.models.MiProgresoResponse
import com.ascrib.nutrifit.api.models.NotificacionesCountResponse
import com.ascrib.nutrifit.api.models.NotificacionesResponse
import com.ascrib.nutrifit.api.models.NutriDesafiosResponse
import com.ascrib.nutrifit.api.models.NutriologoDetailResponse
import com.ascrib.nutrifit.api.models.NutriologoDetailsResponse
import com.ascrib.nutrifit.api.models.PacienteResponse
import com.ascrib.nutrifit.api.models.PlanAlimenticioResponse
import com.ascrib.nutrifit.api.models.ProfileResponse
import com.ascrib.nutrifit.api.models.RegisterRequest
import com.ascrib.nutrifit.api.models.ReservacionData
import com.ascrib.nutrifit.api.models.ReservacionResponse
import com.ascrib.nutrifit.api.models.SocialLoginRequest
import com.ascrib.nutrifit.api.models.TipoConsultaResponse
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
        @Part("fecha_creacion") fecha_creacion: RequestBody,
        //@Part("user_id") userId: RequestBody
    ): Response<ResponseBody>


    @GET("api/pacientest/por-email")
    suspend fun getPacienteByEmail(
        @Query("email") email: String
    ): Response<PacienteResponse>

    @PUT("api/pacientest/update-by-email")
    suspend fun updatePacienteByEmail(
        @Query("email") email: String,
        @Body request: UpdatePacienteRequest
    ): Response<ResponseBody>

    @Multipart
    @POST("api/pacientest/update-with-photo-by-email")
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

    // ===== INICIO RESERVACIONES

    @GET("api/nutriologos")
    suspend fun getNutriologos(): Response<ListaNutriologosResponse>

    @GET("api/nutriologos/byId")
    suspend fun getNutriologoById(
        @Query("user_id") userId: Int
    ): Response<NutriologoDetailResponse>

    @GET("api/nutriologos/detalles/byId")
    suspend fun getNutriologoDetallesById(
        @Query("user_id") userId: Int
    ): Response<NutriologoDetailsResponse>

    @POST("api/pacientest/duplicar-para-nutriologo")
    suspend fun duplicarPaciente(
        @Query("email") email: String,
        @Query("user_id_nutriologo") userIdNutriologo: Int
    ): Response<PacienteResponse>

    @GET("api/tipo_consulta")
    suspend fun getTiposConsulta(): Response<TipoConsultaResponse>

    @POST("api/reservaciones/create")
    suspend fun createReservacion(
        @Query("Paciente_ID") pacienteId: Int,
        @Query("nombre_paciente") nombrePaciente: String,
        @Query("apellidos") apellidos: String?,
        @Query("telefono") telefono: String?,
        @Query("genero") genero: String?,
        @Query("usuario") usuario: String?,
        @Query("edad") edad: Int?,
        @Query("precio_cita") precioCita: Double,
        @Query("motivo_consulta") motivoConsulta: String,
        @Query("nombre_nutriologo") nombreNutriologo: String,
        @Query("fecha_consulta") fechaConsulta: String,
        @Query("origen") origen: String,
        @Query("estado_proximaConsulta") estadoProximaConsulta: Int,
        @Query("user_id") userId: Int // Añade este parámetro
    ): Response<ReservacionResponse>

    // === FIN RESERVACIONES

    // === INICIO NOTIFICACIONES

    @GET("api/movil/notificaciones/{pacienteId}")
    suspend fun getNotificaciones(@Path("pacienteId") pacienteId: Int): Response<NotificacionesResponse>

    @PUT("api/movil/notificaciones/marcar-leida/{notificacionId}/{pacienteId}")
    suspend fun marcarNotificacionLeida(
        @Path("notificacionId") notificacionId: Int,
        @Path("pacienteId") pacienteId: Int
    ): Response<ResponseBody>

    @PUT("api/movil/notificaciones/marcar-todas-leidas/{pacienteId}")
    suspend fun marcarTodasNotificacionesLeidas(
        @Path("pacienteId") pacienteId: Int
    ): Response<ResponseBody>

    @PUT("api/movil/notificaciones/eliminar/{pacienteId}")
    suspend fun eliminarNotificaciones(
        @Path("pacienteId") pacienteId: Int
    ): Response<ResponseBody>

    @GET("api/movil/notificaciones/contar/{pacienteId}")
    suspend fun contarNotificaciones(
        @Path("pacienteId") pacienteId: Int
    ): Response<NotificacionesCountResponse>

    @GET("api/movil/reservaciones/verificar-seguimiento/{reservacionId}/{pacienteId}")
    suspend fun verificarReservacionSeguimiento(
        @Path("reservacionId") reservacionId: Int,
        @Path("pacienteId") pacienteId: Int
    ): Response<ReservacionResponse>

    // === FIN NOTIFICACIONES

// === INICIO HISTORIAL NUTRICIONAL ====

    @GET("api/historial/consultas-por-paciente")
    suspend fun getConsultasPorPaciente(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>
    ): Response<ConsultaResponse2>

    @GET("api/historial/detalle-consulta/{consultaId}")
    suspend fun getDetalleConsulta(
        @Path("consultaId") consultaId: Int,
        @Query("pacienteIds[]") pacienteIds: List<Int>
    ): Response<ApiResponse<ConsultaDetalleResponse>>

    @GET("api/historial/consultas-por-paciente")
    suspend fun getFotoPaciente(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>
    ): Response<ConsultaResponse2>
// === FIN HISTORIAL NUTRICIONAL ====

    // === INICIO PLANES ALIMENTICIOS

    @GET("api/consulta/planes-alimenticios")
    suspend fun getPlanesAlimenticios(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>
    ): Response<ApiResponse<List<PlanAlimenticioResponse>>>

    @GET
    suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>

    // === FIN PLANES ALIMENTICIOS

    // === INICIO NUTRIDESAFIOS

    @GET("api/nutri-desafios")
    suspend fun getNutriDesafios(): Response<GenericResponse<List<NutriDesafiosResponse>>>

    // === FIN NUTRIDESAFIOS


    //==ANEXO PROFILE
    @GET("api/historial/consultas-por-paciente2")
    suspend fun getConsultasPorPaciente2(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>
    ): Response<GenericResponse<List<ConsultaData>>>

    //== FIN ANEXO PROFILE


    // == MI PROGRESO
    @GET("api/historial/consultas-por-paciente3")
    suspend fun getConsultasPorPaciente3(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>
    ): Response<ConsultaResponse2>


    @GET("api/historial/consultas-por-pacienteD")
    suspend fun getGcMmPorConsulta(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>,
        @Query("consultaIds[]") consultaIds: List<Int>
    ): Response<MiProgresoResponse>

    // == FIN MI PROGRESO


    // === INICIO PERFIL SALUD

    @GET("api/historial/consultas-por-paciente4")
    suspend fun getConsultaPorPaciente4(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>
    ): Response<ConsultaResponse2>

    @GET("api/historial/consultas-por-paciente5")
    suspend fun getConsultaPorPaciente5(
        @Query("pacienteIds[]") pacienteIds: List<Int>,
        @Query("nutriologoIds[]") nutriologoIds: List<Int>
    ): Response<ConsultaGraficas>

    // En tu ApiService.kt, actualiza este método:

//    @GET("api/historial/consultas-por-paciente5")
//    suspend fun getConsultaPorPaciente5(
//        @Query("pacienteIds[]") pacienteIds: List<Int>,
//        @Query("nutriologoIds[]") nutriologoIds: List<Int>
//    ): Response<ConsultaGraficasResponse> // Cambiado para incluir el wrapper con success/data/message

    // === FIN PERFIL SALUD


}