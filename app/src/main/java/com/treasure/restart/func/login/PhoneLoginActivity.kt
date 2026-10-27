package com.treasure.restart.func.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.treasure.basic.base.BaseActivity
import com.treasure.restart.databinding.ActivityPhoneLoginBinding
import com.treasure.restart.helper.LoginManager
import com.treasure.restart.helper.LoginType

class PhoneLoginActivity : BaseActivity() {

    private lateinit var binding: ActivityPhoneLoginBinding
    private val viewModel: PhoneLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etPhone.setText(viewModel.phone.value)
        binding.etPassword.setText(viewModel.password.value)

        binding.ivPhoneBack.setOnClickListener { finish() }
        binding.btnVerifyLogin.setOnClickListener { login() }
        binding.tvPasswordLogin.setOnClickListener { toggleLoginMode() }
        binding.tvFindAccount.setOnClickListener {
            Toast.makeText(this, "账号找回建设中", Toast.LENGTH_SHORT).show()
        }
        binding.tvPhoneLoginHelp.setOnClickListener {
            Toast.makeText(this, "帮助中心建设中", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleLoginMode() {
        if (viewModel.isPasswordMode.value == true) {
            showVerifyCodeLogin()
        } else {
            showPasswordLogin()
        }
    }

    private fun showPasswordLogin() {
        viewModel.isPasswordMode.value = true
        binding.passwordLoginGroup.visibility = View.VISIBLE
        binding.tvPasswordLogin.text = "验证码登录"
        binding.btnVerifyLogin.text = "登录"
    }

    private fun showVerifyCodeLogin() {
        viewModel.isPasswordMode.value = false
        binding.passwordLoginGroup.visibility = View.GONE
        binding.tvPasswordLogin.text = "密码登录"
        binding.btnVerifyLogin.text = "验证并登录"
    }

    private fun login() {
        val phone = binding.etPhone.text?.toString()?.trim().orEmpty()
        if (phone.length < 11) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.phone.value = phone
        viewModel.password.value = binding.etPassword.text?.toString().orEmpty()
        if (viewModel.isPasswordMode.value == true && viewModel.password.value.isNullOrBlank()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
            return
        }

        val map = HashMap<String, Any>().apply {
            put("phone", binding.etPhone.text?.trim() ?: "")
            put("pwd", binding.etPassword.text?.trim() ?: "")
        }
        LoginManager.login(
            this,
            if (viewModel.isPasswordMode.value == true) LoginType.TYPE_PWD else LoginType.TYPE_VERIFY_CODE,
            map
        )
    }
}
