package com.treasure.restart.helper

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.gson.Gson
import androidx.core.content.edit
import com.treasure.basic.ContextHolder

object LocalCache {
    private val sharedPreferences = ContextHolder.app().getSharedPreferences("local_cache", Context.MODE_PRIVATE)
    private const val expireTimeInterval = (7 * 24 * 60 * 60).toLong()

    fun saveHttpLocalData(key: String?, json: String) {
        val l = System.currentTimeMillis() / 1000
        sharedPreferences.edit { putString(key, "$l-$json") }
    }

    fun getHttpLocalData(key: String?): String {
        val hotCompanyData = sharedPreferences.getString(key, "")!!
        return filterRealData(hotCompanyData)
    }


        private fun filterRealData(wholeStr: String): String {
            if (!TextUtils.isEmpty(wholeStr)) {
                val split =
                    wholeStr.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val s = split[0]
                val saveTime = try {
                    s.toLong()
                } catch (e: NumberFormatException) {
                    0L
                }
                if (System.currentTimeMillis() / 1000 - saveTime > expireTimeInterval) return ""
                return wholeStr.replaceFirst((split[0] + "-").toRegex(), "")
            }
            return wholeStr
        }
}
