package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.RowNotificationBinding
import com.ascrib.nutrifit.model.Notificaciones

class NotificacionesAdapter(
    private var  notificaciones: List<Notificaciones>,
    private val onItemClick: (Notificaciones) -> Unit
) : RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificacion = notificaciones[position]
        holder.bind(notificacion)

        // Cambiar el fondo si está leída o no

    }

    override fun getItemCount(): Int = notificaciones.size

    fun updateList(newList: List<Notificaciones>) {
        notificaciones = newList.filter { it.estado_movil != 0 } // Asignación directa
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: RowNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notificacion: Notificaciones) {
            binding.notificaciones = notificacion
            binding.root.setOnClickListener { onItemClick(notificacion) }
            binding.executePendingBindings()
        }
    }
}