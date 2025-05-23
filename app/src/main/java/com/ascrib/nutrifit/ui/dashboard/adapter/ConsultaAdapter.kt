package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.RowConsultaBinding
import com.ascrib.nutrifit.handler.ConsultaHandler
import com.ascrib.nutrifit.model.Consulta
import com.ascrib.nutrifit.ui.planList.HistoryNutriListFragment

class ConsultaAdapter(
    private val data: List<Consulta>,
    private val consultaHandler: ConsultaHandler
): RecyclerView.Adapter<ConsultaAdapter.ViewHolder>() {
    private var limitedData = data.take(10)
    fun updateData(newData: List<Consulta>) {
        // Ordenar descendente y tomar solo los 10 más recientes
        limitedData = newData.sortedByDescending { it.fecha_consulta }.take(10)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowConsultaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(limitedData[position])
    }

    override fun getItemCount(): Int = limitedData.size

    inner class ViewHolder(val binding: RowConsultaBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(consulta: Consulta) {
            binding.apply {

                root.isClickable = consulta.estado_proximaConsulta?.toIntOrNull() == 3
                root.setOnClickListener {
                    if (consulta.estado_proximaConsulta?.toIntOrNull() == 3) {
                        consultaHandler.consultaClicked(consulta)
                    }
                }
                // Convertir estado numérico a texto
                val estadoTexto = when(consulta.estado_proximaConsulta?.toIntOrNull()) {
                    1 -> "En progreso"
                    2 -> "Próxima consulta"
                    3 -> "Realizado"
                    4 -> "En espera"
                    else -> "Desconocido"
                }
                this.consulta = consulta.copy(estado_proximaConsulta = estadoTexto)
                this.handler = consultaHandler as HistoryNutriListFragment?
                executePendingBindings()
            }

            // Configurar colores según el estado
            when(consulta.estado_proximaConsulta?.toIntOrNull()) {
                1,  -> {
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.colorSecondary)
                    )
                }
                2 -> {
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.orange)
                    )
                }
                3, -> {
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.colorVariant)
                    )
                }
                4, ->{
                    binding.textStatusType.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.purple_700)
                    )
                }
            }
        }
    }
}