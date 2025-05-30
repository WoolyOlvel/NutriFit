package com.ascrib.nutrifit.model

data class PlanAlimenticio(
    val Consulta_ID: Int?,
    val foto_paciente: String? = null,
    val nombre_paciente: String?,
    val apellidos: String?,
    val nombre_nutriologo: String? = null,
    val enfermedad: String? = null,
    val plan_nutricional_path: String? = null,
    val fecha_consulta: String? = null,
) {
    // Propiedad computada para el nombre completo
    val nombreCompleto: String
        get() = "$nombre_paciente $apellidos"
    val especial: String
        get() = "Dieta Especial Para: $enfermedad"
    val lic : String
        get() = "Dieta Recetada Por: \nNut $nombre_nutriologo"
    val fecha:  String
        get() = "Fecha Consulta: $fecha_consulta"
}
