package com.ascrib.nutrifit.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.Chat
import com.ascrib.nutrifit.model.Notification
import com.ascrib.nutrifit.model.Patient
import com.ascrib.nutrifit.model.PlanList

class DashboardViewModel : ViewModel() {

    fun getListPlan() = listplan
    fun getAppointmentList () = appointment
    fun getChat() = dataChat
    fun getAppointment() = appointment
    fun getInProgressAppointments() = appointment.filter { it.statusType == 1 }
    fun getNextAppointments() = appointment.filter { it.statusType == 2 }
    fun getPastAppointments() = appointment.filter { it.statusType in listOf(3, 4) }
    fun getNotification () = notification
    fun getPatient() = patients

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

    private var patients = arrayListOf(
        Patient(1, "Ram Kumar", "24 years old", "+963255874", R.drawable.big_square, "Male"),
        Patient(2, "Shyam Hamal", "26 years old", "+963255874", R.drawable.big_square, "Female"),

    )

    private var data = arrayListOf(
        Chat(
            1,
            "Ram Prasad",
            "Contrary to popular belief, Lorem Ipsum is not simply random text.",
            R.drawable.big_square,
            "1 min ago",
            true,
            false
        ),
        Chat(
            1,
            "Binod Sharma",
            "Contrary to popular belief",
            R.drawable.big_square,
            "15 min ago",
            false,
            true
        ),

    )

}