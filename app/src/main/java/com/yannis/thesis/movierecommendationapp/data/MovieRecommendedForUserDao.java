package com.yannis.thesis.movierecommendationapp.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.yannis.thesis.movierecommendationapp.models.MovieRecommendedForUser;
import java.util.List;

@Dao
public interface MovieRecommendedForUserDao {
    @Query("SELECT * FROM MovieRecommendedForUser WHERE userId = :userId ORDER BY predictedRating DESC")
    List<MovieRecommendedForUser> findAllForUserByRating(String userId);
    @Query("SELECT * FROM MovieRecommendedForUser WHERE userId = :userId AND movieId = :movieId")
    List<MovieRecommendedForUser> findForMovie(String userId, String movieId);
    @Insert
    void insert(MovieRecommendedForUser movie);
    @Delete
    void delete(List<MovieRecommendedForUser> movies);
}
