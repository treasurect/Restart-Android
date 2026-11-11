package com.treasure.restart.func.main.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treasure.business.bean.UserInfoBean
import com.treasure.restart.databinding.ItemProfileActionBinding
import com.treasure.restart.databinding.ItemProfileHeaderBinding

class ProfileListAdapter(
    private val onAvatarClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ProfileListItem>()

    fun setProfile(profile: UserInfoBean) {
        items.clear()
        items.add(ProfileListItem.Header(profile))
        items.add(ProfileListItem.Action("① 浏览记录", "看过的笔记"))
        items.add(ProfileListItem.Action("@ 钱包", "查看详情"))
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProfileListItem.Header -> TYPE_HEADER
            is ProfileListItem.Action -> TYPE_ACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemProfileHeaderBinding.inflate(inflater, parent, false),
                onAvatarClick
            )
            else -> ActionViewHolder(
                ItemProfileActionBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProfileListItem.Header -> (holder as HeaderViewHolder).bind(item.profile)
            is ProfileListItem.Action -> (holder as ActionViewHolder).bind(item.title, item.value)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(
        private val binding: ItemProfileHeaderBinding,
        private val onAvatarClick: (() -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: UserInfoBean) {
            val nickname = profile.nickname.orEmpty().ifBlank { "踩单车" }
            binding.tvProfileAvatar.text = nickname.firstOrNull()?.toString() ?: "踩"
            binding.tvProfileNickname.text = nickname
            binding.tvProfileUserId.text = profile.userId?.let { "小红书号：$it" } ?: "小红书号：26248013403"
            binding.tvProfileBio.text = "点击这里，填写简介"
            binding.tvFollowCount.text = "0"
            binding.tvFansCount.text = "0"
            binding.tvLikeCount.text = "0"
            binding.tvProfileAvatar.setOnClickListener { onAvatarClick?.invoke() }
        }
    }

    class ActionViewHolder(private val binding: ItemProfileActionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, value: String) {
            binding.tvProfileActionTitle.text = title
            binding.tvProfileActionValue.text = value
        }
    }

    private sealed class ProfileListItem {
        data class Header(val profile: UserInfoBean) : ProfileListItem()
        data class Action(val title: String, val value: String) : ProfileListItem()
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ACTION = 1
    }
}
