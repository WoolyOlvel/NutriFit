package com.ascrib.nutrifit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NutriologoInfo(
    var user_id_nutriologo: Int?,
    var foto: String?,
    var nombre_nutriologo: String?,
    var apellido_nutriologo: String?,
    var modalidad: String?,
    var disponibilidad: String?,
    var especialidad: String?,
    var edad: String?,
    var fecha_nacimiento: String?,
    var especializacion: String?,
    var experiencia: String?,
    var pacientes_tratados: String?,
    var horario_antencion: String?,
    var descripcion_nutriologo: String?,
    var ciudad: String?,
    var estado: String?,
    var genero: String?,
    var enfermedades_tratadas: String?,
): Parcelable {
    // Propiedad computada para el nombre completo
    val nombreCompleto: String
        get() = " Nut. $nombre_nutriologo $apellido_nutriologo"
    val CiudadEstado: String
        get()= "$ciudad, $estado"

}
