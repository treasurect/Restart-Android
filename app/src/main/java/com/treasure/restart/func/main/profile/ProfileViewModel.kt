package com.treasure.restart.func.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.treasure.basic.utils.JsonUtils
import com.treasure.basic.utils.ToastUtils
import com.treasure.business.bean.UserInfoBean
import com.treasure.business.network.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()

    val tabs = listOf("笔记", "收藏", "赞过")

    private val _profile = MutableLiveData(UserInfoBean())
    val profile: LiveData<UserInfoBean> = _profile

    fun loadUserInfo(showJson: Boolean = false) {
        viewModelScope.launch {
            userRepository.getUserInfo().collect { response ->
                val user = response.data
                if (response.isSuccess() && user != null) {
                    _profile.value = user
                    if (showJson) {
                        ToastUtils.show(JsonUtils.toJson(user))
                    }
                }
            }
        }
    }
}
