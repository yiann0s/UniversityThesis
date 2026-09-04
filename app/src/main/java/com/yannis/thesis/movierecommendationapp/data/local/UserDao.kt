package com.yannis.thesis.movierecommendationapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yannis.thesis.movierecommendationapp.data.local.User

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    fun findByEmail(email: String): User?

    @Query("SELECT * FROM User WHERE password = :password LIMIT 1")
    fun findByPassword(password: String): User?

    @Query("SELECT * FROM User WHERE id != :id")
    fun findAllExcept(id: String): List<User>

    @Insert
    fun insert(user: User)
}
