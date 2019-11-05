package com.github.luoyemyy.daily.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.ext.toJsonString
import com.github.luoyemyy.daily.db.entity.Record
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.UserInfo
import com.github.luoyemyy.daily.util.getBackupFile
import java.io.FileWriter


class BackupService : IntentService("BackupService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_BACKUP -> {
                handleActionBackup(intent.getLongExtra(ACTION_BACKUP_ID, 0))
            }
        }
    }

    private fun handleActionBackup(id: Long) {
        runOnThread {
            getRecordDao().get(id)?.also { record ->
                //                needBackupMonth(record)
                record.toJsonString()?.also { json ->
                    saveBackup(json, getTime(record))
                }
            }
        }
    }

    private fun saveBackup(json: String, time: Int) {
        getBackupFile(time)?.also { file ->
            FileWriter(file).use {
                it.write(json)
                it.flush()
            }
        }
    }

    private fun getTime(record: Record): Int {
        return getTime(record.year, record.month, record.day)
    }

    private fun getTime(year: Int, month: Int, day: Int): Int {
        return year * 10000 + month * 100 + day
    }


    private fun backupMonthTime(record: Record): Triple<Int, Int, Int> {
        var m = record.month - 1
        var y = record.year
        val d = 0
        if (m == 0) {
            m = 12
            y--
        }
        return Triple(y, m, d)
    }

    private fun nextBackupMonthTime(record: Record): Int {
        var m = record.month + 1
        var y = record.year
        val d = 0
        if (m == 13) {
            m = 1
            y++
        }
        return getTime(y, m, d)
    }

    @WorkerThread
    private fun needBackupMonth(record: Record) {
        val nextTime = UserInfo.nextBackupTime(this)
        if (nextTime < getTime(record)) {
            val (y, m, d) = backupMonthTime(record)
            getRecordDao().getListByMonth(UserInfo.getUser().id, y, m)?.also { records ->
                records.toJsonString()?.also { json ->
                    saveBackup(json, getTime(y, m, d))
                    UserInfo.setNextBackupTime(this, nextBackupMonthTime(record))
                }
            }
        }
    }

    companion object {

        private const val ACTION_BACKUP = "com.github.luoyemyy.daily.service.action.BACKUP"
        private const val ACTION_BACKUP_ID = "id"

        @JvmStatic
        fun startActionBackup(context: Context, id: Long) {
            if (UserInfo.autoBackup(context)) {
                Intent(context, BackupService::class.java).apply {
                    action = ACTION_BACKUP
                    putExtra(ACTION_BACKUP_ID, id)
                    context.startService(this)
                }
            }
        }
    }
}
