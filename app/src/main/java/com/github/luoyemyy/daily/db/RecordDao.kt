package com.github.luoyemyy.daily.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.luoyemyy.daily.db.entity.Record

@Dao
interface RecordDao {

    @Insert
    fun insert(record: Record): Long

    @Update
    fun update(record: Record)

    @Query("select count(*) from record where userId=:userId")
    fun countAll(userId: Long): Long

    @Query("select count(*) from record where userId = :userId and  year = :year and month = :month and day=:day")
    fun countByDate(userId: Long, year: Int, month: Int, day: Int): Long

    @Query("select * from record where id=:id")
    fun get(id: Long): Record?

    @Query("select * from record where rowId=:rowId")
    fun getByRowId(rowId: Long): Record?

    @Query("select * from record where userId=:userId")
    fun getAll(userId: Long): List<Record>?

    @Query("select * from record where userId = :userId and  year = :year and month = :month order by day asc")
    fun getListByMonth(userId: Long, year: Int, month: Int): List<Record>?
}