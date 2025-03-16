package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.RowAppointmentBinding
import com.ascrib.nutrifit.databinding.RowDesafioNutriBinding
import com.ascrib.nutrifit.handler.DesafioNutriHandler
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.Desafio

class DesafioAdapter (
    private val data: ArrayList<Desafio>,
    private val desafioNutriHandler: DesafioNutriHandler
) : RecyclerView.Adapter<DesafioAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DesafioAdapter.ViewHolder {
        val binding = RowDesafioNutriBinding.inflate(LayoutInflater.from(parent.context),parent, false)

        binding.handler = this
        binding.executePendingBindings()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DesafioAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: RowDesafioNutriBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(desafio: Desafio){
            binding.desafio = desafio

            if(desafio.statusType == 1){
                binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorSecondary))
            }else if (desafio.statusType == 2){
                binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorSecondary))
            }else if(desafio.statusType === 3){
                binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.orange))
            } else{
                binding.textStatusType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
            }
        }
    }

    fun DesafioNutriClicked(desafio: Desafio) {
        desafioNutriHandler.DesafioNutriClicked(desafio)
    }


}