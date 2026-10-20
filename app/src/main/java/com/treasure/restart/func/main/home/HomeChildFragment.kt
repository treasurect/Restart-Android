package com.treasure.restart.func.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.treasure.restart.R
import com.treasure.restart.base.BaseFragment
import com.treasure.restart.bean.HistoryItem
import com.treasure.restart.databinding.FragmentHomeChildBinding
import kotlinx.coroutines.launch

class HomeChildFragment : BaseFragment() {

    companion object {
        private const val ARG_PAGE_TYPE = "region"

        fun getInstance(region: String): Fragment {
            return HomeChildFragment().apply {
                arguments = Bundle().apply { putString(ARG_PAGE_TYPE, region) }
            }
        }
    }

    private lateinit var binding: FragmentHomeChildBinding
    private lateinit var adapter: HistoryAdapter

    private val homeViewModel: HomeViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private val region: String
        get() = arguments?.getString(ARG_PAGE_TYPE) ?: ""

    private var showLoadingRequested = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeChildBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun lazyInit(view: View?) {
        initRecyclerView()
        initRefresh()
        observeData()

        showLoadingRequested = true
        showLoading()
        homeViewModel.loadHistory(region, refresh = true)
    }

    private fun initRecyclerView() {
        adapter = HistoryAdapter { item -> openDetail(item) }
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter
        binding.rvList.itemAnimator = null
    }

    private fun initRefresh() {
        binding.smartlay.setOnRefreshListener {
            homeViewModel.loadHistory(region, refresh = true)
        }
        binding.smartlay.setOnLoadMoreListener {
            homeViewModel.loadHistory(region, refresh = false)
        }
    }

    private fun openDetail(item: HistoryItem) {
        startActivity(
            Intent(requireContext(), HistoryDetailActivity::class.java).apply {
                putExtra(HistoryDetailActivity.EXTRA_ITEM, item)
            }
        )
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var previous: HomeViewModel.HistoryPageState? = null
                homeViewModel.historyState(region).collect { state ->
                    adapter.submitList(state.items)
                    if (showLoadingRequested && !state.isRefreshing && !state.isLoadingMore) {
                        hideLoading()
                        showLoadingRequested = false
                    }

                    val prev = previous
                    if (prev != null) {
                        if (prev.isRefreshing && !state.isRefreshing) {
                            binding.smartlay.finishRefresh(state.error == null)
                            binding.smartlay.setNoMoreData(!state.hasMore)
                        }
                        if (prev.isLoadingMore && !state.isLoadingMore) {
                            binding.smartlay.finishLoadMore(state.error == null)
                            binding.smartlay.setNoMoreData(!state.hasMore)
                        }
                    }
                    previous = state
                }
            }
        }
    }
}
