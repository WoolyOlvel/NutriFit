package com.ascrib.nutrifit.model

data class MiProgreso(
    val Consulta_ID: Int?,
    val Paciente_ID: Int? = null,
    val nutriologo_id: Int? = null,
    val paciente_id : Int? = null,
    val gc: Int? = null,
    val mm: Int? = null,
    val fecha_creacion: String? = null,
)
