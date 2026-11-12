package com.treasure.restart.func.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treasure.restart.bean.FeedItem
import com.treasure.restart.databinding.ItemFeedBinding

class FeedAdapter : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    private val items = mutableListOf<FeedItem>()

    fun submitList(newItems: List<FeedItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class FeedViewHolder(private val binding: ItemFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedItem) {
            binding.tvFeedTitle.text = item.title
            binding.feedCover.setBackgroundColor(item.coverColor)
            binding.tvFeedCoverLabel.text = item.imageLabel
            binding.tvFeedAuthor.text = item.author
            binding.tvFeedLike.text = item.likeCount
        }
    }
}
