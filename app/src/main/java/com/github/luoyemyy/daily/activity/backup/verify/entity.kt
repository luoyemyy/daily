package com.github.luoyemyy.daily.activity.backup.verify

import com.github.luoyemyy.aclin.mvp.core.MvpData

class BackupVerify : MvpData() {
    var select: Boolean = false
    var year: Int = 0
    var name: String? = null
    var verifyTime: String? = null
}