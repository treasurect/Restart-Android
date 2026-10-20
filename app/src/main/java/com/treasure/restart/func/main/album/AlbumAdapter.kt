package com.treasure.restart.func.main.album

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treasure.basic.utils.GlideUtils
import com.treasure.basic.utils.GlideUtils.wholeUrl
import com.treasure.restart.bean.AlbumItem
import com.treasure.restart.databinding.ItemAlbumBinding

class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    private val items = mutableListOf<AlbumItem>()

    fun submitList(newItems: List<AlbumItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class AlbumViewHolder(private val binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AlbumItem) {
            binding.tvAlbumTitle.text = item.momentContent
            binding.tvAlbumCount.text = item.createUserName

            val coverUrl = if (item.imageUrl.startsWith("http", ignoreCase = true)) {
                item.imageUrl
            } else {
                item.imageUrl.wholeUrl()
            }
            GlideUtils.loadImage(binding.ivAlbumCover.context, coverUrl, binding.ivAlbumCover)
        }
    }
}
