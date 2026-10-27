package com.treasure.restart.func.main.home

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.treasure.basic.utils.GlideUtils
import com.treasure.basic.utils.GlideUtils.wholeUrl
import com.treasure.basic.base.BaseActivity
import com.treasure.restart.bean.HistoryItem
import com.treasure.restart.databinding.ActivityHistoryDetailBinding

class HistoryDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_ITEM = "extra_history_item"
    }

    private lateinit var binding: ActivityHistoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getSerializableExtra(EXTRA_ITEM) as? HistoryItem ?: return

        binding.tvDetailTitle.text = item.title
        binding.tvDetailDesc.text = item.description
        binding.tvDetailContent.text = item.content

        binding.llDetailImages.removeAllViews()
        item.imageList.forEach { url ->
            if (url.isNotBlank()) {
                binding.llDetailImages.addView(createImageView(url))
            }
        }
    }

    private fun createImageView(url: String): ImageView {
        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8.dp() }
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        GlideUtils.loadImage(this, url.toFullUrl(), imageView)
        return imageView
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()
}

private fun String.toFullUrl(): String =
    if (startsWith("http", ignoreCase = true)) this else wholeUrl()
