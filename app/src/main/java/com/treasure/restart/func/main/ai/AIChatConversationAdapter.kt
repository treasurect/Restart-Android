package com.treasure.restart.func.main.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treasure.restart.bean.AIChatConversation
import com.treasure.restart.databinding.ItemAiConversationBinding

class AIChatConversationAdapter(
    private val onItemClick: (AIChatConversation) -> Unit,
    private val onItemLongClick: (AIChatConversation, android.view.View) -> Unit
) : RecyclerView.Adapter<AIChatConversationAdapter.ViewHolder>() {

    private val conversations = mutableListOf<AIChatConversation>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {

        val binding = ItemAiConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size

    fun setData(list: List<AIChatConversation>) {
        conversations.clear()
        conversations.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemAiConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AIChatConversation) {

            binding.tvConversationTitle.text = item.title

            // 普通点击
            binding.root.setOnClickListener {
                if (item.id != -1L)
                    onItemClick(item)
            }

            // 长按
            binding.root.setOnLongClickListener {
                if (item.id != -1L)
                    onItemLongClick(item, binding.root)
                // 返回 true，表示长按事件已经消费
                true
            }
        }
    }
}