package com.treasure.restart.func.main.moment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treasure.basic.helper.AppEvent
import com.treasure.basic.helper.AppEventManager
import com.treasure.basic.utils.ToastUtils
import com.treasure.restart.R
import com.treasure.basic.base.BaseActivity
import com.treasure.restart.func.FileUploadViewModel
import com.treasure.restart.func.media.ImagePreviewActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class MomentPublishActivity : BaseActivity() {

    companion object {
        private const val MAX_IMAGE_COUNT = 9

        const val EXTRA_IMAGES = "extra_images"

        fun start(activity: AppCompatActivity, images: List<Uri> = emptyList()) {
            val intent = Intent(activity, MomentPublishActivity::class.java)
            intent.putStringArrayListExtra(
                EXTRA_IMAGES, ArrayList(images.map { it.toString() })
            )
            activity.startActivity(intent)
        }
    }

    private val fileUploadViewModel: FileUploadViewModel by viewModels()
    private val momentViewModel: MomentViewModel by viewModels()

    private lateinit var rvImages: RecyclerView
    private lateinit var etContent: EditText
    private lateinit var tvPublish: TextView
    private lateinit var tvCancel: TextView

    private lateinit var adapter: MomentPublishImageAdapter

    private val images = ArrayList<Uri>()

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->

        if (uris.isEmpty()) {
            return@registerForActivityResult
        }

        val remain = MAX_IMAGE_COUNT - images.size

        if (remain <= 0) {
            return@registerForActivityResult
        }

        images.addAll(
            uris.take(remain)
        )

        adapter.setImages(images)
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_publish)

        initImages()

        initView()
        initRecyclerView()
        initClick()
        initObserver()
    }

    /**
     * 接收外部传入的图片
     */
    private fun initImages() {
        val imageStrings = intent.getStringArrayListExtra(EXTRA_IMAGES) ?: return
        images.clear()
        images.addAll(
            imageStrings.take(MAX_IMAGE_COUNT)
                .mapNotNull { runCatching { Uri.parse(it) }.getOrNull() })
    }

    private fun initView() {

        rvImages = findViewById(R.id.rvImages)

        etContent = findViewById(R.id.etContent)

        tvPublish = findViewById(R.id.tvPublish)

        tvCancel = findViewById(R.id.tvCancel)
    }

    private fun initRecyclerView() {

        adapter = MomentPublishImageAdapter(

            onClick = { position ->
                openPreview(position)
            },

            onDelete = { position ->

                if (position in images.indices) {

                    images.removeAt(position)

                    adapter.setImages(images)
                }
            },

            onAdd = {

                if (images.size >= MAX_IMAGE_COUNT) {
                    return@MomentPublishImageAdapter
                }

                imagePicker.launch("image/*")
            })

        rvImages.layoutManager = GridLayoutManager(
            this, 3
        )

        rvImages.adapter = adapter

        // 初始化已有图片
        adapter.setImages(images)
    }

    private fun initClick() {

        tvCancel.setOnClickListener {
            finish()
        }

        tvPublish.setOnClickListener {
            publish()
        }
    }

    private fun initObserver() {
        lifecycleScope.launch {
            fileUploadViewModel.uploadResult.collectLatest {
                if (it.isNullOrEmpty()) {
                    hideLoading()
                } else {
                    momentViewModel.publishMoment(etContent.text.toString().trim(), imageUrls = it)
                }
            }
        }
        lifecycleScope.launch {
            momentViewModel.publishResult.collectLatest {
                it?.let {
                    AppEventManager.post(AppEvent.MomentPublishSuccess)
                    ToastUtils.show("动态发布成功 momentId: $it")
                    finish()
                } ?: run {
                    ToastUtils.show("动态发布失败")
                    hideLoading()
                }
            }
        }
    }

    private fun openPreview(position: Int) {
        val intent = Intent(this, ImagePreviewActivity::class.java)
        intent.putStringArrayListExtra(
            ImagePreviewActivity.EXTRA_IMAGES, ArrayList(images.map { it.toString() })
        )
        intent.putExtra(ImagePreviewActivity.EXTRA_POSITION, position)
        startActivity(intent)
    }

    private fun publish() {
        showLoading("发布中...")
        if (images.isEmpty()) {
            momentViewModel.publishMoment(etContent.text.toString().trim())
        } else {
            fileUploadViewModel.uploadImage(images)
        }
    }
}