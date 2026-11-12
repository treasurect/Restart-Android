package com.treasure.restart.func.moment

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treasure.restart.R

class MomentPublishImageAdapter(
    private val onClick: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val onAdd: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_IMAGE = 1
        private const val TYPE_ADD = 2
    }

    private val images = ArrayList<Uri>()

    fun setImages(list: List<Uri>) {
        images.clear()
        images.addAll(list)
        notifyDataSetChanged()
    }

    fun addImages(list: List<Uri>) {
        val oldSize = images.size

        images.addAll(list)

        notifyItemRangeInserted(
            oldSize,
            list.size
        )
    }

    fun removeImage(position: Int) {

        if (position !in images.indices) {
            return
        }

        images.removeAt(position)

        notifyItemRemoved(position)
        notifyItemRangeChanged(
            position,
            images.size - position
        )
    }

    fun getImages(): List<Uri> {
        return images.toList()
    }

    override fun getItemCount(): Int {
        return images.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < images.size) {
            TYPE_IMAGE
        } else {
            TYPE_ADD
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == TYPE_IMAGE) {

            val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_publish_image,
                    parent,
                    false
                )

            ImageHolder(view)

        } else {

            val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_add_image,
                    parent,
                    false
                )

            AddHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        if (holder is ImageHolder) {

            val uri = images[position]

            Glide.with(holder.image)
                .load(uri)
                .centerCrop()
                .into(holder.image)

            holder.image.setOnClickListener {
                onClick(position)
            }

            holder.delete.setOnClickListener {
                onDelete(position)
            }

        } else if (holder is AddHolder) {

            holder.itemView.setOnClickListener {
                onAdd()
            }
        }
    }

    private class ImageHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val image: ImageView =
            view.findViewById(R.id.ivImage)

        val delete: TextView =
            view.findViewById(R.id.tvDelete)
    }

    private class AddHolder(
        view: View
    ) : RecyclerView.ViewHolder(view)
}