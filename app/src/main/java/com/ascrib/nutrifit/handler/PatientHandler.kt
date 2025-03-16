package com.ascrib.nutrifit.handler

import com.ascrib.nutrifit.model.Patient

interface PatientHandler {
    fun patientClicked(patient: Patient)
}