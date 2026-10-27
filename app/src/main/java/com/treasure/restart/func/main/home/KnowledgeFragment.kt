package com.treasure.restart.func.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.treasure.basic.utils.DoubleClickUtilsKt.setDebouncedClickListener
import com.treasure.restart.R
import com.treasure.basic.base.BaseFragment
import com.treasure.restart.databinding.FragmentKnowledgeBinding
import com.treasure.restart.func.chat.ChatActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class HistoryPageType(val value: Int, val desc: String) {
    TYPE_CHINA(1, "china"),
    TYPE_EUROPE(2, "europe"),
    TYPE_USA(3, "usa"),
    TYPE_RUSSIA(4, "russia"),
}

class KnowledgeFragment : BaseFragment() {
    private lateinit var binding: FragmentKnowledgeBinding
    private lateinit var autoScrollJob: Job
    private val fragments: ArrayList<Fragment> = ArrayList()
    private val homeViewModel: HomeViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, b: Bundle?): View {
        binding = FragmentKnowledgeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun lazyInit(view: View?) {
        binding.ivDrawerSwitch.setDebouncedClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
            requireActivity().overridePendingTransition(
                R.anim.slide_in_left, R.anim.slide_out_right
            )
        }
        initHeader()
        initTabLayout()
        initListener()
    }

    private fun initListener() {
        binding.clSearch.setDebouncedClickListener {

        }
    }

    override fun onResume() {
        super.onResume()
        initLocArea()
    }

    private fun initLocArea() {

    }

    private fun refreshPageData() {
        fragments.clear()
        initTabLayout()
    }

    private fun initHeader() {
        val items = listOf(
            "尧舜禹，夏，商，西周，春秋，战国", "秦汉，三国，两晋，南北朝", "隋唐，五代十国，宋元明清"
        )
        binding.rvNotice.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = NoticeLoopAdapter(items)
            scrollToPosition(Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % items.size)
        }
        autoScrollJob = lifecycleScope.launch {
            while (isActive) {
                delay(16) // 控制滑动速度
                binding.rvNotice.scrollBy(0, 1)
            }
        }
        lifecycleScope.launch {
            homeViewModel.carouselList.collectLatest {
                binding.carouselView.isVisible = it.isNotEmpty()
                binding.carouselView.setItems(it)
            }
        }
        homeViewModel.loadCarousel()
    }


    private fun initTabLayout() {
        fragments.add(HomeChildFragment.getInstance(HistoryPageType.TYPE_CHINA.desc))
        fragments.add(HomeChildFragment.getInstance(HistoryPageType.TYPE_EUROPE.desc))
        fragments.add(HomeChildFragment.getInstance(HistoryPageType.TYPE_USA.desc))
        fragments.add(HomeChildFragment.getInstance(HistoryPageType.TYPE_RUSSIA.desc))
        val tabType = arrayOf("China", "Europe", "USA", "Russia")
        binding.viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabType.size

            override fun createFragment(position: Int): Fragment {
                return fragments.getOrNull(position) ?: fragments[0]
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = tabType[position]
        }.attach()
    }

    fun switchTabByIndex(tagIndex: Int) {
        binding.viewpager ?: return
        if (binding.viewpager.currentItem == tagIndex) return
        binding.viewpager.postDelayed(
            {
                binding.viewpager.setCurrentItem(tagIndex, false)
                // 更新tab选中状态
//                binding.tabLayout.getTabAt(tagIndex)?.let { tab ->
//                    // 确保tab状态正确更新
//                    for (i in 0 until binding.tabLayout.tabCount) {
//                        updateTabView(binding.tabLayout.getTabAt(i)!!, i, i == tagIndex)
//                    }
//                }

            }, 200
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::autoScrollJob.isInitialized) autoScrollJob.cancel()
    }

    class NoticeLoopAdapter(private val items: List<String>) : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int = Int.MAX_VALUE  // 无限循环

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_notice, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val realPosition = position % items.size
            val tvTitle = holder.tvTitle
            tvTitle.text = items.getOrNull(realPosition) ?: ""
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
    }
}