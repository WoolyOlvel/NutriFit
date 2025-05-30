package com.ascrib.nutrifit.model

import com.ascrib.nutrifit.R
import com.google.gson.annotations.SerializedName

data class Notificaciones(
    val id: Int,
    val reservationId: Int?,
    val patientId: Int?,
    var user_id: Int?,
    val type: Int,
    val nutritionistName: String?,
    @SerializedName("consultationStatus")
    private val rawConsultationStatus: Int?,
    val origen: String?,
    val status_movil: Int,
    val estado_movil: Int,
    val message: String,
    val motivoConsulta: String? = null,
    val creationDate: String,
    var foto: Int = ConsultationStatus.from(rawConsultationStatus).iconResId,

) {
    val consultationStatus: ConsultationStatus
        get() = ConsultationStatus.from(rawConsultationStatus)

    val title: String
        get() = consultationStatus.description

    val isLeidaMovil: Boolean get() = status_movil == 1
    val isEliminadaMovil: Boolean get() = estado_movil == 1

}

enum class ConsultationStatus(
    val code: Int,
    val description: String,
    val iconResId: Int // Nuevo parámetro para el recurso de imagen
) {
    CANCELADO(
        0,
        "Cita Cancelada Por Nutriologo.",
        R.drawable.icono_reservacion_cancelado
    ),
    EN_ESPERA(
        4,
        "Cita Generada, En Espera De Confirmación Por Nutriólogo.",
        R.drawable.icono_reservacion_espera
    ),
    EN_PROGRESO(
        1,
        "Cita Confirmada. Revise Los Detalles De Cita",
        R.drawable.icono_reservacion
    ),
    PROXIMA_CONSULTA(
        2,
        "Se Ha Reservado Su Fecha Para Proxima Consulta",
        R.drawable.icono_reservacion_proxima
    ),
    REALIZADO(
        3,
        "Su Cita Ha Sido Completada",
        R.drawable.icono_reservacion_completado
    ),
    UNKNOWN(
        -1,
        "Ocurrio Un Error Al Procesar Su Cita. Intentelo Más Tarde.",
        R.drawable.icono_reservacion_error
    );

    companion object {
        fun from(code: Int?): ConsultationStatus {
            return values().firstOrNull { it.code == code } ?: UNKNOWN
        }
    }
}