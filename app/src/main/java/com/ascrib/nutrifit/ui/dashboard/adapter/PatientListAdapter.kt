package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.databinding.RowPatientBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.handler.PatientHandler
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.Patient

class PatientListAdapter(
    private val data: ArrayList<Patient>,
    private val patientHandler: PatientHandler
) :
    RecyclerView.Adapter<PatientListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PatientListAdapter.ViewHolder {
        val binding =
            RowPatientBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.handler = this
        binding.executePendingBindings()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientListAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: RowPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: Patient) {
            binding.patient = patient

        }
    }

    fun onPatientClicked(patient: Patient) {
        patientHandler.patientClicked(patient)
    }
}