package com.yannis.thesis.movierecommendationapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser

@Dao
interface MovieRecommendedForUserDao {
    @Query("SELECT * FROM MovieRecommendedForUser WHERE userId = :userId ORDER BY predictedRating DESC")
    fun findAllForUserByRating(userId: String?): List<MovieRecommendedForUser>

    @Query("SELECT * FROM MovieRecommendedForUser WHERE userId = :userId AND movieId = :movieId")
    fun findForMovie(userId: String?, movieId: String?): List<MovieRecommendedForUser>

    @Insert
    fun insert(movie: MovieRecommendedForUser)

    @Delete
    fun delete(movies: List<MovieRecommendedForUser>)
}
