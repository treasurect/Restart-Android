package com.treasure.restart.func.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.treasure.basic.base.BaseFragment
import com.treasure.restart.databinding.FragmentProfileTabBinding

class ProfileTabFragment : BaseFragment() {

    private var _binding: FragmentProfileTabBinding? = null
    private val binding get() = _binding!!
    private val tabIndex: Int
        get() = arguments?.getInt(ARG_TAB_INDEX) ?: 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (tabIndex) {
            0 -> {
                binding.tvProfileEmpty.text = "记录你笔下的自己"
                binding.btnProfileAction.text = "发作品"
                binding.btnProfileAction.visibility = View.VISIBLE
            }
            1 -> {
                binding.tvProfileEmpty.text = "还没有收藏内容"
                binding.btnProfileAction.visibility = View.GONE
            }
            else -> {
                binding.tvProfileEmpty.text = "还没有赞过内容"
                binding.btnProfileAction.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TAB_INDEX = "tab_index"

        fun newInstance(tabIndex: Int): ProfileTabFragment {
            return ProfileTabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_INDEX, tabIndex)
                }
            }
        }
    }
}
