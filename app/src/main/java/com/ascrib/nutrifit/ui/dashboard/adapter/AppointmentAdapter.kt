package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.RowAppointmentBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.model.Appointment

class AppointmentAdapter (
    private val data: ArrayList<Appointment>,
    private val appointmentHandler: AppointmentHandler
): RecyclerView.Adapter<AppointmentAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppointmentAdapter.ViewHolder {
        val binding = RowAppointmentBinding.inflate(LayoutInflater.from(parent.context),parent, false)

        binding.handler = this
        binding.executePendingBindings()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: RowAppointmentBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(appointment: Appointment){
            binding.appointment = appointment

            if(appointment.statusType == 1){
                binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorSecondary))
            }else if (appointment.statusType == 2){
                binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.orange))
            }else if(appointment.statusType === 3){
              binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorVariant))
            } else{
                binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
            }
        }
    }

    fun onAppointmentClicked(appointment: Appointment) {
        appointmentHandler.appointmentClicked(appointment)
    }

}