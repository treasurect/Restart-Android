package com.treasure.restart.func.main.moment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.treasure.basic.utils.GlideUtils.wholeUrl
import com.treasure.restart.R

class MomentListImageAdapter(private val images: List<String>, private val onClick: (Int, List<String>) -> Unit) : RecyclerView.Adapter<MomentListImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_moment_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageView = holder.imageView
        Glide.with(imageView).load(images[position].wholeUrl()).centerCrop().into(imageView)
        imageView.setOnClickListener { onClick(position, images) }
    }

    override fun getItemCount(): Int = images.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
}