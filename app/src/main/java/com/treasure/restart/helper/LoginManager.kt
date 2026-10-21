package com.treasure.restart.helper

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.treasure.basic.SharedKey
import com.treasure.basic.helper.AppEventManager
import com.treasure.basic.helper.AppRestartHelper
import com.treasure.basic.helper.LogHelper
import com.treasure.basic.helper.SharePreferenceManager
import com.treasure.basic.utils.ToastUtils
import com.treasure.restart.network.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object LoginManager {

    private const val PREFS_NAME = "restart_login"


    fun isLoggedIn(): Boolean {
        return SharePreferenceManager.getBoolean(SharedKey.KEY_LOGGED_IN)
    }

    fun login(context: Context?, loginType: LoginType, map: HashMap<String, Any> = HashMap()) {
        when (loginType) {
            LoginType.TYPE_PWD -> {
                actionLogin(context, map, loginType)
            }

            LoginType.TYPE_VERIFY_CODE -> {
                map["user_pwd"] = "666666"
                actionLogin(context, map, loginType)
            }

            LoginType.TYPE_ONE_KEY -> {
                map["user_name"] = "one_key"
                map["user_pwd"] = "123456"
                actionLogin(context, map, loginType)
            }

            LoginType.TYPE_WECHAT -> {
                map["user_name"] = "wechat"
                map["user_pwd"] = "123456"
                actionLogin(context, map, loginType)
            }

            LoginType.TYPE_ALIPAY -> {
                map["user_name"] = "alipay"
                map["user_pwd"] = "123456"
                actionLogin(context, map, loginType)
            }
        }


    }

    private fun actionLogin(context: Context?, map: HashMap<String, Any>, loginType: LoginType) {
        (context as? FragmentActivity)?.lifecycleScope?.launch {
            val phone = "${map["user_name"] ?: ""}"
            val value = "${map["user_pwd"] ?: ""}"
            UserRepository().actionLoginCode(phone, value).collectLatest {
                if (it.code == 200) {
                    ToastUtils.show(it.msg ?: "")
                    SharePreferenceManager.putBoolean(SharedKey.KEY_LOGGED_IN, true)
                    SharePreferenceManager.putString(SharedKey.ACCESS_TOKEN, it.data?.token ?: "")
                    AppEventManager.resetLoginExpired()
                    delay(500.milliseconds)
                    AppRestartHelper.restart(context)
                } else {
                    ToastUtils.show("登录失败：${it.msg}")
                    LogHelper.e("Login failed: $it")
                }
            }
        }
    }

    fun logout() {
        SharePreferenceManager.putBoolean(SharedKey.KEY_LOGGED_IN, false)
        SharePreferenceManager.remove(SharedKey.ACCESS_TOKEN)
    }
}

enum class LoginType(type: Int) {
    TYPE_PWD(1),
    TYPE_VERIFY_CODE(2),
    TYPE_ONE_KEY(3),
    TYPE_WECHAT(4),
    TYPE_ALIPAY(5),
}
