package com.ascrib.nutrifit.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.model.PlanList

class DashboardViewModel : ViewModel() {

    fun getListPlan() = listplan


    private var listplan = arrayListOf(
        PlanList(1, "Alcrya Lumina",R.drawable.perfil_prueba2,"+52 9961025841", "Dieta hipocalórica", "Un Mes" )
    )

}