package com.treasure.restart.func.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.treasure.basic.base.BaseFragment
import com.treasure.basic.utils.DoubleClickUtilsKt.setDebouncedClickListener
import com.treasure.restart.databinding.FragmentMomentsMixBinding
import com.treasure.restart.func.main.album.AlbumFragment
import com.treasure.restart.func.main.moment.MomentListFragment

class MomentsMixFragment : BaseFragment() {
    private lateinit var binding: FragmentMomentsMixBinding
    private val fragments: ArrayList<Fragment> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, b: Bundle?): View {
        binding = FragmentMomentsMixBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun lazyInit(view: View?) {
        initTabLayout()
    }

    private fun initTabLayout() {
        fragments.add(MomentListFragment())
        fragments.add(AlbumFragment())
        val tabType = arrayOf("Moments", "Albums")
        binding.viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabType.size

            override fun createFragment(position: Int): Fragment {
                return fragments.getOrNull(position) ?: fragments[0]
            }
        }
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tvTabMoments.isChecked = position == 0
                binding.tvTabMomentsText.setTextColor(if (position == 0) Color.WHITE else Color.BLACK)
                binding.tvTabAlbum.isChecked = position != 0
                binding.tvTabAlbumText.setTextColor(if (position != 0) Color.WHITE else Color.BLACK)
            }
        })
        binding.tvTabMoments.setDebouncedClickListener { binding.viewpager.currentItem = 0 }
        binding.tvTabAlbum.setDebouncedClickListener { binding.viewpager.currentItem = 1 }
    }

}