package com.ascrib.nutrifit.repository

import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.NotificacionData
import com.ascrib.nutrifit.api.models.NotificacionesResponse
import okhttp3.ResponseBody
import retrofit2.Response

class NotificacionRepository {
    suspend fun getNotificaciones(pacienteId: Int): Response<NotificacionesResponse> {
        return RetrofitClient.apiService.getNotificaciones(pacienteId)
    }

    suspend fun marcarNotificacionLeida(notificacionId: Int, pacienteId: Int): Response<ResponseBody> {
        return RetrofitClient.apiService.marcarNotificacionLeida(notificacionId, pacienteId)
    }

    suspend fun marcarTodasLeidas(pacienteId: Int): Response<ResponseBody> {
        return RetrofitClient.apiService.marcarTodasNotificacionesLeidas(pacienteId)
    }

    suspend fun eliminarNotificaciones(pacienteId: Int): Response<ResponseBody> {
        return RetrofitClient.apiService.eliminarNotificaciones(pacienteId)
    }

    suspend fun contarNotificacionesNoLeidas(pacienteId: Int): Int {
        return try {
            val response = RetrofitClient.apiService.getNotificaciones(pacienteId)
            if (response.isSuccessful && response.body()?.success == true) {
                val notificacionesData = response.body()?.notificaciones ?: emptyList()

                // Procesar notificaciones
                val notificacionesUnicas = mutableListOf<NotificacionData>()
                val notificacionesPorReservacion = notificacionesData.groupBy { it.Reservacion_ID }

                notificacionesPorReservacion.forEach { (reservacionId, notifs) ->
                    if (reservacionId != null) {
                        // Ordenar por ID descendente
                        val notifsOrdenadas = notifs.sortedByDescending { it.Notificacion_ID ?: 0 }
                        val ultimaNotif = notifsOrdenadas.firstOrNull()

                        // Si es estado 3 (completada), marcamos como leída y no la contamos
                        if (ultimaNotif?.reservacion?.estado_proximaConsulta == 3) {
                            marcarNotificacionComoLeida(ultimaNotif.Notificacion_ID ?: 0, pacienteId)

                            // Buscamos la notificación de seguimiento asociada
                            val notifSeguimiento = buscarNotificacionSeguimiento(pacienteId, reservacionId)
                            notifSeguimiento?.let { notificacionesUnicas.add(it) }
                        } else {
                            // Para otros estados, tomar la más reciente
                            ultimaNotif?.let { notificacionesUnicas.add(it) }
                        }
                    }
                }

                // Filtrar solo las no leídas y contar
                notificacionesUnicas
                    .filter {
                        it.status_movil != 1 &&
                                it.estado_movil != 0 &&
                                it.reservacion?.estado_proximaConsulta != 3
                    }
                    .size
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun buscarNotificacionSeguimiento(pacienteId: Int, reservacionOriginalId: Int): NotificacionData? {
        return try {
            // Primero obtener la nueva reservación de seguimiento
            val response = RetrofitClient.apiService.verificarReservacionSeguimiento(reservacionOriginalId, pacienteId)
            if (response.isSuccessful && response.body()?.success == true) {
                val nuevaReservacionId = response.body()?.data?.paciente_id

                // Buscar la notificación de la nueva reservación
                val notificacionesResponse = RetrofitClient.apiService.getNotificaciones(pacienteId)
                if (notificacionesResponse.isSuccessful && notificacionesResponse.body()?.success == true) {
                    notificacionesResponse.body()?.notificaciones?.firstOrNull { notif ->
                        notif.Reservacion_ID == nuevaReservacionId ||
                                notif.reservacion?.motivoConsulta?.startsWith("Seguimiento:") == true
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun marcarNotificacionComoLeida(notificacionId: Int, pacienteId: Int) {
        try {
            RetrofitClient.apiService.marcarNotificacionLeida(notificacionId, pacienteId)
        } catch (e: Exception) {
            // Silenciar errores, no es crítico para la funcionalidad
        }
    }




}