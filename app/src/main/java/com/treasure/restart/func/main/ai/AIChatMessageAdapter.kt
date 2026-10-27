package com.treasure.restart.func.main.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.treasure.restart.bean.AIChatMessage
import com.treasure.restart.databinding.ItemAiAssistantMessageBinding
import com.treasure.restart.databinding.ItemAiUserMessageBinding

class AIChatMessageAdapter : ListAdapter<AIChatMessage, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_ASSISTANT = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AIChatMessage>() {

            // 判断两条消息是否为同一条消息
            override fun areItemsTheSame(
                oldItem: AIChatMessage,
                newItem: AIChatMessage
            ): Boolean {
                return oldItem.id == newItem.id
            }

            // 判断同一条消息的内容是否发生变化
            override fun areContentsTheSame(
                oldItem: AIChatMessage,
                newItem: AIChatMessage
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) {
            TYPE_USER
        } else {
            TYPE_ASSISTANT
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_USER -> {
                UserViewHolder(
                    ItemAiUserMessageBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }

            else -> {
                AssistantViewHolder(
                    ItemAiAssistantMessageBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)

        when (holder) {
            is UserViewHolder -> holder.bind(item)
            is AssistantViewHolder -> holder.bind(item)
        }
    }

    class UserViewHolder(
        private val binding: ItemAiUserMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AIChatMessage) {
            binding.tvUserMessage.text = item.content
        }
    }

    class AssistantViewHolder(
        private val binding: ItemAiAssistantMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AIChatMessage) {
            binding.tvAssistantMessage.text = item.content
        }
    }
}