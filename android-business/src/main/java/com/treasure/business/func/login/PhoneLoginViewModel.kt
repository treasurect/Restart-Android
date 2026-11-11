package com.treasure.business.func.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhoneLoginViewModel : ViewModel() {

    val phone = MutableLiveData("")
    val password = MutableLiveData("")
    val isPasswordMode = MutableLiveData(false)
}
