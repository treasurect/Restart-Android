package com.treasure.restart

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatDelegate
import com.treasure.basic.AppBridge
import com.treasure.basic.AppConfig
import com.treasure.basic.AppEnvironment
import com.treasure.basic.AppInitializer
import com.treasure.basic.EnvManager
import com.treasure.basic.helper.LogHelper
import kotlin.system.exitProcess

class RestartAPP: Application(), AppConfig {
    companion object {
        lateinit var instance: RestartAPP
            private set
    }
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this

        if (!isMainProcess()) return

        EnvManager.init(this, AppEnvProvider(this, AppEnvironment.PROD))
        AppInitializer.init(this)
        AppConfig.VERSION_NAME = BuildConfig.VERSION_NAME
        AppConfig.isDebug = BuildConfig.DEBUG
        AppConfig.userSourceType = 1
        AppBridge.config = this
        registerLifecycle()
        registerGlobalCrashHandler()

//        if (hasPrivacyConsent()) {
//            processBusinessInit()
//        }
    }
    override fun processBusinessInit() {

    }

    /**
     * 注册生命周期监听（用于前后台判断）
     */
    private fun registerLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var activityCount = 0

            override fun onActivityStarted(activity: Activity) {
                if (activityCount++ == 0) {
                    LogHelper.d("App switched to foreground")
                }
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount = (activityCount - 1).coerceAtLeast(0)
                if (activityCount == 0) {
                    LogHelper.d("App switched to background")
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    /**
     * 全局异常捕获
     */
    private fun registerGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            LogHelper.e("Uncaught exception in ${thread.name}", throwable = throwable)
            defaultHandler?.uncaughtException(thread, throwable) ?: run {
                android.os.Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
    }

    /**
     * 判断当前是否主进程，避免多进程重复初始化
     */
    private fun isMainProcess(): Boolean = resolveProcessName() == packageName

    private fun resolveProcessName(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName()
        }
        return getProcessName(this, android.os.Process.myPid())
    }

    private fun getProcessName(context: Context, pid: Int): String? {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        return manager?.runningAppProcesses
            ?.firstOrNull { it.pid == pid }
            ?.processName
    }
}