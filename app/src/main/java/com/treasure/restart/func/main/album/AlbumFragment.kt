package com.treasure.restart.func.main.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.treasure.basic.base.BaseFragment
import com.treasure.restart.databinding.FragmentAlbumBinding
import kotlinx.coroutines.launch

class AlbumFragment : BaseFragment() {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumViewModel by viewModels()
    private lateinit var adapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var showLoadingRequested = false

    override fun lazyInit(view: View?) {
        initRecyclerView()
        initRefresh()
        observeData()

        showLoadingRequested = true
        showLoading()
        viewModel.loadAlbums(refresh = true)
    }

    private fun initRecyclerView() {
        adapter = AlbumAdapter()
        binding.rvAlbum.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvAlbum.adapter = adapter
        binding.rvAlbum.itemAnimator = null
    }

    private fun initRefresh() {
        binding.smartlay.setOnRefreshListener {
            viewModel.loadAlbums(refresh = true)
        }
        binding.smartlay.setOnLoadMoreListener {
            viewModel.loadAlbums(refresh = false)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var previous: AlbumViewModel.AlbumListState? = null
                viewModel.state.collect { state ->
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
