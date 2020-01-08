package com.github.luoyemyy.daily.activity.read

import com.github.luoyemyy.aclin.mvp.core.MvpData
import com.github.luoyemyy.daily.util.formatDate

class ReadDay(var year: Int, var month: Int, var day: Int, var content: String?) : MvpData() {
    var date: String? = formatDate(year, month, day)
}

class SeekIndex(var month: Int) : MvpData()