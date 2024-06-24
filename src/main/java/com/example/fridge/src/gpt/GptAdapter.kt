package com.example.fridge.src.gpt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.fridge.chatGpt.Sender
import com.example.fridge.chatGpt.dto.ChatContent
import com.example.fridge.databinding.ItemGptSpeechBubbleBinding
import com.example.fridge.databinding.ItemUserSpeechBubbleBinding

class GptAdapter(private val onItemLongClick: (ChatContent) -> Unit) :
    ListAdapter<ChatContent, ViewHolder>(diffUtil) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == Sender.GPT) {
            GPT
        } else {
            USER
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatContent>() {
            override fun areItemsTheSame(oldItem: ChatContent, newItem: ChatContent): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ChatContent, newItem: ChatContent): Boolean {
                return oldItem == newItem
            }

        }
        const val GPT = 1
        const val USER = 0
    }

    inner class GptViewHolder(private val binding: ItemGptSpeechBubbleBinding) :
        ViewHolder(binding.root) {
        fun bind(chatContent: ChatContent) {
            binding.tvGptText.text = chatContent.content
            binding.root.setOnLongClickListener {
                onItemLongClick(chatContent)
                true
            }
        }
    }

    inner class UserViewHolder(private val binding: ItemUserSpeechBubbleBinding) :
        ViewHolder(binding.root) {
        fun bind(chatContent: ChatContent) {
            binding.tvUserText.text = chatContent.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == USER) {
            val binding = ItemUserSpeechBubbleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            UserViewHolder(binding)
        } else {
            val binding = ItemGptSpeechBubbleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            GptViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(getItem(position))
        } else if (holder is GptViewHolder) {
            holder.bind(getItem(position))
        }
    }


}