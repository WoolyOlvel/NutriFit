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
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>

    @POST("logout")
    suspend fun logout(): Response<ResponseBody>

    @GET("auto-login")
    suspend fun autoLogin(): Response<AuthResponse>

    // ===== AUTENTICACIÓN SOCIAL =====

    @POST("api/social-login/google")
    suspend fun googleLogin(@Body socialLoginRequest: SocialLoginRequest): Response<AuthResponse>

    @POST("api/social-login/facebook")
    suspend fun facebookLogin(@Body socialLoginRequest: SocialLoginRequest): Response<AuthResponse>

    // ===== USUARIOS =====

    @GET("usuarios")
    suspend fun getUsers(): Response<List<User>>

    @GET("usuarios/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<User>

    @POST("usuarios")
    suspend fun createUser(@Body user: User): Response<User>

    @PUT("usuarios/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: User): Response<User>

    @PATCH("usuarios/{id}")
    suspend fun updateUserPartially(@Path("id") userId: Int, @Body partialUser: Map<String, Any>): Response<User>

    @DELETE("usuarios/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<ResponseBody>

    @GET("user")
    suspend fun getCurrentUser(): Response<User>

    // ===== PACIENTES =====

    @GET("pacientes/listar")
    suspend fun listPacientes(): Response<List<Paciente>>

    @POST("pacientes/guardar_editar")
    suspend fun savePaciente(@Body paciente: Paciente): Response<Paciente>

    @POST("pacientes/mostrar")
    suspend fun showPaciente(@Body request: IdRequest): Response<Paciente>

    @POST("pacientes/eliminar")
    suspend fun deletePaciente(@Body request: IdRequest): Response<ResponseBody>

    // ===== MIS PACIENTES =====

    @GET("misPacientes/listar")
    suspend fun listMisPacientes(): Response<List<Paciente>>

    @POST("misPacientes/guardar_editar")
    suspend fun saveMiPaciente(@Body paciente: Paciente): Response<Paciente>

    @POST("misPacientes/mostrar")
    suspend fun showMiPaciente(@Body request: IdRequest): Response<Paciente>

    @POST("misPacientes/eliminar")
    suspend fun deleteMiPaciente(@Body request: IdRequest): Response<ResponseBody>

    // ===== APPOINTMENTS =====

    @GET("appointments")
    suspend fun getAppointments(): Response<List<Appointment>>

    @GET("appointments/{id}")
    suspend fun getAppointment(@Path("id") appointmentId: Int): Response<Appointment>

    @POST("appointments")
    suspend fun createAppointment(@Body appointment: Appointment): Response<Appointment>

    @PUT("appoitnments/{id}")  // Nota: Hay un error en la ruta original ("appoitnments")
    suspend fun updateAppointment(@Path("id") appointmentId: Int, @Body appointment: Appointment): Response<Appointment>

    @PATCH("appointments/{id}")
    suspend fun updateAppointmentPartially(@Path("id") appointmentId: Int, @Body partialAppointment: Map<String, Any>): Response<Appointment>

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(@Path("id") appointmentId: Int): Response<ResponseBody>

    // ===== CHATS =====

    @GET("chats")
    suspend fun getChats(): Response<List<Chat>>

    @GET("chats/{id}")
    suspend fun getChat(@Path("id") chatId: Int): Response<Chat>

    @POST("chats")
    suspend fun createChat(@Body chat: Chat): Response<Chat>

    @PUT("chats/{id}")
    suspend fun updateChat(@Path("id") chatId: Int, @Body chat: Chat): Response<Chat>

    @PATCH("chats/{id}")
    suspend fun updateChatPartially(@Path("id") chatId: Int, @Body partialChat: Map<String, Any>): Response<Chat>

    @DELETE("chats/{id}")
    suspend fun deleteChat(@Path("id") chatId: Int): Response<ResponseBody>

    // ===== DESAFÍOS =====

    @GET("desafios")
    suspend fun getDesafios(): Response<List<Desafio>>

    @GET("desafios/{id}")
    suspend fun getDesafio(@Path("id") desafioId: Int): Response<Desafio>

    @POST("desafios")
    suspend fun createDesafio(@Body desafio: Desafio): Response<Desafio>

    @PUT("desafios/{id}")
    suspend fun updateDesafio(@Path("id") desafioId: Int, @Body desafio: Desafio): Response<Desafio>

    @PATCH("desafios/{id}")
    suspend fun updateDesafioPartially(@Path("id") desafioId: Int, @Body partialDesafio: Map<String, Any>): Response<Desafio>

    @DELETE("desafios/{id}")
    suspend fun deleteDesafio(@Path("id") desafioId: Int): Response<ResponseBody>

    // ===== NOTIFICATIONS =====

    @GET("notifications")
    suspend fun getNotifications(): Response<List<Notification>>

    @GET("notifications/{id}")
    suspend fun getNotification(@Path("id") notificationId: Int): Response<Notification>

    @POST("notifications")
    suspend fun createNotification(@Body notification: Notification): Response<Notification>

    @PUT("notifications/{id}")
    suspend fun updateNotification(@Path("id") notificationId: Int, @Body notification: Notification): Response<Notification>

    @PATCH("notifications/{id}")
    suspend fun updateNotificationPartially(@Path("id") notificationId: Int, @Body partialNotification: Map<String, Any>): Response<Notification>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: Int): Response<ResponseBody>

    // ===== UTILIDADES =====

    // Tallas
    @GET("talla/listar")
    suspend fun getTallas(): Response<List<Talla>>

    @POST("talla/guardar_editar")
    suspend fun saveTalla(@Body talla: Talla): Response<Talla>

    @POST("talla/mostrar")
    suspend fun showTalla(@Body request: IdRequest): Response<Talla>

    @POST("talla/eliminar")
    suspend fun deleteTalla(@Body request: IdRequest): Response<ResponseBody>

    // Sistema Métrico
    @GET("sistema_metrico/listar")
    suspend fun getSistemaMetricos(): Response<List<SistemaMetrico>>

    @POST("sistema_metrico/guardar_editar")
    suspend fun saveSistemaMetrico(@Body sistemaMetrico: SistemaMetrico): Response<SistemaMetrico>

    @POST("sistema_metrico/mostrar")
    suspend fun showSistemaMetrico(@Body request: IdRequest): Response<SistemaMetrico>

    @POST("sistema_metrico/eliminar")
    suspend fun deleteSistemaMetrico(@Body request: IdRequest): Response<ResponseBody>

    // Medidas Corporales
    @GET("medidas_corporales/listar")
    suspend fun getMedidasCorporales(): Response<List<MedidaCorporal>>

    @POST("medidas_corporales/guardar_editar")
    suspend fun saveMedidaCorporal(@Body medidaCorporal: MedidaCorporal): Response<MedidaCorporal>

    @POST("medidas_corporales/mostrar")
    suspend fun showMedidaCorporal(@Body request: IdRequest): Response<MedidaCorporal>

    @POST("medidas_corporales/eliminar")
    suspend fun deleteMedidaCorporal(@Body request: IdRequest): Response<ResponseBody>

    // Composición Corporal
    @GET("composicion_corporal/listar")
    suspend fun getComposicionCorporal(): Response<List<ComposicionCorporal>>

    @POST("composicion_corporal/guardar_editar")
    suspend fun saveComposicionCorporal(@Body composicionCorporal: ComposicionCorporal): Response<ComposicionCorporal>

    @POST("composicion_corporal/mostrar")
    suspend fun showComposicionCorporal(@Body request: IdRequest): Response<ComposicionCorporal>

    @POST("composicion_corporal/eliminar")
    suspend fun deleteComposicionCorporal(@Body request: IdRequest): Response<ResponseBody>

    // Estatura
    @GET("estatura/listar")
    suspend fun getEstaturas(): Response<List<Estatura>>

    @POST("estatura/guardar_editar")
    suspend fun saveEstatura(@Body estatura: Estatura): Response<Estatura>

    @POST("estatura/mostrar")
    suspend fun showEstatura(@Body request: IdRequest): Response<Estatura>

    @POST("estatura/eliminar")
    suspend fun deleteEstatura(@Body request: IdRequest): Response<ResponseBody>

    // Divisas
    @GET("divisas/listar")
    suspend fun getDivisas(): Response<List<Divisa>>

    @POST("divisas/guardar_editar")
    suspend fun saveDivisa(@Body divisa: Divisa): Response<Divisa>

    @POST("divisas/mostrar")
    suspend fun showDivisa(@Body request: IdRequest): Response<Divisa>

    @POST("divisas/eliminar")
    suspend fun deleteDivisa(@Body request: IdRequest): Response<ResponseBody>

    // ===== PLAN LISTS =====

    @GET("plan_lists")
    suspend fun getPlanLists(): Response<List<PlanList>>

    @GET("plan_lists/{id}")
    suspend fun getPlanList(@Path("id") planListId: Int): Response<PlanList>

    @POST("plan_lists")
    suspend fun createPlanList(@Body planList: PlanList): Response<PlanList>

    @PUT("plan_lists/{id}")
    suspend fun updatePlanList(@Path("id") planListId: Int, @Body planList: PlanList): Response<PlanList>

    @PATCH("plan_lists/{id}")
    suspend fun updatePlanListPartially(@Path("id") planListId: Int, @Body partialPlanList: Map<String, Any>): Response<PlanList>

    @DELETE("plan_lists/{id}")
    suspend fun deletePlanList(@Path("id") planListId: Int): Response<ResponseBody>
}