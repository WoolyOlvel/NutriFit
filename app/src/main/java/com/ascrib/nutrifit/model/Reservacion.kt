package com.ascrib.nutrifit.model

data class Reservacion(
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
    val origen:String?,
    val estado_proximaConsulta: Int?,
)
