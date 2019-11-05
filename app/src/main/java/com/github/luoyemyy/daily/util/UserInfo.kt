package com.github.luoyemyy.daily.util

import android.content.Context
import com.github.luoyemyy.aclin.ext.editor
import com.github.luoyemyy.aclin.ext.spf
import com.github.luoyemyy.daily.db.entity.User

object UserInfo {
    fun getUser(): User {
        return User(1, "", "")
    }

    fun autoBackup(context: Context): Boolean {
        return context.spf().getBoolean("auto-backup", false)
    }

    fun setAutoBackup(context: Context, auto: Boolean) {
        context.editor().putBoolean("auto-backup", auto).apply()
    }

    fun nextBackupTime(context: Context): Int {
        return context.spf().getInt("next-backup-time", 0)
    }

    fun setNextBackupTime(context: Context, nextTime: Int) {
        context.editor().putInt("next-backup-time", nextTime).apply()
    }
}