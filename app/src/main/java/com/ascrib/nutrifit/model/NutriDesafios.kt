package com.ascrib.nutrifit.model

data class NutriDesafios(
    val NutriDesafios_ID: Int?,
    val foto: String? = null ,
    val nombre: String ? = null,
    val url: String? = null,
    val tipo: String? = null,
    val status: Int? = null,
    val estado: Int? = null,
    val fecha_creacion: String? = null,
){
    val statusText: String?
        get() = when(status) {
            1 -> "Activo"
            2 -> "Próximamente"
            else -> ""
        }

    val fecha:  String
        get() = "Juego Subido El: $fecha_creacion"

    val fechaDisplay: String
        get() = when(status) {
            1 -> "Juego Subido El: ${fecha_creacion ?: "Fecha no disponible"}"
            2 -> "En espera de publicación"
            else -> ""
        }
}
