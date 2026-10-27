package com.treasure.restart.func.media

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.treasure.basic.base.BaseActivity
import com.treasure.restart.databinding.ActivityAlbumViewBinding
import com.treasure.restart.func.main.moment.MomentPublishActivity

class AlbumViewActivity : BaseActivity() {

    private lateinit var binding: ActivityAlbumViewBinding
    private val images = mutableListOf<AlbumImage>()
    private val selectedUris = mutableListOf<Uri>()
    private val adapter = AlbumImageAdapter { image ->
        onImageClick(image)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.albumRecycler.layoutManager = GridLayoutManager(this, 3)
        binding.albumRecycler.adapter = adapter

        binding.ivAlbumClose.setOnClickListener { finish() }
        binding.btnAlbumDone.setOnClickListener { returnSelectedImages() }

        loadImages()
    }

    private fun loadImages() {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(AlbumImage(uri))
            }
        }
        adapter.submitData(images, selectedUris)
    }

    private fun onImageClick(image: AlbumImage) {
        val selectedIndex = selectedUris.indexOf(image.uri)
        if (selectedIndex >= 0) {
            selectedUris.removeAt(selectedIndex)
        } else {
            if (selectedUris.size >= MAX_SELECT_COUNT) {
                Toast.makeText(this, "最多选择 $MAX_SELECT_COUNT 张图片", Toast.LENGTH_SHORT).show()
                return
            }
            selectedUris.add(image.uri)
        }
        adapter.submitData(images, selectedUris)
        binding.btnAlbumDone.text = "完成(${selectedUris.size}/$MAX_SELECT_COUNT)"
    }

    private fun returnSelectedImages() {
        MomentPublishActivity.start(this, selectedUris)
        finish()
    }

    companion object {
        const val MAX_SELECT_COUNT = 9
    }
}
