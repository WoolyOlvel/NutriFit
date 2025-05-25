package com.ascrib.nutrifit.api.models

import com.ascrib.nutrifit.model.Notificaciones
import org.w3c.dom.Text
import java.util.Date

/**
 * Modelo para solicitudes de autenticación
 */
data class RegisterRequest(
    val nombre: String,
    val apellidos: String,
    val email: String,
    val usuario: String,
    val password: String,
    val rol_id: Int = 2,
)
/**
 * Modelo para recibir respuestas de autenticación
 */
 {
}
/**
 * Modelo para login con redes sociales
 */
data class SocialLoginRequest(
    val token: String,
    val email: String? = null,
    val name: String? = null
)
/**
 * Modelo de Paciente
 */
data class Paciente(
    val Paciente_ID: Int? = null,
    val foto: String? = null,
    val nombre: String,
    val apellidos: String,
    val email: String? = null,
    val telefono: String? = null,
    val genero: String? = null,
    val usuario: String? = null,
    val rol_id: Int? = null,
    val user_id: Int?,
    val enfermedad: String? = null,
    val status: Int? = null,
    val estado: Int? = null,
    val ciudad: String? = null,
    val localidad: String?= null,
    val edad: Int?= null,
    val fecha_nacimiento: String? = null,
    val fecha_creacion: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class ProfileResponse(
    val user: ProfileUserData
)

data class ProfileUserData(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val usuario: String
)

/**
 * Modelo para actualizar perfil de usuario
 */
data class UpdateProfileRequest(
    val nombre: String,
    val apellidos: String,
    val email: String,
    val usuario: String
)
/**
 * Modelo para crear paciente
 */
data class CreatePacienteRequest(
    val foto: String?,
    val nombre: String,
    val user_id: Int?,
    val apellidos: String,
    val email: String?,
    val telefono: String?,
    val genero: String?,
    val usuario: String?,
    val rol_id: Int?,
    val enfermedad: String?,
    val status: Boolean?,
    val estado: Boolean?,
    val ciudad: String?,
    val localidad: String?,
    val edad: Int?,
    val fecha_nacimiento: String?,
    val fecha_creacion: String?
)

data class PacienteResponse(
    val paciente: Paciente,
    val user_id: Int?
)

data class DuplicarPacienteRequest(
    val email: String,
    val user_id_nutriologo: Int
)

data class DuplicarPacienteResponse(
    val success: Boolean,
    val message: String?,
    val paciente: Paciente?,
    val nuevo: Boolean?
)

data class UpdatePacienteRequest(
    val foto: String?,
    val nombre: String,
    val apellidos: String,
    val email: String?,
    val telefono: String?,
    val genero: String?,
    val usuario: String?,
    val enfermedad: String?,
    val ciudad: String?,
    val localidad: String?,
    val edad: Int?,
    val fecha_nacimiento: String?,
    var  user_id: Int? = null
)

data class ListaNutriologosResponse(
    val success: Boolean,
    val data: List<NutriologoData>
)

data class NutriologoData(
    val user_id: Int,
    val foto: String?,
    val nombre_nutriologo: String?,
    val apellido_nutriologo: String?,
    val modalidad: String?,
    val disponibilidad: String?,
    val especialidad: String?
)

data class NutriologoDetailResponse(
    val success: Boolean,
    val data: NutriologoDetailData
)

data class NutriologoDetailData(
    val user_id: Int,
    val foto: String?,
    val nombre_nutriologo: String?,
    val apellido_nutriologo: String?,
    val modalidad: String?,
    val disponibilidad: String?,
    val especialidad: String?,
    val edad: Int?,
    val fecha_nacimiento: String?,
    val especializacion: String?,
    val experiencia: Int?,
    val pacientes_tratados: Int?,
    val horario_antencion: String?,
    val descripcion_nutriologo: String?,
    val ciudad: String?,
    val estado: String?,
    val genero: String?,
    val enfermedades_tratadas: String?
)

data class NutriologoDetailsResponse(
    val success: Boolean,
    val data: NutriologoDetailsData
)

data class NutriologoDetailsData(
    val user_id: Int,
    val foto: String?,
    val nombre_nutriologo: String?,
    val apellido_nutriologo: String?,
    val especialidad: String?,
    val edad: Int?,
    val fecha_nacimiento: String?,
    val especializacion: String?,
    val experiencia: Int?,
    val pacientes_tratados: Int?,
    val horario_antencion: String?,
    val descripcion_nutriologo: String?,
    val ciudad: String?,
    val estado: String?,
    val genero: String?,
    val telefono: String?,
    var displomados: String?,
    var profesion: String?,
    var universidad: String?,
    var descripcion_especialziacion: String?
)

data class ReservacionResponse(
    val success: Boolean,
    val data: ReservacionData
)

data class ReservacionData(
    val paciente_id: Int?,
    val user_id: Int?,
    val nombre_nutriologo: String?,
    val nombre_paciente: String?,
    val apellidos: String?,
    val telefono: String?,
    val genero: String?,
    val usuario: String?,
    val edad: Int?,
    val precio_cita: Int?,
    val motivo_consulta: String?,
    val fecha_consulta:String?,
    val motivoConsulta: String? = null,
    val origen:String?,
    val estado_proximaConsulta: Int?,
)

data class TipoConsultaResponse(
    val success: Boolean,
    val data: List<TipoConsulta>
)

data class TipoConsulta(
    val Tipo_Consulta_ID: Int,
    val Nombre: String,
    val total_pago: Double,
    val Precio: String?,
    val Duracion: String?
)

data class NotificacionesResponse(
    val success: Boolean,
    val notificaciones: List<NotificacionData>,
    val total: Int
)

data class NotificacionData(
    val Notificacion_ID: Int?,
    val Reservacion_ID: Int?,
    val Paciente_ID: Int?,
    var user_id: Int?,
    val tipo_notificacion: Int?,
    val nombre_nutriologo: String?,
    val status_movil: Int?,
    val estado_movil: Int?,
    val descripcion_mensaje: String?,
    val fecha_creacion: String?,
    val reservacion: ReservacionesData?
)
data class ReservacionesData(
    val estado_proximaConsulta: Int?,
    val origen: String?,
    val motivoConsulta: String?,

)
data class NotificacionesCountResponse(
    val success: Boolean,
    val total: Int
)

data class Consulta(
    val Consulta_ID: Int?,
    val Paciente_ID: Int?,
    val Tipo_Consulta_ID: Int?,
    val foto: String?,
    val nombre_paciente: String?,
    val apellidos: String?,
    val email: String?,
    val telefono: String?,
    val genero: String?,
    val usuario: String?,
    val edad: String?,
    val localidad: String?,
    val ciudad: String?,
    val fecha_nacimiento: String?,
    val nombre_nutriologo: String?,
    val peso: String?,
    val talla: String?,
    val cintura: String?,
    val cadera: String?,
    val gc: String?,
    val mm: String?,
    val em: String?,
    val altura: String?,
    val detalles_diagnostico: String?,
    val resultados_evaluacion: String?,
    val analisis_nutricional: String?,
    val objetivo_descripcion: String?,
    val nombre_consultorio: String?,
    val direccion_consultorio: String?,
    val total_pago: String?,
    val estado_proximaConsulta: String?,
    val fecha_consulta: String?,
    val tipo_consulta:String?,
    val fecha_creacion: String?,
    val proxima_consulta:String?,
    val fechaConsulta: String?,
    val foto_paciente: String?,
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
data class ConsultaResponse2(
    val success: Boolean,
    val data: List<ConsultaData>,
    val message: String?
)
data class GenericResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String?
)
data class ConsultaData(
    val reservacion_id: Int,
    val consulta_id: Int,
    val paciente_id: Int,
    val nutriologo_id: Int,
    val nombre_nutriologo: String?,
    val nombre_paciente: String?,
    val estado_consulta: Int?,
    val fecha_consulta: String?,
    val fecha_formateada: String?,
    val foto_nutriologo: String?,
    val foto_paciente: String?,
    val tipo_consulta: String?,
    val motivo_consulta: String?,
    val fechaConsulta: String?,
    val nombreOriginal: String? = null,
)

data class ConsultaResponse(
    val reservacion_id: Int,
    val paciente_id: Int,
    val nutriologo_id: Int,
    val nombre_nutriologo: String,
    val nombre_paciente: String,
    val estado_consulta: Int,
    val fecha_consulta: String,
    val fecha_formateada: String,
    val foto_nutriologo: String?,
    val tipo_consulta: String,
    val motivo_consulta: String?
)

data class ConsultaDetalleResponse(
    val consulta: Consulta?,
    val consulta_id: Int?,
    val reservacion: ReservacionData?,
    val tipo_consulta: TipoConsulta?,
    val foto_nutriologo: String?
)

data class PlanAlimenticioResponse(
    val Consulta_ID: Int?,
    val foto_paciente: String? = null,
    val nombre_paciente: String?,
    val apellidos: String?,
    val nombre_nutriologo: String? = null,
    val enfermedad: String? = null,
    val plan_nutricional_path: String? = null,
    val fecha_consulta: String? = null,
)

data class NutriDesafiosResponse(
    val NutriDesafios_ID: Int?,
    val foto: String? = null ,
    val nombre: String ? = null,
    val url: String? = null,
    val tipo: String? = null,
    val status: Int? = null,
    val estado: Int? = null,
    val fecha_creacion: String? = null,
)

data class LoginRequest(
    val email: String,
    val password: String,
    val remember: Boolean = true,
    val is_mobile: Boolean = true
)

data class AuthResponse(
    val message: String,
    val user: UserResponse,
    val remember_token: String? = null
)

data class UserResponse(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val usuario: String,
    val rol_id: Int,
    val activo: Int,
    val eliminado: Int,
    val role: RoleResponse?
)

data class RoleResponse(
    val id: Int,
    val nombre: String
)
