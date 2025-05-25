package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.RowListplanBinding
import com.ascrib.nutrifit.handler.PlanAlimenticioHandler
import com.ascrib.nutrifit.model.PlanAlimenticio
import com.ascrib.nutrifit.util.loadImage
import com.bumptech.glide.Glide

class PlanAlimenticioAdapter(
    private val handler: PlanAlimenticioHandler
) : ListAdapter<PlanAlimenticio, PlanAlimenticioAdapter.PlanAlimenticioViewHolder>(DiffCallback()) {
    private val maxItems = 20 // Límite de 20 items
    private val items = mutableListOf<PlanAlimenticio>()

    fun addItem(newItem: PlanAlimenticio) {
        if (items.size >= maxItems) {
            items.removeAt(0) // Elimina el más antiguo si llegamos al límite
        }
        items.add(newItem)
        submitList(items.toList()) // Notifica al adaptador del cambio
    }

    fun submitLimitedList(newList: List<PlanAlimenticio>) {
        items.clear()
        items.addAll(newList.takeLast(maxItems)) // Solo guarda los últimos 20
        submitList(items.toList())
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanAlimenticioViewHolder {
        val binding = RowListplanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanAlimenticioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanAlimenticioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanAlimenticioViewHolder(
        private val binding: RowListplanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: PlanAlimenticio) {
            binding.lisPlan = plan
            binding.handler = this@PlanAlimenticioAdapter

            Glide.with(binding.root.context)
                .load(plan.foto_paciente)
                .placeholder(R.drawable.descargar)
                .into(binding.fotoPaciente)

            binding.btnDownload.setOnClickListener {
                handler.planAlimenticioClicked(plan)
            }

            binding.executePendingBindings()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlanAlimenticio>() {
        override fun areItemsTheSame(oldItem: PlanAlimenticio, newItem: PlanAlimenticio): Boolean {
            return oldItem.Consulta_ID == newItem.Consulta_ID
        }

        override fun areContentsTheSame(oldItem: PlanAlimenticio, newItem: PlanAlimenticio): Boolean {
            return oldItem == newItem
        }
    }

    fun planAlimenticioClicked(plan: PlanAlimenticio) {
        handler.planAlimenticioClicked(plan)
    }
}