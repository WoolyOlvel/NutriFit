package com.ascrib.nutrifit.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.Chat
import com.ascrib.nutrifit.model.Notification
import com.ascrib.nutrifit.model.PlanList

class DashboardViewModel : ViewModel() {

    fun getListPlan() = listplan
    fun getAppointmentList () = appointment
    fun getChat() = dataChat
    fun getInProgressAppointments() = appointment.filter { it.statusType == 1 }
    fun getNextAppointments() = appointment.filter { it.statusType == 2 }
    fun getPastAppointments() = appointment.filter { it.statusType in listOf(3, 4) }
    fun getNotification () = notification

    private var listplan = arrayListOf(
        PlanList(1, "Alcrya Lumina",R.drawable.perfil_prueba2,"+52 9961025841", "Dieta hipocalórica", "Un Mes" )
    )

    //Completado (1)
    //En Progreso (2)
    //Aceptado (4)
    //Sin Confirmar, Cancelado o Rechazado (3)

    private var appointment = arrayListOf(
        Appointment(
            1,
            "Nut. Wilbert Edward",
            "Presencial",
            "Faltan 2 horas para la consulta",
            R.drawable.perfil_nutri,
            "En progreso",
            1
        ),
        Appointment(
            2,
            "Nut. Alessandra Muñoz",
            "Virtual",
            "Programado a las 09:45 AM",
            R.drawable.img_log,
            "Proxima Consulta",
            2
        ),
        Appointment(
            3,
            "Nut. Rodolfo Cardenaz",
            "Presencial",
            "08:00 PM - 08:30 PM",
            R.drawable.opa,
            "Realizado",
            3
        ),
        Appointment(
            3,
            "Nut. Isabel Medina",
            "Chat",
            "Sin Información",
            R.drawable.fer,
            "Cancelado",
            4
        ),

    )


    var dataChat = arrayListOf(
        Chat(
            1,
            "Nut.Wilbert Edward",
            "Te mandaré los planes nutricionales de esta semana",
            R.drawable.perfil_nutri,
            "1 min ago",
            true,
            true,

        ),
        Chat(
            2,
            "Nut.Isabel Medina",
            "Está bien, buenas tardes",
            R.drawable.fer,
            "15 min ago",
            false,
            true
        ),


    )




    private var notification = arrayListOf(
        Notification(
            1,
            "Nut. Wilbert Edward",
            "Tu próxima cita está cerca. ¡Prepárate y anota tus preguntas!",
            R.drawable.perfil_nutri,
            "Hace 5 minutos"
        ),
        Notification(
            2,
            "Nut. Isabel Medina",
            "No olvides realizar tu actividad física programada para hoy",
            R.drawable.fer,
            "Hace 2 horas"
        ),
        Notification(
            3,
            "Nut. Alessandra Muñoz",
            "No olvides tu cita programada para mañana",
            R.drawable.img_log,
            "Ayer, 2:15pm"
        )

    )

}