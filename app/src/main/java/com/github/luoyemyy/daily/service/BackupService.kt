package com.github.luoyemyy.daily.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.luoyemyy.aclin.ext.formatDate
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.ext.toJsonString
import com.github.luoyemyy.aclin.file.FileManager
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.UserInfo
import java.io.File
import java.io.FileWriter
import java.util.*


class BackupService : IntentService("BackupService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_BACKUP -> {
                handleActionBackup()
            }
        }
    }


    private fun getFile(suffix: String = ".json"): File? {
        return FileManager.getInstance().outer().publicDir(FileManager.FILE)?.let {
            val name = "daily_backup_${Date().formatDate()!!}${suffix}"
            File(it, name).apply {
                Log.e("BackupService", "getFile: $absoluteFile ")
                return if (!exists()) {
                    this
                } else if (delete()) {
                    this
                } else {
                    null
                }
            }
        }
    }

    private fun handleActionBackup() {
        runOnThread {
            getFile()?.also { file ->
                getRecordDao().getAll()?.also { records ->
                    records.toJsonString()?.also { json ->
                        FileWriter(file).use {
                            it.write(json)
                            it.flush()
                        }
                    }
                }
            }
        }
    }

    companion object {

        private const val ACTION_BACKUP = "com.github.luoyemyy.daily.service.action.BACKUP"

        @JvmStatic
        fun startActionBackup(context: Context) {
            if (UserInfo.autoBackup(context)) {
                Intent(context, BackupService::class.java).apply {
                    action = ACTION_BACKUP
                    context.startService(this)
                }
            }
        }
    }
}
