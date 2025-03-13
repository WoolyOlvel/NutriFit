package com.ascrib.nutrifit.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.PlanList

class DashboardViewModel : ViewModel() {

    fun getListPlan() = listplan
    fun getAppointmentList () = appointment

    private var listplan = arrayListOf(
        PlanList(1, "Alcrya Lumina",R.drawable.perfil_prueba2,"+52 9961025841", "Dieta hipocalórica", "Un Mes" )
    )

    //Completado (1)
    //En Progreso (2)

    private var appointment = arrayListOf(
        Appointment(
            1,
            "Nut. Wilbert Edward",
            "Presencial",
            "1 hora restante",
            R.drawable.big_logo,
            "En progreso",
            2
        ),
        Appointment(
            1,
            "Nut. Alessandra Muñoz",
            "Virtual",
            "Realizado a las 09:45 AM",
            R.drawable.img_log,
            "Completado",
            1
        ),
        Appointment(
            1,
            "Nut. Rodolfo Cardenaz",
            "Presencial",
            "08:00 PM - 08:30 PM",
            R.drawable.opa,
            "Aceptado",
            2
        ),
        Appointment(
            1,
            "Nut. Isabel Medina",
            "Chat",
            "Quedan 4 horas para confirmar",
            R.drawable.fer,
            "Sin Confirmar",
            3
        ),

    )

}