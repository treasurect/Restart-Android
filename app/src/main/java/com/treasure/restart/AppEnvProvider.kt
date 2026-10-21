package com.treasure.restart

import android.content.Context
import com.treasure.basic.AppEnvironment
import com.treasure.basic.EnvManager
import com.treasure.basic.IEnvironmentProvider

class AppEnvProvider(context: Context, currentEnv: AppEnvironment) : IEnvironmentProvider {
    
    init {
        EnvManager.setEnvironment(context, currentEnv)
    }
    
    override fun getBaseUrl(env: AppEnvironment): String = when (env) {
        AppEnvironment.DEV -> "http://124.222.46.122:8080/"
        AppEnvironment.TEST -> "http://10.17.11.33:8080/"
        AppEnvironment.PRE -> "http://10.17.11.33:8080/"
        AppEnvironment.PROD -> "https://api.treasurect.site/"
    }

    override fun logEnable(env: AppEnvironment): Boolean =
        when (env) {
//            AppEnvironment.PROD -> BuildConfig.DEBUG
            else -> true
        }
}