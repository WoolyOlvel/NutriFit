package com.ascrib.nutrifit.ui.chat.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.databinding.RowChatHeadBinding
import com.ascrib.nutrifit.model.Chat

class ChatHeadAdapter(
    private val data: ArrayList<Chat>
) :
    RecyclerView.Adapter<ChatHeadAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowChatHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.executePendingBindings()
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.setIsRecyclable(false)
        if (holder.layoutPosition % 2 === 0) {
            holder.binding.llChat.gravity = Gravity.START
            holder.binding.tvTimeOfMessage.gravity = Gravity.START
            holder.binding.cvProfileImage.visibility = View.VISIBLE
            holder.binding.tvMessage.setBackgroundColor(Color.parseColor("#f1f1f1"))
            holder.binding.tvMessage.setTextColor(Color.parseColor("#000000"))
        } else {
            holder.binding.tvTimeOfMessage.gravity = Gravity.END
            holder.binding.cvProfileImage.visibility = View.GONE
            holder.binding.llChat.gravity = Gravity.END
            val params : LinearLayout.LayoutParams = holder.binding.tvTimeOfMessage.layoutParams as LinearLayout.LayoutParams
            params.gravity = Gravity.END
            holder.binding.tvTimeOfMessage.layoutParams = params
            holder.binding.tvMessage.setBackgroundColor(Color.parseColor("#00a3c8"))
            holder.binding.tvMessage.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: RowChatHeadBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chathead = chat
        }
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}