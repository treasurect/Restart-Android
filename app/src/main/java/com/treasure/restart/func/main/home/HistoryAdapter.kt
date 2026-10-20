package com.treasure.restart.func.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.treasure.basic.utils.GlideUtils
import com.treasure.basic.utils.GlideUtils.wholeUrl
import com.treasure.restart.bean.HistoryItem
import com.treasure.restart.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<HistoryItem>()

    fun submitList(newItems: List<HistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItem) {
            binding.tvHistoryTitle.text = item.title

            val imageViews = listOf(
                binding.ivHistoryImage1,
                binding.ivHistoryImage2,
                binding.ivHistoryImage3
            )
            val images = item.imageList.take(3)
            imageViews.forEachIndexed { index, imageView ->
                val url = images.getOrNull(index)
                if (url.isNullOrBlank()) {
                    imageView.visibility = View.GONE
                } else {
                    imageView.visibility = View.VISIBLE
                    GlideUtils.loadImage(imageView.context, url.wholeUrl(), imageView)
                }
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}

