package com.github.luoyemyy.daily.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "record")
data class Record(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var userId: Long,
    var day: Int,
    var month: Int,
    var year: Int,
    var content: String
)