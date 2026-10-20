package com.treasure.restart.func.main.moment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treasure.basic.ContextHolder
import com.treasure.basic.utils.GlideUtils
import com.treasure.basic.utils.GlideUtils.wholeUrl
import com.treasure.restart.R
import com.treasure.restart.bean.MomentItem
import com.treasure.restart.bean.UserInfoBean

class MomentListAdapter(
    private val items: MutableList<MomentItem>,
    private val onImageClick: (Int, List<String>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MOMENT = 1
    }

    override fun getItemViewType(position: Int): Int =
        if (position == 0) TYPE_HEADER else TYPE_MOMENT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) HeaderViewHolder(
            inflater.inflate(
                R.layout.item_moment_header,
                parent,
                false
            )
        ) else MomentViewHolder(inflater.inflate(R.layout.item_moment, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind()
        } else if (holder is MomentViewHolder) {
            holder.bind(items[position - 1])
        }
    }

    override fun getItemCount(): Int = items.size + 1

    fun getMomentCount(): Int = items.size

    fun replaceData(data: List<MomentItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: List<MomentItem>) {
        if (data.isEmpty()) return
        val startPosition = items.size + 1
        items.addAll(data)
        notifyItemRangeInserted(startPosition, data.size)
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val coverImage: ImageView = view.findViewById(R.id.coverImage)
        private val avatar: ImageView = view.findViewById(R.id.avatar)
        private val nickname: TextView = view.findViewById(R.id.nickname)

        fun bind() {
            userInfo?.let {
                GlideUtils.loadImage(ContextHolder.app(), it.coverUrl.wholeUrl(), coverImage)
                GlideUtils.loadCircle(ContextHolder.app(), it.avatar.wholeUrl(), avatar)
                nickname.text = it.username
            } ?: run {
                coverImage.setImageResource(R.drawable.ic_moment_cover)
                avatar.setImageResource(R.drawable.ic_default_avatar)
                nickname.text = "我的昵称"
            }

        }
    }

    protected var userInfo: UserInfoBean? = null
    fun setHeader(userInfoBean: UserInfoBean) {
        this.userInfo = userInfoBean
        notifyItemChanged(0)
    }

    inner class MomentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val avatar: ImageView = view.findViewById(R.id.avatar)
        private val username: TextView = view.findViewById(R.id.username)
        private val content: TextView = view.findViewById(R.id.content)
        private val imageRecyclerView: RecyclerView = view.findViewById(R.id.imageRecyclerView)
        private val time: TextView = view.findViewById(R.id.time)

        fun bind(item: MomentItem) {
            GlideUtils.loadCircle(ContextHolder.app(), item.userAvatar, avatar)
            username.text = item.username
            content.text = item.content.orEmpty()
            content.visibility = if (item.content.isNullOrBlank()) View.GONE else View.VISIBLE
            time.text = formatTime(item.createdAt)

            val images = item.images.orEmpty()
            if (images.isEmpty()) {
                imageRecyclerView.visibility = View.GONE
            } else {
                imageRecyclerView.visibility = View.VISIBLE
                imageRecyclerView.layoutManager =
                    createImageLayoutManager(imageRecyclerView.context, images.size)
                imageRecyclerView.adapter = MomentListImageAdapter(images, onImageClick)
            }
        }

        private fun formatTime(value: String): String = value.replace("T", " ")

        private fun createImageLayoutManager(context: Context, count: Int): GridLayoutManager {
            val spanCount = when (count) {
                1 -> 1
                2, 4 -> 2
                else -> 3
            }
            return GridLayoutManager(context, spanCount)
        }
    }
}