package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.api.models.ConsultaData
import com.ascrib.nutrifit.databinding.RowConsulta2Binding
import com.ascrib.nutrifit.databinding.RowConsulta3Binding

class NutriologosGAdapter (
    private val consultas: List<ConsultaData>,
    private val onClickListener: (ConsultaData) -> Unit
) : RecyclerView.Adapter<NutriologosGAdapter.NutriologosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutriologosViewHolder {
        val binding = RowConsulta3Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NutriologosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutriologosViewHolder, position: Int) {
        holder.bind(consultas[position], onClickListener)
    }

    override fun getItemCount(): Int = consultas.size

    class NutriologosViewHolder(private val binding: RowConsulta3Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(consulta: ConsultaData, clickListener: (ConsultaData) -> Unit) {
            binding.consulta = consulta
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }
}