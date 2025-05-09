package com.ascrib.nutrifit.api

import com.ascrib.nutrifit.api.models.Appointment
import com.ascrib.nutrifit.api.models.AuthResponse
import com.ascrib.nutrifit.api.models.Chat
import com.ascrib.nutrifit.api.models.ComposicionCorporal
import com.ascrib.nutrifit.api.models.Desafio
import com.ascrib.nutrifit.api.models.Divisa
import com.ascrib.nutrifit.api.models.Estatura
import com.ascrib.nutrifit.api.models.IdRequest
import com.ascrib.nutrifit.api.models.MedidaCorporal
import com.ascrib.nutrifit.api.models.Notification
import com.ascrib.nutrifit.api.models.Paciente
import com.ascrib.nutrifit.api.models.PlanList
import com.ascrib.nutrifit.api.models.RegisterRequest
import com.ascrib.nutrifit.api.models.SistemaMetrico
import com.ascrib.nutrifit.api.models.SocialLoginRequest
import com.ascrib.nutrifit.api.models.Talla
import com.ascrib.nutrifit.api.models.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


/**
 * Interfaz principal para la comunicación con la API de NutriFit
 */

interface ApiService {
// ===== AUTENTICACIÓN =====

    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/logout")
    suspend fun logout(): Response<ResponseBody>

    @GET("api/auto-login")
    suspend fun autoLogin(): Response<AuthResponse>

    // ===== AUTENTICACIÓN SOCIAL =====

    @POST("api/social-login/google")
    suspend fun googleLogin(@Body socialLoginRequest: SocialLoginRequest): Response<AuthResponse>

    @POST("api/social-login/facebook")
    suspend fun facebookLogin(@Body socialLoginRequest: SocialLoginRequest): Response<AuthResponse>

    // ===== USUARIOS =====

    @GET("api/usuarios")
    suspend fun getUsers(): Response<List<User>>

