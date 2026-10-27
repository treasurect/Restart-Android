package com.treasure.restart.func.main.ai

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.treasure.basic.R
import com.treasure.basic.base.BaseFragment
import com.treasure.basic.constant.SPKey
import com.treasure.basic.utils.DialogExt
import com.treasure.basic.utils.ScreenUtils
import com.treasure.basic.utils.ShareUtils
import com.treasure.basic.view.dialog.CenterDialogBuilder
import com.treasure.basic.view.dialog.DropPopupItem
import com.treasure.basic.view.dialog.DropPopupWindow
import com.treasure.restart.databinding.FragmentAiBinding
import com.treasure.restart.helper.LoginManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AIChatFragment : BaseFragment() {

    private lateinit var binding: FragmentAiBinding
    private val viewModel: AIChatViewModel by viewModels()

    private lateinit var messageAdapter: AIChatMessageAdapter
    private lateinit var conversationAdapter: AIChatConversationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun lazyInit(view: View?) {
        initMessageList()
        initConversationList()
        initClickListeners()
        initInput()
        observerEvent()

        LoginManager.checkLoginIn {
            val cid = ShareUtils.getCurrentLong(SPKey.CUR_AI_CONVERSATION)
            if (cid != 0L) {
                viewModel.loadMessagesByCid(cid)
            } else {
                viewModel.loadCurConversations(initial = true)
            }
        }
    }

    /**
     * 初始化聊天列表
     */
    private fun initMessageList() {
        messageAdapter = AIChatMessageAdapter()

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
        }
    }

    /**
     * 初始化最近会话列表
     */
    private fun initConversationList() {
        conversationAdapter = AIChatConversationAdapter(onItemClick = { conversation ->
            binding.tvTitle.text = conversation.title
            viewModel.loadMessagesByCid(conversation.id)
            closeDrawer()
            hideKeyboard()
        }, onItemLongClick = { conversation, itemView ->
            val options = listOf(DropPopupItem("edit", "重命名"), DropPopupItem("delete", "删除"))
            DropPopupWindow(
                itemView.context,
                itemView,
                options,
                width = ScreenUtils.dp2px(dp =  150f),
                showCheck = false
            ) { _, item ->
                when (item.id) {
                    "edit" -> {
                        DialogExt.showEditDialog(
                            activity,
                            title = "重命名会话",
                            content = conversation.title,
                            rightCallback = { newTitle ->
                                viewModel.renameConversation(conversation.id, newTitle)
                            })
                    }

                    "delete" -> {
                        viewModel.deleteConversation(conversation.id)
                    }
                }
            }.show()
        })

        binding.rvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }

    /**
     * 初始化页面点击事件
     */
    private fun initClickListeners() {
        binding.btnSend.setOnClickListener {
            if (binding.btnSend.isSelected) {
                viewModel.stopGenerate()
            } else {
                sendCurrentMessage()
            }
        }
        // 菜单按钮：打开或关闭抽屉
        binding.btnMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START).not()) {
                viewModel.loadCurConversations()
            }
            toggleDrawer()
        }

        // 新建会话
        binding.btnNewChat.setOnClickListener {
            closeDrawer()
            hideKeyboard()
            binding.etMessage.text?.clear()

            viewModel.newChat()
        }

        // 快捷提问
        binding.btnSuggestion1.setOnClickListener {
            sendSuggestion(binding.btnSuggestion1.text.toString())
        }

        binding.btnSuggestion2.setOnClickListener {
            sendSuggestion(binding.btnSuggestion2.text.toString())
        }

        binding.btnSuggestion3.setOnClickListener {
            sendSuggestion(binding.btnSuggestion3.text.toString())
        }
    }

    /**
     * 初始化输入框与发送按钮
     */
    private fun initInput() {
        binding.etMessage.setOnEditorActionListener { _, actionId, event ->
            val sendByIme = actionId == EditorInfo.IME_ACTION_SEND

            val sendByEnter =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN && !event.isShiftPressed

            if (sendByIme || sendByEnter) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }
    }

    /**
     * 发送快捷提问
     */
    private fun sendSuggestion(text: String) {
        if (text.isNotEmpty()) {
            binding.etMessage.text?.clear()
            closeDrawer()
            hideKeyboard()
            viewModel.sendMessageStream(text)
        }
    }

    /**
     * 发送输入框消息
     */
    private fun sendCurrentMessage() {
        val message = binding.etMessage.text?.toString()?.trim().orEmpty()

        if (message.isEmpty()) return

        binding.etMessage.text?.clear()
        hideKeyboard()
        viewModel.sendMessageStream(message)
    }

    /**
     * 监听消息列表
     */
    private fun observerEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collectLatest { messages ->
                    messageAdapter.submitList(messages) {
                        val count = messageAdapter.itemCount

                        binding.welcomeContainer.isVisible = count == 0
                        binding.rvMessages.isVisible = count > 0

                        if (count > 0) {
                            binding.rvMessages.scrollToPosition(count - 1)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.conversations.collectLatest {
                    if (it.first) {
                        it.second.getOrNull(0)?.let {
                            viewModel.loadMessagesByCid(it.id)
                        }
                    } else {
                        conversationAdapter.setData(it.second)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentConversationId.collectLatest {
                if (it == null) return@collectLatest
                ShareUtils.putCurrentLong(SPKey.CUR_AI_CONVERSATION, it)
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.thinking.collectLatest {
                    binding.btnSend.isSelected = it
                }
            }
        }
    }

    /**
     * 打开或关闭抽屉
     */
    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            hideKeyboard()
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * 关闭抽屉
     */
    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * 收起软键盘
     */
    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(
            binding.etMessage.windowToken, 0
        )
        binding.etMessage.clearFocus()
    }

    override fun onDestroyView() {
        binding.rvMessages.adapter = null
        binding.rvConversations.adapter = null
        super.onDestroyView()
    }
}
