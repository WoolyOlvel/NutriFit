package com.ascrib.nutrifit.model

data class NutriologoDetalles(
    var user_id_nutriologo: Int?,
    var foto: String?,
    var nombre_nutriologo: String?,
    var apellido_nutriologo: String?,
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
    var displomados: String?,
    var telefono: String?,
    var profesion: String?,
    var universidad: String?,
    var descripcion_especialziacion: String?,

){
    // Propiedad computada para el nombre completo
    val nombreCompleto: String
        get() = " Nut. $nombre_nutriologo $apellido_nutriologo"
    val CiudadEstado: String
        get()= "$ciudad, $estado"

}

