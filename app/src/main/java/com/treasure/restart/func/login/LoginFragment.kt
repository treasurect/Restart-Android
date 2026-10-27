package com.treasure.restart.func.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.treasure.basic.base.BaseFragment
import com.treasure.restart.databinding.FragmentLoginBinding
import com.treasure.restart.helper.LoginManager
import com.treasure.restart.helper.LoginType

class LoginFragment : BaseFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvLoginPhone.text = viewModel.maskedPhone.value

        binding.btnOneKeyLogin.setOnClickListener { LoginManager.login(
            context,
            LoginType.TYPE_ONE_KEY,
        ) }
        binding.btnWechatLogin.setOnClickListener { LoginManager.login(
            context,
            LoginType.TYPE_WECHAT,
        ) }
        binding.btnAppleLogin.setOnClickListener { LoginManager.login(
            context,
            LoginType.TYPE_ALIPAY,
        ) }
        binding.btnOtherLogin.setOnClickListener {
            startActivity(Intent(requireContext(), PhoneLoginActivity::class.java))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
