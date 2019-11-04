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

    @Query("select count(*) from record")
    fun countAll(): Long

    @Query("select * from record where id=:id")
    fun get(id: Long): Record?

    @Query("select * from record where rowId=:rowId")
    fun getOneByRowId(rowId: Long): Record?

    @Query("select * from record")
    fun getAll(): List<Record>?

    @Query("select * from record where userId = :userId and  year = :year and month = :month order by day asc")
    fun getAllByDate(userId: Long, year: Int, month: Int): List<Record>?
}