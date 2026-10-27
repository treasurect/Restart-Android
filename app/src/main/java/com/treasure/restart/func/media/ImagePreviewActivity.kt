package com.treasure.restart.func.media

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.treasure.basic.helper.LogHelper
import com.treasure.restart.R
import com.treasure.basic.base.BaseActivity

class ImagePreviewActivity : BaseActivity() {

    companion object {

        const val EXTRA_IMAGES =
            "extra_images"

        const val EXTRA_POSITION =
            "extra_position"
    }

    private lateinit var rvPreview: RecyclerView
    private lateinit var tvIndex: TextView
    private lateinit var tvBack: TextView

    private var currentPosition = 0

    private var images =
        ArrayList<String>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_image_preview
        )

        images = intent.getStringArrayListExtra(
                EXTRA_IMAGES
            ) ?: arrayListOf()

        LogHelper.i("Images: ${images.joinToString(",")}")

        currentPosition =
            intent.getIntExtra(
                EXTRA_POSITION,
                0
            )

        initView()
        initRecyclerView()
        initPager()
        updateIndex()
    }

    private fun initView() {

        rvPreview =
            findViewById(
                R.id.rvPreview
            )

        tvIndex =
            findViewById(
                R.id.tvIndex
            )

        tvBack =
            findViewById(
                R.id.tvBack
            )

        tvBack.setOnClickListener {
            finish()
        }
    }

    private fun initRecyclerView() {

        val layoutManager =
            LinearLayoutManager(
                this,
                RecyclerView.HORIZONTAL,
                false
            )

        rvPreview.layoutManager =
            layoutManager

        rvPreview.adapter =
            ImagePreviewAdapter(
                images
            )

        /**
         * 初始定位
         */
        rvPreview.scrollToPosition(
            currentPosition
        )
    }

    private fun initPager() {

        val snapHelper =
            PagerSnapHelper()

        snapHelper.attachToRecyclerView(
            rvPreview
        )

        rvPreview.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {

                    super.onScrollStateChanged(
                        recyclerView,
                        newState
                    )

                    if (
                        newState ==
                        RecyclerView.SCROLL_STATE_IDLE
                    ) {

                        val view =
                            snapHelper
                                .findSnapView(
                                    recyclerView.layoutManager
                                )

                        if (view != null) {

                            currentPosition =
                                recyclerView
                                    .getChildAdapterPosition(
                                        view
                                    )

                            updateIndex()
                        }
                    }
                }
            }
        )
    }

    private fun updateIndex() {

        if (images.isEmpty()) {

            tvIndex.text = ""

            return
        }

        tvIndex.text =
            "${currentPosition + 1} / ${images.size}"
    }
}