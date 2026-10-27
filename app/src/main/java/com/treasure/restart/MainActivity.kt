package com.treasure.restart

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.treasure.basic.base.BaseActivity
import com.treasure.basic.helper.AppEvent
import com.treasure.basic.helper.AppEventManager
import com.treasure.basic.utils.DoubleClickUtilsKt.setDebouncedClickListener
import com.treasure.basic.utils.PermissionHelper
import com.treasure.basic.view.dialog.BottomDialogBuilder
import com.treasure.restart.databinding.ActivityMainBinding
import com.treasure.restart.func.login.LoginFragment
import com.treasure.restart.func.main.MomentsMixFragment
import com.treasure.restart.func.main.ai.AIChatFragment
import com.treasure.restart.func.main.home.KnowledgeFragment
import com.treasure.restart.func.main.profile.ProfileFragment
import com.treasure.restart.func.media.AlbumViewActivity
import com.treasure.restart.func.media.CameraViewActivity
import com.treasure.restart.helper.LoginManager
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragments = arrayListOf<Fragment>()
    private val navTexts = arrayListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewPager()
        initListener()
        observeAppEvents()
        updateBottomNav(0)
    }

    private fun initViewPager() {
        navTexts.apply {
            add(binding.tabAI)
            add(binding.tabMoments)
            add(binding.tabKnowledge)
            add(binding.tabProfile)
        }
        fragments.apply {
            add(AIChatFragment())
            add(MomentsMixFragment())
            add(KnowledgeFragment())
            add(if (LoginManager.isLoggedIn()) ProfileFragment() else LoginFragment())
        }
        binding.mainPager.isUserInputEnabled = false
        binding.mainPager.adapter = MainPagerAdapter(this)
        binding.mainPager.offscreenPageLimit = fragments.size
    }

    private fun initListener() {
        binding.tabAI.setDebouncedClickListener {
            binding.mainPager.setCurrentItem(navTexts.indexOf(binding.tabAI), false)
        }
        binding.tabMoments.setDebouncedClickListener {
            binding.mainPager.setCurrentItem(navTexts.indexOf(binding.tabMoments), false)
        }
        binding.tabPublish.setDebouncedClickListener {
            showPublishDialog()
        }
        binding.tabKnowledge.setDebouncedClickListener {
            binding.mainPager.setCurrentItem(navTexts.indexOf(binding.tabKnowledge), false)
        }
        binding.tabProfile.setDebouncedClickListener {
            binding.mainPager.setCurrentItem(navTexts.indexOf(binding.tabProfile), false)
        }

        binding.mainPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateBottomNav(position)
            }
        })
    }

    private fun observeAppEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppEventManager.events.collect {
                    when (it) {
                        AppEvent.LoginExpired -> {
                            LoginManager.logout()
//                            lifecycleScope.launch {
//                                delay(500.milliseconds)
//                                AppRestartHelper.restart(this@MainActivity)
//                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun updateBottomNav(selected: Int) {
        val selectedColor = Color.rgb(255, 36, 66)
        val normalColor = Color.rgb(102, 102, 102)
        navTexts.forEachIndexed { index, textView ->
            val color = if (index == selected) selectedColor else normalColor
            textView.compoundDrawableTintList = ColorStateList.valueOf(color)
            textView.setTextColor(color)
        }
    }

    private var launchingMedia = false

    private fun showPublishDialog() {
        BottomDialogBuilder(this).setLayout(R.layout.dialog_publish_action).setCancelable(true)
            .onBindView { view, dialog ->
                view?.findViewById<TextView>(R.id.btnTakePhoto)?.setOnClickListener {
                    launchingMedia = true
                    dialog.dismiss()
                    openCamera()
                }
                view?.findViewById<TextView>(R.id.btnPickAlbum)?.setOnClickListener {
                    launchingMedia = true
                    dialog.dismiss()
                    openAlbum()
                }
                view?.findViewById<TextView>(R.id.btnCancel)?.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.setOnDismissListener {
                    if (!launchingMedia) {
                        finish()
                    }
                }
            }.show()

    }

    private fun openCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        PermissionHelper.requestPermissions(this, permissions) {
            startActivity(Intent(this, CameraViewActivity::class.java))
        }
    }

    private fun openAlbum() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        PermissionHelper.requestPermissions(this, permissions) {
            startActivity(Intent(this, AlbumViewActivity::class.java))
        }
    }

    inner class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments.getOrNull(position) ?: KnowledgeFragment()
        }
    }
}
