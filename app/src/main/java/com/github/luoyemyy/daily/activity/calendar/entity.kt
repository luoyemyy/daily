package com.github.luoyemyy.daily.activity.calendar

import com.github.luoyemyy.aclin.mvp.core.MvpData

class Day(var id: Long, var hasDaily: Boolean, var year: Int, var month: Int, var day: Int) {

    var flag: Int = 0   // 0 空白 1 天 2 月
    var today: Boolean = false

    constructor() : this(0, false, 0, 0, 0)

    fun today(year: Int, month: Int, day: Int) {
        today = this.year == year && this.month == month && this.day == day
    }

    fun dayDesc(): String {
        return if (flag == 0) "" else if (flag == 1) "$day" else "${month}月"
    }

    fun value(): Int {
        return year * 10000 + month * 100 + day
    }
}

data class Week(var monday: Day, var tuesday: Day, var wednesday: Day, var thursday: Day, var friday: Day, var saturday: Day, var sunday: Day) : MvpData() {

    var min = 0
    var max = 0
    var isTitle: Boolean = false

    constructor() : this(Day(), Day(), Day(), Day(), Day(), Day(), Day())
    constructor(isTitle: Boolean) : this(Day(), Day(), Day(), Day(), Day(), Day(), Day()) {
        this.isTitle = isTitle
    }

    fun setValue() {
        listOf(monday.value(), tuesday.value(), wednesday.value(), thursday.value(), friday.value(), saturday.value(), sunday.value()).filter { it > 0 }.apply {
            min = this.min() ?: 0
            max = this.max() ?: 0
        }
    }

    fun findDay(value: Int): Day? {
        return when (value) {
            monday.value() -> monday
            tuesday.value() -> tuesday
            wednesday.value() -> wednesday
            thursday.value() -> thursday
            friday.value() -> friday
            saturday.value() -> saturday
            sunday.value() -> sunday
            else -> null
        }
    }
}