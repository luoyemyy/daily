package com.github.luoyemyy.daily.util

import android.content.Context
import com.github.luoyemyy.aclin.ext.editor
import com.github.luoyemyy.aclin.ext.spf
import java.util.*

object AppCache {

    fun getUser(context: Context): User {
        return User(getUserId(context), getUserName(context), "", getUserMoments(context))
    }

    fun getUserId(context: Context): Long {
        return context.spf().getLong("user-id", 1L)
    }

    fun getUserName(context: Context): String? {
        return context.spf().getString("user-name", null)
    }

    fun getUserMoments(context: Context): String? {
        return context.spf().getString("user-moments", null)
    }

    fun setUserName(context: Context, name: String?) {
        context.editor().putString("user-name", name).apply()
    }

    fun setUserMoments(context: Context, moments: String?) {
        context.editor().putString("user-moments", moments).apply()
    }

    fun autoBackup(context: Context): Boolean {
        return context.spf().getBoolean("auto-backup", false)
    }

    fun setAutoBackup(context: Context, auto: Boolean) {
        context.editor().putBoolean("auto-backup", auto).apply()
    }

    fun getVerifyTime(context: Context, year: Int): Long {
        return context.spf().getLong("verifyTime-${year}", 0L)
    }

    fun setVerifyTime(context: Context, year: Int, time: Date) {
        context.editor().putLong("verifyTime-${year}", time.time).apply()
    }
}