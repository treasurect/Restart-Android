package com.treasure.restart.func.main.moment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.treasure.basic.ContextHolder
import com.treasure.basic.helper.AppEvent
import com.treasure.basic.helper.AppEventManager
import com.treasure.basic.utils.GlideUtils
import com.treasure.basic.utils.GlideUtils.wholeUrl
import com.treasure.basic.utils.ToastUtils
import com.treasure.restart.R
import com.treasure.restart.base.BaseFragment
import com.treasure.restart.databinding.FragmentMomentListBinding
import com.treasure.restart.func.main.profile.ProfileViewModel
import com.treasure.restart.func.media.ImagePreviewActivity
import kotlinx.coroutines.launch

class MomentListFragment : BaseFragment() {

    private lateinit var binding: FragmentMomentListBinding
    private lateinit var adapter: MomentListAdapter

    private val viewModel: MomentViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMomentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun lazyInit(view: View?) {
        super.lazyInit(view)
        initRecyclerView()
        initRefresh()
        observeData()
        observeAppEvents()

        viewModel.loadMomentList(refresh = true)
        profileViewModel.loadUserInfo()
    }

    private fun initRecyclerView() {
        adapter = MomentListAdapter(mutableListOf()) { position, images ->
            openImagePreview(position, images)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(false)
        binding.recyclerView.itemAnimator = null

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                updateStickyHeader()
            }
        })
    }

    private fun initRefresh() {
        binding.smartlay.setEnableRefresh(true)
        binding.smartlay.setEnableLoadMore(true)
        binding.smartlay.setEnableAutoLoadMore(true)
        binding.smartlay.setEnableLoadMoreWhenContentNotFull(false)

        binding.smartlay.setOnRefreshListener {
            viewModel.loadMomentList(refresh = true)
        }
        binding.smartlay.setOnLoadMoreListener {
            viewModel.loadMomentList(refresh = false)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.momentList.collect { list ->
                        adapter.replaceData(list)
                        updateStickyHeader()
                    }
                }

                launch {
                    viewModel.listState.collect { state ->
                        when (state.first) {
                            1 -> if (state.second) binding.smartlay.finishRefresh() else binding.smartlay.finishRefreshWithNoMoreData()
                            2 -> if (state.second) binding.smartlay.finishLoadMore() else binding.smartlay.finishLoadMoreWithNoMoreData()
                        }
                    }
                }
            }
        }

        profileViewModel.profile.observe(viewLifecycleOwner) {
            adapter.setHeader(it)
        }
    }

    private fun updateStickyHeader() {
        val layoutManager = binding.recyclerView.layoutManager as? LinearLayoutManager ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

        if (firstVisiblePosition <= 0) {
            binding.stickyHeader.visibility = View.GONE
            return
        }

        binding.stickyHeader.visibility = View.VISIBLE
        profileViewModel.profile.value?.let {
            GlideUtils.loadCircle(ContextHolder.app(), it.avatar.wholeUrl(), binding.stickyAvatar)
            binding.stickyNickname.text = it.username
        } ?: run {
            binding.stickyAvatar.setImageResource(R.drawable.ic_default_avatar)
            binding.stickyNickname.text = "我的昵称"
        }
    }

    private fun openImagePreview(position: Int, images: List<String>) {
        val intent = Intent(activity, ImagePreviewActivity::class.java)
        intent.putStringArrayListExtra(ImagePreviewActivity.EXTRA_IMAGES, ArrayList(images))
        intent.putExtra(ImagePreviewActivity.EXTRA_POSITION, position)
        startActivity(intent)
    }

    private fun observeAppEvents() {
        lifecycleScope.launch {
            AppEventManager.events.collect {
                when (it) {
                    AppEvent.MomentPublishSuccess -> {
                        viewModel.loadMomentList(refresh = true)
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        super.onDestroyView()
    }
}
