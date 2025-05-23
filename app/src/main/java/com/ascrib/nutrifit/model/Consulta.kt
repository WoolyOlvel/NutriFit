package com.ascrib.nutrifit.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.w3c.dom.Text
@Parcelize
data class Consulta(
    val Consulta_ID: Int? = null,
    val Reservacion_ID: Int? = null,
    val Paciente_ID: Int? = null,
    val foto: String? = null,
    val foto_nutriologo: String? = null,
    val nombre_paciente: String? = null,
    val apellidos: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val genero: String? = null,
    val usuario: String? = null,
    val edad: String? = null,
    val localidad: String? = null,
    val ciudad: String? = null,
    val fecha_nacimiento: String? = null,
    val nombre_nutriologo: String? = null,
    val peso: String? = null,
    val talla: String? = null,
    val cintura: String? = null,
    val cadera: String? = null,
    val gc: String? = null,
    val mm: String? = null,
    val em: Int? = null,
    val altura: String? = null,
    val detalles_diagnostico: String? = null,
    val resultados_evaluacion: String? = null,
    val analisis_nutricional: String? = null,
    val objetivo_descripcion: String? = null,
    val nombre_consultorio: String? = null,
    val direccion_consultorio: String? = null,
    val total_pago: String? = null,
    val estado_proximaConsulta: String? = null,
    val fecha_consulta: String? = null,
    val proxima_consulta: String?= null,
    val fecha_creacion: String?= null,
    val tipo_consulta: String? = null,
    val estado: String? = null,
    val fechaConsulta: String? = null,
    var fechaConsultaFormateada: String? = null,
    val foto_paciente: String? = null,
): Parcelable {
    // Propiedad computada para el nombre completo
    val nombreOriginal: String
        get() = " Nut. $nombre_nutriologo"

}
