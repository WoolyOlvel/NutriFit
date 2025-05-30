package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.api.models.ConsultaData
import com.ascrib.nutrifit.databinding.RowConsulta2Binding

class NutriologosAdapter(
    private val consultas: List<ConsultaData>,
    private val onClickListener: (ConsultaData) -> Unit
) : RecyclerView.Adapter<NutriologosAdapter.NutriologoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutriologoViewHolder {
        val binding = RowConsulta2Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NutriologoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutriologoViewHolder, position: Int) {
        holder.bind(consultas[position], onClickListener)
    }

    override fun getItemCount(): Int = consultas.size

    class NutriologoViewHolder(private val binding: RowConsulta2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(consulta: ConsultaData, clickListener: (ConsultaData) -> Unit) {
            binding.consulta = consulta
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }
}