    @GET("api/usuarios/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<User>

    @POST("api/usuarios")
    suspend fun createUser(@Body user: User): Response<User>

    @PUT("api/usuarios/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: User): Response<User>

    @PATCH("api/usuarios/{id}")
    suspend fun updateUserPartially(@Path("id") userId: Int, @Body partialUser: Map<String, Any>): Response<User>

    @DELETE("api/usuarios/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<ResponseBody>

    @GET("api/user")
    suspend fun getCurrentUser(): Response<User>

    @POST("api/forgot-password")
    suspend fun recoverPassword(@Body email: String): Response<Void>

    // ===== PACIENTES =====

    @GET("api/pacientes/listar")
    suspend fun listPacientes(): Response<List<Paciente>>

    @POST("api/pacientes/guardar_editar")
    suspend fun savePaciente(@Body paciente: Paciente): Response<Paciente>

    @POST("api/pacientes/mostrar")
    suspend fun showPaciente(@Body request: IdRequest): Response<Paciente>

    @POST("api/pacientes/eliminar")
    suspend fun deletePaciente(@Body request: IdRequest): Response<ResponseBody>

    // ===== MIS PACIENTES =====

    @GET("api/misPacientes/listar")
    suspend fun listMisPacientes(): Response<List<Paciente>>

    @POST("api/misPacientes/guardar_editar")
    suspend fun saveMiPaciente(@Body paciente: Paciente): Response<Paciente>

    @POST("api/misPacientes/mostrar")
    suspend fun showMiPaciente(@Body request: IdRequest): Response<Paciente>

    @POST("api/misPacientes/eliminar")
    suspend fun deleteMiPaciente(@Body request: IdRequest): Response<ResponseBody>

    // ===== APPOINTMENTS =====

    @GET("api/appointments")
    suspend fun getAppointments(): Response<List<Appointment>>

    @GET("api/appointments/{id}")
    suspend fun getAppointment(@Path("id") appointmentId: Int): Response<Appointment>

    @POST("api/appointments")
    suspend fun createAppointment(@Body appointment: Appointment): Response<Appointment>

    @PUT("api/appoitnments/{id}")  // Nota: Hay un error en la ruta original ("appoitnments")
    suspend fun updateAppointment(@Path("id") appointmentId: Int, @Body appointment: Appointment): Response<Appointment>

    @PATCH("api/appointments/{id}")
    suspend fun updateAppointmentPartially(@Path("id") appointmentId: Int, @Body partialAppointment: Map<String, Any>): Response<Appointment>

    @DELETE("api/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") appointmentId: Int): Response<ResponseBody>

    // ===== CHATS =====

    @GET("api/chats")
    suspend fun getChats(): Response<List<Chat>>

    @GET("api/chats/{id}")
    suspend fun getChat(@Path("id") chatId: Int): Response<Chat>

    @POST("api/chats")
    suspend fun createChat(@Body chat: Chat): Response<Chat>

    @PUT("api/chats/{id}")
    suspend fun updateChat(@Path("id") chatId: Int, @Body chat: Chat): Response<Chat>

    @PATCH("api/chats/{id}")
    suspend fun updateChatPartially(@Path("id") chatId: Int, @Body partialChat: Map<String, Any>): Response<Chat>

    @DELETE("api/chats/{id}")
    suspend fun deleteChat(@Path("id") chatId: Int): Response<ResponseBody>

    // ===== DESAFÍOS =====

    @GET("api/desafios")
    suspend fun getDesafios(): Response<List<Desafio>>

    @GET("api/desafios/{id}")
    suspend fun getDesafio(@Path("id") desafioId: Int): Response<Desafio>

    @POST("api/desafios")
    suspend fun createDesafio(@Body desafio: Desafio): Response<Desafio>

    @PUT("api/desafios/{id}")
    suspend fun updateDesafio(@Path("id") desafioId: Int, @Body desafio: Desafio): Response<Desafio>

    @PATCH("api/desafios/{id}")
    suspend fun updateDesafioPartially(@Path("id") desafioId: Int, @Body partialDesafio: Map<String, Any>): Response<Desafio>

    @DELETE("api/desafios/{id}")
    suspend fun deleteDesafio(@Path("id") desafioId: Int): Response<ResponseBody>

    // ===== NOTIFICATIONS =====

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @GET("api/notifications/{id}")
    suspend fun getNotification(@Path("id") notificationId: Int): Response<Notification>

    @POST("api/notifications")
    suspend fun createNotification(@Body notification: Notification): Response<Notification>

    @PUT("api/notifications/{id}")
    suspend fun updateNotification(@Path("id") notificationId: Int, @Body notification: Notification): Response<Notification>

    @PATCH("api/notifications/{id}")
    suspend fun updateNotificationPartially(@Path("id") notificationId: Int, @Body partialNotification: Map<String, Any>): Response<Notification>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: Int): Response<ResponseBody>

    // ===== UTILIDADES =====

    // Tallas
    @GET("api/talla/listar")
    suspend fun getTallas(): Response<List<Talla>>

    @POST("api/talla/guardar_editar")
    suspend fun saveTalla(@Body talla: Talla): Response<Talla>

    @POST("api/talla/mostrar")
    suspend fun showTalla(@Body request: IdRequest): Response<Talla>

    @POST("api/talla/eliminar")
    suspend fun deleteTalla(@Body request: IdRequest): Response<ResponseBody>

    // Sistema Métrico
    @GET("api/sistema_metrico/listar")
    suspend fun getSistemaMetricos(): Response<List<SistemaMetrico>>

    @POST("api/sistema_metrico/guardar_editar")
    suspend fun saveSistemaMetrico(@Body sistemaMetrico: SistemaMetrico): Response<SistemaMetrico>

    @POST("api/sistema_metrico/mostrar")
    suspend fun showSistemaMetrico(@Body request: IdRequest): Response<SistemaMetrico>

    @POST("api/sistema_metrico/eliminar")
    suspend fun deleteSistemaMetrico(@Body request: IdRequest): Response<ResponseBody>

    // Medidas Corporales
    @GET("api/medidas_corporales/listar")
    suspend fun getMedidasCorporales(): Response<List<MedidaCorporal>>

    @POST("api/medidas_corporales/guardar_editar")
    suspend fun saveMedidaCorporal(@Body medidaCorporal: MedidaCorporal): Response<MedidaCorporal>

    @POST("api/medidas_corporales/mostrar")
    suspend fun showMedidaCorporal(@Body request: IdRequest): Response<MedidaCorporal>

    @POST("api/medidas_corporales/eliminar")
    suspend fun deleteMedidaCorporal(@Body request: IdRequest): Response<ResponseBody>

    // Composición Corporal
    @GET("api/composicion_corporal/listar")
    suspend fun getComposicionCorporal(): Response<List<ComposicionCorporal>>

    @POST("api/composicion_corporal/guardar_editar")
    suspend fun saveComposicionCorporal(@Body composicionCorporal: ComposicionCorporal): Response<ComposicionCorporal>

    @POST("api/composicion_corporal/mostrar")
    suspend fun showComposicionCorporal(@Body request: IdRequest): Response<ComposicionCorporal>

    @POST("api/composicion_corporal/eliminar")
    suspend fun deleteComposicionCorporal(@Body request: IdRequest): Response<ResponseBody>

    // Estatura
    @GET("api/estatura/listar")
    suspend fun getEstaturas(): Response<List<Estatura>>

    @POST("api/estatura/guardar_editar")
    suspend fun saveEstatura(@Body estatura: Estatura): Response<Estatura>

    @POST("api/estatura/mostrar")
    suspend fun showEstatura(@Body request: IdRequest): Response<Estatura>

    @POST("api/estatura/eliminar")
    suspend fun deleteEstatura(@Body request: IdRequest): Response<ResponseBody>

    // Divisas
    @GET("api/divisas/listar")
    suspend fun getDivisas(): Response<List<Divisa>>

    @POST("api/divisas/guardar_editar")
    suspend fun saveDivisa(@Body divisa: Divisa): Response<Divisa>

    @POST("api/divisas/mostrar")
    suspend fun showDivisa(@Body request: IdRequest): Response<Divisa>

    @POST("api/divisas/eliminar")
    suspend fun deleteDivisa(@Body request: IdRequest): Response<ResponseBody>

    // ===== PLAN LISTS =====

    @GET("api/plan_lists")
    suspend fun getPlanLists(): Response<List<PlanList>>

    @GET("api/plan_lists/{id}")
    suspend fun getPlanList(@Path("id") planListId: Int): Response<PlanList>

    @POST("api/plan_lists")
    suspend fun createPlanList(@Body planList: PlanList): Response<PlanList>

    @PUT("api/plan_lists/{id}")
    suspend fun updatePlanList(@Path("id") planListId: Int, @Body planList: PlanList): Response<PlanList>

    @PATCH("api/plan_lists/{id}")
    suspend fun updatePlanListPartially(@Path("id") planListId: Int, @Body partialPlanList: Map<String, Any>): Response<PlanList>

    @DELETE("api/plan_lists/{id}")
    suspend fun deletePlanList(@Path("id") planListId: Int): Response<ResponseBody>
}