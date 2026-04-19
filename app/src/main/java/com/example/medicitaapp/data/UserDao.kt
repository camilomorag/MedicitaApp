package com.example.medicitaapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE documento = :documento LIMIT 1")
    suspend fun getUserByDocumento(documento: String): UserEntity?

    @Query("SELECT * FROM users WHERE documento = :documento AND password = :password LIMIT 1")
    suspend fun login(documento: String, password: String): UserEntity?
}