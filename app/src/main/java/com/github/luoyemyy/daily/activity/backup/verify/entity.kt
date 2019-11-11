package com.github.luoyemyy.daily.activity.backup.verify

import com.github.luoyemyy.aclin.mvp.DataItem

class BackupVerify : DataItem() {
    var year: Int = 0
    var name: String? = null
    var verifyTime: String? = null
}