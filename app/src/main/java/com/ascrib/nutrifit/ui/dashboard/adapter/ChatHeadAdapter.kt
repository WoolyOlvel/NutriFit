package com.ascrib.nutrifit.ui.dashboard.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.databinding.RowChatBinding
import com.ascrib.nutrifit.databinding.RowChatCircleBinding
import com.ascrib.nutrifit.databinding.RowChatHeadBinding
import com.ascrib.nutrifit.handler.ChatHandler
import com.ascrib.nutrifit.model.Chat

class ChatHeadAdapter(
    private val data: ArrayList<Chat>,
    private val chatHandler: ChatHandler
) :
    RecyclerView.Adapter<ChatHeadAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHeadAdapter.ViewHolder {
        val binding =
            RowChatCircleBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.handler = this
        binding.executePendingBindings()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatHeadAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: RowChatCircleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
        }
    }

    fun onChatClicked(chat: Chat){
        chatHandler.onChatClicked(chat)
    }
}