package com.github.luoyemyy.daily.util

import android.content.Context
import com.github.luoyemyy.aclin.ext.isNumber
import com.github.luoyemyy.aclin.ext.toJsonString
import com.github.luoyemyy.aclin.ext.toObject
import com.github.luoyemyy.aclin.file.FileManager
import com.github.luoyemyy.daily.activity.backup.day.BackupDay
import com.github.luoyemyy.daily.activity.backup.month.BackupMonth
import com.github.luoyemyy.daily.activity.backup.year.BackupYear
import com.github.luoyemyy.daily.db.entity.Record
import com.github.luoyemyy.daily.db.getRecordDao
import java.io.File
import java.io.FileReader
import java.io.FileWriter

private val BACKUP_TYPE = FileManager.Type("backup", "")

fun getBackupDir(): File? {
    return FileManager.getInstance().outer().publicDir(BACKUP_TYPE)
}

fun getBackupYearDir(y: Int): File? {
    return getBackupDir()?.let { dir ->
        File(dir, "$y").takeIf { it.exists() || it.mkdirs() }
    }
}

fun getBackupMonthDir(y: Int, m: Int): File? {
    return getBackupYearDir(y)?.let { dir ->
        File(dir, "$m").takeIf { it.exists() || it.mkdirs() }
    }
}

fun getBackupDayFile(y: Int, m: Int, d: Int): File? {
    return getBackupMonthDir(y, m)?.let {
        File(it, "$d").apply {
            return if (exists()) {
                this
            } else {
                null
            }
        }
    }
}

fun createBackupDayFile(y: Int, m: Int, d: Int): File? {
    return getBackupMonthDir(y, m)?.let {
        File(it, "$d").apply {
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

fun backupDay(record: Record) {
    record.toJsonString()?.also { json ->
        createBackupDayFile(record.year, record.month, record.day)?.also { file ->
            FileWriter(file).use {
                it.write((AesUtils.encrypt(json)))
                it.flush()
            }
        }
    }
}

fun syncYear(context: Context, year: Int) {
    getBackupYearDir(year)?.let {
        if (it.exists()) {
            it.list()?.forEach { month ->
                if (month.isNumber()) {
                    syncMonth(context, year, month.toInt())
                }
            }
        }
    }
}

fun syncMonth(context: Context, year: Int, month: Int) {
    getBackupMonthDir(year, month)?.let {
        if (it.exists()) {
            it.list()?.forEach { day ->
                if (day.isNumber()) {
                    syncDay(context, year, month, day.toInt())
                }
            }
        }
    }
}

fun syncDay(context: Context, year: Int, month: Int, day: Int): Boolean {
    val recordDao = getRecordDao()
    getBackupDayFile(year, month, day)?.let {
        if (it.exists()) {
            FileReader(it).readText().let {
                AesUtils.decrypt(it)
            }.toObject<Record>()?.apply {
                id = 0L
                userId = UserInfo.getUserId(context)
                recordDao.getByDate(userId, year, month, day) ?: let {
                    recordDao.insert(this)
                    return true
                }
            }
        }
    }
    return false
}

fun getBackupYears(context: Context): List<BackupYear>? {
    return getBackupDir()?.let { file ->
        file.list()?.filter { it.isNumber() }?.map {
            BackupYear().apply {
                year = it.toInt()
                months = getBackupMonths(context, year)
                countNotSync = months?.sumBy { it.countNotSync } ?: 0
            }
        }?.sortedBy { it.year }?.toMutableList()
    }
}

fun getBackupMonths(context: Context, y: Int): List<BackupMonth>? {
    return getBackupYearDir(y)?.let { file ->
        file.list()?.filter { it.isNumber() }?.map {
            BackupMonth().apply {
                year = y
                month = it.toInt()
                days = getBackupDays(context, year, month)
                countNotSync = days?.count { !it.sync } ?: 0
            }
        }?.sortedBy { it.month }?.toMutableList()
    }
}

fun getBackupDays(context: Context, y: Int, m: Int): List<BackupDay>? {
    return getBackupMonthDir(y, m)?.let { file ->
        file.list()?.filter { it.isNumber() }?.map {
            BackupDay().apply {
                year = y
                month = m
                day = it.toInt()
                path = file.absolutePath
                sync = getRecordDao().countByDate(UserInfo.getUserId(context), year, month, day) > 0
            }
        }?.sortedBy { it.day }?.toMutableList()
    }
}

fun verifyBackupYear(context: Context, year: Int): List<Record>? {
    return getRecordDao().getListByYear(UserInfo.getUserId(context), year)?.filter {
        getBackupDayFile(it.year, it.month, it.day) == null
    }?.apply {
        this.forEach { backupDay(it) }
    }
}

fun verifyBackupMonth(context: Context, year: Int, month: Int): List<Record>? {
    return getRecordDao().getListByMonth(UserInfo.getUserId(context), year, month)?.filter {
        getBackupDayFile(it.year, it.month, it.day) == null
    }?.apply {
        this.forEach { backupDay(it) }
    }
}