package com.ascrib.nutrifit.api.models

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
data class AuthResponse(
    val success: Boolean,
    val token: String?,
    val user: User?
) {

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
 * Modelo de Usuario
 */
data class User(
    val id: Int? = null,
    val name: String,
    val email: String,
    val email_verified_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo genérico para solicitudes con ID
 */
data class IdRequest(
    val id: Int
)

/**
 * Modelo de Paciente
 */
data class Paciente(
    val id: Int? = null,
    val nombre: String,
    val apellido: String,
    val email: String? = null,
    val telefono: String? = null,
    val fecha_nacimiento: String? = null,
    val genero: String? = null,
    val direccion: String? = null,
    val foto: String? = null,
    val usuario_id: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Cita
 */
data class Appointment(
    val id: Int? = null,
    val fecha: String,
    val hora: String,
    val estado: String,
    val paciente_id: Int,
    val usuario_id: Int,
    val notas: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Chat
 */
data class Chat(
    val id: Int? = null,
    val mensaje: String,
    val fecha: String,
    val hora: String,
    val paciente_id: Int,
    val usuario_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Desafío
 */
data class Desafio(
    val id: Int? = null,
    val titulo: String,
    val descripcion: String,
    val fecha_inicio: String,
    val fecha_fin: String,
    val estado: String,
    val paciente_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Notificación
 */
data class Notification(
    val id: Int? = null,
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    val hora: String,
    val leida: Boolean,
    val usuario_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Talla
 */
data class Talla(
    val id: Int? = null,
    val nombre: String,
    val medida: String,
    val sistema_metrico_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Sistema Métrico
 */
data class SistemaMetrico(
    val id: Int? = null,
    val nombre: String,
    val abreviacion: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Medida Corporal
 */
data class MedidaCorporal(
    val id: Int? = null,
    val nombre: String,
    val valor: Double,
    val fecha: String,
    val paciente_id: Int,
    val talla_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Composición Corporal
 */
data class ComposicionCorporal(
    val id: Int? = null,
    val nombre: String,
    val valor: Double,
    val fecha: String,
    val paciente_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Estatura
 */
data class Estatura(
    val id: Int? = null,
    val valor: Double,
    val fecha: String,
    val paciente_id: Int,
    val sistema_metrico_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Divisa
 */
data class Divisa(
    val id: Int? = null,
    val nombre: String,
    val simbolo: String,
    val codigo: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

/**
 * Modelo de Plan List
 */
data class PlanList(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String? = null,
    val fecha_inicio: String,
    val fecha_fin: String,
    val estado: String,
    val paciente_id: Int,
    val usuario_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)