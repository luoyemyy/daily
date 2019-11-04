package com.github.luoyemyy.daily.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.luoyemyy.daily.db.entity.User

@Dao
interface UserDao {

    @Insert
    fun insert(user: User)

    @Query("select * from user")
    fun getAll(): LiveData<List<User>>
}