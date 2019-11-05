package com.github.luoyemyy.daily.util

import com.github.luoyemyy.aclin.file.FileManager
import java.io.File

private val BACKUP_TYPE = FileManager.Type("backup", ".json")

fun getBackupDir(): File? {
    return FileManager.getInstance().outer().publicDir(BACKUP_TYPE)
}

fun getBackupFile(time: Int): File? {
    return getBackupDir()?.let {
        File(it, "$time${BACKUP_TYPE.suffix}").apply {
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

fun formatDateNum(n: Int): String {
    return if (n < 10) {
        "0$n"
    } else {
        "$n"
    }
}