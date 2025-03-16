package com.ascrib.nutrifit.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.databinding.RowTimeBinding

class TimeAdapter(
    private var count: Int,
    private var deleteAppointment: deleteAppointments
) :
    RecyclerView.Adapter<TimeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeAdapter.ViewHolder {
        val binding =
            RowTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.handler = this
        binding.executePendingBindings()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeAdapter.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return count
    }

    inner class ViewHolder(val binding: RowTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    interface deleteAppointments {
        fun onDeleteTimer()
    }

    fun onTimerDelete(){
        deleteAppointment.onDeleteTimer()
    }
}