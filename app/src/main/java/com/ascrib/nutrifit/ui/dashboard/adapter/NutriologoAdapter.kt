package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.media3.common.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.RowAppointmentBinding
import com.ascrib.nutrifit.handler.NutriologoHandler
import com.ascrib.nutrifit.model.Nutriologo

class NutriologoAdapter(
    private val data: List<Nutriologo>,
    private val nutriologoHandler: NutriologoHandler

): RecyclerView.Adapter<NutriologoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowAppointmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.handler = this
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(val binding: RowAppointmentBinding ): RecyclerView.ViewHolder(binding.root) {
        fun bind(nutriologo: Nutriologo) {
            binding.apply {
                this.nutriologo = nutriologo
                this.handler = this@NutriologoAdapter // Asignar el adaptador como handler
                executePendingBindings()
            }
            when(nutriologo.disponibilidad?.lowercase()) {
                "disponibles" -> {
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.colorSecondary)
                    )
                }
                "pocos cupos" -> {
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.orange)
                    )
                }
                "no disponible" -> {
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.red)
                    )
                }
                else -> {
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.colorVariant)
                    )
                }
            }
        }
    }

    fun onAppointmentClicked(nutriologo: Nutriologo) {
        if (!nutriologo.disponibilidad.equals("No Disponible", ignoreCase = true)) {
            nutriologoHandler.nutriologoClicked(nutriologo)
        }

    }
}

// Extensión para convertir Nutriologo a un modelo similar a Appointment
private fun Nutriologo.toNutriologoModel(): Nutriologo {
    return Nutriologo(
        user_id_nutriologo = this.user_id_nutriologo,
        foto = this.foto,
        especialidad = this.especialidad,
        disponibilidad = this.disponibilidad,
        nombre_nutriologo = this.nombre_nutriologo,
        apellido_nutriologo = this.apellido_nutriologo,
        modalidad = this.modalidad

    )
}