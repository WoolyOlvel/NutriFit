package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.RowDesafioNutriBinding
import com.ascrib.nutrifit.handler.NutriDesafiosHandler
import com.ascrib.nutrifit.model.NutriDesafios

class NutriDesafiosAdapter(
    private val desafios: List<NutriDesafios>,
    private val handler: NutriDesafiosHandler
) : RecyclerView.Adapter<NutriDesafiosAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowDesafioNutriBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(desafio: NutriDesafios) {
            binding.desafio = desafio
            binding.handler = this@NutriDesafiosAdapter

            // Configurar el color del status
            val statusColor = when(desafio.status) {
                1 -> binding.root.context.getColor(R.color.colorSecondary)
                2 -> binding.root.context.getColor(R.color.orange)
                else -> binding.root.context.getColor(R.color.grey)
            }
            binding.textStatusType.setTextColor(statusColor)
            // Deshabilitar clicks si el status es 2
            binding.root.isClickable = desafio.status != 2
            binding.root.isEnabled = desafio.status != 2
            binding.root.alpha = if (desafio.status == 2) 0.6f else 1f //
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<RowDesafioNutriBinding>(
            LayoutInflater.from(parent.context),
            R.layout.row_desafio_nutri,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(desafios[position]) // Esta es la línea clave que debes cambiar
    }

    override fun getItemCount(): Int = desafios.size

    fun nutriDesafiosClicked(nutriDesafios: NutriDesafios) {
        handler.nutriDesafiosClicked(nutriDesafios)
    }
}