package com.github.luoyemyy.daily.activity.backup.group

import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.daily.activity.backup.record.BackupFile
import com.github.luoyemyy.daily.util.formatDateNum

class BackupGroup() : DataItem() {
    var name: String? = null
    var count: Int = 0
    var year: Int = 0
    var month: Int = 0
    var sync: Boolean = false

    var files: List<BackupFile>? = null

    fun countDesc(): String {
        return if (count == 0) "" else "$count"
    }

    constructor(name: String, files: List<BackupFile>?) : this() {
        val time = name.toInt()
        this.year = time / 10000
        this.month = (time - this.year * 10000) / 100
        this.name = "$year-${formatDateNum(month)}"
        this.files = files?.sortedBy { it.day }?.toMutableList()
        this.count = files?.size ?: 0
    }
}
