package com.ascrib.nutrifit.ui.dashboard.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.databinding.RowChatBinding
import com.ascrib.nutrifit.handler.ChatHandler
import com.ascrib.nutrifit.model.Chat

class ChatAdapter(
    private val data: ArrayList<Chat>,
    private val chatHandler: ChatHandler
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ViewHolder {
        val binding =
            RowChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.handler = this
        binding.executePendingBindings()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: RowChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat

            if (chat.read){
                binding.textMessage.setTypeface(binding.textMessage.typeface,Typeface.NORMAL)
                binding.textChatName.setTypeface(binding.textMessage.typeface,Typeface.NORMAL)
                binding.textChatTime.setTypeface(binding.textMessage.typeface,Typeface.NORMAL)
            }else{
                binding.textMessage.setTypeface(binding.textMessage.typeface,Typeface.BOLD)
                binding.textChatName.setTypeface(binding.textMessage.typeface,Typeface.BOLD)
                binding.textChatTime.setTypeface(binding.textMessage.typeface,Typeface.BOLD)
            }
        }
    }

    fun onChatClicked(chat: Chat){
        chatHandler.onChatClicked(chat)
    }
}