package com.yannis.thesis.movierecommendationapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yannis.thesis.movierecommendationapp.models.UserRatesMovie

@Dao
interface UserRatesMovieDao {
    @Query("SELECT * FROM UserRatesMovie WHERE userId = :userId AND movieId = :movieId LIMIT 1")
    fun findForMovie(userId: String?, movieId: String?): UserRatesMovie?

    @Query("SELECT * FROM UserRatesMovie WHERE userId = :userId ORDER BY dateAndTime DESC")
    fun findAllForUser(userId: String?): List<UserRatesMovie>

    @Query("SELECT * FROM UserRatesMovie")
    fun getAll(): List<UserRatesMovie>

    @Insert
    fun insert(movie: UserRatesMovie)
}
