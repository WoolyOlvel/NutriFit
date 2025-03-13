package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ascrib.nutrifit.databinding.RowListplanBinding
import com.ascrib.nutrifit.handler.ListHandler
import com.ascrib.nutrifit.model.PlanList

class PlanListAdapter (
    private val data: ArrayList<PlanList>,
    private val listHandler: ListHandler):
    RecyclerView.Adapter<PlanListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): PlanListAdapter.ViewHolder {
        val binding = RowListplanBinding.inflate(LayoutInflater.from(parent.context), parent,false)

        binding.handler = this
        binding.executePendingBindings()

        return ViewHolder(binding)

    }

    override fun onBindViewHolder(
        holder: PlanListAdapter.ViewHolder,
        position: Int,
    ) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: RowListplanBinding):
            RecyclerView.ViewHolder(binding.root){
                fun bind(lisPlan: PlanList){
                    binding.lisPlan = lisPlan
                }

    }


}