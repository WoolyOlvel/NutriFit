package com.ascrib.nutrifit.handler

import com.ascrib.nutrifit.model.Appointment

interface AppointmentHandler {
    fun appointmentClicked(appointment: Appointment)

}