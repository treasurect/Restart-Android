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
import com.treasure.basic.SharedKey
import com.treasure.basic.helper.AppEvent
import com.treasure.basic.helper.AppEventManager
import com.treasure.basic.helper.AppRestartHelper
import com.treasure.basic.helper.SharePreferenceManager
import com.treasure.basic.view.dialog.BottomDialogBuilder
import com.treasure.basic.utils.PermissionHelper
import com.treasure.restart.base.BaseActivity
import com.treasure.restart.databinding.ActivityMainBinding
import com.treasure.restart.func.login.LoginFragment
import com.treasure.restart.helper.LoginManager
import com.treasure.restart.func.main.home.HomeFragment
import com.treasure.restart.func.main.moment.MomentListFragment
import com.treasure.restart.func.main.album.AlbumFragment
import com.treasure.restart.func.main.profile.ProfileFragment
import com.treasure.restart.func.media.AlbumViewActivity
import com.treasure.restart.func.media.CameraViewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var loggedIn = LoginManager.isLoggedIn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainPager.isUserInputEnabled = false
        binding.mainPager.adapter = MainPagerAdapter(this)

        binding.navHome.setOnClickListener {
            binding.mainPager.setCurrentItem(0, false)
        }
        binding.navMarket.setOnClickListener {
            binding.mainPager.setCurrentItem(1, false)
        }
        binding.navPublish.setOnClickListener {
            showPublishDialog()
        }
        binding.navMessage.setOnClickListener {
            binding.mainPager.setCurrentItem(2, false)
        }
        binding.navMe.setOnClickListener {
            binding.mainPager.setCurrentItem(3, false)
        }

        binding.mainPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateBottomNav(position)
            }
        })
        updateBottomNav(0)

        observeAppEvents()
    }

    private fun observeAppEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppEventManager.events.collect {
                    when (it) {
                        AppEvent.LoginExpired -> {
                            SharePreferenceManager.putBoolean(SharedKey.KEY_LOGGED_IN, false)
                            SharePreferenceManager.remove(SharedKey.ACCESS_TOKEN)
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

    override fun onResume() {
        super.onResume()
        val nowLoggedIn = LoginManager.isLoggedIn()
        if (nowLoggedIn != loggedIn) {
            loggedIn = nowLoggedIn
            binding.mainPager.adapter = MainPagerAdapter(this)
            binding.mainPager.setCurrentItem(3, false)
        }
    }

    private fun updateBottomNav(selected: Int) {
        val selectedColor = Color.rgb(255, 36, 66)
        val normalColor = Color.rgb(102, 102, 102)
        val navTexts = listOf(
            binding.navHome,
            binding.navMarket,
            binding.navMessage,
            binding.navMe
        )

        navTexts.forEachIndexed { index, textView ->
            val color = if (index == selected) selectedColor else normalColor
            textView.compoundDrawableTintList = ColorStateList.valueOf(color)
            textView.setTextColor(color)
        }
    }

    private var launchingMedia = false

    private fun showPublishDialog() {
        BottomDialogBuilder(this)
            .setLayout(R.layout.dialog_publish_action)
            .setCancelable(true)
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

    class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> MomentListFragment()
                2 -> AlbumFragment()
                else -> if (LoginManager.isLoggedIn()) ProfileFragment() else LoginFragment()
            }
        }
    }
}
