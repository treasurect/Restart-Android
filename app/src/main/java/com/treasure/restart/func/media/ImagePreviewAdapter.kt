package com.treasure.restart.func.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treasure.basic.helper.LogHelper
import com.treasure.basic.utils.GlideUtils.wholeUrl
import com.treasure.basic.view.CommonImageView
import com.treasure.restart.R

class ImagePreviewAdapter(
    private val images: List<String>
) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_image_preview,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val wholeUrl = images[position].wholeUrl()
        LogHelper.i("Image Preview URL: $wholeUrl")
        holder.imageView.load(wholeUrl)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val imageView =
            view.findViewById<CommonImageView>(
                R.id.imageView
            )
    }
}
