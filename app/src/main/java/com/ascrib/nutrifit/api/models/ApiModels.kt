package com.ascrib.nutrifit.api.models

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
    val user_id: Int? = null,
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
    val paciente: Paciente
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
    val fecha_nacimiento: String?
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
