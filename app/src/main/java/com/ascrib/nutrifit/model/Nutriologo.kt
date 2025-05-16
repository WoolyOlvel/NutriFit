package com.ascrib.nutrifit.model
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Nutriologo(
    var user_id_nutriologo: Int?,
    var foto: String?,
    var nombre_nutriologo: String?,
    var apellido_nutriologo: String?,
    var modalidad: String?,
    var disponibilidad: String?,
    var especialidad: String?,

): Parcelable {
    // Propiedad computada para el nombre completo
    val nombreCompleto: String
        get() = " Nut. $nombre_nutriologo $apellido_nutriologo"

}
