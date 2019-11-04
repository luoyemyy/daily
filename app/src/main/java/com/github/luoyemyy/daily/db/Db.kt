package com.github.luoyemyy.daily.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.luoyemyy.daily.App
import com.github.luoyemyy.daily.db.entity.Record
import com.github.luoyemyy.daily.db.entity.User

@Database(entities = [User::class, Record::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class Db : RoomDatabase() {

    companion object {
        private lateinit var db: Db
        fun initDb(app: App) {
            db = Room.databaseBuilder(app, Db::class.java, "db").build()
        }

        fun getInstance(): Db = db
    }

    abstract fun userDao(): UserDao
    abstract fun recordDao(): RecordDao
}


fun getUserDao() = Db.getInstance().userDao()
fun getRecordDao() = Db.getInstance().recordDao()